package org.example.medcenterservice.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentSlotDto {
    private LocalTime time;
    private int availableSlots;
    private boolean isAvailable;

    public AppointmentSlotDto() {}

    public AppointmentSlotDto(LocalTime time, int availableSlots) {
        this.time = time;
        this.availableSlots = availableSlots;
        this.isAvailable = availableSlots > 0;
    }

    // Getters and Setters
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
        this.isAvailable = availableSlots > 0;
    }

    public boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(boolean isAvailable) { this.isAvailable = isAvailable; }
}