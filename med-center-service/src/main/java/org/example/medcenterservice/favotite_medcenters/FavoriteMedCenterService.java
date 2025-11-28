package org.example.medcenterservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.medcenterservice.activities_story.ActivityStoryService;
import org.example.medcenterservice.favotite_medcenters.FavoriteMedCenter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteMedCenterService {

    private final FavoriteMedCenterRepository favoriteMedCenterRepository;
    private final MedCenterService medCenterService;
    private final ActivityStoryService activityStoryService;

    @Transactional
    public boolean addToFavorites(Long donorId, Long medCenterId) {
        try {
            // Используем новый метод
            if (favoriteMedCenterRepository.existsByDonorIdAndMedCenterId(donorId, medCenterId)) {
                log.info("MedCenter {} is already in favorites for donor {}", medCenterId, donorId);
                return false;
            }

            // Получаем медцентр
            MedCenter medCenter = medCenterService.get_by_id(medCenterId);
            if (medCenter == null) {
                log.error("MedCenter {} not found", medCenterId);
                return false;
            }

            // Создаем запись об избранном медцентре
            FavoriteMedCenter favorite = new FavoriteMedCenter(donorId, medCenter);
            favoriteMedCenterRepository.save(favorite);

            // Логируем активность
            activityStoryService.record_activity(
                    donorId,
                    "DONOR",
                    "MED_CENTER_ADDED_TO_FAVORITES",
                    "Donor added medical center to favorites: " + medCenter.getName() + " (ID: " + medCenterId + ")"
            );

            log.info("MedCenter {} added to favorites for donor {}", medCenterId, donorId);
            return true;

        } catch (Exception e) {
            log.error("Error adding med center {} to favorites for donor {}: {}", medCenterId, donorId, e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean removeFromFavorites(Long donorId, Long medCenterId) {
        try {
            // Используем новый метод
            if (!favoriteMedCenterRepository.existsByDonorIdAndMedCenterId(donorId, medCenterId)) {
                log.info("MedCenter {} is not in favorites for donor {}", medCenterId, donorId);
                return false;
            }

            favoriteMedCenterRepository.deleteByDonorIdAndMedCenterId(donorId, medCenterId);

            // Логируем активность
            activityStoryService.record_activity(
                    donorId,
                    "DONOR",
                    "MED_CENTER_REMOVED_FROM_FAVORITES",
                    "Donor removed medical center from favorites: ID " + medCenterId
            );

            log.info("MedCenter {} removed from favorites for donor {}", medCenterId, donorId);
            return true;

        } catch (Exception e) {
            log.error("Error removing med center {} from favorites for donor {}: {}", medCenterId, donorId, e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<MedCenter> getFavoriteMedCenters(Long donorId) {
        return favoriteMedCenterRepository.findFavoriteMedCentersByDonorId(donorId);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long donorId, Long medCenterId) {
        return favoriteMedCenterRepository.existsByDonorIdAndMedCenterId(donorId, medCenterId);
    }

    @Transactional(readOnly = true)
    public int getFavoriteCount(Long donorId) {
        return favoriteMedCenterRepository.countByDonorId(donorId);
    }
}