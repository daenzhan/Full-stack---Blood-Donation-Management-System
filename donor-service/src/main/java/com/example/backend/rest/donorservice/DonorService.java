package com.example.backend.rest.donorservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;
    private final GamificationService gamificationService;

    public Donor completeProfile(Long userId, DonorRequest request) {
        if (donorRepository.existsByUserId(userId)) {
            throw new RuntimeException("Profile already exists");
        }

        LocalDate minDate = LocalDate.now().minusYears(65);
        LocalDate maxDate = LocalDate.now().minusYears(18);
        if (request.getDateOfBirth().isAfter(maxDate) || request.getDateOfBirth().isBefore(minDate)) {
            throw new RuntimeException("Donor must be between 18 and 65 years old");
        }

        Donor profile = new Donor();
        profile.setUserId(userId);
        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setBloodType(request.getBloodType());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());
        profile.setGender(request.getGender());
        profile.setCreatedAt(LocalDateTime.now());
        gamificationService.initializeDonorStats(userId);
        return donorRepository.save(profile);
    }

    public Donor getProfileByUserId(Long userId) {
        return donorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public void deleteProfile(Long userId) {
        Donor donor = getProfileByUserId(userId);
        donorRepository.delete(donor);
    }


    public Donor findByUserId(String userId) {
        return donorRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("Donor not found"));
    }

    public void updateDonorProfile(String userId, Donor updatedDonor) {
        Donor existingDonor = findByUserId(userId);
        existingDonor.setFullName(updatedDonor.getFullName());
        existingDonor.setDateOfBirth(updatedDonor.getDateOfBirth());
        existingDonor.setGender(updatedDonor.getGender());
        existingDonor.setBloodType(updatedDonor.getBloodType());
        existingDonor.setPhoneNumber(updatedDonor.getPhoneNumber());
        existingDonor.setAddress(updatedDonor.getAddress());

        donorRepository.save(existingDonor);

        System.out.println("Profile updated for donor: " + existingDonor.getFullName());
    }
}

