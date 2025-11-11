package org.example.mst_medical_app.model;

import org.example.mst_medical_app.core.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class AppointmentRepository {

    private Appointment mapRowToAppointment(ResultSet rs) throws SQLException {

        Date sqlDate = rs.getDate("appointment_date");
        Time sqlTime = rs.getTime("appointment_time");
        LocalDateTime appointmentDateTime = null;
        if (sqlDate != null && sqlTime != null) {
            appointmentDateTime = LocalDateTime.of(sqlDate.toLocalDate(), sqlTime.toLocalTime());
        } else {
            appointmentDateTime = LocalDateTime.now();
        }

        Appointment appt = new Appointment(
                rs.getInt("appointment_id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                appointmentDateTime,
                Appointment.Status.valueOf(rs.getString("status").toUpperCase()),
                rs.getString("notes")
        );

        // gán tên doctor/patient nếu có trong ResultSet
        if (hasColumn(rs, "doctor_name"))
            appt.setDoctorName(rs.getString("doctor_name"));

        if (hasColumn(rs, "patient_name"))
            appt.setPatientName(rs.getString("patient_name"));

        return appt;
    }

    // kiểm tra column có tồn tại trong ResultSet hay không
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }


    // Lấy tất cả lịch hẹn(Admin)
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

    // Lấy lịch hẹn của bác sĩ
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

    // Lấy lịch hẹn của bệnh nhân
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

    // Tạo lịch hẹn mới
    public Integer createAppointment(int patientId, int doctorId, LocalDateTime dateTime, String notes) {
        String sql = """
        INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, notes, status)
        VALUES (?, ?, ?, ?, ?, 'PENDING')
    """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);

            ps.setDate(3, java.sql.Date.valueOf(dateTime.toLocalDate()));
            ps.setTime(4, java.sql.Time.valueOf(dateTime.toLocalTime()));

            ps.setString(5, notes);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Cập nhật trạng thái cuộc hẹn
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

    // Lấy tất cả lịch hẹn của người dùng hiện tại (doctor hoặc patient)
    public static List<Appointment> loadForCurrentUser() {
        return new AppointmentRepository().findAll();
    }
    // Lấy danh sách lịch hẹn sắp tới (thời gian >= hiện tại) cho bác sĩ
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

    // Lấy danh sách lịch hẹn đã qua (thời gian < hiện tại) cho bác sĩ
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

    // Kiểm tra trùng lịch hẹn
    public boolean existsAppointmentAt(int doctorId, LocalDateTime dateTime) {
        String sql = """
        SELECT COUNT(*)
        FROM appointments
        WHERE doctor_id = ?
          AND appointment_date = ?
          AND appointment_time = ?
          AND status != 'CANCELED'
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setDate(2, java.sql.Date.valueOf(dateTime.toLocalDate()));
            ps.setTime(3, java.sql.Time.valueOf(dateTime.toLocalTime()));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // lấy thời gian của các lịch đã hẹn
    public List<LocalTime> getBookedTimesForDoctor(int doctorId, LocalDate date) {
        String sql = """
        SELECT appointment_time FROM appointments
        WHERE doctor_id = ? AND appointment_date = ? AND status != 'CANCELED'
    """;
        List<LocalTime> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setDate(2, java.sql.Date.valueOf(date));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getTime("appointment_time").toLocalTime());
            }

        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

}
