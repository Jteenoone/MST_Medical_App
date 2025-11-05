package org.example.mst_medical_app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.core.utils.SceneManager;
import org.example.mst_medical_app.core.utils.UserSession;
import org.example.mst_medical_app.model.UserModel;
import org.example.mst_medical_app.service.UserService;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messengerLabel;

    // Gọi Service (nơi xử lý logic đăng nhập)
    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showMessage("Vui lòng nhập đầy đủ thông tin.", "red");
            return;
        }

        // Gọi Service để xử lý đăng nhập
        UserModel user = userService.login(usernameOrEmail, password);

        if (user == null) {
            showMessage("Sai tài khoản hoặc mật khẩu!", "red");
            return;
        }

        // Đăng nhập thành công
        AuthManager.login(user);
        UserSession.setUser(user);

        showMessage("Đăng nhập thành công (" + user.getRole() + ")", "green");

        // Chuyển scene theo vai trò
        switch (user.getRole().toUpperCase()) {
            case "ADMIN" -> SceneManager.switchScene("/org/example/mst_medical_app/MainLayouts.fxml", "Admin Dashboard");
            case "DOCTOR" -> SceneManager.switchScene("/org/example/mst_medical_app/MainLayouts.fxml", "Doctor Dashboard");
            case "PATIENT" -> SceneManager.switchScene("/org/example/mst_medical_app/MainLayouts.fxml", "Patient Dashboard");
            default -> showMessage("Role không hợp lệ!", "red");
        }
    }

    private void showMessage(String message, String color) {
        messengerLabel.setText(message);
        messengerLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    @FXML
    private void handleSignUp() {
        SceneManager.switchScene("/org/example/mst_medical_app/SignUpView.fxml", "Sign Up");
    }
}
