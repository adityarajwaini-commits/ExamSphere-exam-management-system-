package com.exam.dao.impl;

import com.exam.dao.QuestionDAO;
import com.exam.model.Question;
import com.exam.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAOImpl implements QuestionDAO {
    @Override
    public int create(Question question) throws SQLException {
        String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, "
                + "option_d, correct_option, marks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, question.getExamId());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getOptionA());
            ps.setString(4, question.getOptionB());
            ps.setString(5, question.getOptionC());
            ps.setString(6, question.getOptionD());
            ps.setString(7, question.getCorrectOption());
            ps.setInt(8, question.getMarks());
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
    public Question findById(int questionId) throws SQLException {
        String sql = "SELECT * FROM questions WHERE question_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Question> findByExamId(int examId) throws SQLException {
        String sql = "SELECT * FROM questions WHERE exam_id = ? ORDER BY question_id";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapRow(rs));
                }
            }
        }
        return questions;
    }

    @Override
    public boolean update(Question question) throws SQLException {
        String sql = "UPDATE questions SET question_text = ?, option_a = ?, option_b = ?, option_c = ?, "
                + "option_d = ?, correct_option = ?, marks = ? WHERE question_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, question.getQuestionText());
            ps.setString(2, question.getOptionA());
            ps.setString(3, question.getOptionB());
            ps.setString(4, question.getOptionC());
            ps.setString(5, question.getOptionD());
            ps.setString(6, question.getCorrectOption());
            ps.setInt(7, question.getMarks());
            ps.setInt(8, question.getQuestionId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE question_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteByExamId(int examId) throws SQLException {
        String sql = "DELETE FROM questions WHERE exam_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            return ps.executeUpdate() > 0;
        }
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("question_id");
        int examId = rs.getInt("exam_id");
        String text = rs.getString("question_text");
        String optionA = rs.getString("option_a");
        String optionB = rs.getString("option_b");
        String optionC = rs.getString("option_c");
        String optionD = rs.getString("option_d");
        String correctOption = rs.getString("correct_option");
        int marks = rs.getInt("marks");

        return new Question(id, examId, text, optionA, optionB, optionC, optionD, correctOption, marks);
    }
}
