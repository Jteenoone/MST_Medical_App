package com.clinic.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static volatile boolean driverLoaded = false;

    private Database() {}

    private static void ensureDriverLoaded() {
        if (driverLoaded) {
            return;
        }
        synchronized (Database.class) {
            if (!driverLoaded) {
                try {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                } catch (ClassNotFoundException ignored) {
                    // Modern JDBC drivers load automatically; ignore if not present
                }
                driverLoaded = true;
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        ensureDriverLoaded();
        String url = DatabaseConfig.getUrl();
        String user = DatabaseConfig.getUser();
        String password = DatabaseConfig.getPassword();
        return DriverManager.getConnection(url, user, password);
    }
}
