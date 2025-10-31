package org.example.analysisservice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// DonorRecommendation.java
public class DonorRecommendation {
    private Long analysisId;
    private Long donorId;
    private Long donationId;
    private EligibilityStatus eligibilityStatus;
    private LocalDateTime nextDonationDate;
    private List<String> healthRecommendations;
    private List<String> dietaryRecommendations;
    private List<String> rejectionReasons;
    private String bloodGroupInfo;
    private LocalDateTime analysisDate;

    // Конструкторы
    public DonorRecommendation() {}

    public DonorRecommendation(Long analysisId, Long donorId, Long donationId,
                               EligibilityStatus eligibilityStatus, LocalDateTime nextDonationDate,
                               List<String> healthRecommendations, List<String> dietaryRecommendations,
                               List<String> rejectionReasons, String bloodGroupInfo, LocalDateTime analysisDate) {
        this.analysisId = analysisId;
        this.donorId = donorId;
        this.donationId = donationId;
        this.eligibilityStatus = eligibilityStatus;
        this.nextDonationDate = nextDonationDate;
        this.healthRecommendations = healthRecommendations;
        this.dietaryRecommendations = dietaryRecommendations;
        this.rejectionReasons = rejectionReasons;
        this.bloodGroupInfo = bloodGroupInfo;
        this.analysisDate = analysisDate;
    }

    // Builder pattern для удобства
    public static DonorRecommendationBuilder builder() {
        return new DonorRecommendationBuilder();
    }

    // Геттеры и сеттеры
    public Long getAnalysisId() { return analysisId; }
    public void setAnalysisId(Long analysisId) { this.analysisId = analysisId; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }

    public EligibilityStatus getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(EligibilityStatus eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }

    public LocalDateTime getNextDonationDate() { return nextDonationDate; }
    public void setNextDonationDate(LocalDateTime nextDonationDate) { this.nextDonationDate = nextDonationDate; }

    public List<String> getHealthRecommendations() { return healthRecommendations; }
    public void setHealthRecommendations(List<String> healthRecommendations) { this.healthRecommendations = healthRecommendations; }

    public List<String> getDietaryRecommendations() { return dietaryRecommendations; }
    public void setDietaryRecommendations(List<String> dietaryRecommendations) { this.dietaryRecommendations = dietaryRecommendations; }

    public List<String> getRejectionReasons() { return rejectionReasons; }
    public void setRejectionReasons(List<String> rejectionReasons) { this.rejectionReasons = rejectionReasons; }

    public String getBloodGroupInfo() { return bloodGroupInfo; }
    public void setBloodGroupInfo(String bloodGroupInfo) { this.bloodGroupInfo = bloodGroupInfo; }

    public LocalDateTime getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDateTime analysisDate) { this.analysisDate = analysisDate; }

    // Builder class
    public static class DonorRecommendationBuilder {
        private Long analysisId;
        private Long donorId;
        private Long donationId;
        private EligibilityStatus eligibilityStatus;
        private LocalDateTime nextDonationDate;
        private List<String> healthRecommendations = new ArrayList<>();
        private List<String> dietaryRecommendations = new ArrayList<>();
        private List<String> rejectionReasons = new ArrayList<>();
        private String bloodGroupInfo;
        private LocalDateTime analysisDate;

        public DonorRecommendationBuilder analysisId(Long analysisId) {
            this.analysisId = analysisId;
            return this;
        }

        public DonorRecommendationBuilder donorId(Long donorId) {
            this.donorId = donorId;
            return this;
        }

        public DonorRecommendationBuilder donationId(Long donationId) {
            this.donationId = donationId;
            return this;
        }

        public DonorRecommendationBuilder eligibilityStatus(EligibilityStatus eligibilityStatus) {
            this.eligibilityStatus = eligibilityStatus;
            return this;
        }

        public DonorRecommendationBuilder nextDonationDate(LocalDateTime nextDonationDate) {
            this.nextDonationDate = nextDonationDate;
            return this;
        }

        public DonorRecommendationBuilder healthRecommendations(List<String> healthRecommendations) {
            this.healthRecommendations = healthRecommendations;
            return this;
        }

        public DonorRecommendationBuilder addHealthRecommendation(String recommendation) {
            this.healthRecommendations.add(recommendation);
            return this;
        }

        public DonorRecommendationBuilder dietaryRecommendations(List<String> dietaryRecommendations) {
            this.dietaryRecommendations = dietaryRecommendations;
            return this;
        }

        public DonorRecommendationBuilder addDietaryRecommendation(String recommendation) {
            this.dietaryRecommendations.add(recommendation);
            return this;
        }

        public DonorRecommendationBuilder rejectionReasons(List<String> rejectionReasons) {
            this.rejectionReasons = rejectionReasons;
            return this;
        }

        public DonorRecommendationBuilder addRejectionReason(String reason) {
            this.rejectionReasons.add(reason);
            return this;
        }

        public DonorRecommendationBuilder bloodGroupInfo(String bloodGroupInfo) {
            this.bloodGroupInfo = bloodGroupInfo;
            return this;
        }

        public DonorRecommendationBuilder analysisDate(LocalDateTime analysisDate) {
            this.analysisDate = analysisDate;
            return this;
        }

        public DonorRecommendation build() {
            return new DonorRecommendation(
                    analysisId, donorId, donationId, eligibilityStatus, nextDonationDate,
                    healthRecommendations, dietaryRecommendations, rejectionReasons,
                    bloodGroupInfo, analysisDate
            );
        }
    }
}
