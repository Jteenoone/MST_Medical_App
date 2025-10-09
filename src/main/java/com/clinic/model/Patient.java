package com.clinic.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Patient {
    private Integer id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender; // "Nam" / "Nữ" / "Khác"
    private String phone;
    private String address;
    private LocalDateTime createdAt;

    public Patient() {}

    public Patient(Integer id, String fullName, LocalDate dateOfBirth, String gender, String phone, String address, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
