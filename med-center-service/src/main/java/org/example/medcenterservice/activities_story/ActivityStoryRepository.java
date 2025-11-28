package org.example.medcenterservice.activities_story;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityStoryRepository extends JpaRepository<ActivityStory, Long> {
}