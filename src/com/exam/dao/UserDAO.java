package com.exam.dao;

import com.exam.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    int create(User user) throws SQLException;

    User findById(int userId) throws SQLException;

    User findByEmail(String email) throws SQLException;

    List<User> findAllStudents() throws SQLException;

    boolean createDefaultAdminIfAbsent(String fullName, String email, String passwordHash,
                                       String role, String status) throws SQLException;

    boolean update(User user) throws SQLException;

    boolean updateStatus(int userId, String status) throws SQLException;

    boolean delete(int userId) throws SQLException;
}
