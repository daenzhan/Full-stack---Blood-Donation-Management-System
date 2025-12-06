//package org.example.bloodrequestservice;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface TemplateRepository extends JpaRepository<Template, Long> {
//    List<Template> findByMedcenterId(Long medcenterId);
//
//    @Query("SELECT t FROM Template t WHERE t.medcenterId = :medcenterId AND LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))")
//    List<Template> findByMedcenterIdAndNameContaining(@Param("medcenterId") Long medcenterId,
//                                                      @Param("search") String search);
//}
//


package org.example.bloodrequestservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByMedcenterId(Long medcenterId);
}