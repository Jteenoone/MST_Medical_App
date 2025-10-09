package com.clinic.db;

public final class DatabaseConfig {
    private DatabaseConfig() {}

    public static String getHost() {
        String value = System.getenv("DB_HOST");
        return value != null && !value.isBlank() ? value : "localhost";
    }

    public static int getPort() {
        String value = System.getenv("DB_PORT");
        try {
            return value != null ? Integer.parseInt(value) : 1433;
        } catch (NumberFormatException ex) {
            return 1433;
        }
    }

    public static String getDatabase() {
        String value = System.getenv("DB_NAME");
        return value != null && !value.isBlank() ? value : "clinic_db";
    }

    public static String getUser() {
        String value = System.getenv("DB_USER");
        return value != null && !value.isBlank() ? value : "sa";
    }

    public static String getPassword() {
        String value = System.getenv("DB_PASSWORD");
        return value != null ? value : "YourStrong!Passw0rd";
    }

    public static String getUrl() {
        // Trust server cert to simplify local dev; adjust for production
        return String.format(
            "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=true;trustServerCertificate=true;applicationName=ClinicApp",
            getHost(), getPort(), getDatabase()
        );
    }
}
