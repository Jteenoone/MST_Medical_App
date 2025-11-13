package org.example.mst_medical_app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.mst_medical_app.core.utils.SceneManager;
import org.example.mst_medical_app.core.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUpController {

    @FXML private TextField usernameField, emailField, fullNameField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label messageLabel;
    @FXML private Button signupButton;
    @FXML private Hyperlink loginLink;

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("Patient", "Doctor");

        signupButton.setOnAction(e -> handleSignUp());
        loginLink.setOnAction(e -> SceneManager.switchScene("/org/example/mst_medical_app/LoginView.fxml", "Login"));
    }

    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() ) {
            showMessage("Please fill in all fields.", "red");
            return;
        }

        if (!password.equals(confirm)) {
            showMessage("Passwords do not match!", "red");
            return;
        }

//        int roleId = switch (role.toUpperCase()) {
//            case "DOCTOR" -> 2;
//            case "PATIENT" -> 1;
//            default -> 0;
//        };
//
//        if (roleId == 0) {
//            showMessage("Invalid role selected.", "red");
//            return;
//        }
        int roleId = 1;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password_hash, full_name, email, role_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, fullName);
            stmt.setString(4, email);
            stmt.setInt(5, roleId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                showMessage("Account created successfully!", "green");
            } else {
                showMessage("Failed to create account!", "red");
            }

        } catch (SQLException ex) {
            showMessage("Error: " + ex.getMessage(), "red");
        }
    }

    private void showMessage(String text, String color) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
    }
}
