package org.example.medcenterservice.Appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DonationAppointmentRepository extends JpaRepository<DonationAppointment, Long> {

    List<DonationAppointment> findByMedCenterId(Long medCenterId);

    List<DonationAppointment> findByDonorId(Long donorId);

    List<DonationAppointment> findByMedCenterIdAndAppointmentDate(Long medCenterId, LocalDate appointmentDate);

    List<DonationAppointment> findByMedCenterIdAndStatusNot(
            Long medCenterId, String status);

    List<DonationAppointment> findByMedCenterIdAndAppointmentDateAndStatusNot(
            Long medCenterId, LocalDate appointmentDate, String status);

    @Query("SELECT COUNT(a) FROM DonationAppointment a WHERE " +
            "a.medCenterId = :medCenterId AND " +
            "a.appointmentDate = :date AND " +
            "a.appointmentTime = :time AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countByMedCenterDateTime(
            @Param("medCenterId") Long medCenterId,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time);

    @Query("SELECT a FROM DonationAppointment a WHERE " +
            "a.medCenterId = :medCenterId AND " +
            "a.appointmentDate = :date AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
            "ORDER BY a.appointmentTime ASC")
    List<DonationAppointment> findActiveAppointmentsByDate(
            @Param("medCenterId") Long medCenterId,
            @Param("date") LocalDate date);

    List<DonationAppointment> findByMedCenterIdAndStatus(Long medCenterId, String status);

    @Query("SELECT DISTINCT a.appointmentDate FROM DonationAppointment a WHERE " +
            "a.medCenterId = :medCenterId AND " +
            "a.appointmentDate >= CURRENT_DATE AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<LocalDate> findUpcomingAppointmentDates(@Param("medCenterId") Long medCenterId);
}