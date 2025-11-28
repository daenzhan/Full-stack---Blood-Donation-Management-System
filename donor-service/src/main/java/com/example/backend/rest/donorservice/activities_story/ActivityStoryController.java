package com.example.backend.rest.donorservice.activities_story;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/activities")
public class ActivityStoryController {

    private final ActivityStoryRepository activityStoryRepository;

    public ActivityStoryController(ActivityStoryRepository activityStoryRepository) {
        this.activityStoryRepository = activityStoryRepository;
    }

    @GetMapping
    public String getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,

            // NEW FILTERS:
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,

            Model model) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ActivityStory> activitiesPage;

        // ORDER OF PRIORITY (you can modify)
        if (userId != null) {
            activitiesPage = activityStoryRepository.findByUserId(userId, pageable);
        }
        else if (role != null && !role.isEmpty()) {
            activitiesPage = activityStoryRepository.findByUserRole(role, pageable);
        }
        else if (service != null && !service.isEmpty()) {
            activitiesPage = activityStoryRepository.findByServiceSource(service, pageable);
        }
        else if (action != null && !action.isEmpty()) {
            activitiesPage = activityStoryRepository.findByActionType(action, pageable);
        }
        else if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            activitiesPage = activityStoryRepository.findByCreatedAtBetween(start, end, pageable);
        }
        else {
            activitiesPage = activityStoryRepository.findAll(pageable);
        }

        model.addAttribute("activities", activitiesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activitiesPage.getTotalPages());
        model.addAttribute("totalItems", activitiesPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("pageSize", size);

        // return selected filters back to UI
        model.addAttribute("filterUserId", userId);
        model.addAttribute("filterRole", role);
        model.addAttribute("filterService", service);
        model.addAttribute("filterAction", action);
        model.addAttribute("filterStartDate", startDate);
        model.addAttribute("filterEndDate", endDate);

        return "activities/list";
    }


    @GetMapping("/user")
    public String getActivitiesByUser(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ActivityStory> activitiesPage = activityStoryRepository.findAll(pageable);
        // Если нужно фильтровать по userId, потребуется дополнительный метод в репозитории

        model.addAttribute("activities", activitiesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activitiesPage.getTotalPages());
        model.addAttribute("totalItems", activitiesPage.getTotalElements());
        model.addAttribute("filterUserId", userId);

        return "activities/list";
    }

    // Получение последних активностей (для дашборда)
    @GetMapping("/recent")
    public String getRecentActivities(Model model) {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        List<ActivityStory> recentActivities = activityStoryRepository.findAll(pageable).getContent();

        model.addAttribute("recentActivities", recentActivities);
        return "activities/recent"; // или другой шаблон для дашборда
    }
}