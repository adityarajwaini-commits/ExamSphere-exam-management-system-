package com.exam.dao.impl;

import com.exam.dao.AttemptDAO;
import com.exam.model.Attempt;
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

public class AttemptDAOImpl implements AttemptDAO {
    @Override
    public int create(Attempt attempt) throws SQLException {
        String sql = "INSERT INTO attempts (user_id, exam_id, status) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, attempt.getUserId());
            ps.setInt(2, attempt.getExamId());
            ps.setString(3, attempt.getStatus());
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
    public Attempt findById(int attemptId) throws SQLException {
        String sql = "SELECT * FROM attempts WHERE attempt_id = ?";

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
    public Attempt findActiveAttempt(int userId, int examId) throws SQLException {
        String sql = "SELECT * FROM attempts WHERE user_id = ? AND exam_id = ? "
                + "AND status = 'IN_PROGRESS' ORDER BY start_time DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, examId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Attempt> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM attempts WHERE user_id = ? ORDER BY start_time DESC";
        List<Attempt> attempts = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapRow(rs));
                }
            }
        }
        return attempts;
    }

    @Override
    public List<Attempt> findByExamId(int examId) throws SQLException {
        String sql = "SELECT * FROM attempts WHERE exam_id = ? ORDER BY start_time DESC";
        List<Attempt> attempts = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapRow(rs));
                }
            }
        }
        return attempts;
    }

    @Override
    public boolean update(Attempt attempt) throws SQLException {
        String sql = "UPDATE attempts SET start_time = ?, end_time = ?, status = ? WHERE attempt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, toTimestamp(attempt.getStartTime()));
            ps.setTimestamp(2, toTimestamp(attempt.getEndTime()));
            ps.setString(3, attempt.getStatus());
            ps.setInt(4, attempt.getAttemptId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int attemptId, String status) throws SQLException {
        String sql = "UPDATE attempts SET status = ? WHERE attempt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, attemptId);
            return ps.executeUpdate() > 0;
        }
    }

    private Attempt mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("attempt_id");
        int userId = rs.getInt("user_id");
        int examId = rs.getInt("exam_id");
        LocalDateTime startTime = toLocalDateTime(rs.getTimestamp("start_time"));
        LocalDateTime endTime = toLocalDateTime(rs.getTimestamp("end_time"));
        String status = rs.getString("status");

        return new Attempt(id, userId, examId, startTime, endTime, status);
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private Timestamp toTimestamp(LocalDateTime dt) {
        return dt != null ? Timestamp.valueOf(dt) : null;
    }
}
