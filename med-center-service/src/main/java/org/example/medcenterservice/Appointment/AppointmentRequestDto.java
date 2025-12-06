package org.example.medcenterservice.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentRequestDto {
    private Long donorId;
    private String donorName;
    private String donorBloodType;
    private String donorPhone;
    private Long medCenterId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String notes;

    // Getters and Setters
    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorBloodType() { return donorBloodType; }
    public void setDonorBloodType(String donorBloodType) { this.donorBloodType = donorBloodType; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public Long getMedCenterId() { return medCenterId; }
    public void setMedCenterId(Long medCenterId) { this.medCenterId = medCenterId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}