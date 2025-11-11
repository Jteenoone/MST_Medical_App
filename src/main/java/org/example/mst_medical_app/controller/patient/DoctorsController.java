package org.example.mst_medical_app.controller.patient;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.mst_medical_app.core.database.DatabaseConnection;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.core.utils.SceneManager;
import org.example.mst_medical_app.core.utils.UserSession;
import org.example.mst_medical_app.features.chat.ChatDAO;
import org.example.mst_medical_app.model.AppointmentRepository;
import org.example.mst_medical_app.model.Doctor;
import org.example.mst_medical_app.model.chat.Message;
import org.example.mst_medical_app.service.AppointmentService;
import org.example.mst_medical_app.service.ChatService;
import org.example.mst_medical_app.service.DoctorService;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;


public class DoctorsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> specializationFilter;
    @FXML private FlowPane doctorContainer;

    @FXML
    private DatePicker appointmentDatePicker;

    @FXML
    private ComboBox<Integer> hourBox;

    @FXML
    private ComboBox<Integer> minuteBox;

    @FXML
    private TextArea noteField;




    private final AppointmentService appointmentService = new AppointmentService();
    private final DoctorService doctorService = new DoctorService();
    private ChatService chatService = new ChatService();

    private ChatDAO chatDAO;

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            chatDAO = new ChatDAO(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }

        specializationFilter.getItems().addAll("All", "Dermatology", "Surgery", "Aesthetic", "Cardiology");
        specializationFilter.setValue("All");
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


    private void handleBookAppointment(Doctor doctor) {
        try {
            // === 1. Mở popup chọn ngày giờ ===
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Đặt lịch khám");

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/mst_medical_app/patient/BookAppointmentPopup.fxml"
            ));
            dialog.getDialogPane().setContent(loader.load());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {

                    BookAppointmentPopupController popup = loader.getController();
                    LocalDateTime selectedDateTime = popup.getSelectedDateTime();
                    String notes = popup.getNote();

                    if (selectedDateTime == null) {
                        showError("Vui lòng chọn ngày và giờ hẹn!");
                        return;
                    }

                    int patientId = AuthManager.getCurUser().getId();
                    int doctorId = doctor.getDoctorId();  // doctor_id (not user_id)

                    // === 2. Book appointment ===
                    Integer appointmentId = appointmentService.bookAppointment(
                            patientId,
                            doctorId,
                            selectedDateTime,
                            notes
                    );
                    if (appointmentId == null) {
                        showError("⛔ Bác sĩ đã có lịch vào thời điểm này, vui lòng chọn giờ khác.");
                        return;
                    }
                    showSuccess("✅ Đặt lịch thành công! Tin nhắn xác nhận đã gửi cho bác sĩ.");

                    // === 3. Tạo tin nhắn chat có appointmentId ===
                    int doctorUserId = doctor.getUserId();

                    ChatDAO chatDAO;
                    try {
                        chatDAO = new ChatDAO(DatabaseConnection.getConnection());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    int conversationId = chatService.createOrGetConversation(patientId, doctorUserId);

                    String msg = """
                        📅 Bệnh nhân đã đặt lịch khám.
                        • Ngày: %s
                        • Giờ: %s
                        • Ghi chú: %s
                        """.formatted(
                            selectedDateTime.toLocalDate(),
                            selectedDateTime.toLocalTime(),
                            (notes == null || notes.isEmpty()) ? "Không có ghi chú" : notes
                    );

                    // ⭐ Gửi message có appointmentId & status = PENDING
                    Message message = new Message();
                    message.setConversationId(conversationId);
                    message.setSenderId(patientId);
                    message.setContent(msg);
                    message.setAppointmentId(appointmentId);
                    message.setAppointmentStatus("PENDING");

                    try {
                        chatDAO.sendMessage(message);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    // === Mở cửa sổ chat luôn ===
                    try {
                        SceneManager.openChat(conversationId, doctor);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Lỗi khi đặt lịch: " + ex.getMessage());
        }
    }

    /** ✅ đóng popup sau khi đặt lịch */
    private void closeWindow() {
        Stage stage = (Stage) appointmentDatePicker.getScene().getWindow();
        stage.close();
    }


    /** ✅ Thông báo lỗi */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /** ✅ Thông báo thành công */
    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
