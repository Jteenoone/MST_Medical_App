package org.example.mst_medical_app.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.mst_medical_app.core.database.DatabaseConnection;

import java.sql.*;


public class DoctorRepository {
    private Doctor mapRowToDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("doctor_id"),
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("specialization"),
                rs.getInt("experience_years"),
                rs.getString("license_number"),
                rs.getString("phone_number"),
                rs.getString("email")
        );
    }
    public ObservableList<Doctor> findAllDoctors() {
        ObservableList<Doctor> doctors = FXCollections.observableArrayList();

        String sql = """
            SELECT d.doctor_id, d.user_id, u.full_name, u.email, u.phone_number,
                   d.specialization, d.experience_years, d.license_number
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                doctors.add(mapRowToDoctor(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

     // Tìm bác sĩ theo chuyên khoa
    public ObservableList<Doctor> findBySpecialization(String specialization) {
        ObservableList<Doctor> doctors = FXCollections.observableArrayList();
        String sql = """
            SELECT d.doctor_id, d.user_id, u.full_name, u.email, u.phone_number,
                   d.specialization, d.experience_years, d.license_number
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
            WHERE d.specialization = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, specialization);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    doctors.add(mapRowToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    // Thêm bác sĩ mới (cần user_id đã tồn tại)
    public boolean createDoctor(Doctor doctor) {
        String sql = "INSERT INTO doctors (user_id, specialization, experience_years, license_number) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctor.getUserId());
            ps.setString(2, doctor.getSpecialization());
            ps.setInt(3, doctor.getExperienceYears());
            ps.setString(4, doctor.getLicenseNumber());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

     // Cập nhật thông tin bác sĩ
    public boolean updateDoctor(Doctor doctor) {
        String sql = "UPDATE doctors SET specialization = ?, experience_years = ?, license_number = ? WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.getSpecialization());
            ps.setInt(2, doctor.getExperienceYears());
            ps.setString(3, doctor.getLicenseNumber());
            ps.setInt(4, doctor.getDoctorId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa bác sĩ
    public boolean deleteDoctor(int doctorId) {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //  Lấy bác sĩ theo ID
    public ObservableList<Doctor> findByDoctorId(int doctorId) {
        ObservableList<Doctor> doctors = FXCollections.observableArrayList();
        String sql = """
            SELECT d.doctor_id, d.user_id, u.full_name, u.email, u.phone_number,
                   d.specialization, d.experience_years, d.license_number
            FROM doctors d
            JOIN users u ON d.user_id = u.user_id
            WHERE d.doctor_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    doctors.add(mapRowToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    public int createDoctorAccount(String username, String password, String fullName) {
        String sql = """
        INSERT INTO users (username, password_hash, full_name, role_id)
        VALUES (?, ?, ?, '2')
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // trả về user_id vừa được tạo
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // lỗi → trả về -1
    }

    public boolean updateDoctorFull(Doctor doctor) {
        String sql = """
        UPDATE doctors d
        JOIN users u ON d.user_id = u.id
        SET u.full_name = ?, u.email = ?, u.phone_number = ?,
            d.specialization = ?, d.experience_years = ?, 
            d.license_number = ?
        WHERE d.doctor_id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.getFullName());
            ps.setString(2, doctor.getEmail());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getSpecialization());
            ps.setInt(5, doctor.getExperienceYears());
            ps.setString(6, doctor.getLicenseNumber());
            ps.setInt(8, doctor.getDoctorId());

            int updated = ps.executeUpdate();
            return updated > 0 ? true : false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
