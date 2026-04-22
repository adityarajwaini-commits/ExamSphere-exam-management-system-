package com.exam.dao;

import java.sql.SQLException;
import java.util.List;
import com.exam.model.Result;

public interface ResultDAO {
    int create(Result result) throws SQLException;

    Result findByAttemptId(int attemptId) throws SQLException;

    List<Result> findByUserId(int userId) throws SQLException;

    List<Result> findByExamId(int examId) throws SQLException;

    List<Result> findTopByExamId(int examId, int limit) throws SQLException;
}
