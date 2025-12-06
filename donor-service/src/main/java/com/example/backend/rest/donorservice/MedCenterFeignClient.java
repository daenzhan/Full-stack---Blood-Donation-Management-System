package com.example.backend.rest.donorservice;

import com.example.backend.rest.donorservice.DonorAppointment.AppointmentRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "med-center-service", configuration = FeignConfig.class)
public interface MedCenterFeignClient {

    @GetMapping("/medcenters/client/{medcenter_id}")
    ResponseEntity<MedCenterDto> get_medcenter_by_id(@PathVariable Long medcenter_id);

    // ИСПРАВЬТЕ ЭТО:
    @GetMapping("/medcenters/client/search")
    ResponseEntity<List<MedCenterDto>> search_by_name(@RequestParam String name);

    // ИСПРАВЬТЕ ЭТИ URL:
    @PostMapping("/donation-appointments/create")
    ResponseEntity<Map<String, Object>> createAppointment(@RequestBody AppointmentRequestDto request);
    @GetMapping("/donation-appointments/api/slots")
    ResponseEntity<Map<String, Object>> getAvailableSlots(@RequestParam Long medCenterId,
                                                          @RequestParam String date);

    @GetMapping("/donation-appointments/api/check-slot")
    ResponseEntity<Map<String, Object>> checkSlotAvailability(@RequestParam Long medCenterId,
                                                              @RequestParam String date,
                                                              @RequestParam String time);

    @PostMapping("/donation-appointments/donor/cancel")
    ResponseEntity<Map<String, Object>> cancelAppointment(@RequestParam Long appointmentId,
                                                          @RequestParam(required = false) String reason,
                                                          @RequestParam Long donorId);

    @PostMapping("/donation-appointments/donor/reschedule")
    ResponseEntity<Map<String, Object>> rescheduleAppointment(@RequestParam Long appointmentId,
                                                              @RequestParam String newDate,
                                                              @RequestParam String newTime,

                                                              @RequestParam Long donorId);


    @GetMapping("/donation-appointments/donor/{donorId}")
    ResponseEntity<List<Map<String, Object>>> getDonorAppointments(@PathVariable Long donorId);

}