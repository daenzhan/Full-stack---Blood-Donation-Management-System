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

    public void update_analysis_status(Long donation_id, Long analysis_id) {
        Optional<DonationHistory> donation_opt = donationHistoryRepository.findById(donation_id);
        if (donation_opt.isPresent()) {
            DonationHistory donation = donation_opt.get();
            donation.setHasAnalysis(true);
            donation.setAnalysisId(analysis_id);
            donationHistoryRepository.save(donation);
        }
    }

    public void remove_analysis_status(Long donation_id) {
        Optional<DonationHistory> donation_opt = donationHistoryRepository.findById(donation_id);
        if (donation_opt.isPresent()) {
            DonationHistory donation = donation_opt.get();
            donation.setHasAnalysis(false);
            donation.setAnalysisId(null);
            donationHistoryRepository.save(donation);
        }
    }
}