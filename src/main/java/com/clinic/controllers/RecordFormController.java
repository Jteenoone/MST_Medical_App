package com.clinic.controllers;

import com.clinic.model.MedicalRecord;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class RecordFormController {
    @FXML private TextField patientIdField;
    @FXML private DatePicker visitDatePicker;
    @FXML private TextField diagnosisField;
    @FXML private TextField prescriptionField;
    @FXML private TextArea notesArea;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private MedicalRecord record;
    private boolean saved;

    @FXML
    private void initialize() {
        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> { saved = false; close(); });
    }

    public void setRecord(MedicalRecord r) {
        this.record = r;
        if (r != null) {
            patientIdField.setText(String.valueOf(r.getPatientId()));
            visitDatePicker.setValue(r.getVisitDate());
            diagnosisField.setText(r.getDiagnosis());
            prescriptionField.setText(r.getPrescription());
            notesArea.setText(r.getNotes());
        }
    }

    public boolean isSaved() { return saved; }
    public MedicalRecord getRecord() { return record; }

    private void handleSave() {
        if (record == null) record = new MedicalRecord();
        record.setPatientId(Integer.parseInt(patientIdField.getText().trim()));
        LocalDate d = visitDatePicker.getValue();
        record.setVisitDate(d);
        record.setDiagnosis(diagnosisField.getText());
        record.setPrescription(prescriptionField.getText());
        record.setNotes(notesArea.getText());
        saved = true;
        close();
    }

    private void close() {
        ((Stage) saveBtn.getScene().getWindow()).close();
    }
}
