package com.example.backend.rest.donorservice;


import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donor_stats")
@Data
public class DonorStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "donor_id", unique = true)
    private Long donorId;
    @Column(name = "total_donations")
    private Integer totalDonations = 0;
    @Column(name = "total_lives_saved")
    private Integer totalLivesSaved = 0;
    @Column(name = "current_streak")
    private Integer currentStreak = 0;
    @Column(name = "longest_streak")
    private Integer longestStreak = 0;
    private Integer points = 0;
    private String rank = "New Donor";
    @Column(name = "last_donation_date")
    private LocalDateTime lastDonationDate;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}