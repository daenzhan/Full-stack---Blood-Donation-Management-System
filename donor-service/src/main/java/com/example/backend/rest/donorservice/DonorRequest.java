package com.example.backend.rest.donorservice;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DonorRequest {
    private String fullName;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String phoneNumber;
    private String address;
    private String gender;
}
