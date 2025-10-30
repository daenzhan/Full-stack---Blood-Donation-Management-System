package org.example.bloodrequestservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/requests")
@Slf4j
public class BloodRequestController {

    private final BloodRequestService service;
    private final EmailService emailService;
    private final BloodRequestRepository repository;
    private final MedCenterFeignClient medCenterFeignClient;

    public BloodRequestController(BloodRequestService service, EmailService emailService, BloodRequestRepository repository, MedCenterFeignClient medCenterFeignClient) {
        this.service = service;
        this.emailService = emailService;
        this.repository = repository;
        this.medCenterFeignClient = medCenterFeignClient;

    }

    @GetMapping
    public String list_requests(
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String rheusFactor,
            @RequestParam(required = false) String componentType,
            @RequestParam(required = false) String medcenterName,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String email,
            Model model) {

        List<BloodRequest> requests;

        boolean hasActiveFilters = hasActiveFilters(bloodGroup, rheusFactor, componentType, medcenterName);

        if (hasActiveFilters) {
            requests = service.search_and_filter_with_sort(bloodGroup, rheusFactor, componentType, medcenterName, sort);
        } else {
            if (sort != null && !sort.trim().isEmpty()) {
                requests = service.get_all_requests_sorted(sort);
            } else {
                requests = service.get_all_requests();
            }
        }

        Map<Long, String> medcenterNames = new HashMap<>();
        for (BloodRequest req : requests) {
            if (req.getMedcenter_id() != null && !medcenterNames.containsKey(req.getMedcenter_id())) {
                medcenterNames.put(req.getMedcenter_id(), service.getMedCenterName(req.getMedcenter_id()));
            }
        }

        model.addAttribute("requests", requests);
        model.addAttribute("medcenterNames", medcenterNames);
        model.addAttribute("bloodGroup", bloodGroup);
        model.addAttribute("rheusFactor", rheusFactor);
        model.addAttribute("componentType", componentType);
        model.addAttribute("medcenterName", medcenterName);
        model.addAttribute("sort", sort);
        model.addAttribute("user_id", user_id);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        model.addAttribute("sortOptions", List.of(
                "deadline_asc: По сроку (сначала ближайшие)",
                "deadline_desc: По сроку (сначала дальние)",
                "bloodgroup_asc: По группе крови (А-Я)",
                "bloodgroup_desc: По группе крови (Я-А)",
                "volume_asc: По объему (по возрастанию)",
                "volume_desc: По объему (по убыванию)"
        ));
        return "list";
    }


    @PostMapping("/{id}/join-donation")
    public String joinDonation(
            @PathVariable("id") Long id,
            @RequestParam String user_id,
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam String requestId,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("Received join donation request for ID: " + id);
            System.out.println("RequestId from form: " + requestId);

            Long effectiveId = (id != null) ? id : Long.valueOf(requestId);

            BloodRequest request = service.get_request_by_id(effectiveId)
                    .orElseThrow(() -> new RuntimeException("Request not found with id: " + effectiveId));

            emailService.sendDonationConfirmation(email, request);

            redirectAttributes.addFlashAttribute("successMessage",
                    "You have successfully joined the donation! Confirmation email has been sent.");

        } catch (Exception e) {
            System.err.println("Error joining donation: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to join donation. Please try again.");
        }

        return "redirect:/requests?user_id=" + user_id + "&role=" + role + "&email=" + email;
    }

    private boolean hasActiveFilters(String bloodGroup, String rheusFactor,
                                     String componentType, String medcenterName) {
        return (bloodGroup != null && !bloodGroup.trim().isEmpty()) ||
                (rheusFactor != null && !rheusFactor.trim().isEmpty()) ||
                (componentType != null && !componentType.trim().isEmpty()) ||
                (medcenterName != null && !medcenterName.trim().isEmpty());
    }

    @GetMapping("/new")
    public String create_request(Model model) {
        model.addAttribute("bloodRequest", new BloodRequest());
        return "form";
    }

    @PostMapping("/save")
    public String save_request(@ModelAttribute BloodRequest blood_request) {
        service.save_request(blood_request);
        return "redirect:/requests";
    }

    @GetMapping("/{id}/edit")
    public String edit_request(@PathVariable Long id, Model model) {
        var opt = service.get_request_by_id(id);
        if (opt.isEmpty()) {
            return "redirect:/requests";
        }

        BloodRequest bloodRequest = opt.get();
        String medcenterName = service.getMedCenterName(bloodRequest.getMedcenter_id());

        model.addAttribute("bloodRequest", bloodRequest);
        model.addAttribute("medcenterName", medcenterName);

        return "edit-form";
    }

    @PostMapping("/{id}/edit")
    public String update_request(@PathVariable Long id, @ModelAttribute BloodRequest bloodRequest) {
        var opt = service.get_request_by_id(id);
        if (opt.isEmpty()) {
            return "redirect:/requests";
        }

        BloodRequest existingRequest = opt.get();

        existingRequest.setComponent_type(bloodRequest.getComponent_type());
        existingRequest.setBlood_group(bloodRequest.getBlood_group());
        existingRequest.setRhesus_factor(bloodRequest.getRhesus_factor());
        existingRequest.setVolume(bloodRequest.getVolume());
        existingRequest.setDeadline(bloodRequest.getDeadline());
        existingRequest.setComments(bloodRequest.getComments());

        service.save_request(existingRequest);
        return "redirect:/requests";
    }

    @PostMapping("/{id}/delete")
    public String delete_request(@PathVariable Long id) {
        service.delete_request(id);
        return "redirect:/requests";
    }

    @GetMapping("/{id}")
    public String view_details(@PathVariable Long id, Model model) {
        var opt = service.get_request_by_id(id);
        if (opt.isEmpty()) {
            return "redirect:/requests";
        }
        BloodRequest request = opt.get();
        model.addAttribute("request", request);
        model.addAttribute("medcenterName", service.getMedCenterName(request.getMedcenter_id()));
        return "details";
    }

    @GetMapping("/medcenter/{medcenter_id}")
    public String list_by_medcenter(
            @PathVariable Long medcenter_id,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String rheusFactor,
            @RequestParam(required = false) String componentType,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String email,
            Model model) {

        List<BloodRequest> requests;

        if (bloodGroup != null || rheusFactor != null || componentType != null) {
            requests = service.search_by_medcenter_with_sort(
                    medcenter_id, bloodGroup, rheusFactor, componentType, sort);
        } else {
            if (sort != null && !sort.trim().isEmpty()) {
                requests = service.search_by_medcenter_with_sort(
                        medcenter_id, null, null, null, sort);
            } else {
                requests = service.get_requests_by_medcenter(medcenter_id);
            }
        }

        // Получаем данные медицинского центра
        MedCenterDto medCenter = null;
        try {
            medCenter = medCenterFeignClient.get_medcenter_by_id(medcenter_id);
        } catch (Exception e) {
            log.warn("Could not fetch medical center details for id: {}", medcenter_id, e);
            // Создаем временный объект если не удалось получить данные
            medCenter = new MedCenterDto();
            medCenter.setMed_center_id(medcenter_id);
            medCenter.setName("Medical Center " + medcenter_id);
        }

        model.addAttribute("requests", requests);
        model.addAttribute("medcenter_id", medcenter_id);
        model.addAttribute("medcenterName", service.getMedCenterName(medcenter_id));
        model.addAttribute("medCenter", medCenter); // ← ДОБАВЬТЕ ЭТУ СТРОКУ
        model.addAttribute("bloodGroup", bloodGroup);
        model.addAttribute("rheusFactor", rheusFactor);
        model.addAttribute("componentType", componentType);
        model.addAttribute("sort", sort);
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        model.addAttribute("sortOptions", List.of(
                "deadline_asc: По сроку (сначала ближайшие)",
                "deadline_desc: По сроку (сначала дальние)",
                "bloodgroup_asc: По группе крови (А-Я)",
                "bloodgroup_desc: По группе крови (Я-А)",
                "volume_asc: По объему (по возрастанию)",
                "volume_desc: По объему (по убыванию)"
        ));
        return "list-by-medcenter";
    }


    @GetMapping("/statistics")
    public String getStatistics(
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String email,
            Model model) {

        List<BloodRequest> allRequests = service.get_all_requests();

        // Статистика по группам крови
        Map<String, Long> bloodGroupStats = allRequests.stream()
                .collect(Collectors.groupingBy(
                        BloodRequest::getBlood_group,
                        Collectors.counting()
                ));

        // Статистика по резус-фактору
        Map<String, Long> rhesusStats = allRequests.stream()
                .collect(Collectors.groupingBy(
                        BloodRequest::getRhesus_factor,
                        Collectors.counting()
                ));

        // Статистика по компонентам
        Map<String, Long> componentStats = allRequests.stream()
                .collect(Collectors.groupingBy(
                        BloodRequest::getComponent_type,
                        Collectors.counting()
                ));

        // Статистика по медцентрам
        Map<String, Long> medcenterStats = allRequests.stream()
                .collect(Collectors.groupingBy(
                        req -> service.getMedCenterName(req.getMedcenter_id()),
                        Collectors.counting()
                ));

        // Общее количество запросов
        long totalRequests = allRequests.size();

        // Количество активных запросов (с дедлайном в будущем)
        long activeRequests = allRequests.stream()
                .filter(req -> req.getDeadline() != null && req.getDeadline().isAfter(LocalDateTime.now()))
                .count();

        // Количество срочных запросов (дедлайн в ближайшие 3 дня)
        long urgentRequests = allRequests.stream()
                .filter(req -> req.getDeadline() != null &&
                        req.getDeadline().isAfter(LocalDateTime.now()) &&
                        req.getDeadline().isBefore(LocalDateTime.now().plusDays(3)))
                .count();

        // Самые популярные группы крови (топ-3)
        List<Map.Entry<String, Long>> topBloodGroups = bloodGroupStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        // Средний объем запроса
        double averageVolume = allRequests.stream()
                .filter(req -> req.getVolume() != null && !req.getVolume().trim().isEmpty())
                .mapToDouble(req -> {
                    try {
                        return Double.parseDouble(req.getVolume().replaceAll("[^0-9.]", ""));
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .average()
                .orElse(0.0);

        model.addAttribute("bloodGroupStats", bloodGroupStats);
        model.addAttribute("rhesusStats", rhesusStats);
        model.addAttribute("componentStats", componentStats);
        model.addAttribute("medcenterStats", medcenterStats);
        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("activeRequests", activeRequests);
        model.addAttribute("urgentRequests", urgentRequests);
        model.addAttribute("topBloodGroups", topBloodGroups);
        model.addAttribute("averageVolume", String.format("%.2f", averageVolume));
        model.addAttribute("user_id", user_id);
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        return "stat";
    }

}
