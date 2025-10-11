package org.example.medcenterservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedCenterRepository extends JpaRepository<MedCenter, Long> {
    List<MedCenter> findByNameContainingIgnoreCase(String name);
    Optional<MedCenter> findByUser_id(@Param("user_id") Long user_id);
    boolean existsByUser_id(@Param("user_id") Long user_id);
}