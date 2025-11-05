package org.example.mst_medical_app.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/medical_app";
    private static final String USER = "root"; // đổi nếu bạn dùng user khác
    private static final String PASSWORD = "23112005"; // điền mật khẩu MySQL của bạn

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
