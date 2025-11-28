package com.example.backend.rest.donationhistoryservice.activities_story;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities_story")
public class ActivityStory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_role", nullable = false, length = 50)
    private String userRole;

    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "service_source", nullable = false, length = 50)
    private String serviceSource;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ActivityStory() {}

    public ActivityStory(Long userId, String userRole, String actionType,
                         String description, String serviceSource) {
        this.userId = userId;
        this.userRole = userRole;
        this.actionType = actionType;
        this.description = description;
        this.serviceSource = serviceSource;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getServiceSource() { return serviceSource; }
    public void setServiceSource(String serviceSource) { this.serviceSource = serviceSource; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}