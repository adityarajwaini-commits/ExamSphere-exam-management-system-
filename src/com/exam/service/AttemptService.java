package com.exam.service;

import com.exam.model.Answer;
import com.exam.model.Attempt;
import java.sql.SQLException;
import java.util.List;

public interface AttemptService {
    int startAttempt(int userId, int examId) throws SQLException;

    Attempt getActiveAttempt(int userId, int examId) throws SQLException;

    boolean updateAttemptStatus(int attemptId, String status) throws SQLException;

    Answer saveAnswer(Answer answer) throws SQLException;

    List<Answer> getAnswersForAttempt(int attemptId) throws SQLException;
}
