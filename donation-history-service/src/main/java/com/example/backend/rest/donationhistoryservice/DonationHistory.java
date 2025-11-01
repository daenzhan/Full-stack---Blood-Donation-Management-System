package com.example.backend.rest.donationhistoryservice;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "donation_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donationId;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "medcenter_id", nullable = false)
    private Long medcenterId;

    @Column(name = "blood_type", nullable = false)
    private String bloodType;

    @Column(name = "component_type", nullable = false)
    private String componentType;

    @Column(name = "donation_date", nullable = false)
    private LocalDateTime donationDate;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "status")
    private String status = "COMPLETED";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "has_analysis")
    private Boolean hasAnalysis = false;

    @Column(name = "analysis_id")
    private Long analysisId;
}
