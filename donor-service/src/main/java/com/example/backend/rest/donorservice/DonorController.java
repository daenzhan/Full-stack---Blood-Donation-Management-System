package com.example.backend.rest.donorservice;
import com.example.backend.rest.donorservice.activities_story.ActivityStoryService;
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
    @Autowired
    private ActivityStoryService activityStoryService;

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam String token,
                                @RequestParam Long userId,
                                @RequestParam String role,
                                @RequestParam String email,
                                Model model) {
        try {
            Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            if (donorOpt.isEmpty()) {return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }
            Donor donor = donorOpt.get();
            gamificationService.initializeDonorStats(donor.getUserId());
            List<BloodRequestDto> matchingRequests = donorBloodRequestService.getMatchingBloodRequests(donor.getBloodType());
            Map<String, Object> donationInfo = donorBloodRequestService.getNextDonationInfo(donor.getUserId());
            List<DonationHistoryDto> donationHistory = getDonationHistoryForDonor(donor.getUserId());
            Map<String, Object> donorProgress = gamificationService.getDonorProgress(donor.getUserId());

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "DASHBOARD_VIEWED",
                    "User viewed donor dashboard"
            );

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
            return "donor-dashboard";
        } catch (Exception e) {log.error("Error in dashboard for user {}: {}", userId, e.getMessage());
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
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
}