package org.example.bloodrequestservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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

    // Новый метод с поддержкой сортировки
    public List<BloodRequest> get_all_requests_sorted(String sort) {
        String normalizedSort = normalizeSortParameter(sort);
        return repository.findAllWithSort(normalizedSort);
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
        return search_and_filter_with_sort(bloodGroup, rheusFactor, componentType, medcenterName, null);
    }

    // Новый метод с поддержкой сортировки
    public List<BloodRequest> search_and_filter_with_sort(String bloodGroup, String rheusFactor,
                                                          String componentType, String medcenterName, String sort) {

        // Нормализуем параметры - преобразуем пустые строки в null
        String normalizedBloodGroup = normalizeParameter(bloodGroup);
        String normalizedRheusFactor = normalizeParameter(rheusFactor);
        String normalizedComponentType = normalizeParameter(componentType);
        String normalizedMedcenterName = normalizeParameter(medcenterName);
        String normalizedSort = normalizeSortParameter(sort);

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

        // Используем один запрос с всеми фильтрами и сортировкой
        return repository.searchAndFilterWithSort(normalizedBloodGroup, normalizedRheusFactor,
                normalizedComponentType, medcenterIds, normalizedSort);
    }

    public List<BloodRequest> search_by_medcenter(Long medcenterId, String bloodGroup,
                                                  String rheusFactor, String componentType) {
        return search_by_medcenter_with_sort(medcenterId, bloodGroup, rheusFactor, componentType, null);
    }

    // Новый метод с поддержкой сортировки для медцентра
    public List<BloodRequest> search_by_medcenter_with_sort(Long medcenterId, String bloodGroup,
                                                            String rheusFactor, String componentType, String sort) {
        // Нормализуем параметры для этого метода тоже
        String normalizedBloodGroup = normalizeParameter(bloodGroup);
        String normalizedRheusFactor = normalizeParameter(rheusFactor);
        String normalizedComponentType = normalizeParameter(componentType);
        String normalizedSort = normalizeSortParameter(sort);

        return repository.searchByMedcenterWithSort(medcenterId, normalizedBloodGroup,
                normalizedRheusFactor, normalizedComponentType, normalizedSort);
    }

    // Вспомогательный метод для нормализации параметров
    private String normalizeParameter(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        return param.trim();
    }

    // Вспомогательный метод для нормализации параметра сортировки
    private String normalizeSortParameter(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return "deadline_asc"; // значение по умолчанию
        }

        String normalizedSort = sort.trim().toLowerCase();

        // Проверяем, что передан корректный параметр сортировки
        List<String> validSortOptions = List.of(
                "deadline_asc", "deadline_desc",
                "bloodgroup_asc", "bloodgroup_desc",
                "volume_asc", "volume_desc"
        );

        if (validSortOptions.contains(normalizedSort)) {
            return normalizedSort;
        } else {
            return "deadline_asc"; // значение по умолчанию при некорректном параметре
        }
    }

    // Вспомогательный метод для получения имени медцентра
//    public String getMedCenterName(Long medcenterId) {
//        if (medcenterId == null) {
//            return "Unknown";
//        }
//
//        try {
//            MedCenterDto medCenter = medCenterFeignClient.get_medcenter_by_id(medcenterId);
//
//            if (medCenter == null || medCenter.getName() == null) {
//                log.warn("Medical center {} not found or has no name", medcenterId);
//                return "Medical Center " + medcenterId;
//            }
//
//            return medCenter.getName();
//
//        } catch (Exception e) {
//            log.warn("Error fetching medcenter name for ID {}: {}", medcenterId, e.getMessage());
//            return "Medical Center " + medcenterId;
//        }
//    }

    public String getMedCenterName(Long medcenterId) {
        if (medcenterId == null) {
            log.warn("Medical center ID is null");
            return "Unknown Medical Center";
        }

        try {
            log.debug("Fetching medical center name for ID: {}", medcenterId);
            MedCenterDto medCenter = medCenterFeignClient.get_medcenter_by_id(medcenterId);

            if (medCenter == null) {
                log.warn("Medical center {} not found - returned null", medcenterId);
                return "Medical Center " + medcenterId;
            }

            String name = medCenter.getName();
            if (name == null || name.trim().isEmpty()) {
                log.warn("Medical center {} has no name", medcenterId);
                return "Medical Center " + medcenterId;
            }

            log.debug("Medical center name found: {}", name);
            return name;

        } catch (Exception e) {
            log.error("Error fetching medcenter name for ID {}: {}", medcenterId, e.getMessage());
            log.debug("Exception details: ", e);
            return "Medical Center " + medcenterId;
        }
    }

}