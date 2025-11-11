package org.example.mst_medical_app.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.mst_medical_app.core.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repository quản lý truy vấn CSDL liên quan đến Bệnh nhân.
 * Cấu trúc bảng:
 *  - patients(patient_id, user_id, gender, date_of_birth, address)
 *  - users(user_id, full_name, email, phone_number)
 *  - patient_doctor(patient_id, doctor_id, assigned_date)
 */
public class PatientRepository {

    /** 🧩 Helper: Chuyển ResultSet → Patient object */
    private Patient mapRowToPatient(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("patient_id"),
                rs.getString("full_name"),
                rs.getString("gender"),
                rs.getDate("date_of_birth") != null
                        ? rs.getDate("date_of_birth").toLocalDate()
                        : null,
                rs.getString("address")
        );
    }

    /** 🩺 Lấy tất cả bệnh nhân (dành cho Admin) */
    public ObservableList<Patient> findAllPatients() {
        ObservableList<Patient> list = FXCollections.observableArrayList();
        String sql = """
            SELECT p.patient_id, u.full_name, p.gender, p.date_of_birth, p.address
            FROM patients p
            JOIN users u ON p.user_id = u.user_id
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRowToPatient(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /** Lấy danh sách bệnh nhân được gán cho 1 bác sĩ */
    public ObservableList<Patient> findPatientsByDoctorId(int doctorId) {
        ObservableList<Patient> list = FXCollections.observableArrayList();
        String sql = """
        SELECT DISTINCT 
            p.patient_id, u.full_name, p.gender, p.date_of_birth, p.address
        FROM patients p
        JOIN users u ON p.user_id = u.user_id
        JOIN appointments a ON p.patient_id = a.patient_id
        WHERE a.doctor_id = ?
          AND (a.status = 'CONFIRMED' or a.status = 'COMPLETED')
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToPatient(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }



    /** 🩺 Thêm bệnh nhân mới */
    public boolean createPatient(int userId, String gender, String address, java.time.LocalDate dob) {
        String sql = "INSERT INTO patients (user_id, gender, address, date_of_birth) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, gender);
            ps.setString(3, address);
            if (dob != null)
                ps.setDate(4, java.sql.Date.valueOf(dob));
            else
                ps.setNull(4, java.sql.Types.DATE);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 🩺 Cập nhật thông tin bệnh nhân */
    public boolean updatePatient(Patient patient) {
        String sql = """
            UPDATE patients
            SET gender = ?, address = ?, date_of_birth = ?
            WHERE patient_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getGender());
            ps.setString(2, patient.getAddress());
            if (patient.getDateOfBirth() != null)
                ps.setDate(3, java.sql.Date.valueOf(patient.getDateOfBirth()));
            else
                ps.setNull(3, java.sql.Types.DATE);
            ps.setInt(4, patient.getPatientId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 🩺 Gán bác sĩ ↔ bệnh nhân */
    public boolean assignDoctorToPatient(int patientId, int doctorId) {
        String sql = """
            INSERT IGNORE INTO patient_doctor (patient_id, doctor_id, assigned_date)
            VALUES (?, ?, CURRENT_TIMESTAMP)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 🩺 Gỡ liên kết bác sĩ ↔ bệnh nhân */
    public boolean removeDoctorFromPatient(int patientId, int doctorId) {
        String sql = "DELETE FROM patient_doctor WHERE patient_id = ? AND doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 🩺 Xóa bệnh nhân */
    public boolean deletePatient(int patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
