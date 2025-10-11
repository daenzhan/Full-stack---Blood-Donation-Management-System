package com.example.backend.rest.donorservice;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BloodRequest {
    private Long blood_request_id;
    private String component_type;
    private String blood_group;
    private String rhesus_factor;
    private String volume;
    private LocalDateTime deadline;
    private String comments;
    private Long medcenter_id;


    public Long getId() {
        return blood_request_id;
    }

    public void setId(Long id) {
        this.blood_request_id = id;
    }
}
