package com.exam.dao.impl;

import com.exam.dao.ExamDAO;
import com.exam.model.Exam;
import com.exam.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamDAOImpl implements ExamDAO {
    @Override
    public int create(Exam exam) throws SQLException {
        String sql = "INSERT INTO exams (title, duration_minutes, total_questions, pass_percentage, "
                + "randomize_questions, created_by) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, exam.getTitle());
            ps.setInt(2, exam.getDurationMinutes());
            ps.setInt(3, exam.getTotalQuestions());
            ps.setInt(4, exam.getPassPercentage());
            ps.setBoolean(5, exam.isRandomizeQuestions());
            ps.setInt(6, exam.getCreatedBy());
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
    public Exam findById(int examId) throws SQLException {
        String sql = "SELECT * FROM exams WHERE exam_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Exam> findAll() throws SQLException {
        String sql = "SELECT * FROM exams ORDER BY created_at DESC";
        List<Exam> exams = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                exams.add(mapRow(rs));
            }
        }
        return exams;
    }

    @Override
    public boolean update(Exam exam) throws SQLException {
        String sql = "UPDATE exams SET title = ?, duration_minutes = ?, total_questions = ?, "
                + "pass_percentage = ?, randomize_questions = ? WHERE exam_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, exam.getTitle());
            ps.setInt(2, exam.getDurationMinutes());
            ps.setInt(3, exam.getTotalQuestions());
            ps.setInt(4, exam.getPassPercentage());
            ps.setBoolean(5, exam.isRandomizeQuestions());
            ps.setInt(6, exam.getExamId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int examId) throws SQLException {
        String sql = "DELETE FROM exams WHERE exam_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            return ps.executeUpdate() > 0;
        }
    }

    private Exam mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("exam_id");
        String title = rs.getString("title");
        int durationMinutes = rs.getInt("duration_minutes");
        int totalQuestions = rs.getInt("total_questions");
        int passPercentage = rs.getInt("pass_percentage");
        boolean randomizeQuestions = rs.getBoolean("randomize_questions");
        int createdBy = rs.getInt("created_by");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

        return new Exam(id, title, durationMinutes, totalQuestions, passPercentage,
                randomizeQuestions, createdBy, createdAt);
    }
}
