package org.example.mst_medical_app.controller.patient;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.mst_medical_app.model.Doctor;
import org.example.mst_medical_app.service.DoctorService;

public class DoctorsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> specializationFilter;
    @FXML private FlowPane doctorContainer;

    private final DoctorService doctorService = new DoctorService();

    @FXML
    public void initialize() {
        // --- Bộ lọc ---
        specializationFilter.getItems().addAll("All", "Dermatology", "Surgery", "Aesthetic", "Cardiology");
        specializationFilter.setValue("All");

        // --- Load dữ liệu ---
        loadDoctors();

        // --- Bắt sự kiện lọc ---
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadDoctors());
        specializationFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadDoctors());
    }

    /**
     * Tải danh sách bác sĩ từ CSDL (qua DoctorService)
     */
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

    /**
     * Tạo card hiển thị thông tin bác sĩ
     */
    private void addDoctorCard(Doctor doctor) {
        Label nameLabel = new Label(doctor.getFullName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label specLabel = new Label("🩺 " + doctor.getSpecialization());
        specLabel.setStyle("-fx-text-fill: #555;");

        Label emailLabel = new Label("📧 " + doctor.getEmail());
        emailLabel.setStyle("-fx-text-fill: #666;");

        Label phoneLabel = new Label("📞 " + (doctor.getPhone() != null ? doctor.getPhone() : "—"));
        phoneLabel.setStyle("-fx-text-fill: #666;");

        Label expLabel = new Label("⏱ " + doctor.getExperienceYears() + " năm kinh nghiệm");
        expLabel.setStyle("-fx-text-fill: #444;");

        Label licenseLabel = new Label("🔖 " + doctor.getLicenseNumber());
        licenseLabel.setStyle("-fx-text-fill: #444;");

        Button bookBtn = new Button("📅 Book Appointment");
        bookBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-padding: 6 12;
        """);

        // Nếu bác sĩ bận hoặc đang nghỉ thì disable
        if (!"Available".equalsIgnoreCase(doctor.getStatus())) {
            bookBtn.setDisable(true);
            bookBtn.setStyle("""
                -fx-background-color: gray;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-opacity: 0.7;
            """);
        }

        // Xử lý khi nhấn "Book Appointment"
        bookBtn.setOnAction(e -> handleBookAppointment(doctor));

        VBox card = new VBox(6, nameLabel, specLabel, emailLabel, phoneLabel, expLabel, licenseLabel, bookBtn);
        card.setPadding(new Insets(15));
        card.setPrefSize(230, 200);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-border-color: #e0e0e0;
            -fx-border-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 3);
        """);

        // Hiệu ứng hover
        card.setOnMouseEntered(e ->
                card.setStyle(card.getStyle() + "-fx-scale-x:1.03; -fx-scale-y:1.03; -fx-cursor: hand;"));
        card.setOnMouseExited(e ->
                card.setStyle(card.getStyle().replace("-fx-scale-x:1.03; -fx-scale-y:1.03; -fx-cursor: hand;", "")));

        doctorContainer.getChildren().add(card);
    }

    /**
     * Xử lý đặt lịch hẹn
     */
    private void handleBookAppointment(Doctor doctor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đặt lịch hẹn");
        confirm.setHeaderText("Bạn có muốn đặt lịch với bác sĩ " + doctor.getFullName() + "?");
        confirm.setContentText("Chuyên khoa: " + doctor.getSpecialization() +
                "\nKinh nghiệm: " + doctor.getExperienceYears() + " năm\nEmail: " + doctor.getEmail());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // TODO: Gọi BookingService để lưu lịch hẹn vào DB
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Lịch hẹn với " + doctor.getFullName() + " đã được ghi nhận!");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
