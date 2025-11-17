package org.example.analysisservice;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
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
}
