package org.example.bloodrequestservice;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BloodRequestService {

    private final BloodRequestRepository repository;
    private final MedCenterFeignClient medCenterFeignClient;

    public BloodRequestService(BloodRequestRepository repository, MedCenterFeignClient medCenterFeignClient) {
        this.repository = repository;
        this.medCenterFeignClient = medCenterFeignClient;
    }

    public List<BloodRequest> get_all_requests() {
        return repository.findAll();
    }

    public Optional<BloodRequest> get_request_by_id(Long id) {
        return repository.findById(id);
    }

    public BloodRequest save_request(BloodRequest request) {
        return repository.save(request);
    }

    public void delete_request(Long id) {
        repository.deleteById(id);
    }

    public List<BloodRequest> get_requests_by_medcenter(Long medcenter_id) {
        return repository.findByMedcenter_id(medcenter_id);
    }

    public List<BloodRequest> search_and_filter(String bloodGroup, String rheusFactor,
                                                String componentType, String medcenterName) {

        // Нормализуем параметры - преобразуем пустые строки в null
        String normalizedBloodGroup = normalizeParameter(bloodGroup);
        String normalizedRheusFactor = normalizeParameter(rheusFactor);
        String normalizedComponentType = normalizeParameter(componentType);
        String normalizedMedcenterName = normalizeParameter(medcenterName);

        List<Long> medcenterIds = null;

        // Если указано название медцентра, ищем соответствующие ID
        if (normalizedMedcenterName != null) {
            try {
                List<MedCenterDto> matchingMedCenters = medCenterFeignClient.search_by_name(normalizedMedcenterName);
                if (!matchingMedCenters.isEmpty()) {
                    medcenterIds = matchingMedCenters.stream()
                            .map(MedCenterDto::getMed_center_id)
                            .collect(Collectors.toList());
                } else {
                    // Если не найдено подходящих медцентров, возвращаем пустой список
                    return List.of();
                }
            } catch (Exception e) {
                System.err.println("Error searching medcenters: " + e.getMessage());
                return List.of();
            }
        }

        // Используем один запрос с всеми фильтрами
        return repository.searchAndFilter(normalizedBloodGroup, normalizedRheusFactor,
                normalizedComponentType, medcenterIds);
    }

    public List<BloodRequest> search_by_medcenter(Long medcenterId, String bloodGroup,
                                                  String rheusFactor, String componentType) {
        // Нормализуем параметры для этого метода тоже
        String normalizedBloodGroup = normalizeParameter(bloodGroup);
        String normalizedRheusFactor = normalizeParameter(rheusFactor);
        String normalizedComponentType = normalizeParameter(componentType);

        return repository.searchByMedcenter(medcenterId, normalizedBloodGroup,
                normalizedRheusFactor, normalizedComponentType);
    }

    // Вспомогательный метод для нормализации параметров
    private String normalizeParameter(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        return param.trim();
    }

    // Вспомогательный метод для получения имени медцентра
    public String getMedCenterName(Long medcenterId) {
        if (medcenterId == null) {
            return "Unknown";
        }

        try {
            MedCenterDto medCenter = medCenterFeignClient.get_medcenter_by_id(medcenterId);
            return medCenter != null ? medCenter.getName() : "Unknown";
        } catch (Exception e) {
            System.err.println("Error fetching medcenter name: " + e.getMessage());
            return "Unknown (ID: " + medcenterId + ")";
        }
    }
}
