package com.clinic.controllers;

import com.clinic.model.Appointment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentFormController {
    @FXML private TextField patientIdField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField; // HH:mm
    @FXML private TextField doctorField;
    @FXML private TextField reasonField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Appointment appointment;
    private boolean saved;

    @FXML
    private void initialize() {
        statusCombo.getItems().addAll("scheduled", "completed", "cancelled");
        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> { saved = false; close(); });
    }

    public void setAppointment(Appointment a) {
        this.appointment = a;
        if (a != null) {
            patientIdField.setText(String.valueOf(a.getPatientId()));
            if (a.getAppointmentTime() != null) {
                datePicker.setValue(a.getAppointmentTime().toLocalDate());
                timeField.setText(a.getAppointmentTime().toLocalTime().toString());
            }
            doctorField.setText(a.getDoctorName());
            reasonField.setText(a.getReason());
            statusCombo.setValue(a.getStatus());
        }
    }

    public boolean isSaved() { return saved; }
    public Appointment getAppointment() { return appointment; }

    private void handleSave() {
        if (appointment == null) appointment = new Appointment();
        appointment.setPatientId(Integer.parseInt(patientIdField.getText().trim()));
        LocalDate d = datePicker.getValue();
        LocalTime t = LocalTime.parse(timeField.getText().trim());
        appointment.setAppointmentTime(LocalDateTime.of(d, t));
        appointment.setDoctorName(doctorField.getText());
        appointment.setReason(reasonField.getText());
        appointment.setStatus(statusCombo.getValue() == null ? "scheduled" : statusCombo.getValue());
        saved = true;
        close();
    }

    private void close() {
        ((Stage) saveBtn.getScene().getWindow()).close();
    }
}
