package com.example.backend.rest.donorservice;
import com.example.backend.rest.donorservice.activities_story.ActivityStoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequestMapping("/donor")
@RequiredArgsConstructor
@Slf4j
public class DonorController {

    private final DonorRepository donorRepository;
    private final DonorService donorService;
    private final DonorBloodRequestService donorBloodRequestService;
    private final DonationHistoryFeignClient donationHistoryFeignClient;
    private final EmailService emailService;
    private final BloodRequestFeignClient bloodRequestFeignClient;
    private final GamificationService gamificationService;
    private final DonorAchievementRepository donorAchievementRepository;
    private final DonorStatsRepository donorStatsRepository;
    private final MedCenterFeignClient  appointmentFeignClient;
    @Autowired
    private ActivityStoryService activityStoryService;

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam String token,
                                @RequestParam Long userId,
                                @RequestParam String role,
                                @RequestParam String email,
                                Model model) {
        try {
            log.info("Dashboard request for user: {}", userId);

            Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            if (donorOpt.isEmpty()) {
                log.warn("Donor not found for user: {}", userId);
                // Вместо редиректа показываем форму прямо здесь
                model.addAttribute("error", "Профиль не найден. Пожалуйста, заполните информацию.");
                model.addAttribute("token", token);
                model.addAttribute("userId", userId);
                model.addAttribute("role", role);
                model.addAttribute("email", email);
                model.addAttribute("minDate", LocalDate.now().minusYears(65));
                model.addAttribute("maxDate", LocalDate.now().minusYears(18));
                return "donor-complete-profile"; // Прямой возврат формы
            }

            Donor donor = donorOpt.get();
            log.info("Donor found: {}", donor.getFullName());

            // Инициализация статистики с обработкой ошибок
            try {
                gamificationService.initializeDonorStats(donor.getUserId());
            } catch (Exception e) {
                log.warn("Failed to initialize stats: {}", e.getMessage());
            }

            // Получение данных с защитой от ошибок
            List<BloodRequestDto> matchingRequests = new ArrayList<>();
            try {
                matchingRequests = donorBloodRequestService.getMatchingBloodRequests(donor.getBloodType());
            } catch (Exception e) {
                log.warn("Failed to get matching requests: {}", e.getMessage());
            }

            Map<String, Object> donationInfo = new HashMap<>();
            try {
                donationInfo = donorBloodRequestService.getNextDonationInfo(donor.getUserId());
            } catch (Exception e) {
                log.warn("Failed to get donation info: {}", e.getMessage());
            }

            List<DonationHistoryDto> donationHistory = new ArrayList<>();
            try {
                donationHistory = getDonationHistoryForDonor(donor.getUserId());
            } catch (Exception e) {
                log.warn("Failed to get donation history: {}", e.getMessage());
            }

            Map<String, Object> donorProgress = new HashMap<>();
            try {
                donorProgress = gamificationService.getDonorProgress(donor.getUserId());
            } catch (Exception e) {
                log.warn("Failed to get donor progress: {}", e.getMessage());
            }

            // Получение назначений
            boolean hasUpcomingAppointments = false;
            int upcomingAppointmentsCount = 0;

            try {
                ResponseEntity<List<Map<String, Object>>> appointmentsResponse =
                        appointmentFeignClient.getDonorAppointments(donor.getUserId());

                if (appointmentsResponse != null &&
                        appointmentsResponse.getStatusCode().is2xxSuccessful() &&
                        appointmentsResponse.getBody() != null) {

                    List<Map<String, Object>> appointments = appointmentsResponse.getBody();
                    LocalDate today = LocalDate.now();

                    for (Map<String, Object> appointment : appointments) {
                        try {
                            if (appointment.get("appointmentDate") == null || appointment.get("status") == null) {
                                continue;
                            }

                            LocalDate appDate = LocalDate.parse(appointment.get("appointmentDate").toString());
                            String status = appointment.get("status").toString();

                            if ((appDate.isAfter(today) || appDate.equals(today)) &&
                                    !status.equals("CANCELLED") &&
                                    !status.equals("COMPLETED")) {
                                upcomingAppointmentsCount++;
                            }
                        } catch (Exception e) {
                            log.warn("Error processing appointment: {}", e.getMessage());
                        }
                    }

                    hasUpcomingAppointments = upcomingAppointmentsCount > 0;
                }
            } catch (Exception e) {
                log.warn("Failed to get appointments: {}", e.getMessage());
            }

            try {
                activityStoryService.record_activity(
                        userId,
                        "DONOR",
                        "DASHBOARD_VIEWED",
                        "User viewed donor dashboard"
                );
            } catch (Exception e) {
                log.warn("Failed to record activity: {}", e.getMessage());
            }

            model.addAttribute("donor", donor);
            model.addAttribute("matchingRequests", matchingRequests);
            model.addAttribute("donationHistory", donationHistory);
            model.addAttribute("donationInfo", donationInfo);
            model.addAttribute("donorProgress", donorProgress);
            model.addAttribute("token", token);
            model.addAttribute("userId", userId);
            model.addAttribute("role", role);
            model.addAttribute("email", email);
            model.addAttribute("donor_id", donor.getUserId());
            model.addAttribute("hasUpcomingAppointments", hasUpcomingAppointments);
            model.addAttribute("upcomingAppointmentsCount", upcomingAppointmentsCount);

            return "donor-dashboard";

        } catch (Exception e) {
            log.error("Error in dashboard for user {}: {}", userId, e.getMessage(), e);

            // ВАЖНО: Вместо редиректа показываем страницу с ошибкой
            model.addAttribute("error", "Ошибка загрузки дашборда: " + e.getMessage());
            model.addAttribute("token", token);
            model.addAttribute("userId", userId);
            model.addAttribute("role", role);
            model.addAttribute("email", email);
            return "error"; // Создайте error.html в templates
        }
    }

    private List<DonationHistoryDto> getDonationHistoryForDonor(Long donorId) {
        try {
            ResponseEntity<List<DonationHistoryDto>> historyResponse =
                    donationHistoryFeignClient.getDonationHistoryByDonorId(donorId);
            if (historyResponse.getStatusCode().is2xxSuccessful() && historyResponse.getBody() != null) {
                List<DonationHistoryDto> donationHistory = historyResponse.getBody();
                for (DonationHistoryDto history : donationHistory) {
                    String medcenterName = donorBloodRequestService.getMedCenterName(history.getMedcenterId());
                    history.setMedcenterName(medcenterName);
                }
                return donationHistory;
            }
        } catch (Exception e) {log.error("Error fetching donation history for donor {}: {}", donorId, e.getMessage());
        }return new ArrayList<>();
    }

    @GetMapping("/requests")
    public String viewBloodRequests(@RequestParam String token,
                                    @RequestParam Long userId,
                                    @RequestParam String role,
                                    @RequestParam String email,
                                    @RequestParam(required = false) String bloodGroup,
                                    @RequestParam(required = false) String rhesusFactor,
                                    @RequestParam(required = false) String componentType,
                                    @RequestParam(required = false) String medcenterName,
                                    Model model) {
        Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
        if (donorOpt.isEmpty()) {return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
        Donor donor = donorOpt.get();
        Map<String, Object> donationInfo = donorBloodRequestService.getNextDonationInfo(donor.getUserId());
        List<BloodRequestDto> requests = donorBloodRequestService.getBloodRequests(bloodGroup, rhesusFactor, componentType, medcenterName);

        activityStoryService.record_activity(
                userId,
                "DONOR",
                "BLOOD_REQUESTS_VIEWED",
                "User viewed blood requests with filters - bloodGroup: " + bloodGroup +
                        ", rhesusFactor: " + rhesusFactor + ", componentType: " + componentType
        );

        model.addAttribute("donor", donor);
        model.addAttribute("requests", requests);
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        model.addAttribute("bloodGroup", bloodGroup);
        model.addAttribute("rhesusFactor", rhesusFactor);
        model.addAttribute("componentType", componentType);
        model.addAttribute("medcenterName", medcenterName);
        model.addAttribute("donationInfo", donationInfo);
        return "donor-blood-requests";
    }

    @PostMapping("/requests/{requestId}/accept")
    public String acceptBloodRequest(@PathVariable Long requestId,
                                     @RequestParam String token,
                                     @RequestParam Long userId,
                                     @RequestParam String role,
                                     @RequestParam String email,
                                     RedirectAttributes redirectAttributes) {
        try {Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            if (donorOpt.isEmpty()) {return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }
            Donor donor = donorOpt.get();
            System.out.println("=== ACCEPTING BLOOD REQUEST ===");
            System.out.println("Donor: " + donor.getFullName() + " (ID: " + donor.getUserId() + ")");
            System.out.println("Request ID: " + requestId);
            ResponseEntity<BloodRequestDto> response = bloodRequestFeignClient.getBloodRequestById(requestId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                redirectAttributes.addFlashAttribute("error", "Blood request not found");
                return "redirect:/donor/requests?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }
            BloodRequestDto bloodRequestDto = response.getBody();
            if (bloodRequestDto.getDeadline().isBefore(LocalDateTime.now())) {

                activityStoryService.record_activity(
                        userId,
                        "DONOR",
                        "BLOOD_REQUEST_EXPIRED_ATTEMPT",
                        "Attempted to accept expired blood request #" + requestId +
                                " (deadline: " + bloodRequestDto.getDeadline().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ")"
                );
                redirectAttributes.addFlashAttribute("error",
                        "This blood request has expired and can no longer be accepted. Deadline was: " +
                                bloodRequestDto.getDeadline().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                return "redirect:/donor/requests?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }
            String donorBloodType = donor.getBloodType();
            String requestBloodType = bloodRequestDto.getBlood_group() + bloodRequestDto.getRhesus_factor();

            if (!donorBloodRequestService.isBloodTypeCompatible(donorBloodType, requestBloodType)) {

                activityStoryService.record_activity(
                        userId,
                        "DONOR",
                        "BLOOD_TYPE_INCOMPATIBLE",
                        "Attempted to accept blood request #" + requestId +
                                " but blood types are incompatible (donor: " + donorBloodType +
                                ", required: " + requestBloodType + ")"
                );

                redirectAttributes.addFlashAttribute("error",
                        "Your blood type (" + donorBloodType + ") is not compatible with the required type (" + requestBloodType + ")");
                return "redirect:/donor/requests?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }
            ResponseEntity<List<DonationHistoryDto>> historyResponse =
                    donationHistoryFeignClient.getDonationHistoryByDonorId(donor.getUserId());
            if (historyResponse.getStatusCode().is2xxSuccessful() && historyResponse.getBody() != null) {
                List<DonationHistoryDto> donationHistory = historyResponse.getBody();
                if (!donationHistory.isEmpty()) {
                    DonationHistoryDto lastDonation = donationHistory.stream()
                            .max(Comparator.comparing(DonationHistoryDto::getDonationDate))
                            .orElse(null);
                    if (lastDonation != null) {
                        LocalDateTime lastDonationDate = lastDonation.getDonationDate();
                        LocalDateTime now = LocalDateTime.now();
                        long daysBetween = ChronoUnit.DAYS.between(lastDonationDate, now);
                        if (daysBetween < 60) {
                            long daysLeft = 60 - daysBetween;

                            activityStoryService.record_activity(
                                    userId,
                                    "DONOR",
                                    "DONATION_TOO_EARLY_ATTEMPT",
                                    "Attempted to donate too early - last donation was " + daysBetween +
                                            " days ago, need to wait " + daysLeft + " more days"
                            );

                            redirectAttributes.addFlashAttribute("error",
                                    "You can only donate blood once every 60 days. " +
                                            "Your last donation was " + daysBetween + " days ago. " +
                                            "Please wait " + daysLeft + " more days before donating again.");
                            return "redirect:/donor/requests?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
                        }
                    }
                }
            }
            BloodRequestDto bloodRequest = convertToBloodRequest(bloodRequestDto);
            try {emailService.sendDonationConfirmation(email, bloodRequest);
                log.info("Donation confirmation email sent successfully to: {}", email);
            } catch (Exception e) {log.error("Failed to send email to {}, but continuing with request acceptance: {}", email, e.getMessage());
            }
            Map<String, Object> result = donorBloodRequestService.acceptBloodRequest(requestId, donor.getUserId());

            boolean success = (Boolean) result.getOrDefault("success", false);
            String message = (String) result.getOrDefault("message", "");

            if (success) {

                activityStoryService.record_activity(
                        userId,
                        "DONOR",
                        "BLOOD_REQUEST_ACCEPTED",
                        "Successfully accepted blood request #" + requestId +
                                " for blood type " + requestBloodType +
                                " at medical center " + bloodRequestDto.getMedcenter_id()
                );

                System.out.println("Calling gamificationService.processDonation for donor: " + donor.getUserId());
                gamificationService.processDonation(donor.getUserId());
                redirectAttributes.addFlashAttribute("success",
                        message + " Confirmation email has been sent to " + email);
            } else {

                activityStoryService.record_activity(
                        userId,
                        "DONOR",
                        "BLOOD_REQUEST_ACCEPT_FAILED",
                        "Failed to accept blood request #" + requestId + " - " + message
                );

                redirectAttributes.addFlashAttribute("error", message);
            }

            return "redirect:/donor/donation-history?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        } catch (Exception e) {

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "BLOOD_REQUEST_ACCEPT_ERROR",
                    "Error accepting blood request #" + requestId + ": " + e.getMessage()
            );

            log.error("Error accepting blood request {} by donor {}: {}", requestId, userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error accepting request: " + e.getMessage());
            return "redirect:/donor/requests?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }
    private BloodRequestDto convertToBloodRequest(BloodRequestDto dto) {
        BloodRequestDto request = new BloodRequestDto();
        request.setBlood_request_id(dto.getBlood_request_id());
        request.setBlood_group(dto.getBlood_group());
        request.setRhesus_factor(dto.getRhesus_factor());
        request.setComponent_type(dto.getComponent_type());
        request.setVolume(dto.getVolume());
        request.setMedcenter_id(dto.getMedcenter_id());
        request.setDeadline(dto.getDeadline());
        request.setComments(dto.getComments());
        return request;
    }

    @GetMapping("/complete-profile")
    public String showCompleteProfileForm(@RequestParam String token,
                                          @RequestParam Long userId,
                                          @RequestParam String role,
                                          @RequestParam String email,
                                          Model model) {
        if (donorRepository.existsByUserId(userId)) {
            return "redirect:/donor/dashboard?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }

        activityStoryService.record_activity(
                userId,
                "DONOR",
                "PROFILE_CREATION_STARTED",
                "User started completing donor profile"
        );

        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        model.addAttribute("minDate", LocalDate.now().minusYears(65));
        model.addAttribute("maxDate", LocalDate.now().minusYears(18));
        return "donor-complete-profile";
    }

    @PostMapping("/complete-profile")
    public String processCompleteProfile(@RequestParam String fullName,
                                         @RequestParam String dateOfBirth,
                                         @RequestParam String bloodType,
                                         @RequestParam String phoneNumber,
                                         @RequestParam String address,
                                         @RequestParam String gender,
                                         @RequestParam String token,
                                         @RequestParam Long userId,
                                         @RequestParam String role,
                                         @RequestParam String email,
                                         RedirectAttributes redirectAttributes) {
        try {
            LocalDate birthDate = LocalDate.parse(dateOfBirth);
            LocalDate minDate = LocalDate.now().minusYears(65);
            LocalDate maxDate = LocalDate.now().minusYears(18);
            if (birthDate.isAfter(maxDate) || birthDate.isBefore(minDate)) {
                redirectAttributes.addFlashAttribute("error", "You must be between 18 and 65 years old to donate blood");
                return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }
            DonorRequest request = new DonorRequest();
            request.setFullName(fullName);
            request.setDateOfBirth(birthDate);
            request.setBloodType(bloodType);
            request.setPhoneNumber(phoneNumber);
            request.setAddress(address);
            request.setGender(gender);
            donorService.completeProfile(userId, request);

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "PROFILE_CREATED",
                    "Donor profile created successfully - Name: " + fullName +
                            ", Blood Type: " + bloodType + ", Phone: " + phoneNumber
            );

            redirectAttributes.addFlashAttribute("success", "Profile completed successfully!");
            return "redirect:/donor/dashboard?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        } catch (Exception e) {

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "PROFILE_CREATION_FAILED",
                    "Failed to create donor profile: " + e.getMessage()
            );

            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }

    @GetMapping("/profile")
    public String viewProfile(@RequestParam String token,
                              @RequestParam Long userId,
                              @RequestParam String role,
                              @RequestParam String email,
                              Model model) {
        Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
        if (donorOpt.isEmpty()) {
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }

        activityStoryService.record_activity(
                userId,
                "DONOR",
                "PROFILE_VIEWED",
                "User viewed their donor profile"
        );

        model.addAttribute("donor", donorOpt.get());
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        return "donor-profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(@RequestParam String token,
                                      @RequestParam Long userId,
                                      @RequestParam String role,
                                      @RequestParam String email,
                                      Model model) {
        Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
        if (donorOpt.isEmpty()) {
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }

        activityStoryService.record_activity(
                userId,
                "DONOR",
                "PROFILE_EDIT_STARTED",
                "User started editing donor profile"
        );

        model.addAttribute("donor", donorOpt.get());
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        model.addAttribute("minDate", LocalDate.now().minusYears(65));
        model.addAttribute("maxDate", LocalDate.now().minusYears(18));
        model.addAttribute("bloodTypes", Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        model.addAttribute("genders", Arrays.asList("MALE", "FEMALE", "OTHER"));
        return "donor-edit-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String dateOfBirth,
                                @RequestParam String bloodType,
                                @RequestParam String phoneNumber,
                                @RequestParam String address,
                                @RequestParam String gender,
                                @RequestParam String token,
                                @RequestParam Long userId,
                                @RequestParam String role,
                                @RequestParam String email,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            if (donorOpt.isEmpty()) {
                return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }
            LocalDate birthDate = LocalDate.parse(dateOfBirth);
            LocalDate minDate = LocalDate.now().minusYears(65);
            LocalDate maxDate = LocalDate.now().minusYears(18);
            if (birthDate.isAfter(maxDate) || birthDate.isBefore(minDate)) {
                redirectAttributes.addFlashAttribute("error", "You must be between 18 and 65 years old to donate blood");
                return "redirect:/donor/profile/edit?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }
            Donor donor = donorOpt.get();
            donor.setFullName(fullName);
            donor.setDateOfBirth(birthDate);
            donor.setBloodType(bloodType);
            donor.setPhoneNumber(phoneNumber);
            donor.setAddress(address);
            donor.setGender(gender);
            donorRepository.save(donor);

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "PROFILE_UPDATED",
                    "Donor profile updated - Name: " + fullName +
                            ", Blood Type: " + bloodType + ", Phone: " + phoneNumber
            );

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/donor/profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        } catch (Exception e) {

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "PROFILE_UPDATE_FAILED",
                    "Failed to update donor profile: " + e.getMessage()
            );

            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/donor/profile/edit?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }

    @PostMapping("/profile/delete")
    public String deleteProfile(@RequestParam String token,
                                @RequestParam Long userId,
                                @RequestParam String role,
                                @RequestParam String email,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            Donor donor = donorOpt.get();

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "PROFILE_DELETED",
                    "Donor profile deleted - Name: " + donor.getFullName() +
                            ", Blood Type: " + donor.getBloodType()
            );

            if (donorOpt.isPresent()) {
                donorRepository.delete(donorOpt.get());
                redirectAttributes.addFlashAttribute("success", "Profile deleted successfully");
            }
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        } catch (Exception e) {

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "PROFILE_DELETE_FAILED",
                    "Failed to delete donor profile: " + e.getMessage()
            );

            redirectAttributes.addFlashAttribute("error", "Error deleting profile: " + e.getMessage());
            return "redirect:/donor/dashboard?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }


    @GetMapping("/donation-history")
    public String viewDonationHistory(@RequestParam String token,
                                      @RequestParam Long userId,
                                      @RequestParam String role,
                                      @RequestParam String email,
                                      Model model) {
        Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
        if (donorOpt.isEmpty()) {
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
        Donor donor = donorOpt.get();

        activityStoryService.record_activity(
                userId,
                "DONOR",
                "DONATION_HISTORY_VIEWED",
                "User viewed donation history"
        );

        List<DonationHistoryDto> donationHistory = getDonationHistoryForDonor(donor.getUserId());
        model.addAttribute("donor", donor);
        model.addAttribute("donationHistory", donationHistory);
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        return "donor-donation-history";
    }

    @GetMapping("/leaderboard")
    public String showLeaderboard(@RequestParam String token,
                                  @RequestParam Long userId,
                                  @RequestParam String role,
                                  @RequestParam String email,
                                  Model model) {
        Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
        if (donorOpt.isEmpty()) {return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;}
        Donor donor = donorOpt.get();
        gamificationService.initializeStatsForExistingDonors();
        gamificationService.initializeDonorStats(donor.getUserId());
        List<Map<String, Object>> leaderboard = gamificationService.getLeaderboard();
        Map<String, Object> donorProgress = gamificationService.getDonorProgress(donor.getUserId());

        System.out.println("=== LEADERBOARD DEBUG ===");
        System.out.println("Current donor: " + donor.getFullName() + " (ID: " + donor.getUserId() + ")");
        System.out.println("Leaderboard entries: " + leaderboard.size());

        List<DonorStats> allStats = donorStatsRepository.findAll();
        System.out.println("All stats in DB:");
        allStats.forEach(s -> {
            Optional<Donor> d = donorRepository.findByUserId(s.getDonorId());
            String name = d.map(Donor::getFullName).orElse("Unknown");
            System.out.println(" - " + name + ": " + s.getPoints() + " points, " + s.getTotalDonations() + " donations");
        });

        int userRank = 1;
        boolean userFound = false;
        for (Map<String, Object> entry : leaderboard) {
            if (entry.get("donorId").equals(donor.getUserId())) {
                userFound = true;
                break;
            }
            userRank++;
        }
        if (!userFound) {
            userRank = leaderboard.size() + 1;
        }

        activityStoryService.record_activity(
                userId,
                "DONOR",
                "LEADERBOARD_VIEWED",
                "User viewed donor leaderboard"
        );

        model.addAttribute("donor", donor);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("donorProgress", donorProgress);
        model.addAttribute("userRank", userRank);
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        return "donor-leaderboard";
    }

    @GetMapping("/achievements")
    public String showAchievements(@RequestParam String token,
                                   @RequestParam Long userId,
                                   @RequestParam String role,
                                   @RequestParam String email,
                                   Model model) {
        Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
        if (donorOpt.isEmpty()) {return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;}
        Donor donor = donorOpt.get();
        List<DonorAchievement> achievements = donorAchievementRepository.findByDonorIdOrderByEarnedAtDesc(donor.getUserId());
        Map<String, Object> donorProgress = gamificationService.getDonorProgress(donor.getUserId());
        achievements.forEach(achievement -> {
            if (achievement.getIsNew()) {
                achievement.setIsNew(false);
                donorAchievementRepository.save(achievement);
            }
        });

        activityStoryService.record_activity(
                userId,
                "DONOR",
                "ACHIEVEMENTS_VIEWED",
                "User viewed their achievements"
        );

        model.addAttribute("donor", donor);
        model.addAttribute("achievements", achievements);
        model.addAttribute("donorProgress", donorProgress);
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        return "donor-achievements";
    }

    @GetMapping("/appointments-menu")
    public String showAppointmentsMenu(@RequestParam String token,
                                       @RequestParam Long userId,
                                       @RequestParam String role,
                                       @RequestParam String email,
                                       Model model) {
        try {
            Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            if (donorOpt.isEmpty()) {
                return "redirect:/donor/complete-profile?token=" + token +
                        "&userId=" + userId + "&role=" + role + "&email=" + email;
            }

            Donor donor = donorOpt.get();

            // Get appointments
            ResponseEntity<List<Map<String, Object>>> appointmentsResponse =
                    appointmentFeignClient.getDonorAppointments(donor.getUserId());

            List<Map<String, Object>> appointments = List.of();
            if (appointmentsResponse != null &&
                    appointmentsResponse.getStatusCode().is2xxSuccessful() &&
                    appointmentsResponse.getBody() != null) {
                appointments = appointmentsResponse.getBody();
            }

            // Separate upcoming and past appointments
            List<Map<String, Object>> upcomingAppointments = new ArrayList<>();
            List<Map<String, Object>> pastAppointments = new ArrayList<>();

            LocalDate today = LocalDate.now();

            for (Map<String, Object> appointment : appointments) {
                try {
                    if (appointment.get("appointmentDate") == null || appointment.get("status") == null) {
                        continue;
                    }

                    LocalDate appDate = LocalDate.parse(appointment.get("appointmentDate").toString());
                    String status = appointment.get("status").toString();

                    if ((appDate.isAfter(today) || appDate.equals(today)) &&
                            !status.equals("CANCELLED") &&
                            !status.equals("COMPLETED")) {
                        upcomingAppointments.add(appointment);
                    } else {
                        pastAppointments.add(appointment);
                    }
                } catch (Exception e) {
                    log.warn("Error processing appointment: {}", e.getMessage());
                }
            }

            model.addAttribute("donor", donor);
            model.addAttribute("upcomingAppointments", upcomingAppointments);
            model.addAttribute("pastAppointments", pastAppointments);
            model.addAttribute("token", token);
            model.addAttribute("userId", userId);
            model.addAttribute("role", role);
            model.addAttribute("email", email);

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "APPOINTMENTS_MENU_VIEWED",
                    "Viewed appointments menu"
            );

            return "donor-appointments-menu";

        } catch (Exception e) {
            log.error("Error loading appointments menu: {}", e.getMessage());
            return "redirect:/donor/dashboard?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }

    @GetMapping("/appointments/api/next-appointment")
    @ResponseBody
    public ResponseEntity<?> getNextAppointment(@RequestParam Long userId) {
        try {
            ResponseEntity<List<Map<String, Object>>> response =
                    appointmentFeignClient.getDonorAppointments(userId);

            if (response != null &&
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null) {

                List<Map<String, Object>> appointments = response.getBody();
                LocalDate today = LocalDate.now();
                Optional<Map<String, Object>> nextAppointment = appointments.stream()
                        .filter(appointment -> {
                            try {
                                if (appointment.get("appointmentDate") == null ||
                                        appointment.get("status") == null) {
                                    return false;
                                }

                                LocalDate appDate = LocalDate.parse(appointment.get("appointmentDate").toString());
                                String status = appointment.get("status").toString();

                                return (appDate.isAfter(today) || appDate.equals(today)) &&
                                        !status.equals("CANCELLED") &&
                                        !status.equals("COMPLETED");
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .min((a, b) -> {
                            LocalDate dateA = LocalDate.parse(a.get("appointmentDate").toString());
                            LocalDate dateB = LocalDate.parse(b.get("appointmentDate").toString());

                            if (dateA.equals(dateB)) {
                                LocalTime timeA = LocalTime.parse(a.get("appointmentTime").toString());
                                LocalTime timeB = LocalTime.parse(b.get("appointmentTime").toString());
                                return timeA.compareTo(timeB);
                            }
                            return dateA.compareTo(dateB);
                        });

                if (nextAppointment.isPresent()) {
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "appointment", nextAppointment.get()
                    ));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No upcoming appointments"
            ));

        } catch (Exception e) {
            log.error("Error getting next appointment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

}