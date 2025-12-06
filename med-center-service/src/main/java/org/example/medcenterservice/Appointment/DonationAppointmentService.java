package org.example.medcenterservice.Appointment;

import org.example.medcenterservice.MedCenter;
import org.example.medcenterservice.MedCenterService;
import org.example.medcenterservice.activities_story.ActivityStoryService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class DonationAppointmentService {

    private final DonationAppointmentRepository appointmentRepository;
    private final MedCenterService medCenterService;
    private final ActivityStoryService activityStoryService;

    private static final int MAX_APPOINTMENTS_PER_TIMESLOT = 10;

    public DonationAppointmentService(DonationAppointmentRepository appointmentRepository,
                                      MedCenterService medCenterService,
                                      ActivityStoryService activityStoryService) {
        this.appointmentRepository = appointmentRepository;
        this.medCenterService = medCenterService;
        this.activityStoryService = activityStoryService;
    }

    @Transactional
    public DonationAppointment createAppointment(Long donorId, String donorName,
                                                 String donorBloodType, String donorPhone,
                                                 Long medCenterId, LocalDate date,
                                                 LocalTime time) {
        // Check if medical center exists
        MedCenter medCenter = medCenterService.get_by_id(medCenterId);
        if (medCenter == null) {
            throw new RuntimeException("Medical center not found");
        }

        // Check if slot is available (max 10 appointments per timeslot)
        long existingAppointments = appointmentRepository.countByMedCenterDateTime(
                medCenterId, date, time);

        if (existingAppointments >= MAX_APPOINTMENTS_PER_TIMESLOT) {
            throw new RuntimeException("This time slot is fully booked. Please choose another time.");
        }

        // Check if donor already has appointment at same time
        List<DonationAppointment> donorAppointments = appointmentRepository.findByDonorId(donorId);
        for (DonationAppointment app : donorAppointments) {
            if (app.getAppointmentDate().equals(date) &&
                    app.getAppointmentTime().equals(time) &&
                    !app.getStatus().equals("CANCELLED")) {
                throw new RuntimeException("You already have an appointment at this time");
            }
        }

        DonationAppointment appointment = new DonationAppointment(
                donorId, donorName, donorBloodType, donorPhone,
                medCenterId, date, time
        );

        appointment.setStatus("SCHEDULED");

        DonationAppointment savedAppointment = appointmentRepository.save(appointment);

        // Record activity
        activityStoryService.record_activity(
                donorId,
                "DONOR",
                "APPOINTMENT_CREATED",
                "Created donation appointment at " + medCenter.getName() +
                        " for " + date + " " + time
        );

        // Also record for medical center
        activityStoryService.record_activity(
                medCenter.getUser_id(),
                "MEDICAL_CENTER",
                "DONOR_APPOINTMENT_CREATED",
                "Donor " + donorName + " scheduled appointment for " + date + " " + time
        );

        return savedAppointment;
    }

    public List<DonationAppointment> getAppointmentsByMedCenter(Long medCenterId) {
        return appointmentRepository.findByMedCenterId(medCenterId);
    }

    public List<DonationAppointment> getAppointmentsByDonor(Long donorId) {
        return appointmentRepository.findByDonorId(donorId);
    }

    public List<DonationAppointment> getAppointmentsByMedCenterAndDate(Long medCenterId, LocalDate date) {
        return appointmentRepository.findActiveAppointmentsByDate(medCenterId, date);
    }

    public List<LocalDate> getUpcomingAppointmentDates(Long medCenterId) {
        return appointmentRepository.findUpcomingAppointmentDates(medCenterId);
    }

    public Map<LocalTime, Integer> getAvailableSlots(Long medCenterId, LocalDate date) {
        // Define working hours (9:00 to 17:00, appointments every 30 minutes)
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        Map<LocalTime, Integer> availableSlots = new HashMap<>();

        for (LocalTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(30)) {
            long bookedCount = appointmentRepository.countByMedCenterDateTime(medCenterId, date, time);
            int available = MAX_APPOINTMENTS_PER_TIMESLOT - (int) bookedCount;
            if (available > 0) {
                availableSlots.put(time, available);
            }
        }

        return availableSlots;
    }

    @Transactional
    public DonationAppointment updateAppointmentStatus(Long appointmentId, String status, String notes) {
        DonationAppointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        String oldStatus = appointment.getStatus();
        appointment.setStatus(status);

        if (notes != null) {
            appointment.setNotes(notes);
        }

        DonationAppointment updated = appointmentRepository.save(appointment);

        // Record status change
        activityStoryService.record_activity(
                appointment.getDonorId(),
                "DONOR",
                "APPOINTMENT_STATUS_CHANGED",
                "Appointment status changed from " + oldStatus + " to " + status
        );

        return updated;
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, String reason) {
        updateAppointmentStatus(appointmentId, "CANCELLED",
                "Cancelled by donor. Reason: " + (reason != null ? reason : "Not specified"));
    }

    public long countAppointmentsByMedCenter(Long medCenterId) {
        return appointmentRepository.findByMedCenterId(medCenterId).size();
    }

    public long countActiveAppointmentsByMedCenter(Long medCenterId) {
        return appointmentRepository.findByMedCenterIdAndStatusNot(medCenterId, "CANCELLED").size();
    }

    public DonationAppointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    @Transactional
    public DonationAppointment updateAppointment(Long appointmentId, LocalDate newDate,
                                                 LocalTime newTime) {
        DonationAppointment appointment = getAppointmentById(appointmentId);

        // Check if new slot is available
        long existingAppointments = appointmentRepository.countByMedCenterDateTime(
                appointment.getMedCenterId(), newDate, newTime);

        if (existingAppointments >= MAX_APPOINTMENTS_PER_TIMESLOT) {
            throw new RuntimeException("This time slot is fully booked");
        }

        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);

        return appointmentRepository.save(appointment);
    }

    public boolean isSlotAvailable(Long medCenterId, LocalDate date, LocalTime time) {
        long bookedCount = appointmentRepository.countByMedCenterDateTime(medCenterId, date, time);
        return bookedCount < MAX_APPOINTMENTS_PER_TIMESLOT;
    }

    public List<DonationAppointment> getTodayAppointmentsByMedCenter(Long medCenterId) {
        return appointmentRepository.findActiveAppointmentsByDate(medCenterId, LocalDate.now());
    }

    public List<DonationAppointment> searchAppointments(Long medCenterId,
                                                        String donorName,
                                                        String bloodType,
                                                        LocalDate date) {
        Specification<DonationAppointment> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("medCenterId"), medCenterId)
        );

        if (donorName != null && !donorName.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("donorName")), "%" + donorName.toLowerCase() + "%"));
        }

        if (bloodType != null && !bloodType.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("donorBloodType"), bloodType));
        }

        if (date != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("appointmentDate"), date));
        }

        return appointmentRepository.findAll(Sort.by("appointmentDate", "appointmentTime"));
    }
}