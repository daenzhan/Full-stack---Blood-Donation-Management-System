//package org.example.medcenterservice.Appointment;
//
//import org.example.medcenterservice.MedCenter;
//import org.example.medcenterservice.MedCenterService;
//import org.example.medcenterservice.activities_story.ActivityStoryService;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/donation-appointments")
//public class DonationAppointmentController {
//
//    private final DonationAppointmentService appointmentService;
//    private final MedCenterService medCenterService;
//    private final ActivityStoryService activityStoryService;
//
//    public DonationAppointmentController(DonationAppointmentService appointmentService,
//                                         MedCenterService medCenterService,
//                                         ActivityStoryService activityStoryService) {
//        this.appointmentService = appointmentService;
//        this.medCenterService = medCenterService;
//        this.activityStoryService = activityStoryService;
//    }
//
//    @PostMapping("/create")
//    @ResponseBody
//    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequestDto request) {
//        try {
//            DonationAppointment appointment = appointmentService.createAppointment(
//                    request.getDonorId(),
//                    request.getDonorName(),
//                    request.getDonorBloodType(),
//                    request.getDonorPhone(),
//                    request.getMedCenterId(),
//                    request.getAppointmentDate(),
//                    request.getAppointmentTime()
//            );
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Appointment created successfully",
//                    "appointmentId", appointment.getAppointmentId()
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", e.getMessage()
//            ));
//        }
//    }
//
//    @GetMapping("/donor/schedule")
//    public String showDonorSchedulePage(@RequestParam String token,
//                                        @RequestParam Long userId,
//                                        @RequestParam String role,
//                                        @RequestParam String email,
//                                        @RequestParam(required = false) Long medCenterId,
//                                        Model model) {
//        try {
//            // Get all medical centers
//            List<MedCenter> medCenters = medCenterService.get_all();
//
//            MedCenter selectedCenter = null;
//            if (medCenterId != null) {
//                selectedCenter = medCenterService.get_by_id(medCenterId);
//            }
//
//            // Get available dates (next 30 days)
//            LocalDate today = LocalDate.now();
//            List<LocalDate> availableDates = new ArrayList<>();
//            for (int i = 0; i < 30; i++) {
//                availableDates.add(today.plusDays(i));
//            }
//
//            model.addAttribute("medCenters", medCenters);
//            model.addAttribute("selectedCenter", selectedCenter);
//            model.addAttribute("availableDates", availableDates);
//            model.addAttribute("today", today);
//            model.addAttribute("maxDate", today.plusDays(29));
//            model.addAttribute("token", token);
//            model.addAttribute("userId", userId);
//            model.addAttribute("role", role);
//            model.addAttribute("email", email);
//
//            return "donor-schedule-appointment";
//
//        } catch (Exception e) {
//            return "redirect:/donor/dashboard?token=" + token + "&userId=" + userId +
//                    "&role=" + role + "&email=" + email + "&error=" + e.getMessage();
//        }
//    }
//
//    // API для получения доступных слотов
//    @GetMapping("/api/slots")
//    @ResponseBody
//    public ResponseEntity<?> getAvailableSlots(@RequestParam Long medCenterId,
//                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//                                               LocalDate date) {
//        try {
//            Map<LocalTime, Integer> slots = appointmentService.getAvailableSlots(medCenterId, date);
//
//            List<AppointmentSlotDto> slotDtos = slots.entrySet().stream()
//                    .map(entry -> new AppointmentSlotDto(entry.getKey(), entry.getValue()))
//                    .sorted((a, b) -> a.getTime().compareTo(b.getTime()))
//                    .collect(Collectors.toList());
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "slots", slotDtos,
//                    "date", date.toString()
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", e.getMessage()
//            ));
//        }
//    }
//
//    // API для проверки доступности слота
//    @GetMapping("/api/check-slot")
//    @ResponseBody
//    public ResponseEntity<?> checkSlotAvailability(@RequestParam Long medCenterId,
//                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//                                                   LocalDate date,
//                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
//                                                   LocalTime time) {
//        try {
//            boolean available = appointmentService.isSlotAvailable(medCenterId, date, time);
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "available", available
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", e.getMessage()
//            ));
//        }
//    }
//
//    // Веб-страница для медцентра - просмотр записей
//    @GetMapping("/medcenter")
//    public String showMedCenterAppointments(@RequestParam String token,
//                                            @RequestParam Long userId,
//                                            @RequestParam String role,
//                                            @RequestParam String email,
//                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//                                            LocalDate filterDate,
//                                            Model model) {
//        try {
//            // Get medical center by user ID
//            MedCenter medCenter = medCenterService.getProfileByUserId(userId);
//
//            if (filterDate == null) {
//                filterDate = LocalDate.now();
//            }
//
//            List<DonationAppointment> appointments =
//                    appointmentService.getAppointmentsByMedCenterAndDate(medCenter.getMed_center_id(), filterDate);
//
//            // Get appointment statistics
//            long totalAppointments = appointmentService.countAppointmentsByMedCenter(medCenter.getMed_center_id());
//            long activeAppointments = appointmentService.countActiveAppointmentsByMedCenter(medCenter.getMed_center_id());
//
//            // Get upcoming dates with appointments
//            List<LocalDate> upcomingDates = appointmentService.getUpcomingAppointmentDates(medCenter.getMed_center_id());
//
//            model.addAttribute("medCenter", medCenter);
//            model.addAttribute("appointments", appointments);
//            model.addAttribute("filterDate", filterDate);
//            model.addAttribute("totalAppointments", totalAppointments);
//            model.addAttribute("activeAppointments", activeAppointments);
//            model.addAttribute("upcomingDates", upcomingDates);
//            model.addAttribute("today", LocalDate.now());
//            model.addAttribute("token", token);
//            model.addAttribute("userId", userId);
//            model.addAttribute("role", role);
//            model.addAttribute("email", email);
//
//            return "med-center-appointments";
//
//        } catch (Exception e) {
//            return "redirect:/medcenters/dashboard?token=" + token + "&userId=" + userId +
//                    "&role=" + role + "&email=" + email + "&error=" + e.getMessage();
//        }
//    }
//
//    // API для изменения статуса записи (медцентр)
//    @PostMapping("/medcenter/update-status")
//    @ResponseBody
//    public ResponseEntity<?> updateAppointmentStatus(@RequestParam Long appointmentId,
//                                                     @RequestParam String status,
//                                                     @RequestParam(required = false) String notes) {
//        try {
//            DonationAppointment updated = appointmentService.updateAppointmentStatus(appointmentId, status, notes);
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Status updated successfully",
//                    "appointment", updated
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", e.getMessage()
//            ));
//        }
//    }
//
//    // Веб-страница для доноров - мои записи
//    @GetMapping("/donor/my-appointments")
//    public String showMyAppointments(@RequestParam String token,
//                                     @RequestParam Long userId,
//                                     @RequestParam String role,
//                                     @RequestParam String email,
//                                     Model model) {
//        try {
//            List<DonationAppointment> appointments = appointmentService.getAppointmentsByDonor(userId);
//
//            // Separate upcoming and past appointments
//            List<DonationAppointment> upcomingAppointments = new ArrayList<>();
//            List<DonationAppointment> pastAppointments = new ArrayList<>();
//
//            LocalDate today = LocalDate.now();
//
//            for (DonationAppointment appointment : appointments) {
//                if (appointment.getAppointmentDate().isAfter(today) ||
//                        (appointment.getAppointmentDate().equals(today) &&
//                                !appointment.getStatus().equals("COMPLETED") &&
//                                !appointment.getStatus().equals("CANCELLED"))) {
//                    upcomingAppointments.add(appointment);
//                } else {
//                    pastAppointments.add(appointment);
//                }
//            }
//
//            // Sort upcoming by date/time
//            upcomingAppointments.sort((a, b) -> {
//                int dateCompare = a.getAppointmentDate().compareTo(b.getAppointmentDate());
//                if (dateCompare == 0) {
//                    return a.getAppointmentTime().compareTo(b.getAppointmentTime());
//                }
//                return dateCompare;
//            });
//
//            // Sort past by date/time descending
//            pastAppointments.sort((a, b) -> {
//                int dateCompare = b.getAppointmentDate().compareTo(a.getAppointmentDate());
//                if (dateCompare == 0) {
//                    return b.getAppointmentTime().compareTo(a.getAppointmentTime());
//                }
//                return dateCompare;
//            });
//
//            model.addAttribute("upcomingAppointments", upcomingAppointments);
//            model.addAttribute("pastAppointments", pastAppointments);
//            model.addAttribute("medCenterService", medCenterService);
//            model.addAttribute("token", token);
//            model.addAttribute("userId", userId);
//            model.addAttribute("role", role);
//            model.addAttribute("email", email);
//
//            return "donor-my-appointments";
//
//        } catch (Exception e) {
//            return "redirect:/donor/dashboard?token=" + token + "&userId=" + userId +
//                    "&role=" + role + "&email=" + email + "&error=" + e.getMessage();
//        }
//    }
//
//    // API для отмены записи (донор)
//    @PostMapping("/donor/cancel")
//    @ResponseBody
//    public ResponseEntity<?> cancelAppointment(@RequestParam Long appointmentId,
//                                               @RequestParam(required = false) String reason,
//                                               @RequestParam Long donorId) {
//        try {
//            DonationAppointment appointment = appointmentService.getAppointmentById(appointmentId);
//
//            // Check if donor owns this appointment
//            if (!appointment.getDonorId().equals(donorId)) {
//                return ResponseEntity.badRequest().body(Map.of(
//                        "success", false,
//                        "message", "You cannot cancel this appointment"
//                ));
//            }
//
//            // Check if it's not too late to cancel (at least 2 hours before)
//            LocalDateTime appointmentDateTime = LocalDateTime.of(
//                    appointment.getAppointmentDate(), appointment.getAppointmentTime());
//            LocalDateTime now = LocalDateTime.now();
//
//            if (now.plusHours(2).isAfter(appointmentDateTime)) {
//                return ResponseEntity.badRequest().body(Map.of(
//                        "success", false,
//                        "message", "Cannot cancel within 2 hours of appointment time"
//                ));
//            }
//
//            appointmentService.cancelAppointment(appointmentId, reason);
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Appointment cancelled successfully"
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", e.getMessage()
//            ));
//        }
//    }
//
//    // API для переноса записи
//    @PostMapping("/donor/reschedule")
//    @ResponseBody
//    public ResponseEntity<?> rescheduleAppointment(@RequestParam Long appointmentId,
//                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//                                                   LocalDate newDate,
//                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
//                                                   LocalTime newTime,
//                                                   @RequestParam Long donorId) {
//        try {
//            DonationAppointment appointment = appointmentService.getAppointmentById(appointmentId);
//
//            // Check if donor owns this appointment
//            if (!appointment.getDonorId().equals(donorId)) {
//                return ResponseEntity.badRequest().body(Map.of(
//                        "success", false,
//                        "message", "You cannot reschedule this appointment"
//                ));
//            }
//
//            // Check if new slot is available
//            boolean available = appointmentService.isSlotAvailable(
//                    appointment.getMedCenterId(), newDate, newTime);
//
//            if (!available) {
//                return ResponseEntity.badRequest().body(Map.of(
//                        "success", false,
//                        "message", "Selected time slot is not available"
//                ));
//            }
//
//            DonationAppointment updated = appointmentService.updateAppointment(appointmentId, newDate, newTime);
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Appointment rescheduled successfully",
//                    "appointment", updated
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", e.getMessage()
//            ));
//        }
//    }
//
//    @GetMapping("/medcenter/today")
//    @ResponseBody
//    public ResponseEntity<?> getTodayAppointments(@RequestParam Long userId,
//                                                  @RequestParam String token) {
//        try {
//            // Получаем медцентр по userId
//            MedCenter medCenter = medCenterService.getProfileByUserId(userId);
//
//            // Получаем сегодняшние записи
//            List<DonationAppointment> appointments =
//                    appointmentService.getTodayAppointmentsByMedCenter(medCenter.getMed_center_id());
//
//            // Преобразуем в DTO
//            List<Map<String, Object>> appointmentDtos = appointments.stream()
//                    .map(appointment -> {
//                        Map<String, Object> dto = new HashMap<>();
//                        dto.put("appointmentId", appointment.getAppointmentId());
//                        dto.put("donorName", appointment.getDonorName());
//                        dto.put("donorBloodType", appointment.getDonorBloodType());
//                        dto.put("appointmentTime", appointment.getAppointmentTime().toString());
//                        dto.put("status", appointment.getStatus());
//                        return dto;
//                    })
//                    .collect(Collectors.toList());
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "appointments", appointmentDtos,
//                    "count", appointmentDtos.size()
//            ));
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", e.getMessage()
//            ));
//        }
//    }
//
//}