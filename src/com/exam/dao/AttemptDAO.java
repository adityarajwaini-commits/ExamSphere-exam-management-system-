package com.exam.dao;

import java.sql.SQLException;
import java.util.List;
import com.exam.model.Attempt;

public interface AttemptDAO {
    int create(Attempt attempt) throws SQLException;

    Attempt findById(int attemptId) throws SQLException;

    Attempt findActiveAttempt(int userId, int examId) throws SQLException;

    List<Attempt> findByUserId(int userId) throws SQLException;

    List<Attempt> findByExamId(int examId) throws SQLException;

    boolean update(Attempt attempt) throws SQLException;

    boolean updateStatus(int attemptId, String status) throws SQLException;
}
