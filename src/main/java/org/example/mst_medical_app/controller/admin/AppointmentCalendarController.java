package org.example.mst_medical_app.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.Appointment;
import org.example.mst_medical_app.model.AppointmentRepository;
import org.example.mst_medical_app.service.AppointmentService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AppointmentCalendarController {

    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;

    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private  Button addAppointmentBtn;

    @FXML private ToggleButton dayBtn;
    @FXML private ToggleButton weekBtn;
    @FXML private ToggleButton monthBtn;


    /** Data trạng thái */
    private YearMonth currentMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();

    private enum ViewMode { MONTH, WEEK, DAY }
    private ViewMode currentViewMode = ViewMode.MONTH;

    private final AppointmentRepository repository = new AppointmentRepository();


    @FXML
    public void initialize() {
        updateToggleUI();
        render();
//        addAppointmentBtn.setOnAction(e -> handleAddAppointment());
    }


    // ========= TOGGLE BUTTON (DAY / WEEK / MONTH) =========

    @FXML
    private void switchToMonth() {
        currentViewMode = ViewMode.MONTH;
        currentMonth = YearMonth.from(selectedDate);
        updateToggleUI();
        render();
    }

    @FXML
    private void switchToWeek() {
        currentViewMode = ViewMode.WEEK;
        updateToggleUI();
        render();
    }

    @FXML
    private void switchToDay() {
        currentViewMode = ViewMode.DAY;
        updateToggleUI();
        render();
    }

    private void updateToggleUI() {
        monthBtn.setSelected(currentViewMode == ViewMode.MONTH);
        weekBtn.setSelected(currentViewMode == ViewMode.WEEK);
        dayBtn.setSelected(currentViewMode == ViewMode.DAY);
    }


    // ========= PREVIOUS / NEXT BUTTONS =========

    @FXML
    private void goPrevious() {
        switch (currentViewMode) {
            case MONTH -> currentMonth = currentMonth.minusMonths(1);
            case WEEK -> selectedDate = selectedDate.minusWeeks(1);
            case DAY -> selectedDate = selectedDate.minusDays(1);
        }
        render();
    }

    @FXML
    private void goNext() {
        switch (currentViewMode) {
            case MONTH -> currentMonth = currentMonth.plusMonths(1);
            case WEEK -> selectedDate = selectedDate.plusWeeks(1);
            case DAY -> selectedDate = selectedDate.plusDays(1);
        }
        render();
    }


    // ========= MAIN RENDER =========

    private void render() {
        switch (currentViewMode) {
            case MONTH -> renderMonth();
            case WEEK -> renderWeek();
            case DAY -> renderDay();
        }
    }


    // ========= MONTH VIEW =========

    private void renderMonth() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()).toUpperCase()
                + " " + currentMonth.getYear());

        String[] days = {"MON","TUE","WED","THU","FRI","SAT","SUN"};

        // Header thứ
        for (int c = 0; c < 7; c++) {
            Label label = new Label(days[c]);
            HBox wrap = new HBox(label);
            wrap.setAlignment(Pos.CENTER);
            wrap.setPrefHeight(10);
            calendarGrid.add(wrap, c, 0);

            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100 / 7.0);
            calendarGrid.getColumnConstraints().add(cc);
        }

        // 6 tuần
        for (int r = 1; r <= 6; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(120 / 6.0);
            calendarGrid.getRowConstraints().add(rc);
        }

        LocalDate first = currentMonth.atDay(1);
        int firstDayOfWeek = (first.getDayOfWeek().getValue() + 6) % 7;
        LocalDate cursor = first.minusDays(firstDayOfWeek);

        for (int r = 1; r <= 6; r++) {
            for (int c = 0; c < 7; c++) {
                VBox cell = createDayCell(cursor, cursor.getMonth().equals(currentMonth.getMonth()));
                calendarGrid.add(cell, c, r);
                cursor = cursor.plusDays(1);
            }
        }

        loadAppointments(currentMonth.atDay(1), currentMonth.atEndOfMonth());
    }


    // ========= WEEK VIEW =========

    private void renderWeek() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        LocalDate monday = selectedDate.with(java.time.DayOfWeek.MONDAY);

        monthLabel.setText("WEEK OF " + monday);

        for (int c = 0; c < 7; c++) {
            VBox cell = createDayCell(monday.plusDays(c), true);
            calendarGrid.add(cell, c, 0);

            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100 / 7.0);
            calendarGrid.getColumnConstraints().add(cc);
        }

        loadAppointments(monday, monday.plusDays(6));
    }


    // ========= DAY VIEW =========

    private void renderDay() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        monthLabel.setText("DAY — " + selectedDate);

        ColumnConstraints timeCol = new ColumnConstraints();
        timeCol.setPrefWidth(80);
        calendarGrid.getColumnConstraints().add(timeCol);

        ColumnConstraints contentCol = new ColumnConstraints();
        contentCol.setPercentWidth(100);
        calendarGrid.getColumnConstraints().add(contentCol);

        int startHour = 8;
        int endHour = 18;

        for (int hour = startHour; hour < endHour; hour++) {
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill:#374151;");
            timeLabel.setPadding(new Insets(6));

            calendarGrid.add(timeLabel, 0, hour - startHour);

            VBox cell = new VBox();
            cell.setStyle("-fx-border-color: #e5e7eb; -fx-background-color: white;");
            cell.setPadding(new Insets(6));
            calendarGrid.add(cell, 1, hour - startHour);

            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(60); // chiều cao 1 dòng
            calendarGrid.getRowConstraints().add(rc);
        }

        loadAppointments(selectedDate, selectedDate);
    }



    // ========= COMMON FUNCTIONS =========

    private void loadAppointments(LocalDate start, LocalDate end) {
        AppointmentService service = new AppointmentService();
        List<Appointment> appointments = service.getAppointmentsForCurrentUser();

        appointments.stream()
                .filter(a -> {
                    LocalDate d = a.getAppointmentTime().toLocalDate();
                    return !d.isBefore(start) && !d.isAfter(end);
                })
                .forEach(a -> {
                    switch (currentViewMode) {
                        case DAY -> addEventToDay(a);
                        case WEEK -> addEventToWeek(a, start);
                        case MONTH -> addEventToMonth(a);
                    }
                });
    }


    private VBox createDayCell(LocalDate date, boolean inMonth) {
        VBox cell = new VBox(6);
        cell.setPadding(new Insets(10));
        cell.setStyle("-fx-background-color: white; -fx-border-color:#e5e7eb;");
        if (!inMonth) cell.setOpacity(0.45);

        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.setStyle("-fx-font-weight:bold;");
        cell.getChildren().add(dayNum);
        return cell;
    }


    private HBox createPill(Appointment appt) {
        HBox pill = new HBox();
        pill.setPadding(new Insets(4, 8, 4, 8));
        pill.setAlignment(Pos.CENTER_LEFT);

        String color = switch (appt.getStatus()) {
            case PENDING -> "#facc15";
            case CONFIRMED -> "#3b82f6";
            case COMPLETED -> "#10b981";
            case CANCELED -> "#ef4444";
        };

        pill.setStyle("-fx-background-radius:10; -fx-background-color:" + color);

        String title;
        if(AuthManager.isPatient()) {
            title = appt.getDoctorName() != null ? appt.getDoctorName() : "Appointment";
        }
        else if(AuthManager.isDoctor()) {
            title = appt.getPatientName() != null ? appt.getPatientName() : "Appointment";
        }
        else {
            title = appt.getDoctorName() != null ? appt.getDoctorName() : "Appointment";
        }
        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill:white; -fx-font-weight:bold;");
        pill.getChildren().add(lbl);

        pill.setOnMouseClicked(e -> openPopup(appt));
        return pill;
    }


    private void openPopup(Appointment appt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/mst_medical_app/admin/Appointment_Event_Popup.fxml"
            ));
            Parent root = loader.load();

            AppointmentEventPopupController controller = loader.getController();
            controller.bind(appt);

            Stage stage = new Stage();
            stage.setTitle("Appointment Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addEventToMonth(Appointment appt) {
        LocalDate date = appt.getAppointmentTime().toLocalDate();

        LocalDate first = currentMonth.atDay(1);
        int fd = (first.getDayOfWeek().getValue() + 6) % 7;
        LocalDate cursor = first.minusDays(fd);

        int idx = (int) java.time.temporal.ChronoUnit.DAYS.between(cursor, date);
        int row = idx / 7 + 1;
        int col = idx % 7;

        addToGrid(col, row, createPill(appt));
    }

    private void addEventToWeek(Appointment appt, LocalDate monday) {
        int col = (int) java.time.temporal.ChronoUnit.DAYS.between(monday, appt.getAppointmentTime().toLocalDate());
        addToGrid(col, 0, createPill(appt));
    }

    private void addEventToDay(Appointment appt) {
        int startHour = 8;

        int row = appt.getAppointmentTime().toLocalTime().getHour() - startHour;
        if (row < 0) return;

        Node eventCell = calendarGrid.getChildren().stream()
                .filter(n -> GridPane.getRowIndex(n) == row && GridPane.getColumnIndex(n) == 1)
                .findFirst()
                .orElse(null);

        if (eventCell instanceof VBox vbox) {
            vbox.getChildren().add(createPill(appt));
        }
    }



    private void addToGrid(int col, int row, HBox pill) {
        Optional<javafx.scene.Node> cell = calendarGrid.getChildren().stream()
                .filter(n -> GridPane.getColumnIndex(n) == col && GridPane.getRowIndex(n) == row)
                .findFirst();

        cell.ifPresent(n -> ((Pane) n).getChildren().add(pill));
    }

//    private void handleAddAppointment() {
//        Dialog<ButtonType> dialog = new Dialog<>();
//        dialog.setTitle("Create Appointment");
//        dialog.setHeaderText("Tạo lịch hẹn mới");
//
//        ButtonType saveButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
//        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
//
//        TextField doctorField = new TextField();
//        doctorField.setPromptText("Doctor Name");
//
//        TextField patientField = new TextField();
//        patientField.setPromptText("Patient Name");
//
//        DatePicker datePicker = new DatePicker(selectedDate);
//
//        TextField timeField = new TextField();
//        timeField.setPromptText("HH:mm");
//
//        TextArea notesArea = new TextArea();
//        notesArea.setPromptText("Notes");
//
//        VBox layout = new VBox(10,
//                new Label("Doctor:"), doctorField,
//                new Label("Patient:"), patientField,
//                new Label("Date:"), datePicker,
//                new Label("Time:"), timeField,
//                new Label("Notes:"), notesArea
//        );
//
//        dialog.getDialogPane().setContent(layout);
//
//        Optional<ButtonType> result = dialog.showAndWait();
//
//        if (result.isPresent() && result.get() == saveButton) {
//            try {
//                Appointment appointment = new Appointment();
//                appointment.setDoctorName(doctorField.getText());
//                appointment.setPatientName(patientField.getText());
//                appointment.setAppointmentTime(LocalDateTime.of(
//                        datePicker.getValue(),
//                        LocalTime.parse(timeField.getText()) // "09:30"
//                ));
//                appointment.setStatus(Appointment.Status.PENDING);
//                appointment.setNotes(notesArea.getText());
//
//                repository.save(appointment);
//
//                // Refresh calendar
//                renderCurrentMode();
//
//                new Alert(Alert.AlertType.INFORMATION, "Created successfully.").show();
//
//            } catch (Exception ex) {
//                new Alert(Alert.AlertType.ERROR, "Invalid date/time format!").show();
//            }
//        }
//    }
//    private void renderCurrentMode() {
//        if (dayBtn.isSelected()) renderDay();
//        else if (weekBtn.isSelected()) renderWeek();
//        else renderMonth();
//    }

}
