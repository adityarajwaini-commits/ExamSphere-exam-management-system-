package com.exam.dao.impl;

import com.exam.dao.ResultDAO;
import com.exam.model.Result;
import com.exam.util.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResultDAOImpl implements ResultDAO {
    @Override
    public int create(Result result) throws SQLException {
        String sql = "INSERT INTO results (attempt_id, user_id, exam_id, total_questions, correct_count, "
                + "wrong_count, skipped_count, score, percentage, time_taken_seconds) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, result.getAttemptId());
            ps.setInt(2, result.getUserId());
            ps.setInt(3, result.getExamId());
            ps.setInt(4, result.getTotalQuestions());
            ps.setInt(5, result.getCorrectCount());
            ps.setInt(6, result.getWrongCount());
            ps.setInt(7, result.getSkippedCount());
            ps.setInt(8, result.getScore());
            ps.setBigDecimal(9, result.getPercentage());
            ps.setInt(10, result.getTimeTakenSeconds());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public Result findByAttemptId(int attemptId) throws SQLException {
        String sql = "SELECT * FROM results WHERE attempt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attemptId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Result> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM results WHERE user_id = ? ORDER BY submitted_at DESC";
        List<Result> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    @Override
    public List<Result> findByExamId(int examId) throws SQLException {
        String sql = "SELECT r.*, u.full_name FROM results r "
                + "JOIN users u ON r.user_id = u.user_id "
                + "WHERE r.exam_id = ? ORDER BY r.score DESC, r.time_taken_seconds ASC";
        List<Result> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    @Override
    public List<Result> findTopByExamId(int examId, int limit) throws SQLException {
        String sql = "SELECT r.*, u.full_name FROM results r "
                + "JOIN users u ON r.user_id = u.user_id "
                + "WHERE r.exam_id = ? ORDER BY r.score DESC, r.time_taken_seconds ASC "
                + "LIMIT ?";
        List<Result> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    private Result mapRow(ResultSet rs) throws SQLException {
        int resultId = rs.getInt("result_id");
        int attemptId = rs.getInt("attempt_id");
        int userId = rs.getInt("user_id");
        int examId = rs.getInt("exam_id");
        int totalQuestions = rs.getInt("total_questions");
        int correct = rs.getInt("correct_count");
        int wrong = rs.getInt("wrong_count");
        int skipped = rs.getInt("skipped_count");
        int score = rs.getInt("score");
        BigDecimal percentage = rs.getBigDecimal("percentage");
        int timeTakenSeconds = rs.getInt("time_taken_seconds");
        LocalDateTime submittedAt = toLocalDateTime(rs.getTimestamp("submitted_at"));

        Result res = new Result(resultId, attemptId, userId, examId, totalQuestions, correct,
                wrong, skipped, score, percentage, timeTakenSeconds, submittedAt);
        
        try {
            res.setStudentName(rs.getString("full_name"));
        } catch (SQLException ignore) {}
        
        return res;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
