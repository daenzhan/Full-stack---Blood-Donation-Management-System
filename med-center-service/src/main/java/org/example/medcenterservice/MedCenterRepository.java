package org.example.medcenterservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedCenterRepository extends JpaRepository<MedCenter, Long> {
}