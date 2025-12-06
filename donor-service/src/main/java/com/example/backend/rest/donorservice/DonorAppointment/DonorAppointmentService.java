package com.example.backend.rest.donorservice.DonorAppointment;

import com.example.backend.rest.donorservice.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorAppointmentService {

    private final DonorRepository donorRepository;
    private final MedCenterFeignClient medCenterFeignClient;
    private final EmailService emailService;

    public Map<String, Object> scheduleAppointment(Long donorId, Long medCenterId,
                                                   LocalDate date, LocalTime time, String notes) {
        try {Donor donor = donorRepository.findByUserId(donorId)
                    .orElseThrow(() -> new RuntimeException("Donor not found"));

            ResponseEntity<MedCenterDto> medCenterResponse = medCenterFeignClient.get_medcenter_by_id(medCenterId);
            if (medCenterResponse == null ||
                    !medCenterResponse.getStatusCode().is2xxSuccessful() ||
                    medCenterResponse.getBody() == null) {
                throw new RuntimeException("Medical center not found");
            }
            ResponseEntity<Map<String, Object>> checkResponse = medCenterFeignClient.checkSlotAvailability(
                    medCenterId,
                    date.toString(),
                    time.toString()
            );

            if (checkResponse == null ||
                    !checkResponse.getStatusCode().is2xxSuccessful() ||
                    checkResponse.getBody() == null ||
                    !Boolean.TRUE.equals(checkResponse.getBody().get("available"))) {
                throw new RuntimeException("Selected time slot is no longer available");
            }
            AppointmentRequestDto request = new AppointmentRequestDto();
            request.setDonorId(donorId);
            request.setDonorName(donor.getFullName());
            request.setDonorBloodType(donor.getBloodType());
            request.setDonorPhone(donor.getPhoneNumber());
            request.setMedCenterId(medCenterId);
            request.setAppointmentDate(date);
            request.setAppointmentTime(time);
            request.setNotes(notes);

            ResponseEntity<Map<String, Object>> response = medCenterFeignClient.createAppointment(request);

            if (response != null &&
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    Boolean.TRUE.equals(response.getBody().get("success"))) {
                try {
                    sendAppointmentConfirmationEmail(donor, medCenterResponse.getBody(), date, time);
                } catch (Exception e) {
                    log.warn("Failed to send confirmation email: {}", e.getMessage());
                }

                return Map.of(
                        "success", true,
                        "message", "Appointment scheduled successfully",
                        "appointmentId", response.getBody().get("appointmentId")
                );
            } else {
                String errorMessage = "Failed to create appointment";
                if (response != null && response.getBody() != null) {
                    errorMessage = (String) response.getBody().getOrDefault("message", errorMessage);
                }
                throw new RuntimeException(errorMessage);
            }

        } catch (Exception e) {
            log.error("Error scheduling appointment: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
        }
    }

    private void sendAppointmentConfirmationEmail(Donor donor, MedCenterDto medCenter,
                                                  LocalDate date, LocalTime time) {
        try {
            String subject = "Blood Donation Appointment Confirmation";
            String text = String.format(
                    "Dear %s,\n\n" +
                            "Your blood donation appointment has been successfully scheduled!\n\n" +
                            "Appointment Details:\n" +
                            "📅 Date: %s\n" +
                            "⏰ Time: %s\n" +
                            "🏥 Medical Center: %s\n" +
                            "📍 Location: %s\n" +
                            "📞 Contact: %s\n\n" +
                            "Please arrive 15 minutes before your scheduled time.\n" +
                            "Remember to bring your ID and get a good night's sleep.\n" +
                            "Stay hydrated and eat a healthy meal before your donation.\n\n" +
                            "Thank you for your life-saving contribution!\n\n" +
                            "Best regards,\n" +
                            "BloodConnect Team",
                    donor.getFullName(),
                    date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    medCenter.getName(),
                    medCenter.getLocation(),
                    medCenter.getPhone()
            );
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(donor.getEmail());
            message.setSubject(subject);
            message.setText(text);

            emailService.sendEmail(message);

        } catch (Exception e) {
            log.error("Error creating appointment confirmation email: {}", e.getMessage());
            throw e;
        }
    }

    public Map<String, Object> cancelAppointment(Long appointmentId, Long donorId, String reason) {
        try {
            ResponseEntity<Map<String, Object>> response = medCenterFeignClient.cancelAppointment(
                    appointmentId, reason, donorId);

            if (response != null &&
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    Boolean.TRUE.equals(response.getBody().get("success"))) {
                return Map.of(
                        "success", true,
                        "message", "Appointment cancelled successfully"
                );
            } else {
                String errorMessage = "Failed to cancel appointment";
                if (response != null && response.getBody() != null) {
                    errorMessage = (String) response.getBody().getOrDefault("message", errorMessage);
                }
                throw new RuntimeException(errorMessage);
            }

        } catch (Exception e) {
            log.error("Error cancelling appointment: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
        }
    }

    public Map<String, Object> rescheduleAppointment(Long appointmentId, Long donorId,
                                                     LocalDate newDate, LocalTime newTime) {
        try {
            ResponseEntity<Map<String, Object>> response = medCenterFeignClient.rescheduleAppointment(
                    appointmentId,
                    newDate.toString(),
                    newTime.toString(),
                    donorId
            );

            if (response != null &&
                    response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    Boolean.TRUE.equals(response.getBody().get("success"))) {
                return Map.of(
                        "success", true,
                        "message", "Appointment rescheduled successfully"
                );
            } else {
                String errorMessage = "Failed to reschedule appointment";
                if (response != null && response.getBody() != null) {
                    errorMessage = (String) response.getBody().getOrDefault("message", errorMessage);
                }
                throw new RuntimeException(errorMessage);
            }

        } catch (Exception e) {
            log.error("Error rescheduling appointment: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
        }
    }
}