package com.example.backend.rest.donorservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/donor")
@RequiredArgsConstructor
public class DonorController {

    private final DonorRepository donorRepository;

    @GetMapping("/complete-profile")
    public String showCompleteProfilePage(@RequestParam String token,
                                          @RequestParam Long userId,
                                          @RequestParam String role,
                                          @RequestParam String email,
                                          Model model) {

        model.addAttribute("token", token);
        model.addAttribute("userId", userId);
        model.addAttribute("role", role);
        model.addAttribute("email", email);

        return "donor-complete-profile";
    }

    @PostMapping("/complete-profile")
    public String completeProfile(@RequestParam("fullName") String fullName,
                                  @RequestParam("dateOfBirth") String dateOfBirthStr,
                                  @RequestParam("bloodType") String bloodType,
                                  @RequestParam("phoneNumber") String phoneNumber,
                                  @RequestParam("address") String address,
                                  @RequestParam("gender") String gender,
                                  @RequestParam("token") String token,
                                  @RequestParam("userId") Long userId,
                                  @RequestParam("role") String role,
                                  @RequestParam("email") String email,
                                  RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Received data for user " + userId + ": " + fullName + ", " + dateOfBirthStr + ", " + bloodType);
            if (donorRepository.existsByUserId(userId)) {
                redirectAttributes.addFlashAttribute("error", "Profile already exists");
                return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
            }
            LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr);

            Donor profile = new Donor();
            profile.setUserId(userId);
            profile.setFullName(fullName);
            profile.setDateOfBirth(dateOfBirth);
            profile.setBloodType(bloodType);
            profile.setPhoneNumber(phoneNumber);
            profile.setAddress(address);
            profile.setGender(gender);
            profile.setCreatedAt(LocalDateTime.now());

            donorRepository.save(profile);
            System.out.println("Donor profile saved for user: " + userId);
            return "redirect:http://localhost:8080/home?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email +
                    "&success=Donor profile completed successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error completing profile: " + e.getMessage());
            return "redirect:/donor/complete-profile?token=" + token + "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }
}
