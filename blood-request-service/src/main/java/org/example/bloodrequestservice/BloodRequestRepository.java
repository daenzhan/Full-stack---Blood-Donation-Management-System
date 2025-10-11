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

    // Новый метод с поддержкой сортировки
    @Query("SELECT br FROM BloodRequest br WHERE " +
            "(:bloodGroup IS NULL OR br.blood_group = :bloodGroup) AND " +
            "(:rheusFactor IS NULL OR br.rhesus_factor = :rheusFactor) AND " +
            "(:componentType IS NULL OR br.component_type = :componentType) AND " +
            "(:medcenterIds IS NULL OR br.medcenter_id IN :medcenterIds) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'deadline_asc' THEN br.deadline END ASC, " +
            "CASE WHEN :sort = 'deadline_desc' THEN br.deadline END DESC, " +
            "CASE WHEN :sort = 'bloodgroup_asc' THEN br.blood_group END ASC, " +
            "CASE WHEN :sort = 'bloodgroup_desc' THEN br.blood_group END DESC, " +
            "CASE WHEN :sort = 'volume_asc' THEN br.volume END ASC, " +
            "CASE WHEN :sort = 'volume_desc' THEN br.volume END DESC, " +
            "br.deadline ASC") // сортировка по умолчанию
    List<BloodRequest> searchAndFilterWithSort(
            @Param("bloodGroup") String bloodGroup,
            @Param("rheusFactor") String rheusFactor,
            @Param("componentType") String componentType,
            @Param("medcenterIds") List<Long> medcenterIds,
            @Param("sort") String sort
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

    // Новый метод с поддержкой сортировки для медцентра
    @Query("SELECT br FROM BloodRequest br WHERE br.medcenter_id = :medcenterId AND " +
            "(:bloodGroup IS NULL OR br.blood_group = :bloodGroup) AND " +
            "(:rheusFactor IS NULL OR br.rhesus_factor = :rheusFactor) AND " +
            "(:componentType IS NULL OR br.component_type = :componentType) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'deadline_asc' THEN br.deadline END ASC, " +
            "CASE WHEN :sort = 'deadline_desc' THEN br.deadline END DESC, " +
            "CASE WHEN :sort = 'bloodgroup_asc' THEN br.blood_group END ASC, " +
            "CASE WHEN :sort = 'bloodgroup_desc' THEN br.blood_group END DESC, " +
            "CASE WHEN :sort = 'volume_asc' THEN br.volume END ASC, " +
            "CASE WHEN :sort = 'volume_desc' THEN br.volume END DESC, " +
            "br.deadline ASC") // сортировка по умолчанию
    List<BloodRequest> searchByMedcenterWithSort(
            @Param("medcenterId") Long medcenterId,
            @Param("bloodGroup") String bloodGroup,
            @Param("rheusFactor") String rheusFactor,
            @Param("componentType") String componentType,
            @Param("sort") String sort
    );

    @Query("SELECT b FROM BloodRequest b WHERE b.medcenter_id = :medcenter_id")
    List<BloodRequest> findByMedcenter_id(@Param("medcenter_id") Long medcenter_id);

    // Новый метод для получения всех запросов с сортировкой
    @Query("SELECT br FROM BloodRequest br ORDER BY " +
            "CASE WHEN :sort = 'deadline_asc' THEN br.deadline END ASC, " +
            "CASE WHEN :sort = 'deadline_desc' THEN br.deadline END DESC, " +
            "CASE WHEN :sort = 'bloodgroup_asc' THEN br.blood_group END ASC, " +
            "CASE WHEN :sort = 'bloodgroup_desc' THEN br.blood_group END DESC, " +
            "CASE WHEN :sort = 'volume_asc' THEN br.volume END ASC, " +
            "CASE WHEN :sort = 'volume_desc' THEN br.volume END DESC, " +
            "br.deadline ASC")
    List<BloodRequest> findAllWithSort(@Param("sort") String sort);
}