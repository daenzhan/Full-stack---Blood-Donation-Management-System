package com.example.backend.rest.donorservice;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private static final int MIN_DAYS_BETWEEN_DONATIONS = 60;
    private static final List<String> BLOOD_TYPE_COMPATIBILITY = List.of(
            "O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"
    );

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

    public Map<String, Object> acceptBloodRequest(Long requestId, Long donorId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<Donor> donorOpt = donorRepository.findByUserId(donorId);
            if (donorOpt.isEmpty()) {
                log.error("Donor not found with userId: {}", donorId);
                result.put("success", false);
                result.put("message", "Donor profile not found");
                return result;
            }

            Donor donor = donorOpt.get();


            ResponseEntity<BloodRequestDto> response = bloodRequestFeignClient.getBloodRequestById(requestId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Blood request not found with id: {}", requestId);
                result.put("success", false);
                result.put("message", "Blood request not found");
                return result;
            }

            BloodRequestDto bloodRequest = response.getBody();

            String donorBloodType = donor.getBloodType();
            String requestBloodType = bloodRequest.getBlood_group() + bloodRequest.getRhesus_factor();

            if (!isBloodTypeCompatible(donorBloodType, requestBloodType)) {
                log.warn("Blood type incompatible: donor {} cannot donate to {}", donorBloodType, requestBloodType);
                result.put("success", false);
                result.put("message", "Your blood type (" + donorBloodType + ") is not compatible with the required type (" + requestBloodType + ")");
                return result;
            }

            ResponseEntity<List<DonationHistoryDto>> historyResponse =
                    donationHistoryFeignClient.getDonationHistoryByDonorId(donorId);

            if (historyResponse.getStatusCode().is2xxSuccessful() && historyResponse.getBody() != null) {
                List<DonationHistoryDto> donationHistory = historyResponse.getBody();
                if (!donationHistory.isEmpty()) {
                    DonationHistoryDto lastDonation = donationHistory.get(0);
                    LocalDateTime lastDonationDate = lastDonation.getDonationDate();
                    LocalDateTime now = LocalDateTime.now();

                    long daysBetween = ChronoUnit.DAYS.between(lastDonationDate, now);

                    if (daysBetween < MIN_DAYS_BETWEEN_DONATIONS) {
                        long daysLeft = MIN_DAYS_BETWEEN_DONATIONS - daysBetween;
                        log.warn("Donor {} cannot donate yet. Last donation was {} days ago. Need to wait {} more days.",
                                donorId, daysBetween, daysLeft);
                        result.put("success", false);
                        result.put("message", "You can only donate blood once every " + MIN_DAYS_BETWEEN_DONATIONS +
                                " days. Your last donation was " + daysBetween + " days ago. Please wait " + daysLeft + " more days.");
                        return result;
                    }
                }
            }
            DonationHistoryDto donationHistory = new DonationHistoryDto();
            donationHistory.setDonorId(donorId);
            donationHistory.setRequestId(requestId);
            donationHistory.setMedcenterId(bloodRequest.getMedcenter_id());
            donationHistory.setBloodType(bloodRequest.getBlood_group() + bloodRequest.getRhesus_factor());
            donationHistory.setComponentType(bloodRequest.getComponent_type());
            donationHistory.setVolume(parseVolume(bloodRequest.getVolume()));


            ResponseEntity<DonationHistoryDto> historySaveResponse =
                    donationHistoryFeignClient.createDonationHistory(donationHistory);

            if (!historySaveResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to create donation history for request {} by donor {}", requestId, donorId);
                result.put("success", false);
                result.put("message", "Failed to save donation history");
                return result;
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("donorId", donorId);

            ResponseEntity<String> acceptResponse =
                    bloodRequestFeignClient.acceptBloodRequest(requestId, requestBody);

            boolean success = acceptResponse.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Blood request {} accepted successfully by donor {}", requestId, donorId);
                result.put("success", true);
                result.put("message", "Blood request accepted successfully!");
            } else {
                log.error("Failed to accept blood request {}: {}", requestId,
                        acceptResponse.getBody() != null ? acceptResponse.getBody() : "Unknown error");
                result.put("success", false);
                result.put("message", "Failed to accept blood request: " +
                        (acceptResponse.getBody() != null ? acceptResponse.getBody() : "Unknown error"));
            }

        } catch (Exception e) {
            log.error("Error accepting blood request {} by donor {}: {}", requestId, donorId, e.getMessage());
            result.put("success", false);
            result.put("message", "Error accepting request: " + e.getMessage());
        }

        return result;
    }
    public boolean isBloodTypeCompatible(String donorBloodType, String requestBloodType) {
        if (donorBloodType == null || requestBloodType == null) {
            return false;
        }

        Map<String, List<String>> compatibilityMap = Map.of(
                "O-", List.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"),
                "O+", List.of("O+", "A+", "B+", "AB+"),
                "A-", List.of("A-", "A+", "AB-", "AB+"),
                "A+", List.of("A+", "AB+"),
                "B-", List.of("B-", "B+", "AB-", "AB+"),
                "B+", List.of("B+", "AB+"),
                "AB-", List.of("AB-", "AB+"),
                "AB+", List.of("AB+")
        );

        return compatibilityMap.getOrDefault(donorBloodType, List.of()).contains(requestBloodType);
    }

    public Map<String, Object> getNextDonationInfo(Long donorId) {
        Map<String, Object> info = new HashMap<>();

        try {
            ResponseEntity<List<DonationHistoryDto>> historyResponse =
                    donationHistoryFeignClient.getDonationHistoryByDonorId(donorId);

            if (historyResponse.getStatusCode().is2xxSuccessful() && historyResponse.getBody() != null) {
                List<DonationHistoryDto> donationHistory = historyResponse.getBody();
                if (!donationHistory.isEmpty()) {
                    DonationHistoryDto lastDonation = donationHistory.get(0);
                    LocalDateTime lastDonationDate = lastDonation.getDonationDate();
                    LocalDateTime nextDonationDate = lastDonationDate.plusDays(MIN_DAYS_BETWEEN_DONATIONS);
                    LocalDateTime now = LocalDateTime.now();

                    long daysBetween = ChronoUnit.DAYS.between(lastDonationDate, now);
                    long daysLeft = MIN_DAYS_BETWEEN_DONATIONS - daysBetween;

                    info.put("lastDonationDate", lastDonationDate);
                    info.put("nextDonationDate", nextDonationDate);
                    info.put("daysSinceLastDonation", daysBetween);
                    info.put("daysUntilNextDonation", daysLeft > 0 ? daysLeft : 0);
                    info.put("canDonate", daysBetween >= MIN_DAYS_BETWEEN_DONATIONS);
                } else {
                    info.put("canDonate", true);
                    info.put("daysUntilNextDonation", 0);
                }
            }
        } catch (Exception e) {
            log.error("Error getting next donation info for donor {}: {}", donorId, e.getMessage());
            info.put("canDonate", true);
            info.put("daysUntilNextDonation", 0);
        }

        return info;
    }

    public List<BloodRequestDto> getActiveBloodRequests(String bloodGroup, String rhesusFactor,
                                                        String componentType, String medcenterName) {
        try {
            ResponseEntity<List<BloodRequestDto>> response = bloodRequestFeignClient.getFilteredBloodRequests(
                    bloodGroup, rhesusFactor, componentType, medcenterName);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Фильтруем только активные запросы (дедлайн в будущем)
                return response.getBody().stream()
                        .filter(request -> request.getDeadline() != null &&
                                request.getDeadline().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching active blood requests: {}", e.getMessage());
            return new ArrayList<>();
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
}

