package com.exam.dao;

import java.sql.SQLException;
import java.util.List;
import com.exam.model.Answer;

public interface AnswerDAO {
    int create(Answer answer) throws SQLException;

    Answer findById(int answerId) throws SQLException;

    Answer findByAttemptAndQuestion(int attemptId, int questionId) throws SQLException;

    List<Answer> findByAttemptId(int attemptId) throws SQLException;

    boolean update(Answer answer) throws SQLException;

    boolean deleteByAttemptId(int attemptId) throws SQLException;
}
