package org.example.analysisservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    @Query("SELECT a FROM Analysis a WHERE a.donor_id = :donor_id")
    List<Analysis> findByDonor_id(Long donor_id);
}
