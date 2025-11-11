package org.example.mst_medical_app.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import org.example.mst_medical_app.core.database.DatabaseConnection;
import org.example.mst_medical_app.model.ReportRepository;

/**
 * Cung cấp dữ liệu thống kê cho Dashboard và Trang Báo cáo.
 */
public class ReportService {

    private final ReportRepository repository;

    public ReportService() {
        this.repository = new ReportRepository();
    }

    /**
     * CHỨC NĂNG: Dữ liệu dashboard Admin (Phần KPI)
     */
    public int[] getDashboardKpiCounts() {
        return repository.getDashboardKpiCounts();
    }

    /**
     * CHỨC NĂNG: Sinh báo cáo thống kê (Trạng thái Lịch hẹn)
     */
    public ObservableList<PieChart.Data> getAppointmentStatusReport() {
        return repository.getAppointmentStatusStats();
    }

    /**
     * CHỨC NĂNG: Sinh báo cáo thống kê (Lịch hẹn theo tháng)
     */
    public XYChart.Series<String, Number> getMonthlyAppointmentReport() {
        return repository.getMonthlyAppointmentStats();
    }

    /**
     * CHỨC NĂNG: Dữ liệu dashboard Admin (Biểu đồ Giới tính)
     */
    public ObservableList<PieChart.Data> getPatientGenderDashboard() {
        return repository.getPatientGenderStats();
    }

    // ====================== DOCTOR DASHBOARD SECTION ======================

    /** Số lịch hẹn hôm nay của bác sĩ */
    public int countTodayAppointments(int doctorId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND DATE(appointment_time) = CURDATE()";
        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    /** Số bệnh nhân mới trong tuần của bác sĩ */
    public int countNewPatientsThisWeek(int doctorId) {
        String sql = """
        SELECT COUNT(DISTINCT patient_id)
        FROM appointments
        WHERE doctor_id = ?
          AND YEARWEEK(appointment_time, 1) = YEARWEEK(CURDATE(), 1)
    """;
        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

// Số lịch đã hẹn trong tuần
    public int countCompletedAppointments(int doctorId) {
        String sql = """
        SELECT COUNT(*) FROM appointments
        WHERE doctor_id = ? AND status = 'COMPLETED'
          AND YEARWEEK(appointment_time, 1) = YEARWEEK(CURDATE(), 1)
    """;
        try (var conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    /** Giả lập tỷ lệ hài lòng (sau này có thể lấy từ bảng feedbacks) */
    public int getDoctorSatisfactionRate(int doctorId) {
        // tạm thời giả định = 90 + random(0~10)
        return 90 + (int)(Math.random() * 10);
    }

    /** Biểu đồ giới tính bệnh nhân của bác sĩ */
    public ObservableList<PieChart.Data> getPatientGenderStatsForDoctor(int doctorId) {
        return repository.getDoctorPatientGenderStats(doctorId);
    }

    /** Xu hướng sức khỏe (demo dữ liệu LineChart) */
    public XYChart.Series<String, Number> getHealthTrendForDoctor(int doctorId) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Patient Recovery Trend");
        series.getData().add(new XYChart.Data<>("Jun", 70));
        series.getData().add(new XYChart.Data<>("Jul", 74));
        series.getData().add(new XYChart.Data<>("Aug", 78));
        series.getData().add(new XYChart.Data<>("Sep", 81));
        series.getData().add(new XYChart.Data<>("Oct", 85));
        series.getData().add(new XYChart.Data<>("Nov", 89));
        return series;
    }

    /** Loại lịch hẹn (Routine / Examination) - demo dữ liệu */
    public ObservableList<XYChart.Series<String, Number>> getAppointmentTypeStats(int doctorId) {
        XYChart.Series<String, Number> routine = new XYChart.Series<>();
        routine.setName("Routine");
        routine.getData().add(new XYChart.Data<>("Apr", 12));
        routine.getData().add(new XYChart.Data<>("May", 14));
        routine.getData().add(new XYChart.Data<>("Jun", 18));
        routine.getData().add(new XYChart.Data<>("Jul", 15));

        XYChart.Series<String, Number> exam = new XYChart.Series<>();
        exam.setName("Examination");
        exam.getData().add(new XYChart.Data<>("Apr", 10));
        exam.getData().add(new XYChart.Data<>("May", 12));
        exam.getData().add(new XYChart.Data<>("Jun", 8));
        exam.getData().add(new XYChart.Data<>("Jul", 9));

        return FXCollections.observableArrayList(routine, exam);
    }


}