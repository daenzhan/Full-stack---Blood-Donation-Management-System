package com.example.backend.rest.donorservice;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorBloodRequestService {
    private final BloodRequestFeignClient bloodRequestFeignClient;
    private final DonationHistoryFeignClient donationHistoryFeignClient;
    private final MedCenterFeignClient medCenterFeignClient;
    private final DonorRepository donorRepository;

    public List<BloodRequestDto> getMatchingBloodRequests(String donorBloodType) {
        try {
            ResponseEntity<List<BloodRequestDto>> response = bloodRequestFeignClient.getAllBloodRequests();
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<BloodRequestDto> allRequests = response.getBody();
                return allRequests.stream()
                        .filter(request -> isBloodTypeCompatible(donorBloodType,
                                request.getBlood_group() + request.getRhesus_factor()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching matching blood requests: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public String getMedCenterName(Long medcenterId) {
        if (medcenterId == null) {
            return "Unknown Medical Center";
        }
        try {
            ResponseEntity<MedCenterDto> response = medCenterFeignClient.get_medcenter_by_id(medcenterId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getName();
            }
            return "Medical Center #" + medcenterId;
        } catch (Exception e) {
            log.warn("Medcenter not found for ID {}: {}", medcenterId, e.getMessage());
            return "Medical Center #" + medcenterId;
        }
    }

    public List<BloodRequestDto> getBloodRequests(String bloodGroup, String rhesusFactor,
                                                  String componentType, String medcenterName) {
        try {
            ResponseEntity<List<BloodRequestDto>> response = bloodRequestFeignClient.getFilteredBloodRequests(
                    bloodGroup, rhesusFactor, componentType, medcenterName);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching blood requests: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean acceptBloodRequest(Long requestId, Long donorId) {
        try {
            Optional<Donor> donorOpt = donorRepository.findByUserId(donorId);
            if (donorOpt.isEmpty()) {
                log.error("Donor not found with userId: {}", donorId);
                return false;
            }
            ResponseEntity<BloodRequestDto> response = bloodRequestFeignClient.getBloodRequestById(requestId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Blood request not found with id: {}", requestId);
                return false;
            }

            BloodRequestDto bloodRequest = response.getBody();
            DonationHistoryDto donationHistory = new DonationHistoryDto();
            donationHistory.setDonorId(donorId);
            donationHistory.setRequestId(requestId);
            donationHistory.setMedcenterId(bloodRequest.getMedcenter_id());
            donationHistory.setBloodType(bloodRequest.getBlood_group() + bloodRequest.getRhesus_factor());
            donationHistory.setComponentType(bloodRequest.getComponent_type());
            donationHistory.setVolume(parseVolume(bloodRequest.getVolume()));
            ResponseEntity<DonationHistoryDto> historyResponse =
                    donationHistoryFeignClient.createDonationHistory(donationHistory);

            if (!historyResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to create donation history for request {} by donor {}", requestId, donorId);
                return false;
            }
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("donorId", donorId);

            ResponseEntity<String> acceptResponse =
                    bloodRequestFeignClient.acceptBloodRequest(requestId, requestBody);

            boolean success = acceptResponse.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Blood request {} accepted successfully by donor {}", requestId, donorId);
            } else {
                log.error("Failed to accept blood request {}: {}", requestId,
                        acceptResponse.getBody() != null ? acceptResponse.getBody() : "Unknown error");
            }
            return success;

        } catch (Exception e) {
            log.error("Error accepting blood request {} by donor {}: {}", requestId, donorId, e.getMessage());
            return false;
        }
    }

    private Double parseVolume(String volumeStr) {
        try {
            if (volumeStr == null || volumeStr.trim().isEmpty()) {
                return 1.0;
            }
            String cleaned = volumeStr.replace("units", "").replace("unit", "").trim();
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            log.warn("Failed to parse volume '{}', using default 1.0", volumeStr);
            return 1.0;
        }
    }

    private boolean isBloodTypeCompatible(String donorBloodType, String requestBloodType) {
        if (donorBloodType == null || requestBloodType == null) {
            return false;
        }
        if ("O-".equals(donorBloodType)) {
            return true;
        }
        if ("AB+".equals(requestBloodType)) {
            return true;
        }
        char donorRh = donorBloodType.charAt(donorBloodType.length() - 1);
        char requestRh = requestBloodType.charAt(requestBloodType.length() - 1);
        if (donorRh == '+' && requestRh == '-') {
            return false;
        }

        String donorBase = donorBloodType.substring(0, donorBloodType.length() - 1);
        String requestBase = requestBloodType.substring(0, requestBloodType.length() - 1);

        Map<String, List<String>> compatibilityMap = Map.of(
                "O", List.of("O", "A", "B", "AB"),
                "A", List.of("A", "AB"),
                "B", List.of("B", "AB"),
                "AB", List.of("AB")
        );

        return compatibilityMap.getOrDefault(donorBase, List.of()).contains(requestBase);
    }
}
