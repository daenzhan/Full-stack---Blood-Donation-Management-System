package com.example.backend.rest.donorservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {
    private final DonorStatsRepository donorStatsRepository;
    private final DonorAchievementRepository donorAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final DonorRepository donorRepository;
    private final DonationHistoryFeignClient donationHistoryFeignClient;
    private static final Map<String, Integer> RANK_THRESHOLDS = Map.of(
            "New Donor", 0,
            "Bronze Hero", 100,
            "Silver Savior", 300,
            "Gold Guardian", 600,
            "Platinum Protector", 1000,
            "Diamond Defender", 1500,
            "Legendary Lifesaver", 2500
    );

    @Transactional
    public void initializeDonorStats(Long donorId) {
        if (!donorStatsRepository.existsByDonorId(donorId)) {
            DonorStats stats = createNewStats(donorId);
            donorStatsRepository.save(stats);
            log.info("Initialized stats for donor: {}", donorId);
        }
    }

    @Transactional
    public void initializeStatsForExistingDonors() {
        try {
            System.out.println("=== INITIALIZING STATS FOR EXISTING DONORS ===");

            List<Donor> allDonors = donorRepository.findAll();
            System.out.println("Total donors found: " + allDonors.size());

            for (Donor donor : allDonors) {
                if (!donorStatsRepository.existsByDonorId(donor.getUserId())) {
                    System.out.println("Creating stats for donor: " + donor.getFullName() + " (ID: " + donor.getUserId() + ")");
                    ResponseEntity<List<DonationHistoryDto>> historyResponse =
                            donationHistoryFeignClient.getDonationHistoryByDonorId(donor.getUserId());

                    int totalDonations = 0;
                    int points = 0;

                    if (historyResponse.getStatusCode().is2xxSuccessful() && historyResponse.getBody() != null) {
                        List<DonationHistoryDto> donationHistory = historyResponse.getBody();
                        totalDonations = donationHistory.size();
                        points = totalDonations * 50; // Базовые очки за каждую донацию

                        System.out.println("Found " + totalDonations + " donations for donor " + donor.getFullName());
                    }

                    DonorStats stats = new DonorStats();
                    stats.setDonorId(donor.getUserId());
                    stats.setTotalDonations(totalDonations);
                    stats.setTotalLivesSaved(totalDonations * 3);
                    stats.setCurrentStreak(0);
                    stats.setLongestStreak(0);
                    stats.setPoints(points);
                    stats.setRank(getRankByPoints(points));
                    stats.setCreatedAt(LocalDateTime.now());
                    stats.setUpdatedAt(LocalDateTime.now());

                    donorStatsRepository.save(stats);
                    System.out.println("Created stats for " + donor.getFullName() + ": " + totalDonations + " donations, " + points + " points");
                } else {
                    System.out.println("Stats already exist for donor: " + donor.getFullName());
                }
            }

            System.out.println("=== STATS INITIALIZATION COMPLETED ===");

        } catch (Exception e) {
            log.error("Error initializing stats for existing donors: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private String getRankByPoints(int points) {
        if (points >= 2500) return "Legendary Lifesaver";
        if (points >= 1500) return "Diamond Defender";
        if (points >= 1000) return "Platinum Protector";
        if (points >= 600) return "Gold Guardian";
        if (points >= 300) return "Silver Savior";
        if (points >= 100) return "Bronze Hero";
        return "New Donor";
    }

    @Transactional
    public void processDonation(Long donorId) {
        try {
            System.out.println("=== PROCESSING DONATION FOR DONOR: " + donorId + " ===");

            DonorStats stats = donorStatsRepository.findByDonorId(donorId)
                    .orElseGet(() -> {
                        System.out.println("Creating new stats for donor: " + donorId);
                        DonorStats newStats = createNewStats(donorId);
                        return donorStatsRepository.save(newStats);
                    });

            System.out.println("Before update - Donations: " + stats.getTotalDonations() + ", Points: " + stats.getPoints());

            stats.setTotalDonations(stats.getTotalDonations() + 1);
            stats.setTotalLivesSaved(stats.getTotalLivesSaved() + 3);
            updateDonationStreak(stats);

            int pointsEarned = calculatePoints(stats);
            stats.setPoints(stats.getPoints() + pointsEarned);

            updateRank(stats);
            stats.setLastDonationDate(LocalDateTime.now());

            donorStatsRepository.save(stats);

            System.out.println("After update - Donations: " + stats.getTotalDonations() + ", Points: " + stats.getPoints() + ", Rank: " + stats.getRank());

            checkAchievements(donorId, stats);

            log.info("Processed donation for donor {}: +{} points, new total: {}",
                    donorId, pointsEarned, stats.getPoints());

        } catch (Exception e) {
            log.error("Error processing donation for donor {}: {}", donorId, e.getMessage());
            e.printStackTrace();
        }
    }

    private DonorStats createNewStats(Long donorId) {
        DonorStats stats = new DonorStats();
        stats.setDonorId(donorId);
        stats.setRank("New Donor");
        stats.setTotalDonations(0);
        stats.setTotalLivesSaved(0);
        stats.setCurrentStreak(0);
        stats.setLongestStreak(0);
        stats.setPoints(0);
        return stats;
    }

    private void updateDonationStreak(DonorStats stats) {
        LocalDateTime now = LocalDateTime.now();
        if (stats.getLastDonationDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(stats.getLastDonationDate(), now);
            if (daysBetween <= 90) {
                stats.setCurrentStreak(stats.getCurrentStreak() + 1);
                if (stats.getCurrentStreak() > stats.getLongestStreak()) {
                    stats.setLongestStreak(stats.getCurrentStreak());
                }
            } else {stats.setCurrentStreak(1);}
        } else {stats.setCurrentStreak(1);}
    }

    private int calculatePoints(DonorStats stats) {
        int points = 50;
        if (stats.getCurrentStreak() >= 5) points += 25;
        if (stats.getCurrentStreak() >= 10) points += 50;
        if (stats.getCurrentStreak() >= 25) points += 100;
        return points;
    }

    private void updateRank(DonorStats stats) {
        String newRank = "New Donor";
        int maxPoints = 0;
        for (Map.Entry<String, Integer> entry : RANK_THRESHOLDS.entrySet()) {
            if (stats.getPoints() >= entry.getValue() && entry.getValue() >= maxPoints) {
                newRank = entry.getKey();
                maxPoints = entry.getValue();
            }
        }
        stats.setRank(newRank);
    }

    private void checkAchievements(Long donorId, DonorStats stats) {
        try {
            List<Achievement> allAchievements = achievementRepository.findAll();
            List<DonorAchievement> earnedAchievements = new ArrayList<>();
            for (Achievement achievement : allAchievements) {
                if (!donorAchievementRepository.existsByDonorIdAndAchievementId(donorId, achievement.getId())) {
                    if (isAchievementEarned(achievement, stats)) {
                        DonorAchievement donorAchievement = new DonorAchievement();
                        donorAchievement.setDonorId(donorId);
                        donorAchievement.setAchievementId(achievement.getId());
                        donorAchievement.setEarnedAt(LocalDateTime.now());
                        donorAchievement.setIsNew(true);
                        earnedAchievements.add(donorAchievement);
                        stats.setPoints(stats.getPoints() + achievement.getPointsReward());
                        log.info("Donor {} earned achievement: {}", donorId, achievement.getName());
                    }
                }
            }
            if (!earnedAchievements.isEmpty()) {
                donorAchievementRepository.saveAll(earnedAchievements);
                donorStatsRepository.save(stats);
                log.info("Donor {} earned {} new achievements", donorId, earnedAchievements.size());}
        } catch (Exception e) {log.error("Error checking achievements for donor {}: {}", donorId, e.getMessage());}
    }

    private boolean isAchievementEarned(Achievement achievement, DonorStats stats) {
        if (achievement.getType() == null) return false;
        switch (achievement.getType()) {
            case "FIRST_DONATION":
                return stats.getTotalDonations() >= 1;
            case "MILESTONE":
                return stats.getTotalDonations() >= achievement.getRequirement();
            case "STREAK":
                return stats.getCurrentStreak() >= achievement.getRequirement();
            case "LONG_STREAK":
                return stats.getLongestStreak() >= achievement.getRequirement();
            case "LIVES_SAVED":
                return stats.getTotalLivesSaved() >= achievement.getRequirement();
            default:
                return false;
        }
    }

    public Map<String, Object> getDonorProgress(Long donorId) {
        DonorStats stats = donorStatsRepository.findByDonorId(donorId)
                .orElse(createNewStats(donorId));
        int currentPoints = stats.getPoints();
        String nextRank = getNextRank(stats.getRank());
        int pointsForNextRank = RANK_THRESHOLDS.getOrDefault(nextRank, 0);
        int pointsToNextRank = Math.max(0, pointsForNextRank - currentPoints);
        int progressPercentage = pointsForNextRank > 0 ?
                (int) ((double) currentPoints / pointsForNextRank * 100) : 100;
        Map<String, Object> progress = new HashMap<>();
        progress.put("currentRank", stats.getRank());
        progress.put("nextRank", nextRank);
        progress.put("currentPoints", currentPoints);
        progress.put("pointsToNextRank", pointsToNextRank);
        progress.put("progressPercentage", Math.min(progressPercentage, 100));
        progress.put("totalDonations", stats.getTotalDonations());
        progress.put("livesSaved", stats.getTotalLivesSaved());
        progress.put("currentStreak", stats.getCurrentStreak());
        progress.put("longestStreak", stats.getLongestStreak());
        return progress;
    }

    private String getNextRank(String currentRank) {
        List<String> ranks = new ArrayList<>(RANK_THRESHOLDS.keySet());
        Collections.sort(ranks, Comparator.comparingInt(RANK_THRESHOLDS::get));
        int currentIndex = ranks.indexOf(currentRank);
        return currentIndex < ranks.size() - 1 ? ranks.get(currentIndex + 1) : currentRank;
    }

    public List<Map<String, Object>> getLeaderboard() {
        List<DonorStats> topDonors = donorStatsRepository.findTopDonors();
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        int rank = 1;
        for (DonorStats stats : topDonors) {
            Optional<Donor> donorOpt = donorRepository.findByUserId(stats.getDonorId());
            String donorName = donorOpt.map(Donor::getFullName)
                    .orElse("Donor #" + stats.getDonorId());
            Map<String, Object> donorRank = new HashMap<>();
            donorRank.put("rank", rank++);
            donorRank.put("donorId", stats.getDonorId());
            donorRank.put("donorName", donorName);
            donorRank.put("points", stats.getPoints());
            donorRank.put("totalDonations", stats.getTotalDonations());
            donorRank.put("rankName", stats.getRank());
            donorRank.put("livesSaved", stats.getTotalLivesSaved());
            leaderboard.add(donorRank);
        }
        return leaderboard;
    }



    public List<DonorAchievement> getDonorAchievements(Long donorId) {
        return donorAchievementRepository.findByDonorIdOrderByEarnedAtDesc(donorId);
    }

    public Long getNewAchievementsCount(Long donorId) {
        return donorAchievementRepository.countNewAchievements(donorId);
    }
}