package org.example.bloodrequestservice;

//import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class MedCenterFeignClientFallbackFactory implements FallbackFactory<MedCenterFeignClient> {

    @Override
    public MedCenterFeignClient create(Throwable cause) {
        log.error("MedCenterFeignClient fallback triggered: {}", cause.getMessage());

        return new MedCenterFeignClient() {
            @Override
            public MedCenterDto get_medcenter_by_id(Long medcenter_id) {
                log.warn("Fallback: Returning default medical center for ID: {}", medcenter_id);
                MedCenterDto dto = new MedCenterDto();
                dto.setMed_center_id(medcenter_id);
                dto.setName("Medical Center " + medcenter_id);
                dto.setLocation("Location not available");
                return dto;
            }

            @Override
            public List<MedCenterDto> search_by_name(String name) {
                log.warn("Fallback: Returning empty list for search: {}", name);
                return Collections.emptyList();
            }
        };
    }
}