package com.example.backend.rest.donorservice;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donor_achievements")
@Data
public class DonorAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "donor_id")
    private Long donorId;
    @Column(name = "achievement_id")
    private Long achievementId;
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;
    @Column(name = "is_new")
    private Boolean isNew = true;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", insertable = false, updatable = false)
    private Achievement achievement;

    @PrePersist
    protected void onCreate() {
        if (earnedAt == null) {
            earnedAt = LocalDateTime.now();
        }
    }
}
