package com.example.backend.rest.donationhistoryservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/donation-history")
@RequiredArgsConstructor
public class DonationHistoryController {

    private final DonationHistoryService donationHistoryService;

    @GetMapping("/donor/{donorId}")
    public List<DonationHistory> getDonationHistoryByDonorId(@PathVariable Long donorId) {
        return donationHistoryService.getDonationHistoryByDonorId(donorId);
    }

    @GetMapping("/medcenter/{medcenterId}")
    public List<DonationHistory> getDonationHistoryByMedcenterId(@PathVariable Long medcenterId) {
        return donationHistoryService.getDonationHistoryByMedcenterId(medcenterId);
    }

    @PostMapping
    public DonationHistory createDonationHistory(@RequestBody DonationHistory donationHistory) {
        return donationHistoryService.createDonationHistory(donationHistory);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationHistory> getDonationHistoryById(@PathVariable Long id) {
        Optional<DonationHistory> history = donationHistoryService.getDonationHistoryById(id);
        return history.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
