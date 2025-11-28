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
import java.time.format.DateTimeFormatter;
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
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String serviceSource,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ActivityStory> activitiesPage;

        // Применяем фильтры если они указаны
        if (userId != null || userRole != null || actionType != null || serviceSource != null) {
            activitiesPage = activityStoryRepository.findByFilters(userId, userRole, actionType, serviceSource, pageable);
        } else {
            activitiesPage = activityStoryRepository.findAll(pageable);
        }

        // Добавляем все параметры в модель
        model.addAttribute("activities", activitiesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activitiesPage.getTotalPages());
        model.addAttribute("totalItems", activitiesPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("pageSize", size);

        // Параметры фильтров
        model.addAttribute("filterUserId", userId);
        model.addAttribute("filterUserRole", userRole);
        model.addAttribute("filterActionType", actionType);
        model.addAttribute("filterServiceSource", serviceSource);
        model.addAttribute("filterStartDate", startDate);
        model.addAttribute("filterEndDate", endDate);

        return "activities/list";
    }

    // Получение последних активностей (для дашборда)
    @GetMapping("/recent")
    public String getRecentActivities(Model model) {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        List<ActivityStory> recentActivities = activityStoryRepository.findAll(pageable).getContent();

        model.addAttribute("recentActivities", recentActivities);
        return "activities/recent";
    }
}