package com.clinic.controllers;

import com.clinic.dao.AppointmentDao;
import com.clinic.dao.jdbc.JdbcAppointmentDao;
import com.clinic.model.Appointment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AppointmentsController {
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Button filterButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, Number> idCol;
    @FXML private TableColumn<Appointment, Number> patientIdCol;
    @FXML private TableColumn<Appointment, String> timeCol;
    @FXML private TableColumn<Appointment, String> doctorCol;
    @FXML private TableColumn<Appointment, String> reasonCol;
    @FXML private TableColumn<Appointment, String> statusCol;

    private final AppointmentDao appointmentDao = new JdbcAppointmentDao();
    private final ObservableList<Appointment> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        appointmentsTable.setItems(data);
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        patientIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPatientId()));
        timeCol.setCellValueFactory(c -> {
            LocalDateTime t = c.getValue().getAppointmentTime();
            String s = t == null ? "" : t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(s);
        });
        doctorCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDoctorName()));
        reasonCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getReason()));
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));

        filterButton.setOnAction(e -> reload());
        addButton.setOnAction(e -> openCreateDialog());
        editButton.setOnAction(e -> openEditDialog());
        deleteButton.setOnAction(e -> deleteSelected());
        fromDate.setValue(LocalDate.now());
        toDate.setValue(LocalDate.now().plusDays(7));
        reload();
    }

    private void reload() {
        data.clear();
        LocalDateTime from = LocalDateTime.of(fromDate.getValue(), LocalTime.MIN);
        LocalDateTime to = LocalDateTime.of(toDate.getValue(), LocalTime.MAX);
        data.addAll(appointmentDao.listByDateRange(from, to, 200, 0));
    }

    private void openCreateDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AppointmentForm.fxml"));
            Parent root = loader.load();
            AppointmentFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Thêm lịch hẹn");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                Appointment a = controller.getAppointment();
                int id = appointmentDao.create(a);
                a.setId(id);
                data.add(0, a);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openEditDialog() {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AppointmentForm.fxml"));
            Parent root = loader.load();
            AppointmentFormController controller = loader.getController();
            controller.setAppointment(new Appointment(selected.getId(), selected.getPatientId(), selected.getAppointmentTime(), selected.getDoctorName(), selected.getReason(), selected.getStatus()));

            Stage stage = new Stage();
            stage.setTitle("Sửa lịch hẹn");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                Appointment updated = controller.getAppointment();
                updated.setId(selected.getId());
                if (appointmentDao.update(updated)) {
                    int idx = data.indexOf(selected);
                    data.set(idx, updated);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteSelected() {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        boolean ok = com.clinic.util.Dialogs.confirm("Xóa", "Xóa lịch hẹn ID: " + selected.getId() + "?");
        if (!ok) return;
        if (appointmentDao.delete(selected.getId())) {
            data.remove(selected);
        }
    }
}
