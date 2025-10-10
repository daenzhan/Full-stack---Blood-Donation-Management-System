package org.example.medcenterservice;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "med_centers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MedCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long med_center_id;

    private String name;
    private String location;
    private String phone;
    private String specialization;
    private Long user_id;

    public MedCenter(Long med_center_id, String name, String location, String phone, String specialization, Long user_id) {
        this.med_center_id = med_center_id;
        this.name = name;
        this.location = location;
        this.phone = phone;
        this.specialization = specialization;
        this.user_id = user_id;
    }

    public MedCenter (){}

    public Long getMed_center_id() {
        return med_center_id;
    }

    public void setMed_center_id(Long med_center_id) {
        this.med_center_id = med_center_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }
}