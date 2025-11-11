package org.example.mst_medical_app.controller.admin;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.mst_medical_app.model.Doctor;
import org.example.mst_medical_app.service.DoctorService;

import java.util.Optional;

public class DoctorsController {

    @FXML private TextField searchField;
    @FXML private Button addDoctorBtn;
    @FXML private ComboBox<String> specializationFilter;
    @FXML private FlowPane doctorContainer;

    private final DoctorService doctorService = new DoctorService();

    @FXML
    public void initialize() {
        specializationFilter.getItems().addAll("All", "Dermatology", "Surgery", "Aesthetic", "Cardiology");
        specializationFilter.setValue("All");

        loadDoctors();

        // Gán sự kiện bộ lọc
        searchField.textProperty().addListener((obs, o, n) -> loadDoctors());
        specializationFilter.valueProperty().addListener((obs, o, n) -> loadDoctors());

        addDoctorBtn.setOnAction(e -> handleAddDoctor());
    }

    // Tải danh sách bác sỹ từ DB
    private void loadDoctors() {
        doctorContainer.getChildren().clear();
        ObservableList<Doctor> doctors = doctorService.searchDoctors(
                searchField.getText(),
                specializationFilter.getValue()
        );

        for (Doctor doctor : doctors) {
            addDoctorCard(doctor);
        }
    }

    // Tạo card thông tin bác sỹ
    private void addDoctorCard(Doctor doctor) {
        Label nameLabel = new Label(doctor.getFullName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label idLabel = new Label("ID: " + doctor.getDoctorId() + " • " + doctor.getSpecialization());
        idLabel.setStyle("-fx-text-fill: #555;");

        Label emailLabel = new Label("📧 " + doctor.getEmail());
        emailLabel.setStyle("-fx-text-fill: #666;");

        Label phoneLabel = new Label("📞 " + doctor.getPhone());
        phoneLabel.setStyle("-fx-text-fill: #666;");

        Label expLabel = new Label("🩺 Kinh nghiệm: " + doctor.getExperienceYears() + " năm");
        expLabel.setStyle("-fx-text-fill: #444;");

        Label licenseLabel = new Label("🔖 Mã hành nghề: " + doctor.getLicenseNumber());
        licenseLabel.setStyle("-fx-text-fill: #444;");

        // Nút sửa & xóa
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color:#3B82F6; -fx-text-fill:white; -fx-background-radius:6;");
        editBtn.setOnAction(e -> handleEditDoctor(doctor));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color:#EF4444; -fx-text-fill:white; -fx-background-radius:6;");
        deleteBtn.setOnAction(e -> handleDeleteDoctor(doctor));

        HBox btnBox = new HBox(6, editBtn, deleteBtn);
        btnBox.setPadding(new Insets(6, 0, 0, 0));

        VBox card = new VBox(5, nameLabel, idLabel, emailLabel, phoneLabel, expLabel, licenseLabel, btnBox);
        card.setPadding(new Insets(15));
        card.setPrefSize(250, 200);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-border-color: #e0e0e0;
            -fx-border-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 3);
            """);

        // Hiệu ứng hover
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() +
                "-fx-scale-x:1.03; -fx-scale-y:1.03; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace("-fx-scale-x:1.03; -fx-scale-y:1.03; -fx-cursor: hand;", "")));

        doctorContainer.getChildren().add(card);
    }

   // Thêm bác sỹ mới
    private void handleAddDoctor() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Thêm bác sĩ mới");
        dialog.setHeaderText("Nhập thông tin bác sĩ (userId - chuyên khoa - năm kinh nghiệm - mã hành nghề)");
        dialog.setContentText("Ví dụ: 5 - Cardiology - 8 - ABC12345");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            String[] parts = input.split("-");
            if (parts.length < 4) {
                showAlert(Alert.AlertType.ERROR, "Sai định dạng!", "Vui lòng nhập đủ 4 phần.");
                return;
            }

            try {
                int userId = Integer.parseInt(parts[0].trim());
                String specialization = parts[1].trim();
                int exp = Integer.parseInt(parts[2].trim());
                String license = parts[3].trim();

                String err = doctorService.addNewDoctor(userId, specialization, exp, license);
                if (err == null) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm bác sĩ mới!");
                    loadDoctors();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", err);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Sai định dạng!", "userId và năm kinh nghiệm phải là số.");
            }
        });
    }

    // Chỉnh sửa bác Sỹ
    private void handleEditDoctor(Doctor doctor) {
        TextInputDialog dialog = new TextInputDialog(
                doctor.getSpecialization() + " - " + doctor.getExperienceYears() + " - " + doctor.getLicenseNumber()
        );
        dialog.setTitle("Chỉnh sửa bác sĩ");
        dialog.setHeaderText("Nhập thông tin mới (chuyên khoa - năm kinh nghiệm - mã hành nghề)");
        dialog.setContentText("Ví dụ: Surgery - 10 - XYZ9876");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            String[] parts = input.split("-");
            if (parts.length < 3) {
                showAlert(Alert.AlertType.ERROR, "Sai định dạng!", "Vui lòng nhập đủ 3 phần.");
                return;
            }

            try {
                String spec = parts[0].trim();
                int exp = Integer.parseInt(parts[1].trim());
                String license = parts[2].trim();

                String err = doctorService.updateDoctor(doctor, spec, exp, license);
                if (err == null) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin bác sĩ!");
                    loadDoctors();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", err);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Sai định dạng!", "Năm kinh nghiệm phải là số.");
            }
        });
    }

    // Xóa bác sĩ
    private void handleDeleteDoctor(Doctor doctor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa bác sĩ");
        confirm.setHeaderText("Bạn có chắc muốn xóa " + doctor.getFullName() + "?");
        confirm.setContentText("Hành động này không thể hoàn tác.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean success = doctorService.deleteDoctor(doctor);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa bác sĩ!");
                loadDoctors();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa bác sĩ!");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
