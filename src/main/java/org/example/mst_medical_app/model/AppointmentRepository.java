package org.example.mst_medical_app.model;

import org.example.mst_medical_app.core.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository cho bảng appointments trong database medical_app
 */
public class AppointmentRepository {

    private Appointment mapRowToAppointment(ResultSet rs) throws SQLException {
        Appointment appt = new Appointment(
                rs.getInt("appointment_id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getTimestamp("appointment_time").toLocalDateTime(),
                Appointment.Status.valueOf(rs.getString("status").toUpperCase()),
                rs.getString("notes")
        );
        try {
            appt.setDoctorName(rs.getString("doctor_name"));
            appt.setPatientName(rs.getString("patient_name"));
        } catch (SQLException ignored) {}
        return appt;
    }

    /** Lấy tất cả lịch hẹn (Admin xem toàn bộ) */
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, 
                   u1.full_name AS patient_name,
                   u2.full_name AS doctor_name
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN users u1 ON p.user_id = u1.user_id
            JOIN doctors d ON a.doctor_id = d.doctor_id
            JOIN users u2 ON d.user_id = u2.user_id
            ORDER BY a.appointment_time DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRowToAppointment(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Lấy lịch hẹn của bác sĩ */
    public List<Appointment> findByDoctorId(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, u1.full_name AS patient_name
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN users u1 ON p.user_id = u1.user_id
            WHERE a.doctor_id = ?
            ORDER BY a.appointment_time DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRowToAppointment(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Lấy lịch hẹn của bệnh nhân */
    public List<Appointment> findByPatientId(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, u2.full_name AS doctor_name
            FROM appointments a
            JOIN doctors d ON a.doctor_id = d.doctor_id
            JOIN users u2 ON d.user_id = u2.user_id
            WHERE a.patient_id = ?
            ORDER BY a.appointment_time DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRowToAppointment(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Tạo lịch hẹn mới */
    public boolean create(Appointment appt) {
        String sql = """
            INSERT INTO appointments (patient_id, doctor_id, appointment_time, status, notes)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appt.getPatientId());
            ps.setInt(2, appt.getDoctorId());
            ps.setTimestamp(3, Timestamp.valueOf(appt.getAppointmentTime()));
            ps.setString(4, appt.getStatus().toString());
            ps.setString(5, appt.getNotes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Cập nhật trạng thái */
    public boolean updateStatus(int appointmentId, Appointment.Status newStatus) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.toString());
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Lấy tất cả lịch hẹn của người dùng hiện tại (doctor hoặc patient) */
    public static List<Appointment> loadForCurrentUser() {
        return new AppointmentRepository().findAll();
    }
    /** Lấy danh sách lịch hẹn sắp tới (thời gian >= hiện tại) cho bác sĩ */
    public List<Appointment> findByDoctorIdAndFuture(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, u1.full_name AS patient_name
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN users u1 ON p.user_id = u1.user_id
            WHERE a.doctor_id = ? AND a.appointment_time >= NOW()
            ORDER BY a.appointment_time ASC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAppointment(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Lấy danh sách lịch hẹn đã qua (thời gian < hiện tại) cho bác sĩ */
    public List<Appointment> findByDoctorIdAndPast(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, u1.full_name AS patient_name
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN users u1 ON p.user_id = u1.user_id
            WHERE a.doctor_id = ? AND a.appointment_time < NOW()
            ORDER BY a.appointment_time DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAppointment(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

}
