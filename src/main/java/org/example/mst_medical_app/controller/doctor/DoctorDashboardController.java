package org.example.mst_medical_app.controller.doctor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.mst_medical_app.controller.MainLayoutController;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.Appointment;
import org.example.mst_medical_app.model.Patient;
import org.example.mst_medical_app.service.AppointmentService;
import org.example.mst_medical_app.service.PatientService;
import org.example.mst_medical_app.service.ReportService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoctorDashboardController {

    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    // Header + KPI
    @FXML private Label welcomeLbl;
    @FXML private Label kpiTodayAppointments, kpiNewPatients, kpiCompletedWeek, kpiSatisfaction;

    // Charts
    @FXML private PieChart patientsPie;
    @FXML private LineChart<String, Number> healthLine;
    @FXML private BarChart<String, Number> overallBar;

    // Right-side lists
    @FXML private ListView<String> upcomingList;
    @FXML private ListView<String> previousList;

    // Patients table
    @FXML private TableView<PatientRow> patientsTable;
    @FXML private TableColumn<PatientRow, String> colPatientName;
    @FXML private TableColumn<PatientRow, String> colGender;
    @FXML private TableColumn<PatientRow, String> colChat;
    @FXML private TableColumn<PatientRow, String> colAction;

    @FXML private Button goPatientsBtn;

    private final PatientService patientService = new PatientService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        int doctorId = AuthManager.getCurUser().getId();

        welcomeLbl.setText("Welcome back, Dr. " + AuthManager.getFullName());

        // KPIs thật từ DB
        int todayAppointments = reportService.countTodayAppointments(doctorId);
        int newPatients = reportService.countNewPatientsThisWeek(doctorId);
        int completedThisWeek = reportService.countCompletedAppointments(doctorId);
        int satisfactionRate = reportService.getDoctorSatisfactionRate(doctorId);

        kpiTodayAppointments.setText(String.valueOf(todayAppointments));
        kpiNewPatients.setText(String.valueOf(newPatients));
        kpiCompletedWeek.setText(String.valueOf(completedThisWeek));
        kpiSatisfaction.setText(satisfactionRate + "%");

        // Dữ liệu biểu đồ + bảng
        setupCharts(doctorId);
        setupRightLists(doctorId);
        setupPatientsTable(doctorId);
    }

    private void setupCharts(int doctorId) {

        // Biểu đồ giới tính
        ObservableList<PieChart.Data> genderData = reportService.getPatientGenderStatsForDoctor(doctorId);
        patientsPie.setData(genderData);

        // Biểu đồ Health (ví dụ theo tháng)
        XYChart.Series<String, Number> health = reportService.getHealthTrendForDoctor(doctorId);
        healthLine.getData().setAll(health);

        // Biểu đồ tổng cuộc hẹn
        ObservableList<XYChart.Series<String, Number>> appointmentStats = reportService.getAppointmentTypeStats(doctorId);
        overallBar.getData().setAll(appointmentStats);
    }

    /* --------- Upcoming + Previous Lists --------- */
    private void setupRightLists(int doctorId) {
        List<Appointment> upcoming = appointmentService.findUpcomingAppointmentsByDoctorId(doctorId);
        List<Appointment> previous = appointmentService.findPreviousAppointmentsByDoctorId(doctorId);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, dd MMM  HH:mm");

        ObservableList<String> upList = FXCollections.observableArrayList();
        for (Appointment a : upcoming) {
            upList.add(a.getDateTime().format(fmt) + " • " + a.getPatientName() + " (" + a.getPurpose() + ")");
        }
        upcomingList.setItems(upList);

        ObservableList<String> prevList = FXCollections.observableArrayList();
        for (Appointment a : previous) {
            prevList.add(a.getDateTime().format(fmt) + " • " + a.getPatientName() + " • " + a.getPurpose());
        }
        previousList.setItems(prevList);
    }

    /* ------------- Patients table ---------------- */
    private void setupPatientsTable(int doctorId) {
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colChat.setCellValueFactory(new PropertyValueFactory<>("chat"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));

        List<Patient> patients = patientService.getPatientsForCurrentDoctor();
        ObservableList<PatientRow> rows = FXCollections.observableArrayList();

        for (Patient p : patients) {
            rows.add(new PatientRow(
                    p.getFullName(),
                    p.getGender(),
                    "Chat",
                    "View"
            ));
        }

        patientsTable.setItems(rows);
        goPatientsBtn.setOnAction(e -> {
            if (mainLayoutController != null) {
                mainLayoutController.setContent("/org/example/mst_medical_app/doctor/DoctorPatients_View.fxml");
            } else {
                System.out.println("MainLayoutController is null!");
            }
        });
    }

    /* ------ Simple model class for table ------ */
    public static class PatientRow {
        private final String name;
        private final String gender;
        private final String chat;
        private final String action;

        public PatientRow(String name, String gender, String chat, String action) {
            this.name = name;
            this.gender = gender;
            this.chat = chat;
            this.action = action;
        }

        public String getName() { return name; }
        public String getGender() { return gender; }
        public String getChat() { return chat; }
        public String getAction() { return action; }
    }
}
