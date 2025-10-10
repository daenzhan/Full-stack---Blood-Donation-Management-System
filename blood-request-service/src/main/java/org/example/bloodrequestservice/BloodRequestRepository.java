package org.example.bloodrequestservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    @Query("SELECT br FROM BloodRequest br WHERE " +
            "(:bloodGroup IS NULL OR br.blood_group = :bloodGroup) AND " +
            "(:rheusFactor IS NULL OR br.rhesus_factor = :rheusFactor) AND " +
            "(:componentType IS NULL OR br.component_type = :componentType) AND " +
            "(:medcenterIds IS NULL OR br.medcenter_id IN :medcenterIds)")
    List<BloodRequest> searchAndFilter(
            @Param("bloodGroup") String bloodGroup,
            @Param("rheusFactor") String rheusFactor,
            @Param("componentType") String componentType,
            @Param("medcenterIds") List<Long> medcenterIds
    );

    @Query("SELECT br FROM BloodRequest br WHERE br.medcenter_id = :medcenterId AND " +
            "(:bloodGroup IS NULL OR br.blood_group = :bloodGroup) AND " +
            "(:rheusFactor IS NULL OR br.rhesus_factor = :rheusFactor) AND " +
            "(:componentType IS NULL OR br.component_type = :componentType)")
    List<BloodRequest> searchByMedcenter(
            @Param("medcenterId") Long medcenterId,
            @Param("bloodGroup") String bloodGroup,
            @Param("rheusFactor") String rheusFactor,
            @Param("componentType") String componentType
    );

    @Query("SELECT b FROM BloodRequest b WHERE b.medcenter_id = :medcenter_id")
    List<BloodRequest> findByMedcenter_id(@Param("medcenter_id") Long medcenter_id);
}