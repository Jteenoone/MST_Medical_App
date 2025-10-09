package com.clinic.controllers;

import com.clinic.model.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PatientFormController {
    @FXML private TextField nameField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField genderField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Patient patient;
    private boolean saved;

    public void setPatient(Patient p) {
        this.patient = p;
        if (p != null) {
            nameField.setText(p.getFullName());
            dobPicker.setValue(p.getDateOfBirth());
            genderField.setText(p.getGender());
            phoneField.setText(p.getPhone());
            addressField.setText(p.getAddress());
        }
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void initialize() {
        saveBtn.setOnAction(e -> {
            if (patient == null) patient = new Patient();
            patient.setFullName(nameField.getText());
            patient.setDateOfBirth(dobPicker.getValue());
            patient.setGender(genderField.getText());
            patient.setPhone(phoneField.getText());
            patient.setAddress(addressField.getText());
            saved = true;
            close();
        });
        cancelBtn.setOnAction(e -> { saved = false; close(); });
    }

    public Patient getPatient() { return patient; }

    private void close() {
        ((Stage) saveBtn.getScene().getWindow()).close();
    }
}
