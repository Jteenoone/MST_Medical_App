package org.example.mst_medical_app.controller.patient;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import org.example.mst_medical_app.controller.MainLayoutController;
import org.example.mst_medical_app.model.Appointment;
import org.example.mst_medical_app.service.AppointmentService;

import java.time.format.DateTimeFormatter;


public class PatientDashboardController {

    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colDate, colTime, colDoctor, colStatus;
    @FXML private LineChart<String, Number> healthLineChart;
    @FXML private Label kpiAppointments, kpiDoctors, kpiMessages, kpiTips;
    @FXML private Button viewAllAppointmentsBtn;

    private MainLayoutController mainLayoutController;
    private final AppointmentService appointmentService = new AppointmentService();

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    @FXML
    public void initialize() {
        loadKPIs();
        loadAppointments();
        loadHealthChart();

        // Gắn sự kiện cho nút xem tất cả cuộc hẹn
        viewAllAppointmentsBtn.setOnAction(e -> {
            if (mainLayoutController != null) {
                mainLayoutController.setContent("/org/example/mst_medical_app/admin/Appointments_Calendar_View.fxml");
            }
        });
    }


    private void loadKPIs() {
        kpiAppointments.setText("3");
        kpiDoctors.setText("4");
        kpiMessages.setText("5");
        kpiTips.setText("12");
    }

    /**
     * Tải danh sách lịch hẹn của bệnh nhân hiện tại
     */
    private void loadAppointments() {
        ObservableList<Appointment> data = javafx.collections.FXCollections.observableArrayList(
                appointmentService.getAppointmentsForCurrentUser()
        );

        // Gán dữ liệu vào cột
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colDate.setCellValueFactory(a ->
                new SimpleStringProperty(a.getValue().getDate().format(dateFormatter))
        );

        colTime.setCellValueFactory(a ->
                new SimpleStringProperty(a.getValue().getStart() + " - " + a.getValue().getEnd())
        );

        colDoctor.setCellValueFactory(a ->
                new SimpleStringProperty(a.getValue().getDoctor())
        );

        colStatus.setCellValueFactory(a ->
                new SimpleStringProperty(a.getValue().getStatus().name())
        );

        appointmentTable.setItems(data);
    }


    private void loadHealthChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Chỉ số sức khỏe");

        series.getData().add(new XYChart.Data<>("Jan", 75));
        series.getData().add(new XYChart.Data<>("Feb", 78));
        series.getData().add(new XYChart.Data<>("Mar", 82));
        series.getData().add(new XYChart.Data<>("Apr", 80));
        series.getData().add(new XYChart.Data<>("May", 85));

        healthLineChart.getData().add(series);
    }
}
