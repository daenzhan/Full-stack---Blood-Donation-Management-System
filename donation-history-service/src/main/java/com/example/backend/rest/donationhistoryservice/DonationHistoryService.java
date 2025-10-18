package com.example.backend.rest.donationhistoryservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DonationHistoryService {
    private final DonationHistoryRepository donationHistoryRepository;

    public List<DonationHistory> getDonationHistoryByDonorId(Long donorId) {
        return donationHistoryRepository.findByDonorIdOrderByDonationDateDesc(donorId);
    }

    public List<DonationHistory> getDonationHistoryByMedcenterId(Long medcenterId) {
        return donationHistoryRepository.findByMedcenterIdOrderByDonationDateDesc(medcenterId);
    }

    public DonationHistory createDonationHistory(DonationHistory donationHistory) {
        donationHistory.setDonationDate(LocalDateTime.now());
        donationHistory.setCreatedAt(LocalDateTime.now());
        return donationHistoryRepository.save(donationHistory);
    }

    public Optional<DonationHistory> getDonationHistoryById(Long id) {
        return donationHistoryRepository.findById(id);
    }
}