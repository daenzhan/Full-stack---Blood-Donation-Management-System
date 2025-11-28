package org.example.bloodrequestservice.activities_story;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ActivityStoryService {

    private final ActivityStoryRepository repository;
    private final String currentServiceName;

    public ActivityStoryService(ActivityStoryRepository repository,
                                @Value("${spring.application.name}") String appName) {
        this.repository = repository;
        this.currentServiceName = appName;
    }

    public void record_activity(Long userId, String userRole, String action, String description) {
        ActivityStory activity = new ActivityStory(userId, userRole, action, description, currentServiceName);
        repository.save(activity);
    }
}