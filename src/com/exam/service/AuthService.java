package com.exam.service;

import com.exam.model.User;
import java.sql.SQLException;
import java.util.List;

public interface AuthService {
    User loginStudent(String email, String password) throws SQLException;

    User loginAdmin(String email, String password) throws SQLException;

    int registerStudent(User user) throws SQLException;

    boolean ensureDefaultAdminUser() throws SQLException;

    List<User> getAllStudents() throws SQLException;
}
