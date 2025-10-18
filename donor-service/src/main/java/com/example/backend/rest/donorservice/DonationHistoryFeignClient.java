package com.example.backend.rest.donorservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "donation-history-service", configuration = FeignConfig.class)
public interface DonationHistoryFeignClient {

    @PostMapping("/api/donation-history")
    ResponseEntity<DonationHistoryDto> createDonationHistory(@RequestBody DonationHistoryDto donationHistoryDto);

    @GetMapping("/api/donation-history/donor/{donorId}")
    ResponseEntity<List<DonationHistoryDto>> getDonationHistoryByDonorId(@PathVariable Long donorId);
}
