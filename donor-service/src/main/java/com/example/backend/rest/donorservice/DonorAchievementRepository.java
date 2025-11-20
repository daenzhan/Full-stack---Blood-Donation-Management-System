package com.example.backend.rest.donorservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DonorAchievementRepository extends JpaRepository<DonorAchievement, Long> {
    List<DonorAchievement> findByDonorIdOrderByEarnedAtDesc(Long donorId);
    Boolean existsByDonorIdAndAchievementId(Long donorId, Long achievementId);

    @Query("SELECT COUNT(da) FROM DonorAchievement da WHERE da.donorId = :donorId AND da.isNew = true")
    Long countNewAchievements(Long donorId);
}
