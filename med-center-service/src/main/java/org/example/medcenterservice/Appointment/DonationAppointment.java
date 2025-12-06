package org.example.medcenterservice.Appointment;

import jakarta.persistence.*;
import org.example.medcenterservice.MedCenter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_appointments")
public class DonationAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Column(name = "donor_name", nullable = false)
    private String donorName;

    @Column(name = "donor_blood_type", nullable = false)
    private String donorBloodType;

    @Column(name = "donor_phone")
    private String donorPhone;

    @Column(name = "med_center_id", nullable = false)
    private Long medCenterId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Column(name = "status", nullable = false)
    private String status = "SCHEDULED"; // SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "med_center_id", insertable = false, updatable = false)
    private MedCenter medCenter;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public DonationAppointment() {}

    public DonationAppointment(Long donorId, String donorName, String donorBloodType,
                               String donorPhone, Long medCenterId,
                               LocalDate appointmentDate, LocalTime appointmentTime) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.donorBloodType = donorBloodType;
        this.donorPhone = donorPhone;
        this.medCenterId = medCenterId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
    }

    // Getters and Setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public MedCenter getMedCenter() { return medCenter; }
    public void setMedCenter(MedCenter medCenter) { this.medCenter = medCenter; }
}