package org.example.mst_medical_app.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.mst_medical_app.service.DoctorService;

public class AddDoctorDialogController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> specializationBox;
    @FXML private TextField expField;
    @FXML private TextField licenseField;

    private final DoctorService doctorService = new DoctorService();

    @FXML
    public void initialize() {
        specializationBox.getItems().addAll(
                "Dermatology", "Surgery", "Cardiology", "Aesthetic"
        );
    }

    @FXML
    private void onSave() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String specialization = specializationBox.getValue();
        String expText = expField.getText().trim();
        String license = licenseField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() ||
                specialization == null || expText.isEmpty() || license.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Required", "All fields are required!");
            return;
        }

        try {
            int exp = Integer.parseInt(expText);

            int newUserId = doctorService.createDoctorAccount(username, password, fullName);
            if (newUserId <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot create user account.");
                return;
            }

            String err = doctorService.addNewDoctor(newUserId, specialization, exp, license);
            if (err == null) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Doctor added successfully!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", err);
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Experience must be a number.");
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
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
