package org.example.mst_medical_app.model;

import org.example.mst_medical_app.core.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    public static class LoginData {
        private final UserModel userModel;
        private final String hashedPassword;

        public LoginData(UserModel userModel, String hashedPassword) {
            this.userModel = userModel;
            this.hashedPassword = hashedPassword;
        }

        public UserModel getUserModel() { return userModel; }
        public String getHashedPassword() { return hashedPassword; }
    }


     //Lấy dữ liệu đăng nhập chỉ bằng USERNAME.

    public LoginData getUserDataForLogin(String username) {
        String sql = """
            SELECT u.user_id, u.username, u.full_name, u.email, u.phone_number, 
                   r.role_name, u.password_hash
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE u.username = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToLoginData(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

     // Lấy dữ liệu đăng nhập bằng EMAIL.
    public LoginData getUserDataByEmail(String email) {
        String sql = """
            SELECT u.user_id, u.username, u.full_name, u.email, u.phone_number, 
                   r.role_name, u.password_hash
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE u.email = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToLoginData(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Hỗ trợ khởi tạo nhanh

    private LoginData mapRowToLoginData(ResultSet rs) throws SQLException {
        UserModel user = new UserModel(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("role_name").toUpperCase()
        );
        String hashedPasswordFromDb = rs.getString("password_hash");
        return new LoginData(user, hashedPasswordFromDb);
    }

    // Cập nhật thông tin hồ sơ (Tên, Email, SĐT)
    public boolean updateProfile(UserModel user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone_number = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setInt(4, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật thông tin người dùng: " + e.getMessage());
            return false;
        }
    }


     // Đăng ký
    public boolean createUser(String username, String hashedPassword, String fullName, String email, int roleId) {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, role_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, fullName);
            stmt.setString(4, email);
            stmt.setInt(5, roleId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Lỗi CSDL khi tạo user: " + ex.getMessage());
            return false;
        }
    }

    // Kiểm tra xem username đã tồn tại hay chưa
    public boolean findByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra email tồn tại hay chưa
    public boolean findByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Cập nhật mật khẩu mới cho user
    public boolean updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newHashedPassword);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi đổi mật khẩu: " + e.getMessage());
            return false;
        }
    }
}