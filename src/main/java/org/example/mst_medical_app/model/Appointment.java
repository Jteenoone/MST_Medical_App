package org.example.mst_medical_app.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Appointment model – tương thích với toàn bộ controller:
 * gồm date, start, end, doctor, patient, notes, status...
 */
public class Appointment {

    public enum Status { PENDING, CONFIRMED, COMPLETED, CANCELED }

    private int appointmentId;
    private int patientId;
    private int doctorId;
    private LocalDateTime appointmentTime;
    private Status status;
    private String notes;

    // optional: thời lượng (phút)
    private int durationMinutes = 30;

    private String doctorName;
    private String patientName;

    // --- CONSTRUCTOR ---
    public Appointment(int appointmentId, int patientId, int doctorId,
                       LocalDateTime appointmentTime, Status status, String notes) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.notes = notes;
    }

    // Overload nếu DB trả về status là String
    public Appointment(int appointmentId, int patientId, int doctorId,
                       LocalDateTime appointmentTime, String status, String notes) {
        this(appointmentId, patientId, doctorId, appointmentTime,
                Status.valueOf(status.toUpperCase()), notes);
    }

    // --- GETTERS ---
    public int getAppointmentId() { return appointmentId; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public Status getStatus() { return status; }
    public String getNotes() { return notes; }


    public String getDoctorName() { return doctorName; }
    public String getPatientName() { return patientName; }


    public String getDoctor() { return doctorName; }
    public String getPatient() { return patientName; }

    // --- NEW helper methods (cho Calendar & TableView) ---
    public LocalDate getDate() {
        return appointmentTime != null ? appointmentTime.toLocalDate() : null;
    }

    public LocalTime getStart() {
        return appointmentTime != null ? appointmentTime.toLocalTime() : null;
    }

    public LocalTime getEnd() {
        if (appointmentTime == null) return null;
        if (durationMinutes <= 0) return appointmentTime.toLocalTime();
        return appointmentTime.plusMinutes(durationMinutes).toLocalTime();
    }

    // --- FORMAT helpers ---
    public String getFormattedDate() {
        return appointmentTime != null ? appointmentTime.toLocalDate().toString() : "";
    }

    public String getFormattedTime() {
        if (appointmentTime == null) return "";
        if (durationMinutes <= 0) return appointmentTime.toLocalTime().toString();
        return appointmentTime.toLocalTime() + " - " + getEnd();
    }

    // --- SETTERS ---
    public void setStatus(Status status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    // --- Duration getter ---
    public int getDurationMinutes() { return durationMinutes; }

    public LocalDateTime getDateTime() {
        return appointmentTime;
    }

    public String getPurpose() {
        if (notes == null || notes.trim().isEmpty()) {
            return "Khám bệnh";
        }
        return notes;
    }
}
