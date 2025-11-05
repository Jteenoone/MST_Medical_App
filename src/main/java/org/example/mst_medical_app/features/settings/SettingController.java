package org.example.mst_medical_app.features.settings;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.example.mst_medical_app.controller.MainLayoutController;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.UserModel;
import org.example.mst_medical_app.service.UserService;

import java.io.File;

/**
 * Controller cho phần "Settings" (Hồ sơ cá nhân + Đổi mật khẩu)
 */
public class SettingController {

    private MainLayoutController mainLayoutController;

    @FXML private TextField nameField, emailField, phoneField;
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    @FXML private ImageView avatarImage;
    @FXML private Button saveButton, changeAvatarBtn, togglePasswordSectionBtn, changePasswordBtn;
    @FXML private Label messageLabel;
    @FXML private StackPane passwordSection; // phần popup đổi mật khẩu

    private UserModel currentUser;
    private final UserService userService = new UserService();

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    @FXML
    public void initialize() {
        currentUser = AuthManager.getCurUser();

        if (currentUser == null) {
            messageLabel.setText("⚠ Không thể tải thông tin người dùng!");
            messageLabel.setStyle("-fx-text-fill: red;");
            disableAllInputs();
            return;
        }

        // Gán dữ liệu người dùng
        nameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone());

        // Sự kiện nút
        changeAvatarBtn.setOnAction(e -> handleAvatarChange());
        saveButton.setOnAction(e -> saveProfile());
        togglePasswordSectionBtn.setOnAction(e -> togglePasswordSection());
        changePasswordBtn.setOnAction(e -> handleChangePassword());

        // Ẩn phần đổi mật khẩu khi mới mở
        passwordSection.setVisible(false);
        passwordSection.setManaged(false);
    }

    /**
     * Hiện/ẩn popup đổi mật khẩu (giữa màn hình)
     */
    private void togglePasswordSection() {
        boolean showing = passwordSection.isVisible();

        if (showing) {
            // Ẩn đi
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), passwordSection);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                passwordSection.setVisible(false);
                passwordSection.setManaged(false);
                togglePasswordSectionBtn.setText("🔐 Đổi mật khẩu");
            });
            fadeOut.play();
        } else {
            // Hiện ra (với hiệu ứng trượt + mờ dần)
            passwordSection.setVisible(true);
            passwordSection.setManaged(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), passwordSection);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), passwordSection);
            slideUp.setFromY(30);
            slideUp.setToY(0);

            fadeIn.play();
            slideUp.play();

            togglePasswordSectionBtn.setText("✖ Đóng");
        }
    }

    /** Ngăn chỉnh khi chưa đăng nhập */
    private void disableAllInputs() {
        nameField.setDisable(true);
        emailField.setDisable(true);
        phoneField.setDisable(true);
        saveButton.setDisable(true);
        changeAvatarBtn.setDisable(true);
        togglePasswordSectionBtn.setDisable(true);
    }

    /** Đổi ảnh đại diện */
    private void handleAvatarChange() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            avatarImage.setImage(new Image(file.toURI().toString()));
            messageLabel.setText("✅ Ảnh đã được thay đổi (chưa lưu vào DB)");
            messageLabel.setStyle("-fx-text-fill: #007bff;");
        }
    }

    /** Cập nhật hồ sơ cá nhân */
    private void saveProfile() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        String result = userService.updateProfile(name, email, phone);
        if (result == null) {
            messageLabel.setText("✅ Cập nhật hồ sơ thành công!");
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setText("❌ " + result);
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /** Xử lý đổi mật khẩu */
    private void handleChangePassword() {
        String oldPass = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        String result = userService.changePassword(oldPass, newPass, confirm);
        if (result == null) {
            messageLabel.setText("🔒 Đổi mật khẩu thành công!");
            messageLabel.setStyle("-fx-text-fill: green;");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            togglePasswordSection(); // tự ẩn popup sau khi đổi xong
        } else {
            messageLabel.setText("⚠ " + result);
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
