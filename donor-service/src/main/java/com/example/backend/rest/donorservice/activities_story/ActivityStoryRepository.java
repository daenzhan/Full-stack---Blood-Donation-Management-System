package com.example.backend.rest.donorservice.activities_story;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Repository
public interface ActivityStoryRepository extends JpaRepository<ActivityStory, Long> {
    Page<ActivityStory> findByUserId(Long userId, Pageable pageable);

    Page<ActivityStory> findByUserRole(String userRole, Pageable pageable);

    Page<ActivityStory> findByServiceSource(String serviceSource, Pageable pageable);

    Page<ActivityStory> findByActionType(String actionType, Pageable pageable);

    Page<ActivityStory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}