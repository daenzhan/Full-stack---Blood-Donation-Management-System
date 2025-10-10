package org.example.bloodrequestservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;


public class MedCenterDto {
    private Long med_center_id;
    private String name;
    private String location;
    private String phone;
    private String specialization;
    private Long user_id;
    private String license_file;

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

    public String getLicense_file() {
        return license_file;
    }

    public void setLicense_file(String license_file) {
        this.license_file = license_file;
    }
}
