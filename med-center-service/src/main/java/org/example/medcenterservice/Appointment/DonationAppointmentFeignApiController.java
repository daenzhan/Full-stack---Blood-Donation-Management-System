package org.example.medcenterservice.Appointment;

import org.example.medcenterservice.MedCenter;
import org.example.medcenterservice.MedCenterService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/donation-appointments")
public class DonationAppointmentFeignApiController {

    private final DonationAppointmentService appointmentService;
    private final MedCenterService medCenterService;

    public DonationAppointmentFeignApiController(DonationAppointmentService appointmentService,
                                                 MedCenterService medCenterService) {
        this.appointmentService = appointmentService;
        this.medCenterService = medCenterService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequestDto request) {
        try {
            DonationAppointment appointment = appointmentService.createAppointment(
                    request.getDonorId(),
                    request.getDonorName(),
                    request.getDonorBloodType(),
                    request.getDonorPhone(),
                    request.getMedCenterId(),
                    request.getAppointmentDate(),
                    request.getAppointmentTime()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Appointment created successfully",
                    "appointmentId", appointment.getAppointmentId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/api/slots")
    public ResponseEntity<?> getAvailableSlots(@RequestParam Long medCenterId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                               LocalDate date) {
        try {
            Map<LocalTime, Integer> slots = appointmentService.getAvailableSlots(medCenterId, date);

            List<AppointmentSlotDto> slotDtos = slots.entrySet().stream()
                    .map(entry -> new AppointmentSlotDto(entry.getKey(), entry.getValue()))
                    .sorted((a, b) -> a.getTime().compareTo(b.getTime()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "slots", slotDtos,
                    "date", date.toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/api/check-slot")
    public ResponseEntity<?> checkSlotAvailability(@RequestParam Long medCenterId,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                   LocalDate date,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                                                   LocalTime time) {
        try {
            boolean available = appointmentService.isSlotAvailable(medCenterId, date, time);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "available", available
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/donor/{donorId}")
    public ResponseEntity<List<Map<String, Object>>> getDonorAppointments(@PathVariable Long donorId) {
        try {
            List<DonationAppointment> appointments = appointmentService.getAppointmentsByDonor(donorId);

            List<Map<String, Object>> appointmentDtos = appointments.stream()
                    .map(appointment -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("appointmentId", appointment.getAppointmentId());
                        dto.put("donorId", appointment.getDonorId());
                        dto.put("donorName", appointment.getDonorName());
                        dto.put("donorBloodType", appointment.getDonorBloodType());
                        dto.put("donorPhone", appointment.getDonorPhone());
                        dto.put("medCenterId", appointment.getMedCenterId());
                        dto.put("appointmentDate", appointment.getAppointmentDate());
                        dto.put("appointmentTime", appointment.getAppointmentTime());
                        dto.put("status", appointment.getStatus());
                        dto.put("notes", appointment.getNotes());
                        dto.put("createdAt", appointment.getCreatedAt());
                        dto.put("updatedAt", appointment.getUpdatedAt());

                        // Get medical center name
                        MedCenter medCenter = medCenterService.get_by_id(appointment.getMedCenterId());
                        if (medCenter != null) {
                            dto.put("medCenterName", medCenter.getName());
                            dto.put("medCenterLocation", medCenter.getLocation());
                            dto.put("medCenterPhone", medCenter.getPhone());
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(appointmentDtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @PostMapping("/donor/cancel")
    public ResponseEntity<?> cancelAppointment(@RequestParam Long appointmentId,
                                               @RequestParam(required = false) String reason,
                                               @RequestParam Long donorId) {
        try {
            DonationAppointment appointment = appointmentService.getAppointmentById(appointmentId);

            // Check if donor owns this appointment
            if (!appointment.getDonorId().equals(donorId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "You cannot cancel this appointment"
                ));
            }

            // Check if it's not too late to cancel (at least 2 hours before)
            var appointmentDateTime = java.time.LocalDateTime.of(
                    appointment.getAppointmentDate(), appointment.getAppointmentTime());
            var now = java.time.LocalDateTime.now();

            if (now.plusHours(2).isAfter(appointmentDateTime)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Cannot cancel within 2 hours of appointment time"
                ));
            }

            appointmentService.cancelAppointment(appointmentId, reason);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Appointment cancelled successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/donor/reschedule")
    public ResponseEntity<?> rescheduleAppointment(@RequestParam Long appointmentId,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                   LocalDate newDate,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                                                   LocalTime newTime,
                                                   @RequestParam Long donorId) {
        try {
            DonationAppointment appointment = appointmentService.getAppointmentById(appointmentId);

            // Check if donor owns this appointment
            if (!appointment.getDonorId().equals(donorId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "You cannot reschedule this appointment"
                ));
            }

            // Check if new slot is available
            boolean available = appointmentService.isSlotAvailable(
                    appointment.getMedCenterId(), newDate, newTime);

            if (!available) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Selected time slot is not available"
                ));
            }

            DonationAppointment updated = appointmentService.updateAppointment(appointmentId, newDate, newTime);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Appointment rescheduled successfully",
                    "appointment", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/medcenter/today")
    public ResponseEntity<?> getTodayAppointments(@RequestParam Long userId) {
        try {
            // Получаем медцентр по userId
            MedCenter medCenter = medCenterService.getProfileByUserId(userId);

            // Получаем сегодняшние записи
            List<DonationAppointment> appointments =
                    appointmentService.getTodayAppointmentsByMedCenter(medCenter.getMed_center_id());

            // Преобразуем в DTO
            List<Map<String, Object>> appointmentDtos = appointments.stream()
                    .map(appointment -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("appointmentId", appointment.getAppointmentId());
                        dto.put("donorName", appointment.getDonorName());
                        dto.put("donorBloodType", appointment.getDonorBloodType());
                        dto.put("appointmentTime", appointment.getAppointmentTime().toString());
                        dto.put("status", appointment.getStatus());
                        return dto;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "appointments", appointmentDtos,
                    "count", appointmentDtos.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}