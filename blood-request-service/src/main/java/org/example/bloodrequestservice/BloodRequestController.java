package org.example.bloodrequestservice;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/requests")
public class BloodRequestController {

    private final BloodRequestService service;

    public BloodRequestController(BloodRequestService service) {
        this.service = service;
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

    // Вспомогательный метод для проверки активных фильтров
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

        model.addAttribute("requests", requests);
        model.addAttribute("medcenter_id", medcenter_id);
        model.addAttribute("medcenterName", service.getMedCenterName(medcenter_id));
        model.addAttribute("bloodGroup", bloodGroup);
        model.addAttribute("rheusFactor", rheusFactor);
        model.addAttribute("componentType", componentType);
        model.addAttribute("sort", sort);
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

}
