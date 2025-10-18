package com.example.backend.rest.donorservice;

import java.time.LocalDateTime;

public class BloodRequestDto {
    private Long blood_request_id;
    private String component_type;
    private String blood_group;
    private String rhesus_factor;
    private String volume;
    private LocalDateTime deadline;
    private String comments;
    private Long medcenter_id;

    public BloodRequestDto() {}

    public BloodRequestDto(Long blood_request_id, String component_type, String blood_group,
                           String rhesus_factor, String volume, LocalDateTime deadline,
                           String comments, Long medcenter_id) {
        this.blood_request_id = blood_request_id;
        this.component_type = component_type;
        this.blood_group = blood_group;
        this.rhesus_factor = rhesus_factor;
        this.volume = volume;
        this.deadline = deadline;
        this.comments = comments;
        this.medcenter_id = medcenter_id;
    }

    public Long getBlood_request_id() { return blood_request_id; }
    public void setBlood_request_id(Long blood_request_id) { this.blood_request_id = blood_request_id; }
    public String getComponent_type() { return component_type; }
    public void setComponent_type(String component_type) { this.component_type = component_type; }
    public String getBlood_group() { return blood_group; }
    public void setBlood_group(String blood_group) { this.blood_group = blood_group; }
    public String getRhesus_factor() { return rhesus_factor; }
    public void setRhesus_factor(String rhesus_factor) { this.rhesus_factor = rhesus_factor; }
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Long getMedcenter_id() { return medcenter_id; }
    public void setMedcenter_id(Long medcenter_id) { this.medcenter_id = medcenter_id; }
}