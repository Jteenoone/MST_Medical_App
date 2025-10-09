package com.clinic.controllers;

import com.clinic.dao.PatientDao;
import com.clinic.dao.jdbc.JdbcPatientDao;
import com.clinic.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class PatientsController {
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, Number> idCol;
    @FXML private TableColumn<Patient, String> nameCol;
    @FXML private TableColumn<Patient, String> dobCol;
    @FXML private TableColumn<Patient, String> genderCol;
    @FXML private TableColumn<Patient, String> phoneCol;
    @FXML private TableColumn<Patient, String> addressCol;

    private final PatientDao patientDao = new JdbcPatientDao();
    private final ObservableList<Patient> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        patientsTable.setItems(data);
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFullName()));
        dobCol.setCellValueFactory(c -> {
            if (c.getValue().getDateOfBirth() == null) return new javafx.beans.property.SimpleStringProperty("");
            String formatted = c.getValue().getDateOfBirth().format(DateTimeFormatter.ISO_DATE);
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
        genderCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getGender()));
        phoneCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));
        addressCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAddress()));

        searchButton.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());
        addButton.setOnAction(e -> openCreateDialog());
        editButton.setOnAction(e -> openEditDialog());
        deleteButton.setOnAction(e -> deleteSelected());

        performSearch();
    }

    private void performSearch() {
        data.clear();
        String keyword = searchField.getText();
        data.addAll(patientDao.search(keyword, 100, 0));
    }

    private void openCreateDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PatientForm.fxml"));
            Parent root = loader.load();
            PatientFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Thêm bệnh nhân");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                Patient p = controller.getPatient();
                int id = patientDao.create(p);
                p.setId(id);
                data.add(0, p);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openEditDialog() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PatientForm.fxml"));
            Parent root = loader.load();
            PatientFormController controller = loader.getController();
            controller.setPatient(new Patient(selected.getId(), selected.getFullName(), selected.getDateOfBirth(), selected.getGender(), selected.getPhone(), selected.getAddress(), selected.getCreatedAt()));

            Stage stage = new Stage();
            stage.setTitle("Sửa bệnh nhân");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                Patient updated = controller.getPatient();
                updated.setId(selected.getId());
                if (patientDao.update(updated)) {
                    int idx = data.indexOf(selected);
                    data.set(idx, updated);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteSelected() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        boolean ok = com.clinic.util.Dialogs.confirm("Xóa", "Xóa bệnh nhân: " + selected.getFullName() + "?");
        if (!ok) return;
        if (patientDao.delete(selected.getId())) {
            data.remove(selected);
        }
    }
}
