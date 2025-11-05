package org.example.mst_medical_app.model;

import org.example.mst_medical_app.core.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý CSDL cho các cài đặt
 */
public class SettingRepository {

    /**
     * Tải tất cả cài đặt của một người dùng
     */
    public Map<String, String> loadSettingsForUser(int userId) {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM user_preferences WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return settings;
    }

    /**
     * Lưu một cài đặt
     */
    public boolean saveSettingForUser(int userId, String key, String value) {
        // Câu lệnh "UPSERT" cho MySQL
        String sql = """
            INSERT INTO user_preferences (user_id, setting_key, setting_value)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE setting_value = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, key);
            ps.setString(3, value);
            ps.setString(4, value);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}