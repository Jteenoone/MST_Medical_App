package com.clinic.model;

import java.time.LocalDateTime;

public class Appointment {
    private Integer id;
    private Integer patientId;
    private LocalDateTime appointmentTime;
    private String doctorName;
    private String reason;
    private String status; // scheduled, completed, cancelled

    public Appointment() {}

    public Appointment(Integer id, Integer patientId, LocalDateTime appointmentTime, String doctorName, String reason, String status) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentTime = appointmentTime;
        this.doctorName = doctorName;
        this.reason = reason;
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
