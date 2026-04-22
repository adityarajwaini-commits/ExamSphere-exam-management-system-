package com.exam.main;

import com.exam.util.DBConnection;
import com.exam.view.MainFrame;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (!DBConnection.isDriverAvailable()) {
                JOptionPane.showMessageDialog(
                        null,
                        "MySQL JDBC driver is missing from classpath.\n"
                        + "Run: powershell -ExecutionPolicy Bypass -File .\\run-app.ps1",
                        "Database Driver Missing",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            try (Connection connection = DBConnection.getConnection()) {
                if (!connection.isValid(2)) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Connected to MySQL but validation failed.",
                            "Database Connection Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Cannot connect to MySQL.\n"
                                + "Check MySQL server, DB credentials in AppConfig, and schema setup.\n\n"
                                + ex.getMessage(),
                        "Database Connection Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
