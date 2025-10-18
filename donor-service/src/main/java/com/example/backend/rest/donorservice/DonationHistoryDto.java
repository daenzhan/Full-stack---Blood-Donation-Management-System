package com.example.backend.rest.donorservice;

import java.time.LocalDateTime;

public class DonationHistoryDto {
    private Long donationId;
    private Long donorId;
    private Long requestId;
    private Long medcenterId;
    private String bloodType;
    private String componentType;
    private Double volume;
    private LocalDateTime donationDate;
    private String status;
    private String medcenterName;

    public DonationHistoryDto() {}

    public DonationHistoryDto(Long donorId, Long requestId, Long medcenterId,
                              String bloodType, String componentType, Double volume) {
        this.donorId = donorId;
        this.requestId = requestId;
        this.medcenterId = medcenterId;
        this.bloodType = bloodType;
        this.componentType = componentType;
        this.volume = volume;
        this.donationDate = LocalDateTime.now();
        this.status = "COMPLETED";
    }

    // Геттеры и сеттеры
    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }
    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Long getMedcenterId() { return medcenterId; }
    public void setMedcenterId(Long medcenterId) { this.medcenterId = medcenterId; }
    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }
    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }
    public LocalDateTime getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDateTime donationDate) { this.donationDate = donationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMedcenterName() { return medcenterName; }
    public void setMedcenterName(String medcenterName) { this.medcenterName = medcenterName; }
}