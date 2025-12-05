package com.example.backend.rest.donorservice.activities_story;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityStoryRepository extends JpaRepository<ActivityStory, Long> {
    Page<ActivityStory> findByUserId(Long userId, Pageable pageable);

    Page<ActivityStory> findByUserRole(String userRole, Pageable pageable);

    Page<ActivityStory> findByServiceSource(String serviceSource, Pageable pageable);

    Page<ActivityStory> findByActionType(String actionType, Pageable pageable);

    Page<ActivityStory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Новый метод для рейтинга активности
    @Query("SELECT a.userId as userId, a.userRole as userRole, COUNT(a) as activityCount " +
            "FROM ActivityStory a " +
            "GROUP BY a.userId, a.userRole " +
            "ORDER BY activityCount DESC")
    List<Object[]> findUserActivityRanking(Pageable pageable);

    // Для получения топ-N пользователей
    @Query("SELECT a.userId as userId, a.userRole as userRole, COUNT(a) as activityCount " +
            "FROM ActivityStory a " +
            "GROUP BY a.userId, a.userRole " +
            "ORDER BY activityCount DESC")
    List<Object[]> findTopActiveUsers(int limit);

    // Для получения рейтинга с пагинацией
    @Query(value = "SELECT a.user_id as userId, a.user_role as userRole, COUNT(*) as activityCount " +
            "FROM activities_story a " +
            "GROUP BY a.user_id, a.user_role " +
            "ORDER BY activityCount DESC",
            countQuery = "SELECT COUNT(DISTINCT a.user_id) FROM activities_story a",
            nativeQuery = true)
    Page<Object[]> findUserActivityRankingPage(Pageable pageable);

    // Для поиска конкретного пользователя в рейтинге
    @Query("SELECT COUNT(a) FROM ActivityStory a WHERE a.userId = :userId")
    Long countActivitiesByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT a.userId) FROM ActivityStory a WHERE a.userId = :userId")
    Long getUserRank(@Param("userId") Long userId);
}