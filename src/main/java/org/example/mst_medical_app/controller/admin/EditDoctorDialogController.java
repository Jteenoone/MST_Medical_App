package org.example.mst_medical_app.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.mst_medical_app.model.Doctor;
import org.example.mst_medical_app.service.DoctorService;

public class EditDoctorDialogController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> specializationBox;
    @FXML private TextField expField;
    @FXML private TextField licenseField;
    @FXML private ComboBox<String> statusBox;

    private final DoctorService doctorService = new DoctorService();
    private Doctor doctor;

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        // Điền dữ liệu hiện tại vào form
        fullNameField.setText(doctor.getFullName());
        emailField.setText(doctor.getEmail());
        phoneField.setText(doctor.getPhone());
        specializationBox.setValue(doctor.getSpecialization());
        expField.setText(String.valueOf(doctor.getExperienceYears()));
        licenseField.setText(doctor.getLicenseNumber());
        statusBox.setValue(doctor.getStatus());
    }

    @FXML
    public void initialize() {
        specializationBox.getItems().addAll("Dermatology", "Surgery", "Aesthetic", "Cardiology");
        statusBox.getItems().addAll("Available", "Busy", "Inactive");
    }

    @FXML
    private void onSave() {
        try {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String spec = specializationBox.getValue();
            int exp = Integer.parseInt(expField.getText().trim());
            String license = licenseField.getText().trim();

            if (fullName.isEmpty() || spec == null || license.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Thiếu thông tin", "Vui lòng điền đầy đủ các trường bắt buộc.");
                return;
            }

            // Gọi service update
            String err = doctorService.updateDoctorFull(doctor, fullName, email, phone, spec, exp, license);
            if (err == null) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin bác sĩ!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", err);
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Sai định dạng", "Số năm kinh nghiệm phải là số.");
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
