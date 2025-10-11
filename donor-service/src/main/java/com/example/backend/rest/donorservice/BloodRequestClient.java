package com.example.backend.rest.donorservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "blood-request-service", url = "http://localhost:8083")
public interface BloodRequestClient {

    @GetMapping("/requests")
    List<BloodRequest> getBloodRequests(
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String rhesusFactor,
            @RequestParam(required = false) String componentType,
            @RequestParam(required = false) String medcenterName
    );
}