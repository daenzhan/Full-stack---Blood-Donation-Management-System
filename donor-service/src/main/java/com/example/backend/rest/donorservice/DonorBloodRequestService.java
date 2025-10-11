package com.example.backend.rest.donorservice;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonorBloodRequestService {

    private final BloodRequestClient bloodRequestClient;

    public List<BloodRequest> getBloodRequests(String bloodGroup, String rhesusFactor,
                                               String componentType, String medcenterName) {
        try {
            System.out.println("Fetching blood requests with params: " +
                    "bloodGroup=" + bloodGroup + ", " +
                    "rhesusFactor=" + rhesusFactor + ", " +
                    "componentType=" + componentType + ", " +
                    "medcenterName=" + medcenterName);

            List<BloodRequest> requests = bloodRequestClient.getBloodRequests(
                    bloodGroup, rhesusFactor, componentType, medcenterName);

            System.out.println("Successfully fetched " + requests.size() + " requests");
            return requests;

        } catch (Exception e) {
            System.err.println("Error fetching blood requests: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public List<BloodRequest> getMatchingBloodRequests(String donorBloodType) {
        if (donorBloodType == null || donorBloodType.length() < 2) {
            System.out.println("Invalid donor blood type: " + donorBloodType);
            return List.of();
        }

        try {
            String bloodGroup = donorBloodType.substring(0, donorBloodType.length() - 1);
            String rhesusFactor = donorBloodType.substring(donorBloodType.length() - 1);

            System.out.println("Searching for matching requests: bloodGroup=" +
                    bloodGroup + ", rhesusFactor=" + rhesusFactor);

            List<BloodRequest> requests = bloodRequestClient.getBloodRequests(
                    bloodGroup, rhesusFactor, null, null);

            System.out.println("Found " + requests.size() + " matching requests for blood type: " + donorBloodType);
            return requests;

        } catch (Exception e) {
            System.err.println("Error fetching matching blood requests: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}
