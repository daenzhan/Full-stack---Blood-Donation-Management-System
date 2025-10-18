package com.example.backend.rest.donationhistoryservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {
    List<DonationHistory> findByDonorIdOrderByDonationDateDesc(Long donorId);
    List<DonationHistory> findByMedcenterIdOrderByDonationDateDesc(Long medcenterId);
}