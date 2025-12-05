package com.example.backend.rest.donorservice.activities_story;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityRatingService {

    private final ActivityStoryRepository activityStoryRepository;

    public ActivityRatingService(ActivityStoryRepository activityStoryRepository) {
        this.activityStoryRepository = activityStoryRepository;
    }

    public List<UserActivityRating> getTopUsers(int limit) {
        List<Object[]> results = activityStoryRepository.findTopActiveUsers(limit);
        return mapToRatingList(results);
    }

    public Page<UserActivityRating> getUserRatingPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> resultsPage = activityStoryRepository.findUserActivityRankingPage(pageable);

        List<UserActivityRating> ratings = mapToRatingList(resultsPage.getContent());

        long startRank = (long) page * size + 1;
        for (int i = 0; i < ratings.size(); i++) {
            ratings.get(i).setRank(startRank + i);
        }

        return new PageImpl<>(ratings, pageable, resultsPage.getTotalElements());
    }

    public UserActivityRating getUserRating(Long userId) {
        Long activityCount = activityStoryRepository.countActivitiesByUserId(userId);
        if (activityCount == 0) {
            return null;
        }

        List<ActivityStory> userActivities = activityStoryRepository.findAll()
                .stream()
                .filter(a -> a.getUserId().equals(userId))
                .collect(Collectors.toList());

        if (userActivities.isEmpty()) {
            return null;
        }

        String userRole = userActivities.get(0).getUserRole();

        UserActivityRating rating = new UserActivityRating();
        rating.setUserId(userId);
        rating.setUserRole(userRole);
        rating.setActivityCount(activityCount);

        return rating;
    }

    public Long getUserPosition(Long userId) {
        List<Object[]> allUsers = activityStoryRepository.findUserActivityRanking(Pageable.unpaged());

        List<Object[]> sorted = allUsers.stream()
                .sorted((a, b) -> Long.compare((Long) b[2], (Long) a[2]))
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i)[0].equals(userId)) {
                return (long) i + 1;
            }
        }

        return null;
    }

    private List<UserActivityRating> mapToRatingList(List<Object[]> results) {
        List<UserActivityRating> ratings = new ArrayList<>();

        for (Object[] result : results) {
            UserActivityRating rating = new UserActivityRating();
            rating.setUserId((Long) result[0]);
            rating.setUserRole((String) result[1]);
            rating.setActivityCount((Long) result[2]);
            ratings.add(rating);
        }

        return ratings;
    }
}