package org.example.medcenterservice;

import org.example.medcenterservice.favotite_medcenters.FavoriteMedCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteMedCenterRepository extends JpaRepository<FavoriteMedCenter, Long> {

    List<FavoriteMedCenter> findByDonorIdOrderByCreatedAtDesc(Long donorId);

    // Используем явные @Query аннотации
    @Query("SELECT fm FROM FavoriteMedCenter fm WHERE fm.donorId = :donorId AND fm.medCenter.med_center_id = :medCenterId")
    Optional<FavoriteMedCenter> findByDonorIdAndMedCenterId(@Param("donorId") Long donorId, @Param("medCenterId") Long medCenterId);

    @Query("SELECT COUNT(fm) > 0 FROM FavoriteMedCenter fm WHERE fm.donorId = :donorId AND fm.medCenter.med_center_id = :medCenterId")
    boolean existsByDonorIdAndMedCenterId(@Param("donorId") Long donorId, @Param("medCenterId") Long medCenterId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FavoriteMedCenter fm WHERE fm.donorId = :donorId AND fm.medCenter.med_center_id = :medCenterId")
    void deleteByDonorIdAndMedCenterId(@Param("donorId") Long donorId, @Param("medCenterId") Long medCenterId);

    @Query("SELECT fm.medCenter FROM FavoriteMedCenter fm WHERE fm.donorId = :donorId")
    List<MedCenter> findFavoriteMedCentersByDonorId(@Param("donorId") Long donorId);

    int countByDonorId(Long donorId);
}