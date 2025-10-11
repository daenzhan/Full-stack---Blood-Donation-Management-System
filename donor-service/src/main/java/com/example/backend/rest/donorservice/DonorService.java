package com.example.backend.rest.donorservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;

    public Donor completeProfile(Long userId, DonorRequest request) {
        if (donorRepository.existsByUserId(userId)) {
            throw new RuntimeException("Profile already completed");
        }

        Donor profile = new Donor();
        profile.setUserId(userId);
        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setBloodType(request.getBloodType());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());
        profile.setGender(request.getGender());

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

        // Обновляем только разрешенные поля
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

