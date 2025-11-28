package org.example.medcenterservice.favotite_medcenters;


import jakarta.persistence.*;
import org.example.medcenterservice.MedCenter;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorite_medcenters", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"donor_id", "med_center_id"})
})
public class FavoriteMedCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "med_center_id", nullable = false)
    private MedCenter medCenter;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Конструкторы
    public FavoriteMedCenter() {}

    public FavoriteMedCenter(Long donorId, MedCenter medCenter) {
        this.donorId = donorId;
        this.medCenter = medCenter;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public MedCenter getMedCenter() { return medCenter; }
    public void setMedCenter(MedCenter medCenter) { this.medCenter = medCenter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}