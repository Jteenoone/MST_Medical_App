package org.example.mst_medical_app.controller.admin;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.mst_medical_app.model.Patient;
import org.example.mst_medical_app.service.AppointmentService;
import org.example.mst_medical_app.service.ReportService;
import org.example.mst_medical_app.service.PatientService;
import java.util.stream.Collectors;

/**
 * Controller cho Admin Dashboard
 */
public class AdminDashboardController {

    @FXML private Label patientCountLabel, prescriptionCountLabel, appointmentCountLabel, revenueLabel;
    @FXML private LineChart<String, Number> patientLineChart;
    @FXML private PieChart patientPieChart;
    @FXML private TableView<Patient> recentPatientTable;
    @FXML private TableColumn<Patient, String> nameColumn, genderColumn, doctorColumn, addressColumn, dobColumn;

    private ReportService reportService;
    private PatientService patientService;
    private AppointmentService appointmentService;

    @FXML
    public void initialize() {
        // 1. Khởi tạo Service
        this.reportService = new ReportService();
        this.patientService = new PatientService();
        this.appointmentService = new AppointmentService();

        // 2. Tải dữ liệu
        loadKpis();
        loadPatientLineChart();
        loadPatientPieChart();
        loadRecentPatientTable();
    }

    /**
     * Tải các KPI từ ReportService
     */
    private void loadKpis() {
        int[] kpiCounts = reportService.getDashboardKpiCounts();

        patientCountLabel.setText(String.valueOf(kpiCounts[0]));
        prescriptionCountLabel.setText(String.valueOf(kpiCounts[1]));
        appointmentCountLabel.setText(String.valueOf(kpiCounts[2]));
        revenueLabel.setText("58.000.000đ");
    }

    /**
     * Tải biểu đồ Line (thống kê bệnh nhân)
     */
    private void loadPatientLineChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Patients 2025");
        series.getData().add(new XYChart.Data<>("Jan", 120));
        series.getData().add(new XYChart.Data<>("Feb", 135));
        series.getData().add(new XYChart.Data<>("Mar", 150));
        series.getData().add(new XYChart.Data<>("Apr", 160));
        patientLineChart.getData().add(series);
    }

    /**
     * Tải biểu đồ Pie (giới tính bệnh nhân)
     */
    private void loadPatientPieChart() {
        ObservableList<PieChart.Data> pieData = reportService.getPatientGenderDashboard();
        patientPieChart.setData(pieData);
    }

    /**
     * Tải bảng Bệnh nhân gần đây
     */
    private void loadRecentPatientTable() {
        nameColumn.setCellValueFactory(data -> data.getValue().fullNameProperty());
        genderColumn.setCellValueFactory(data -> data.getValue().genderProperty());
        addressColumn.setCellValueFactory(data -> data.getValue().addressProperty());
        dobColumn.setCellValueFactory(data -> {
            if (data.getValue().getDateOfBirth() != null)
                return new ReadOnlyStringWrapper(data.getValue().getDateOfBirth().toString());
            else
                return new ReadOnlyStringWrapper("-");
        });
        doctorColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper("N/A"));

        ObservableList<Patient> allPatients = patientService.getAllPatients();
        ObservableList<Patient> recentPatients = FXCollections.observableArrayList();

        if (allPatients != null) {
            recentPatients.addAll(allPatients.stream().limit(5).collect(Collectors.toList()));
        }

        recentPatientTable.setItems(recentPatients);
    }
}
