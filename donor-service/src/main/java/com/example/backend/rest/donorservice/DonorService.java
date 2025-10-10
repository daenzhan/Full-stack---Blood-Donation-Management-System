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
}

