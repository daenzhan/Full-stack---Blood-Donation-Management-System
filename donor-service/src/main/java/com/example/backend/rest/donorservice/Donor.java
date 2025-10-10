package com.example.backend.rest.donorservice;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donors")
@Data
public class Donor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String phoneNumber;
    private String address;
    private String gender;
    private LocalDateTime createdAt = LocalDateTime.now();
}
