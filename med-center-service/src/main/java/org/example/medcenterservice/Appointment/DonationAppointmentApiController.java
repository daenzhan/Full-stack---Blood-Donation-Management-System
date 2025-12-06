//package org.example.medcenterservice.Appointment;
//
//import org.example.medcenterservice.MedCenter;
//import org.example.medcenterservice.MedCenterService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/donation-appointments")
//public class DonationAppointmentApiController {
//
//    private final DonationAppointmentService appointmentService;
//    private final MedCenterService medCenterService;
//
//    public DonationAppointmentApiController(DonationAppointmentService appointmentService,
//                                            MedCenterService medCenterService) {
//        this.appointmentService = appointmentService;
//        this.medCenterService = medCenterService;
//    }
//
//    @GetMapping("/donor/{donorId}")
//    public ResponseEntity<List<Map<String, Object>>> getDonorAppointments(@PathVariable Long donorId) {
//        try {
//            List<DonationAppointment> appointments = appointmentService.getAppointmentsByDonor(donorId);
//
//            List<Map<String, Object>> appointmentDtos = appointments.stream()
//                    .map(appointment -> {
//                        Map<String, Object> dto = new HashMap<>();
//                        dto.put("appointmentId", appointment.getAppointmentId());
//                        dto.put("donorId", appointment.getDonorId());
//                        dto.put("donorName", appointment.getDonorName());
//                        dto.put("donorBloodType", appointment.getDonorBloodType());
//                        dto.put("donorPhone", appointment.getDonorPhone());
//                        dto.put("medCenterId", appointment.getMedCenterId());
//                        dto.put("appointmentDate", appointment.getAppointmentDate());
//                        dto.put("appointmentTime", appointment.getAppointmentTime());
//                        dto.put("status", appointment.getStatus());
//                        dto.put("notes", appointment.getNotes());
//                        dto.put("createdAt", appointment.getCreatedAt());
//                        dto.put("updatedAt", appointment.getUpdatedAt());
//
//                        // Get medical center name
//                        MedCenter medCenter = medCenterService.get_by_id(appointment.getMedCenterId());
//                        if (medCenter != null) {
//                            dto.put("medCenterName", medCenter.getName());
//                            dto.put("medCenterLocation", medCenter.getLocation());
//                            dto.put("medCenterPhone", medCenter.getPhone());
//                        }
//
//                        return dto;
//                    })
//                    .collect(Collectors.toList());
//
//            return ResponseEntity.ok(appointmentDtos);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(List.of());
//        }
//    }
//}
