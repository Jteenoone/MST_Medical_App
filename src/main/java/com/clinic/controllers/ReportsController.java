package com.clinic.controllers;

import com.clinic.dao.AppointmentDao;
import com.clinic.dao.PatientDao;
import com.clinic.dao.jdbc.JdbcAppointmentDao;
import com.clinic.dao.jdbc.JdbcPatientDao;
import com.clinic.model.Appointment;
import com.clinic.model.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsController {
    @FXML private Button exportPatientsBtn;
    @FXML private Button exportAppointmentsBtn;
    @FXML private TextArea logArea;

    private final PatientDao patientDao = new JdbcPatientDao();
    private final AppointmentDao appointmentDao = new JdbcAppointmentDao();

    @FXML
    private void initialize() {
        exportPatientsBtn.setOnAction(e -> exportPatients());
        exportAppointmentsBtn.setOnAction(e -> exportAppointments());
    }

    private void exportPatients() {
        List<Patient> list = patientDao.search("", 10_000, 0);
        Path out = Path.of("patients_export.csv");
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("id,full_name,date_of_birth,gender,phone,address\n");
            DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
            for (Patient p : list) {
                String dob = p.getDateOfBirth() == null ? "" : df.format(p.getDateOfBirth());
                w.write(String.format("%d,%s,%s,%s,%s,%s\n",
                        p.getId(), escape(p.getFullName()), dob, escape(p.getGender()),
                        escape(p.getPhone()), escape(p.getAddress())));
            }
            log("Đã xuất: " + out.toAbsolutePath());
        } catch (IOException ex) {
            log("Lỗi xuất CSV: " + ex.getMessage());
        }
    }

    private void exportAppointments() {
        // Simple export of next 1 year appointments
        var from = java.time.LocalDateTime.now().minusYears(1);
        var to = java.time.LocalDateTime.now().plusYears(1);
        List<Appointment> list = appointmentDao.listByDateRange(from, to, 100_000, 0);
        Path out = Path.of("appointments_export.csv");
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("id,patient_id,appointment_time,doctor_name,reason,status\n");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Appointment a : list) {
                String t = a.getAppointmentTime() == null ? "" : df.format(a.getAppointmentTime());
                w.write(String.format("%d,%d,%s,%s,%s,%s\n",
                        a.getId(), a.getPatientId(), t, escape(a.getDoctorName()),
                        escape(a.getReason()), escape(a.getStatus())));
            }
            log("Đã xuất: " + out.toAbsolutePath());
        } catch (IOException ex) {
            log("Lỗi xuất CSV: " + ex.getMessage());
        }
    }

    private void log(String s) {
        if (logArea != null) {
            logArea.appendText(s + "\n");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}
