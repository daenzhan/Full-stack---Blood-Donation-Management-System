package org.example.medcenterservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedCenterRepository extends JpaRepository<MedCenter, Long> {
    List<MedCenter> findByNameContainingIgnoreCase(String name);
}