package org.example.bloodrequestservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/blood-requests")
@RequiredArgsConstructor
@Slf4j
public class BloodRequestApiController {
    private final BloodRequestService bloodRequestService;

    @GetMapping
    public List<BloodRequest> getAllBloodRequests() {
        return bloodRequestService.get_all_requests();
    }

    @GetMapping("/filter")
    public List<BloodRequest> getFilteredBloodRequests(
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String rhesusFactor,
            @RequestParam(required = false) String componentType,
            @RequestParam(required = false) String medcenterName) {

        return bloodRequestService.search_and_filter(bloodGroup, rhesusFactor, componentType, medcenterName);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<BloodRequest> getBloodRequestById(@PathVariable Long requestId) {
        Optional<BloodRequest> request = bloodRequestService.get_request_by_id(requestId);
        return request.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<String> acceptBloodRequest(@PathVariable Long requestId,
                                                     @RequestBody Map<String, Object> requestBody) {
        try {
            Long donorId = Long.valueOf(requestBody.get("donorId").toString());
            String responseMessage = "Blood request accepted successfully by donor: " + donorId;
            log.info("Blood request {} accepted by donor {}", requestId, donorId);

            return ResponseEntity.ok(responseMessage);

        } catch (Exception e) {
            String errorMessage = "Error accepting blood request: " + e.getMessage();
            log.error(errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage);
        }
    }
}