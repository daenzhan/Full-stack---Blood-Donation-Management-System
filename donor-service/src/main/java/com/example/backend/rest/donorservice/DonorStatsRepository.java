package com.example.backend.rest.donorservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonorStatsRepository extends JpaRepository<DonorStats, Long> {
    Optional<DonorStats> findByDonorId(Long donorId);
    boolean existsByDonorId(Long donorId);

    @Query("SELECT ds FROM DonorStats ds ORDER BY ds.points DESC, ds.totalDonations DESC")
    List<DonorStats> findTopDonors();

    @Query("SELECT ds FROM DonorStats ds WHERE ds.donorId IN :donorIds")
    List<DonorStats> findByDonorIdIn(@Param("donorIds") List<Long> donorIds);
}
