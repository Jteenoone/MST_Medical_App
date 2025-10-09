package com.clinic.model;

import java.time.LocalDate;

public class MedicalRecord {
    private Integer id;
    private Integer patientId;
    private LocalDate visitDate;
    private String diagnosis;
    private String prescription;
    private String notes;

    public MedicalRecord() {}

    public MedicalRecord(Integer id, Integer patientId, LocalDate visitDate, String diagnosis, String prescription, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.visitDate = visitDate;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.notes = notes;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
