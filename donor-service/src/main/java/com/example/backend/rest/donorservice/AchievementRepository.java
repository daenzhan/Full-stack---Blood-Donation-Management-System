package com.example.backend.rest.donorservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByType(String type);
    List<Achievement> findByLevel(Achievement.AchievementLevel level);
}