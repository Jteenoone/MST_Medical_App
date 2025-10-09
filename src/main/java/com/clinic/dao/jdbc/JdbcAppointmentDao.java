package com.clinic.dao.jdbc;

import com.clinic.dao.AppointmentDao;
import com.clinic.db.Database;
import com.clinic.model.Appointment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAppointmentDao implements AppointmentDao {
    @Override
    public int create(Appointment a) {
        String sql = "INSERT INTO appointments(patient_id, appointment_time, doctor_name, reason, status) VALUES(?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getPatientId());
            ps.setTimestamp(2, Timestamp.valueOf(a.getAppointmentTime()));
            ps.setString(3, a.getDoctorName());
            ps.setString(4, a.getReason());
            ps.setString(5, a.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create appointment", ex);
        }
    }

    @Override
    public boolean update(Appointment a) {
        if (a.getId() == null) return false;
        String sql = "UPDATE appointments SET patient_id=?, appointment_time=?, doctor_name=?, reason=?, status=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getPatientId());
            ps.setTimestamp(2, Timestamp.valueOf(a.getAppointmentTime()));
            ps.setString(3, a.getDoctorName());
            ps.setString(4, a.getReason());
            ps.setString(5, a.getStatus());
            ps.setInt(6, a.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update appointment", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM appointments WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete appointment", ex);
        }
    }

    @Override
    public Optional<Appointment> findById(int id) {
        String sql = "SELECT id, patient_id, appointment_time, doctor_name, reason, status FROM appointments WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find appointment", ex);
        }
    }

    @Override
    public List<Appointment> listByDateRange(LocalDateTime from, LocalDateTime to, int limit, int offset) {
        String sql = "SELECT id, patient_id, appointment_time, doctor_name, reason, status FROM appointments " +
                "WHERE appointment_time BETWEEN ? AND ? ORDER BY appointment_time ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            ps.setInt(3, Math.max(offset, 0));
            ps.setInt(4, Math.max(limit, 10));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to list appointments", ex);
        }
    }

    @Override
    public List<Appointment> listByPatient(int patientId, int limit, int offset) {
        String sql = "SELECT id, patient_id, appointment_time, doctor_name, reason, status FROM appointments " +
                "WHERE patient_id = ? ORDER BY appointment_time DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, Math.max(offset, 0));
            ps.setInt(3, Math.max(limit, 10));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to list appointments by patient", ex);
        }
    }

    private Appointment map(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setPatientId(rs.getInt("patient_id"));
        Timestamp ts = rs.getTimestamp("appointment_time");
        if (ts != null) a.setAppointmentTime(ts.toLocalDateTime());
        a.setDoctorName(rs.getString("doctor_name"));
        a.setReason(rs.getString("reason"));
        a.setStatus(rs.getString("status"));
        return a;
    }
}
