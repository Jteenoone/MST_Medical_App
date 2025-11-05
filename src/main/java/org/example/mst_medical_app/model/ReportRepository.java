package org.example.mst_medical_app.model;

import org.example.mst_medical_app.core.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Repository xử lý truy vấn thống kê cho Dashboard.
 */
public class ReportRepository {

    /**
     * Lấy 3 con số KPI chính cho Dashboard:
     * [0] = số bệnh nhân, [1] = số bác sĩ, [2] = số lịch hẹn
     */
    public int[] getDashboardKpiCounts() {
        int[] counts = new int[3];

        String sqlPatients = "SELECT COUNT(*) FROM users u JOIN roles r ON u.role_id = r.role_id WHERE r.role_name = 'PATIENT'";
        String sqlDoctors = "SELECT COUNT(*) FROM users u JOIN roles r ON u.role_id = r.role_id WHERE r.role_name = 'DOCTOR'";
        String sqlAppointments = "SELECT COUNT(*) FROM appointments";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psPatients = conn.prepareStatement(sqlPatients);
             PreparedStatement psDoctors = conn.prepareStatement(sqlDoctors);
             PreparedStatement psAppointments = conn.prepareStatement(sqlAppointments)) {

            try (ResultSet rs = psPatients.executeQuery()) {
                if (rs.next()) counts[0] = rs.getInt(1);
            }

            try (ResultSet rs = psDoctors.executeQuery()) {
                if (rs.next()) counts[1] = rs.getInt(1);
            }

            try (ResultSet rs = psAppointments.executeQuery()) {
                if (rs.next()) counts[2] = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return counts;
    }

    /**
     * Lấy dữ liệu thống kê trạng thái Lịch hẹn (PieChart)
     */
    public ObservableList<PieChart.Data> getAppointmentStatusStats() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        String sql = "SELECT status, COUNT(*) AS count FROM appointments GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                if (status != null)
                    data.add(new PieChart.Data(status, count));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Lấy dữ liệu thống kê Lịch hẹn theo tháng (LineChart)
     */
    public XYChart.Series<String, Number> getMonthlyAppointmentStats() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Lịch hẹn theo tháng");

        String sql = """
            SELECT MONTHNAME(appointment_time) AS month, COUNT(*) AS count
            FROM appointments
            WHERE YEAR(appointment_time) = YEAR(CURDATE())
            GROUP BY MONTH(appointment_time), MONTHNAME(appointment_time)
            ORDER BY MONTH(appointment_time)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        rs.getString("month"),
                        rs.getInt("count")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return series;
    }

    /**
     * Lấy dữ liệu thống kê giới tính Bệnh nhân (PieChart)
     * Hiển thị “Nam”, “Nữ”, “Khác” rõ ràng hơn.
     */
    public ObservableList<PieChart.Data> getPatientGenderStats() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

        // ✅ Truy vấn bảng patients có cột gender
        String sql = """
            SELECT 
                CASE 
                    WHEN LOWER(gender) IN ('male', 'nam') THEN 'Nam'
                    WHEN LOWER(gender) IN ('female', 'nữ', 'nu') THEN 'Nữ'
                    ELSE 'Khác'
                END AS gender_label,
                COUNT(*) AS count
            FROM patients
            WHERE gender IS NOT NULL AND gender <> ''
            GROUP BY gender_label
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String gender = rs.getString("gender_label");
                int count = rs.getInt("count");
                data.add(new PieChart.Data(gender, count));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
    /** Biểu đồ giới tính bệnh nhân của bác sĩ */
    public ObservableList<PieChart.Data> getDoctorPatientGenderStats(int doctorId) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

        String sql = """
        SELECT 
            CASE 
                WHEN LOWER(p.gender) IN ('male', 'nam') THEN 'Nam'
                WHEN LOWER(p.gender) IN ('female', 'nữ', 'nu') THEN 'Nữ'
                ELSE 'Khác'
            END AS gender_label,
            COUNT(*) AS count
        FROM patients p
        JOIN appointments a ON p.patient_id = a.patient_id
        WHERE a.doctor_id = ?
        GROUP BY gender_label
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String gender = rs.getString("gender_label");
                int count = rs.getInt("count");
                data.add(new PieChart.Data(gender, count));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

}
