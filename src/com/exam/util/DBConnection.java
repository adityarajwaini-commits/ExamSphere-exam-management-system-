package com.exam.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private DBConnection() {
    }

    private static volatile boolean driverAvailable;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverAvailable = true;
        } catch (ClassNotFoundException ex) {
            driverAvailable = false;
            SimpleLogger.log("WARN", "MySQL JDBC driver not found in classpath: " + ex.getMessage());
        }
    }

    public static boolean isDriverAvailable() {
        return driverAvailable;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                AppConfig.DB_URL,
                AppConfig.DB_USER,
                AppConfig.DB_PASSWORD
        );
    }
}
