package com.example.backend.rest.donorservice;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AchievementDataLoader implements CommandLineRunner {
    private final AchievementRepository achievementRepository;
    @Override
    public void run(String... args) {
        if (achievementRepository.count() == 0) {
            List<Achievement> achievements = Arrays.asList(
                    createAchievement("First Blood", "Complete your first donation", "fas fa-tint", "FIRST_DONATION", 1, 100, Achievement.AchievementLevel.BRONZE),
                    createAchievement("Hero in Training", "Complete 5 donations", "fas fa-shield-alt", "MILESTONE", 5, 250, Achievement.AchievementLevel.BRONZE),
                    createAchievement("Life Savior", "Complete 10 donations", "fas fa-heart", "MILESTONE", 10, 500, Achievement.AchievementLevel.SILVER),
                    createAchievement("Blood Veteran", "Complete 25 donations", "fas fa-star", "MILESTONE", 25, 1000, Achievement.AchievementLevel.GOLD),
                    createAchievement("Dedicated Donor", "Maintain a 5-donation streak", "fas fa-fire", "STREAK", 5, 200, Achievement.AchievementLevel.BRONZE),
                    createAchievement("Consistent Hero", "Maintain a 10-donation streak", "fas fa-bolt", "STREAK", 10, 500, Achievement.AchievementLevel.SILVER),
                    createAchievement("Lifesaver", "Save 50 lives through donations", "fas fa-hand-holding-heart", "LIVES_SAVED", 50, 300, Achievement.AchievementLevel.SILVER),
                    createAchievement("Guardian Angel", "Save 100 lives", "fas fa-angel", "LIVES_SAVED", 100, 600, Achievement.AchievementLevel.GOLD));
            achievementRepository.saveAll(achievements);
            System.out.println("Loaded " + achievements.size() + " initial achievements");
        }
    }

    private Achievement createAchievement(String name, String description, String icon,
                                          String type, Integer requirement, Integer points,
                                          Achievement.AchievementLevel level) {
        Achievement achievement = new Achievement();
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setIcon(icon);
        achievement.setType(type);
        achievement.setRequirement(requirement);
        achievement.setPointsReward(points);
        achievement.setLevel(level);
        return achievement;
    }
}
