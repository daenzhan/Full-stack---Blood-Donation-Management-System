package com.example.backend.rest.donorservice.activities_story;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityStoryRepository extends JpaRepository<ActivityStory, Long> {

    // Фильтрация по userId
    Page<ActivityStory> findByUserId(Long userId, Pageable pageable);

    // Фильтрация по userRole
    Page<ActivityStory> findByUserRole(String userRole, Pageable pageable);

    // Фильтрация по actionType
    Page<ActivityStory> findByActionType(String actionType, Pageable pageable);

    // Фильтрация по serviceSource
    Page<ActivityStory> findByServiceSource(String serviceSource, Pageable pageable);

    // Комбинированная фильтрация
    Page<ActivityStory> findByUserIdAndUserRole(Long userId, String userRole, Pageable pageable);

    // Фильтрация по дате
    Page<ActivityStory> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Универсальный метод для множественной фильтрации
    @Query("SELECT a FROM ActivityStory a WHERE " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:userRole IS NULL OR a.userRole = :userRole) AND " +
            "(:actionType IS NULL OR a.actionType = :actionType) AND " +
            "(:serviceSource IS NULL OR a.serviceSource = :serviceSource)")
    Page<ActivityStory> findByFilters(@Param("userId") Long userId,
                                      @Param("userRole") String userRole,
                                      @Param("actionType") String actionType,
                                      @Param("serviceSource") String serviceSource,
                                      Pageable pageable);
}