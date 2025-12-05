package org.example.bloodrequestservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "med-center-service")
public interface MedCenterFeignClient {

    @GetMapping("/medcenters/client/{medcenter_id}")
    MedCenterDto get_medcenter_by_id(@PathVariable("medcenter_id") Long medcenter_id);

    @GetMapping("/medcenters/client/search")
    List<MedCenterDto> search_by_name(@RequestParam String name);
}