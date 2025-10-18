package com.example.backend.rest.donorservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "med-center-service", configuration = FeignConfig.class)
public interface MedCenterFeignClient {

    @GetMapping("/medcenters/client/{medcenter_id}")
    ResponseEntity<MedCenterDto> get_medcenter_by_id(@PathVariable Long medcenter_id);

    @GetMapping("/medcenters/client/search")
    ResponseEntity<List<MedCenterDto>> search_by_name(@RequestParam String name);
}
