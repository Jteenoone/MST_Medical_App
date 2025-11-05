package org.example.mst_medical_app.controller.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.mst_medical_app.model.Appointment;
import org.example.mst_medical_app.model.AppointmentRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentsController {

    @FXML private TextField searchField;
    @FXML private Button addAppointmentBtn;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> doctorColumn;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, String> notesColumn;

    private final AppointmentRepository repository = new AppointmentRepository();
    private ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadAppointments();
        setupSearch();
    }

    /**
     * Định nghĩa cột hiển thị trong TableView
     */
    private void setupColumns() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        timeColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getAppointmentTime().format(formatter)
                )
        );
        doctorColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDoctorName() != null ? c.getValue().getDoctorName() : "Unknown"
                )
        );
        patientColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getPatientName() != null ? c.getValue().getPatientName() : "Unknown"
                )
        );
        statusColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getStatus().name()
                )
        );
        notesColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getNotes() != null ? c.getValue().getNotes() : ""
                )
        );
    }

    /**
     * Tải dữ liệu thật từ DB
     */
    private void loadAppointments() {
        List<Appointment> list = repository.findAll();
        allAppointments = FXCollections.observableArrayList(list);
        appointmentTable.setItems(allAppointments);
    }

    /**
     * Bộ lọc tìm kiếm theo tên bác sĩ hoặc bệnh nhân
     */
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();

            List<Appointment> filtered = allAppointments.stream()
                    .filter(a -> a.getDoctorName().toLowerCase().contains(lower)
                            || a.getPatientName().toLowerCase().contains(lower)
                            || a.getStatus().name().toLowerCase().contains(lower))
                    .collect(Collectors.toList());

            appointmentTable.setItems(FXCollections.observableArrayList(filtered));
        });
    }

    /**
     * (Tuỳ chọn) Nút thêm lịch hẹn mới
     * Có thể mở form riêng để tạo Appointment mới.
     */
    @FXML
    private void handleAddAppointment() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chức năng đang phát triển");
        alert.setHeaderText(null);
        alert.setContentText("Tính năng thêm lịch hẹn sẽ được bổ sung sau.");
        alert.showAndWait();
    }
}
