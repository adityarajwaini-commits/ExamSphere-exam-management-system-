package com.exam.controller;

import com.exam.model.User;
import com.exam.service.AuthService;
import com.exam.util.NavigationHelper;
import com.exam.util.SimpleLogger;
import java.sql.SQLException;

public class AuthController {
    private final AuthService authService;
    private final NavigationHelper navigation;
    private String lastError;

    public AuthController(AuthService authService, NavigationHelper navigation) {
        this.authService = authService;
        this.navigation = navigation;
    }

    public User loginStudent(String email, String password) throws SQLException {
        lastError = validateCredentials(email, password);
        if (lastError != null) {
            return null;
        }
        User user = authService.loginStudent(email.trim(), password.trim());
        if (user == null) {
            lastError = "Invalid student credentials.";
            SimpleLogger.log("WARN", "Student login failed: " + email);
        } else {
            SimpleLogger.log("INFO", "Student login success: " + email);
        }
        return user;
    }

    public User loginAdmin(String email, String password) throws SQLException {
        lastError = validateCredentials(email, password);
        if (lastError != null) {
            return null;
        }
        User user = authService.loginAdmin(email.trim(), password.trim());
        if (user == null) {
            lastError = "Invalid admin credentials.";
            SimpleLogger.log("WARN", "Admin login failed: " + email);
        } else {
            SimpleLogger.log("INFO", "Admin login success: " + email);
        }
        return user;
    }

    public int registerStudent(User user) throws SQLException {
        lastError = validateRegistration(user);
        if (lastError != null) {
            return 0;
        }
        int id = authService.registerStudent(user);
        if (id > 0) {
            SimpleLogger.log("INFO", "Student registered: " + user.getEmail());
        }
        return id;
    }

    public void goToView(String viewName) {
        navigation.show(viewName);
    }

    public String getLastError() {
        return lastError;
    }

    private String validateCredentials(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required.";
        }
        if (password == null || password.trim().isEmpty()) {
            return "Password is required.";
        }
        return null;
    }

    private String validateRegistration(User user) {
        if (user == null) {
            return "User data is required.";
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return "Full name is required.";
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return "Email is required.";
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            return "Password is required.";
        }
        return null;
    }
}
