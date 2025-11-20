package com.example.backend.rest.donorservice;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(length = 500)
    private String description;
    private String icon;
    private String type;
    private Integer requirement;
    private Integer pointsReward;
    @Enumerated(EnumType.STRING)
    private AchievementLevel level = AchievementLevel.BRONZE;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AchievementLevel {
        BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
    }
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