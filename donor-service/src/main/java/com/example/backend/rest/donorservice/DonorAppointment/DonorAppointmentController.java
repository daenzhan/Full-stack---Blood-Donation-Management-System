package com.example.backend.rest.donorservice.DonorAppointment;

import com.example.backend.rest.donorservice.*;
import com.example.backend.rest.donorservice.activities_story.ActivityStoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/donor/appointments")
@RequiredArgsConstructor
@Slf4j
public class DonorAppointmentController {

    private final DonorAppointmentService donorAppointmentService;
    private final DonorRepository donorRepository;
    private final MedCenterFeignClient medCenterFeignClient;
    private final ActivityStoryService activityStoryService;

    @GetMapping("/schedule")
    public String showSchedulePage(@RequestParam String token,
                                   @RequestParam Long userId,
                                   @RequestParam String role,
                                   @RequestParam String email,
                                   @RequestParam(required = false) Long medCenterId,
                                   Model model) {
        try {
            log.info("=== LOADING SCHEDULE PAGE ===");
            log.info("User ID: {}, Email: {}, Role: {}", userId, email, role);
            log.info("Token: {}, MedCenterId: {}", token, medCenterId);
            if (userId == null || token == null || role == null || email == null) {
                log.error("Missing required parameters!");
                return "redirect:/donor/dashboard";
            }

            Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            if (donorOpt.isEmpty()) {
                log.error("Donor not found for userId: {}", userId);
                log.info("Redirecting to complete-profile");
                return "redirect:/donor/complete-profile?token=" + token +
                        "&userId=" + userId + "&role=" + role + "&email=" + email;
            }

            Donor donor = donorOpt.get();
            log.info("Found donor: {} (ID: {})", donor.getFullName(), donor.getUserId());
            log.info("Fetching medical centers...");
            ResponseEntity<List<MedCenterDto>> medCentersResponse = medCenterFeignClient.search_by_name("");

            if (medCentersResponse == null) {
                log.error("Med centers response is null!");
            } else {
                log.info("Response status: {}", medCentersResponse.getStatusCode());
                log.info("Response body: {}", medCentersResponse.getBody());
            }

            List<MedCenterDto> medCenters = medCentersResponse != null ? medCentersResponse.getBody() : null;
            log.info("Number of medical centers: {}", medCenters != null ? medCenters.size() : 0);

            model.addAttribute("donor", donor);
            model.addAttribute("medCenters", medCenters != null ? medCenters : List.of());
            model.addAttribute("selectedMedCenterId", medCenterId);
            model.addAttribute("token", token);
            model.addAttribute("userId", userId);
            model.addAttribute("role", role);
            model.addAttribute("email", email);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("maxDate", LocalDate.now().plusDays(30));

            log.info("Returning template: donor-appointment-schedule");
            return "donor-appointment-schedule";

        } catch (Exception e) {
            log.error("Error loading schedule page: {}", e.getMessage());
            e.printStackTrace(); // Добавьте эту строку
            return "redirect:/donor/dashboard?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email +
                    "&error=Error loading appointment schedule: " + e.getMessage();
        }
    }

    @GetMapping("/get-slots")
    @ResponseBody
    public Map<String, Object> getAvailableSlots(@RequestParam Long medCenterId,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                 LocalDate date) {
        try {
            ResponseEntity<Map<String, Object>> response = medCenterFeignClient.getAvailableSlots(
                    medCenterId, date.toString());

            if (response != null &&
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null) {
                return response.getBody();
            } else {
                return Map.of("success", false, "message", "Failed to load slots");
            }
        } catch (Exception e) {
            log.error("Error getting available slots: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/create")
    public String createAppointment(@RequestParam Long medCenterId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                    LocalDate appointmentDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                                    LocalTime appointmentTime,
                                    @RequestParam(required = false) String notes,
                                    @RequestParam String token,
                                    @RequestParam Long userId,
                                    @RequestParam String role,
                                    @RequestParam String email,
                                    RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> result = donorAppointmentService.scheduleAppointment(
                    userId, medCenterId, appointmentDate, appointmentTime, notes);

            if (Boolean.TRUE.equals(result.get("success"))) {
                redirectAttributes.addFlashAttribute("success",
                        "Appointment scheduled successfully! Check your email for confirmation.");

                activityStoryService.record_activity(
                        userId,
                        "DONOR",
                        "APPOINTMENT_CREATED_VIA_SCHEDULE",
                        String.format("Scheduled appointment at center %s for %s %s",
                                medCenterId, appointmentDate, appointmentTime)
                );
                return "redirect:/donor/appointments/my-appointments?token=" + token +
                        "&userId=" + userId + "&role=" + role + "&email=" + email;

            } else {
                redirectAttributes.addFlashAttribute("error", result.get("message"));
                return "redirect:/donor/appointments/schedule?token=" + token +
                        "&userId=" + userId + "&role=" + role + "&email=" + email +
                        "&error=" + URLEncoder.encode(result.get("message").toString(), StandardCharsets.UTF_8);
            }

        } catch (Exception e) {
            log.error("Error creating appointment: {}", e.getMessage());
            return "redirect:/donor/appointments/schedule?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email +
                    "&error=" + URLEncoder.encode("Error: " + e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/my-appointments")
    public String showMyAppointments(@RequestParam String token,
                                     @RequestParam Long userId,
                                     @RequestParam String role,
                                     @RequestParam String email,
                                     HttpServletRequest request,
                                     Model model) {
        try {
            Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            if (donorOpt.isEmpty()) {
                return "redirect:/donor/complete-profile?token=" + token +
                        "&userId=" + userId + "&role=" + role + "&email=" + email;
            }

            Donor donor = donorOpt.get();
            ResponseEntity<List<Map<String, Object>>> response =
                    medCenterFeignClient.getDonorAppointments(userId);

            List<Map<String, Object>> appointments = List.of();
            if (response != null &&
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null) {
                appointments = response.getBody();
                for (Map<String, Object> appointment : appointments) {
                    try {
                        if (appointment.get("appointmentDate") != null) {
                            String dateStr = appointment.get("appointmentDate").toString();
                            LocalDate date = LocalDate.parse(dateStr);
                            appointment.put("formattedDate",
                                    date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
                            appointment.put("shortDate",
                                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        }
                        if (appointment.get("appointmentTime") != null) {
                            String timeStr = appointment.get("appointmentTime").toString();
                            LocalTime time = LocalTime.parse(timeStr);
                            appointment.put("formattedTime",
                                    time.format(DateTimeFormatter.ofPattern("hh:mm a")));
                        }
                        if (appointment.get("appointmentDate") != null &&
                                appointment.get("appointmentTime") != null) {
                            String dateStr = appointment.get("appointmentDate").toString();
                            String timeStr = appointment.get("appointmentTime").toString();
                            LocalDateTime dateTime = LocalDateTime.of(
                                    LocalDate.parse(dateStr),
                                    LocalTime.parse(timeStr)
                            );
                            appointment.put("formattedDateTime",
                                    dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                        }

                    } catch (Exception e) {
                        log.warn("Error formatting appointment date/time: {}", e.getMessage());
                    }
                }
            }

            model.addAttribute("donor", donor);
            model.addAttribute("appointments", appointments);
            model.addAttribute("token", token);
            model.addAttribute("userId", userId);
            model.addAttribute("role", role);
            model.addAttribute("email", email);
            model.addAttribute("successParam", request.getParameter("success"));
            model.addAttribute("errorParam", request.getParameter("error"));

            activityStoryService.record_activity(
                    userId,
                    "DONOR",
                    "MY_APPOINTMENTS_VIEWED",
                    "Viewed my appointments list"
            );

            return "donor-appointments-list";

        } catch (Exception e) {
            log.error("Error loading appointments: {}", e.getMessage());
            return "redirect:/donor/dashboard?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }

    @PostMapping("/cancel/{appointmentId}")
    public String cancelAppointment(@PathVariable Long appointmentId,
                                    @RequestParam(required = false) String reason,
                                    @RequestParam String token,
                                    @RequestParam Long userId,
                                    @RequestParam String role,
                                    @RequestParam String email,
                                    RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> result = donorAppointmentService.cancelAppointment(
                    appointmentId, userId, reason);

            if (Boolean.TRUE.equals(result.get("success"))) {
                redirectAttributes.addFlashAttribute("success", "Appointment cancelled successfully");

                activityStoryService.record_activity(
                        userId,
                        "DONOR",
                        "APPOINTMENT_CANCELLED",
                        String.format("Cancelled appointment %s. Reason: %s",
                                appointmentId, reason != null ? reason : "Not specified")
                );
            } else {
                redirectAttributes.addFlashAttribute("error", result.get("message"));
            }

            return "redirect:/donor/appointments/my-appointments?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email;

        } catch (Exception e) {
            log.error("Error cancelling appointment: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/donor/appointments/my-appointments?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }

    @PostMapping("/reschedule/{appointmentId}")
    public String rescheduleAppointment(@PathVariable Long appointmentId,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                        LocalDate newDate,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                                        LocalTime newTime,
                                        @RequestParam String token,
                                        @RequestParam Long userId,
                                        @RequestParam String role,
                                        @RequestParam String email,
                                        RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> result = donorAppointmentService.rescheduleAppointment(
                    appointmentId, userId, newDate, newTime);

            if (Boolean.TRUE.equals(result.get("success"))) {
                redirectAttributes.addFlashAttribute("success", "Appointment rescheduled successfully");

                activityStoryService.record_activity(
                        userId,
                        "DONOR",
                        "APPOINTMENT_RESCHEDULED",
                        String.format("Rescheduled appointment %s to %s %s",
                                appointmentId, newDate, newTime)
                );
            } else {
                redirectAttributes.addFlashAttribute("error", result.get("message"));
            }

            return "redirect:/donor/appointments/my-appointments?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email;

        } catch (Exception e) {
            log.error("Error rescheduling appointment: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/donor/appointments/my-appointments?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }

    @GetMapping("/quick-schedule/{medCenterId}")
    public String quickSchedule(@PathVariable Long medCenterId,
                                @RequestParam String token,
                                @RequestParam Long userId,
                                @RequestParam String role,
                                @RequestParam String email,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Donor> donorOpt = donorRepository.findByUserId(userId);
            if (donorOpt.isEmpty()) {
                return "redirect:/donor/complete-profile?token=" + token +
                        "&userId=" + userId + "&role=" + role + "&email=" + email;
            }

            Donor donor = donorOpt.get();
            LocalDate today = LocalDate.now();
            LocalDate checkDate = today;
            for (int i = 0; i < 3; i++) {
                ResponseEntity<Map<String, Object>> slotsResponse =
                        medCenterFeignClient.getAvailableSlots(medCenterId, checkDate.toString());

                if (slotsResponse != null &&
                        slotsResponse.getStatusCode().is2xxSuccessful() &&
                        slotsResponse.getBody() != null &&
                        Boolean.TRUE.equals(slotsResponse.getBody().get("success"))) {

                    Object slotsObj = slotsResponse.getBody().get("slots");
                    if (slotsObj instanceof List) {
                        List<Map<String, Object>> slots = (List<Map<String, Object>>) slotsObj;

                        if (slots != null && !slots.isEmpty()) {
                            for (Map<String, Object> slot : slots) {
                                if (Boolean.TRUE.equals(slot.get("isAvailable"))) {
                                    String timeStr = (String) slot.get("time");
                                    LocalTime time = LocalTime.parse(timeStr);
                                    Map<String, Object> result = donorAppointmentService.scheduleAppointment(
                                            userId, medCenterId, checkDate, time, "Quick schedule from favorites");

                                    if (Boolean.TRUE.equals(result.get("success"))) {
                                        redirectAttributes.addFlashAttribute("success",
                                                "Appointment scheduled for " + checkDate + " " + time);

                                        activityStoryService.record_activity(
                                                userId,
                                                "DONOR",
                                                "QUICK_APPOINTMENT_SCHEDULED",
                                                String.format("Quick scheduled at favorite center %s for %s %s",
                                                        medCenterId, checkDate, time)
                                        );

                                        return "redirect:/donor/appointments/my-appointments?token=" + token +
                                                "&userId=" + userId + "&role=" + role + "&email=" + email;
                                    }
                                }
                            }
                        }
                    }
                }

                checkDate = checkDate.plusDays(1);
            }

            redirectAttributes.addFlashAttribute("info",
                    "No available slots found in next 3 days. Please choose a specific date.");
            return "redirect:/donor/appointments/schedule?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email +
                    "&medCenterId=" + medCenterId;

        } catch (Exception e) {
            log.error("Error in quick schedule: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/medcenters/favorites?token=" + token +
                    "&userId=" + userId + "&role=" + role + "&email=" + email;
        }
    }
}