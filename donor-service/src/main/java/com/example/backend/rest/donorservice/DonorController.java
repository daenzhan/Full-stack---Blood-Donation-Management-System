package com.example.backend.rest.donorservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/donor")
@RequiredArgsConstructor
@Slf4j
public class DonorController {

    private final DonorRepository donorRepository;
    private final DonorService donorService;
    private final DonorBloodRequestService donorBloodRequestService;
    private final DonationHistoryFeignClient donationHistoryFeignClient;
    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam String token,
                                @RequestParam Long userId,
                                @RequestParam String role,
                                @RequestParam String email,
                                Model model) {

        try {
            Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            if (donorOpt.isEmpty()) {
                return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }

            Donor donor = donorOpt.get();
            List<BloodRequestDto> matchingRequests = donorBloodRequestService.getMatchingBloodRequests(donor.getBloodType());
            List<DonationHistoryDto> donationHistory = getDonationHistory(donor.getUserId());

            model.addAttribute("donor", donor);
            model.addAttribute("matchingRequests", matchingRequests);
            model.addAttribute("donationHistory", donationHistory);
            model.addAttribute("token", token);
            model.addAttribute("userId", userId);
            model.addAttribute("role", role);
            model.addAttribute("email", email);

            return "donor-dashboard";

        } catch (Exception e) {
            log.error("Error in dashboard for user {}: {}", userId, e.getMessage());
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }

    private List<DonationHistoryDto> getDonationHistory(Long donorId) {
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
        } catch (Exception e) {
            log.error("Error fetching donation history for donor {}: {}", donorId, e.getMessage());
        }
        return new ArrayList<>();
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
        if (donorOpt.isEmpty()) {
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }

        Donor donor = donorOpt.get();
        List<BloodRequestDto> requests = donorBloodRequestService.getBloodRequests(bloodGroup, rhesusFactor, componentType, medcenterName);

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

        return "donor-blood-requests";
    }
    @PostMapping("/requests/{requestId}/accept")
    public String acceptBloodRequest(@PathVariable Long requestId,
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

            Donor donor = donorOpt.get();
            boolean success = donorBloodRequestService.acceptBloodRequest(requestId, donor.getUserId());

            if (success) {
                redirectAttributes.addFlashAttribute("success", "Blood request accepted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to accept blood request. It may have been already accepted.");
            }

            return "redirect:/donor/requests?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error accepting request: " + e.getMessage());
            return "redirect:/donor/requests?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
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

            redirectAttributes.addFlashAttribute("success", "Profile completed successfully!");
            return "redirect:/donor/dashboard?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;

        } catch (Exception e) {
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

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/donor/profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;

        } catch (Exception e) {
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
            if (donorOpt.isPresent()) {
                donorRepository.delete(donorOpt.get());
                redirectAttributes.addFlashAttribute("success", "Profile deleted successfully");
            }
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;

        } catch (Exception e) {
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
        List<DonationHistoryDto> donationHistory = getDonationHistory(donor.getUserId());

        model.addAttribute("donor", donor);
        model.addAttribute("donationHistory", donationHistory);
        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        return "donor-donation-history";
    }
}