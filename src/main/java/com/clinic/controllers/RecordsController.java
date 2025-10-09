package com.clinic.controllers;

import com.clinic.dao.MedicalRecordDao;
import com.clinic.dao.jdbc.JdbcMedicalRecordDao;
import com.clinic.model.MedicalRecord;
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
import java.time.format.DateTimeFormatter;

public class RecordsController {
    @FXML private TextField patientIdField;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Button filterButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private TableView<MedicalRecord> recordsTable;
    @FXML private TableColumn<MedicalRecord, Number> idCol;
    @FXML private TableColumn<MedicalRecord, Number> patientIdCol;
    @FXML private TableColumn<MedicalRecord, String> visitDateCol;
    @FXML private TableColumn<MedicalRecord, String> diagnosisCol;
    @FXML private TableColumn<MedicalRecord, String> prescriptionCol;
    @FXML private TableColumn<MedicalRecord, String> notesCol;

    private final MedicalRecordDao recordDao = new JdbcMedicalRecordDao();
    private final ObservableList<MedicalRecord> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        recordsTable.setItems(data);
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        patientIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPatientId()));
        visitDateCol.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getVisitDate();
            String s = d == null ? "" : d.format(DateTimeFormatter.ISO_DATE);
            return new javafx.beans.property.SimpleStringProperty(s);
        });
        diagnosisCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDiagnosis()));
        prescriptionCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrescription()));
        notesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNotes()));

        fromDate.setValue(LocalDate.now().minusMonths(1));
        toDate.setValue(LocalDate.now());
        filterButton.setOnAction(e -> reload());
        addButton.setOnAction(e -> openCreateDialog());
        editButton.setOnAction(e -> openEditDialog());
        deleteButton.setOnAction(e -> deleteSelected());
        reload();
    }

    private void reload() {
        data.clear();
        String pidText = patientIdField.getText();
        if (pidText != null && !pidText.isBlank()) {
            try {
                int pid = Integer.parseInt(pidText.trim());
                data.addAll(recordDao.listByPatient(pid, 200, 0));
                return;
            } catch (NumberFormatException ignored) {}
        }
        data.addAll(recordDao.listByDate(fromDate.getValue(), toDate.getValue(), 200, 0));
    }

    private void openCreateDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RecordForm.fxml"));
            Parent root = loader.load();
            RecordFormController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Thêm bệnh án");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            if (controller.isSaved()) {
                var r = controller.getRecord();
                int id = recordDao.create(r);
                r.setId(id);
                data.add(0, r);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openEditDialog() {
        MedicalRecord selected = recordsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RecordForm.fxml"));
            Parent root = loader.load();
            RecordFormController controller = loader.getController();
            controller.setRecord(new MedicalRecord(selected.getId(), selected.getPatientId(), selected.getVisitDate(), selected.getDiagnosis(), selected.getPrescription(), selected.getNotes()));
            Stage stage = new Stage();
            stage.setTitle("Sửa bệnh án");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            if (controller.isSaved()) {
                var updated = controller.getRecord();
                updated.setId(selected.getId());
                if (recordDao.update(updated)) {
                    int idx = data.indexOf(selected);
                    data.set(idx, updated);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteSelected() {
        MedicalRecord selected = recordsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        boolean ok = com.clinic.util.Dialogs.confirm("Xóa", "Xóa bệnh án ID: " + selected.getId() + "?");
        if (!ok) return;
        if (recordDao.delete(selected.getId())) {
            data.remove(selected);
        }
    }
}
