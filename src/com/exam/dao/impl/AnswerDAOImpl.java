package com.exam.dao.impl;

import com.exam.dao.AnswerDAO;
import com.exam.model.Answer;
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

public class AnswerDAOImpl implements AnswerDAO {
    @Override
    public int create(Answer answer) throws SQLException {
        String sql = "INSERT INTO answers (attempt_id, question_id, selected_option, is_marked_review) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, answer.getAttemptId());
            ps.setInt(2, answer.getQuestionId());
            ps.setString(3, answer.getSelectedOption());
            ps.setBoolean(4, answer.isMarkedReview());
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
    public Answer findById(int answerId) throws SQLException {
        String sql = "SELECT * FROM answers WHERE answer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, answerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Answer findByAttemptAndQuestion(int attemptId, int questionId) throws SQLException {
        String sql = "SELECT * FROM answers WHERE attempt_id = ? AND question_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attemptId);
            ps.setInt(2, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Answer> findByAttemptId(int attemptId) throws SQLException {
        String sql = "SELECT * FROM answers WHERE attempt_id = ? ORDER BY answer_id";
        List<Answer> answers = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attemptId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    answers.add(mapRow(rs));
                }
            }
        }
        return answers;
    }

    @Override
    public boolean update(Answer answer) throws SQLException {
        String sql = "UPDATE answers SET selected_option = ?, is_marked_review = ?, answered_at = ? "
                + "WHERE answer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, answer.getSelectedOption());
            ps.setBoolean(2, answer.isMarkedReview());
            ps.setTimestamp(3, toTimestamp(answer.getAnsweredAt()));
            ps.setInt(4, answer.getAnswerId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteByAttemptId(int attemptId) throws SQLException {
        String sql = "DELETE FROM answers WHERE attempt_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attemptId);
            return ps.executeUpdate() > 0;
        }
    }

    private Answer mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("answer_id");
        int attemptId = rs.getInt("attempt_id");
        int questionId = rs.getInt("question_id");
        String selectedOption = rs.getString("selected_option");
        boolean markedReview = rs.getBoolean("is_marked_review");
        LocalDateTime answeredAt = toLocalDateTime(rs.getTimestamp("answered_at"));

        return new Answer(id, attemptId, questionId, selectedOption, markedReview, answeredAt);
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private Timestamp toTimestamp(LocalDateTime dt) {
        return dt != null ? Timestamp.valueOf(dt) : null;
    }
}
