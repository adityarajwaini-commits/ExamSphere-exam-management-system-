package com.exam.service;

import com.exam.model.Answer;
import com.exam.model.Question;
import com.exam.model.Result;
import java.sql.SQLException;
import java.util.List;

public interface ResultService {
    Result calculateResult(int attemptId, int userId, int examId,
                           List<Question> questions, List<Answer> answers,
                           int timeTakenSeconds);

    int saveResult(Result result) throws SQLException;

    Result getResultByAttempt(int attemptId) throws SQLException;

    List<Result> getResultsByUser(int userId) throws SQLException;

    List<Result> getResultsByExam(int examId) throws SQLException;

    List<Result> getTopResultsByExam(int examId, int limit) throws SQLException;
}
