package org.example.mst_medical_app.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.mst_medical_app.model.Appointment;
import org.example.mst_medical_app.service.AppointmentService;
import org.example.mst_medical_app.core.security.AuthManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Controller cho lịch hẹn dạng Calendar (hiển thị theo vai trò: Admin / Doctor / Patient)
 */
public class AppointmentCalendarController {

    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;

    private YearMonth currentMonth;

    @FXML
    public void initialize() {
        currentMonth = YearMonth.now();
        renderMonth();
    }

    /**
     * Tạo lịch và hiển thị các cuộc hẹn tương ứng với vai trò người dùng
     */
    private void renderMonth() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Header tháng
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()).toUpperCase()
                + " " + currentMonth.getYear());

        // Header ngày trong tuần
        String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        for (int c = 0; c < 7; c++) {
            Label h = new Label(days[c]);
            h.setStyle("-fx-font-weight:bold; -fx-text-fill:#6b7280;");
            HBox wrap = new HBox(h);
            wrap.setAlignment(Pos.CENTER);
            calendarGrid.add(wrap, c, 0);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100 / 7.0);
            calendarGrid.getColumnConstraints().add(cc);
        }

        // Hàng cho các tuần
        for (int r = 1; r <= 6; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100 / 6.0);
            calendarGrid.getRowConstraints().add(rc);
        }

        // Tạo từng ô ngày trong lịch
        LocalDate first = currentMonth.atDay(1);
        int firstDayOfWeek = (first.getDayOfWeek().getValue() + 6) % 7; // Đổi chủ nhật về cuối
        LocalDate cursor = first.minusDays(firstDayOfWeek);

        for (int r = 1; r <= 6; r++) {
            for (int c = 0; c < 7; c++) {
                VBox cell = createDayCell(cursor, cursor.getMonth().equals(currentMonth.getMonth()));
                calendarGrid.add(cell, c, r);
                cursor = cursor.plusDays(1);
            }
        }

        // ===========================================
        // 🧠 LOAD LỊCH HẸN THEO ROLE NGƯỜI DÙNG
        // ===========================================
        AppointmentService appointmentService = new AppointmentService();
        List<Appointment> appointments = appointmentService.getAppointmentsForCurrentUser();

        System.out.println(">>> Current user: " + AuthManager.getCurUser().getUsername()
                + " (" + AuthManager.getCurUser().getRole() + ")");
        System.out.println(">>> Appointments loaded: " + appointments.size());

        // Lọc theo tháng hiện tại
        appointments.stream()
                .filter(a -> YearMonth.from(a.getAppointmentTime().toLocalDate()).equals(currentMonth))
                .forEach(this::addEventToCalendar);
    }

    /**
     * Tạo một ô trong lịch (một ngày)
     */
    private VBox createDayCell(LocalDate date, boolean inMonth) {
        VBox cell = new VBox(6);
        cell.setPadding(new Insets(10));
        cell.setStyle("-fx-background-color: white; -fx-border-color:#e5e7eb; -fx-border-radius:10;");
        if (!inMonth) cell.setOpacity(0.45);

        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.setStyle("-fx-font-weight:bold; -fx-text-fill:#111827;");
        cell.getChildren().add(dayNum);
        return cell;
    }

    /**
     * Thêm "pill" sự kiện lịch hẹn vào đúng ô ngày
     */
    private void addEventToCalendar(Appointment appt) {
        LocalDate apptDate = appt.getAppointmentTime().toLocalDate();

        LocalDate first = currentMonth.atDay(1);
        int firstDayOfWeek = (first.getDayOfWeek().getValue() + 6) % 7;
        LocalDate cursor = first.minusDays(firstDayOfWeek);

        int targetIndex = (int) java.time.temporal.ChronoUnit.DAYS.between(cursor, apptDate);
        int row = targetIndex / 7 + 1;
        int col = targetIndex % 7;

        HBox pill = new HBox(6);
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setPadding(new Insets(4, 8, 4, 8));

        // 🎨 Màu hiển thị theo trạng thái
        String color = switch (appt.getStatus()) {
            case PENDING -> "#facc15";
            case CONFIRMED -> "#3b82f6";
            case COMPLETED -> "#10b981";
            case CANCELED -> "#ef4444";
        };
        pill.setStyle("-fx-background-radius:10; -fx-background-color:" + color + ";");
        pill.setOnMouseClicked(e -> openEventPopup(appt));

        // 📋 Tên hiển thị trên pill
        String title;
        if (AuthManager.isDoctor()) {
            // Nếu là bác sĩ → hiển thị tên bệnh nhân
            title = appt.getPatientName() != null ? appt.getPatientName() : "Appointment";
        } else if (AuthManager.isPatient()) {
            // Nếu là bệnh nhân → hiển thị tên bác sĩ
            title = appt.getDoctorName() != null ? appt.getDoctorName() : "Appointment";
        } else {
            // Admin → mặc định
            title = (appt.getDoctorName() != null ? appt.getDoctorName() : "Appointment");
        }

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        pill.getChildren().add(titleLbl);

        // Gắn vào đúng ô ngày trong Grid
        Optional<javafx.scene.Node> cell = calendarGrid.getChildren().stream()
                .filter(n -> GridPane.getColumnIndex(n) == col && GridPane.getRowIndex(n) == row)
                .findFirst();

        cell.ifPresent(n -> ((VBox) n).getChildren().add(pill));
    }

    private void openEventPopup(Appointment appt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/mst_medical_app/admin/Appointment_Event_Popup.fxml"
            ));
            Parent root = loader.load();

            // Lấy controller của popup và truyền dữ liệu vào
            org.example.mst_medical_app.controller.admin.AppointmentEventPopupController controller =
                    loader.getController();
            controller.bind(appt);

            Stage stage = new Stage();
            stage.setTitle("Appointment Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
