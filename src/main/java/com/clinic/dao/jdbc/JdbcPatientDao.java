package com.clinic.dao.jdbc;

import com.clinic.dao.PatientDao;
import com.clinic.db.Database;
import com.clinic.model.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPatientDao implements PatientDao {
    @Override
    public int create(Patient patient) {
        String sql = "INSERT INTO patients(full_name, date_of_birth, gender, phone, address) VALUES(?,?,?,?,?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getFullName());
            if (patient.getDateOfBirth() != null) {
                ps.setDate(2, Date.valueOf(patient.getDateOfBirth()));
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setString(5, patient.getAddress());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create patient", ex);
        }
    }

    @Override
    public boolean update(Patient patient) {
        if (patient.getId() == null) return false;
        String sql = "UPDATE patients SET full_name=?, date_of_birth=?, gender=?, phone=?, address=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getFullName());
            if (patient.getDateOfBirth() != null) {
                ps.setDate(2, Date.valueOf(patient.getDateOfBirth()));
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setString(5, patient.getAddress());
            ps.setInt(6, patient.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update patient", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM patients WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete patient", ex);
        }
    }

    @Override
    public Optional<Patient> findById(int id) {
        String sql = "SELECT id, full_name, date_of_birth, gender, phone, address, created_at FROM patients WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPatient(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find patient", ex);
        }
    }

    @Override
    public List<Patient> search(String keyword, int limit, int offset) {
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = "SELECT id, full_name, date_of_birth, gender, phone, address, created_at FROM patients " +
                "WHERE full_name LIKE ? OR phone LIKE ? ORDER BY created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Patient> results = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setInt(3, Math.max(offset, 0));
            ps.setInt(4, Math.max(limit, 10));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapPatient(rs));
                }
            }
            return results;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to search patients", ex);
        }
    }

    private Patient mapPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setFullName(rs.getString("full_name"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            p.setDateOfBirth(dob.toLocalDate());
        }
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setAddress(rs.getString("address"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            p.setCreatedAt(created.toLocalDateTime());
        }
        return p;
    }
}
