package com.exam.service.impl;

import com.exam.dao.UserDAO;
import com.exam.model.User;
import com.exam.service.AuthService;
import java.sql.SQLException;
import java.util.List;

public class AuthServiceImpl implements AuthService {
    private final UserDAO userDAO;

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User loginStudent(String email, String password) throws SQLException {
        User user = userDAO.findByEmail(email);
        if (user == null) {
            return null;
        }
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return isPasswordMatch(user, password) ? user : null;
    }

    @Override
    public User loginAdmin(String email, String password) throws SQLException {
        User user = userDAO.findByEmail(email);
        if (user == null) {
            return null;
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return isPasswordMatch(user, password) ? user : null;
    }

    @Override
    public int registerStudent(User user) throws SQLException {
        user.setRole("STUDENT");
        if (user.getStatus() == null || user.getStatus().isEmpty()) {
            user.setStatus("ACTIVE");
        }
        return userDAO.create(user);
    }

    @Override
    public boolean ensureDefaultAdminUser() throws SQLException {
        return userDAO.createDefaultAdminIfAbsent(
                "Admin User",
                "admin@gmail.com",
                "admin123",
                "ADMIN",
                "ACTIVE"
        );
    }

    @Override
    public List<User> getAllStudents() throws SQLException {
        return userDAO.findAllStudents();
    }

    private boolean isPasswordMatch(User user, String password) {
        if (user.getPasswordHash() == null) {
            return false;
        }
        return user.getPasswordHash().equals(password);
    }
}
