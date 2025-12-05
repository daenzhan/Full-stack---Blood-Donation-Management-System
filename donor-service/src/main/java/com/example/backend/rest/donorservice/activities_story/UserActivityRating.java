package com.example.backend.rest.donorservice.activities_story;


public class UserActivityRating {
    private Long userId;
    private String userRole;
    private Long activityCount;
    private Long rank;

    public UserActivityRating() {}

    public UserActivityRating(Long userId, String userRole, Long activityCount, Long rank) {
        this.userId = userId;
        this.userRole = userRole;
        this.activityCount = activityCount;
        this.rank = rank;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Long getActivityCount() { return activityCount; }
    public void setActivityCount(Long activityCount) { this.activityCount = activityCount; }

    public Long getRank() { return rank; }
    public void setRank(Long rank) { this.rank = rank; }
}