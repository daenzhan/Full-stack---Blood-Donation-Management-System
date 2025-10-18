package com.example.backend.rest.donorservice;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "blood-request-client", url = "${services.blood-request.url:http://localhost:8083}", configuration = FeignConfig.class)
public interface BloodRequestFeignClient {

    @GetMapping("/api/blood-requests")
    ResponseEntity<List<BloodRequestDto>> getAllBloodRequests();

    @GetMapping("/api/blood-requests/filter")
    ResponseEntity<List<BloodRequestDto>> getFilteredBloodRequests(
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String rhesusFactor,
            @RequestParam(required = false) String componentType,
            @RequestParam(required = false) String medcenterName);

    @GetMapping("/api/blood-requests/{requestId}")
    ResponseEntity<BloodRequestDto> getBloodRequestById(@PathVariable Long requestId);

    @PostMapping("/api/blood-requests/{requestId}/accept")
    ResponseEntity<String> acceptBloodRequest(@PathVariable Long requestId,
                                              @RequestBody Map<String, Object> requestBody);
}