package org.example.medcenterservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "med_centers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MedCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "med_center_id")
    private Long med_center_id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "license_file")
    private String license_file;

    @Column(name = "director_name")
    private String directorName;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private Double distance;

    public MedCenter() {}

    public MedCenter(Long med_center_id, String name, String location, String phone,
                     String specialization, Long userId, String license_file,
                     String directorName, String email, Double latitude, Double longitude) {
        this.med_center_id = med_center_id;
        this.name = name;
        this.location = location;
        this.phone = phone;
        this.specialization = specialization;
        this.userId = userId;
        this.license_file = license_file;
        this.directorName = directorName;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Геттеры и сеттеры
    public Long getMed_center_id() { return med_center_id; }
    public void setMed_center_id(Long med_center_id) { this.med_center_id = med_center_id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public Long getUser_id() { return userId; }
    public void setUser_id(Long user_id) { this.userId = user_id; }
    public String getLicense_file() { return license_file; }
    public void setLicense_file(String license_file) { this.license_file = license_file; }
    public String getDirectorName() { return directorName; }
    public void setDirectorName(String directorName) { this.directorName = directorName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
}