package org.example.mst_medical_app.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.mst_medical_app.model.Patient;
import org.example.mst_medical_app.service.PatientService;

import java.time.LocalDate;

public class EditPatientDialogController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderBox;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> statusBox;

    private final PatientService patientService = new PatientService();
    private Patient patient;

    public void setPatient(Patient patient) {
        this.patient = patient;
        fullNameField.setText(patient.getFullName());
        emailField.setText(patient.getEmail());
        phoneField.setText(patient.getPhone());
        dobPicker.setValue(patient.getDateOfBirth());
        genderBox.setValue(patient.getGender());
        addressField.setText(patient.getAddress());
    }

    @FXML
    public void initialize() {
        genderBox.getItems().addAll("Male", "Female", "Other");
        statusBox.getItems().addAll("Active", "Inactive");
    }

    @FXML
    private void onSave() {
        try {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            LocalDate dob = dobPicker.getValue();
            String gender = genderBox.getValue();
            String address = addressField.getText().trim();
            String status = statusBox.getValue();

            if (fullName.isEmpty() || dob == null || gender == null || address.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Thiếu thông tin", "Vui lòng điền đủ các trường bắt buộc!");
                return;
            }

            String err = patientService.updatePatientFull(patient, fullName, email, phone, dob, gender, address);
            if (err == null) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin bệnh nhân thành công!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", err);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi cập nhật thông tin.");
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
