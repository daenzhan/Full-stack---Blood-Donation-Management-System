package org.example.medcenterservice.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public class RescheduleRequestDto {
    private Long appointmentId;
    private LocalDate newDate;
    private LocalTime newTime;
    private Long donorId;

    // Getters and Setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public LocalDate getNewDate() { return newDate; }
    public void setNewDate(LocalDate newDate) { this.newDate = newDate; }

    public LocalTime getNewTime() { return newTime; }
    public void setNewTime(LocalTime newTime) { this.newTime = newTime; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }
}
