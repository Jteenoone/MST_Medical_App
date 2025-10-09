package com.clinic.dao.jdbc;

import com.clinic.dao.MedicalRecordDao;
import com.clinic.db.Database;
import com.clinic.model.MedicalRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMedicalRecordDao implements MedicalRecordDao {
    @Override
    public int create(MedicalRecord r) {
        String sql = "INSERT INTO medical_records(patient_id, visit_date, diagnosis, prescription, notes) VALUES(?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getPatientId());
            ps.setDate(2, Date.valueOf(r.getVisitDate()));
            ps.setString(3, r.getDiagnosis());
            ps.setString(4, r.getPrescription());
            ps.setString(5, r.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create medical record", ex);
        }
    }

    @Override
    public boolean update(MedicalRecord r) {
        if (r.getId() == null) return false;
        String sql = "UPDATE medical_records SET patient_id=?, visit_date=?, diagnosis=?, prescription=?, notes=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getPatientId());
            ps.setDate(2, Date.valueOf(r.getVisitDate()));
            ps.setString(3, r.getDiagnosis());
            ps.setString(4, r.getPrescription());
            ps.setString(5, r.getNotes());
            ps.setInt(6, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update medical record", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM medical_records WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete medical record", ex);
        }
    }

    @Override
    public Optional<MedicalRecord> findById(int id) {
        String sql = "SELECT id, patient_id, visit_date, diagnosis, prescription, notes FROM medical_records WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find medical record", ex);
        }
    }

    @Override
    public List<MedicalRecord> listByPatient(int patientId, int limit, int offset) {
        String sql = "SELECT id, patient_id, visit_date, diagnosis, prescription, notes FROM medical_records " +
                "WHERE patient_id = ? ORDER BY visit_date DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return list(sql, ps -> {
            ps.setInt(1, patientId);
            ps.setInt(2, Math.max(offset, 0));
            ps.setInt(3, Math.max(limit, 10));
        });
    }

    @Override
    public List<MedicalRecord> listByDate(LocalDate from, LocalDate to, int limit, int offset) {
        String sql = "SELECT id, patient_id, visit_date, diagnosis, prescription, notes FROM medical_records " +
                "WHERE visit_date BETWEEN ? AND ? ORDER BY visit_date DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return list(sql, ps -> {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ps.setInt(3, Math.max(offset, 0));
            ps.setInt(4, Math.max(limit, 10));
        });
    }

    private List<MedicalRecord> list(String sql, SqlConsumer<PreparedStatement> binder) {
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to list medical records", ex);
        }
    }

    private MedicalRecord map(ResultSet rs) throws SQLException {
        MedicalRecord r = new MedicalRecord();
        r.setId(rs.getInt("id"));
        r.setPatientId(rs.getInt("patient_id"));
        Date d = rs.getDate("visit_date");
        if (d != null) r.setVisitDate(d.toLocalDate());
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setPrescription(rs.getString("prescription"));
        r.setNotes(rs.getString("notes"));
        return r;
    }

    @FunctionalInterface
    private interface SqlConsumer<T> { void accept(T t) throws SQLException; }
}
