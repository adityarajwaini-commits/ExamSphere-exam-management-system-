package com.exam.service.impl;

import com.exam.dao.ResultDAO;
import com.exam.model.Answer;
import com.exam.model.Question;
import com.exam.model.Result;
import com.exam.service.ResultService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultServiceImpl implements ResultService {
    private final ResultDAO resultDAO;

    public ResultServiceImpl(ResultDAO resultDAO) {
        this.resultDAO = resultDAO;
    }

    @Override
    public Result calculateResult(int attemptId, int userId, int examId,
                                  List<Question> questions, List<Answer> answers,
                                  int timeTakenSeconds) {
        Map<Integer, Answer> answerMap = answers.stream()
                .collect(Collectors.toMap(Answer::getQuestionId, a -> a, (a, b) -> a));

        int total = questions.size();
        int correct = 0;
        int wrong = 0;
        int skipped = 0;
        int score = 0;
        int totalMarks = 0;

        for (Question q : questions) {
            totalMarks += q.getMarks();
            Answer ans = answerMap.get(q.getQuestionId());
            if (ans == null || ans.getSelectedOption() == null || ans.getSelectedOption().isEmpty()) {
                skipped++;
                continue;
            }
            if (q.getCorrectOption().equalsIgnoreCase(ans.getSelectedOption())) {
                correct++;
                score += q.getMarks();
            } else {
                wrong++;
            }
        }

        BigDecimal percentage = BigDecimal.ZERO;
        if (totalMarks > 0) {
            percentage = BigDecimal.valueOf(score * 100.0 / totalMarks)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new Result(attemptId, userId, examId, total, correct, wrong, skipped,
                score, percentage, timeTakenSeconds);
    }

    @Override
    public int saveResult(Result result) throws SQLException {
        return resultDAO.create(result);
    }

    @Override
    public Result getResultByAttempt(int attemptId) throws SQLException {
        return resultDAO.findByAttemptId(attemptId);
    }

    @Override
    public List<Result> getResultsByUser(int userId) throws SQLException {
        return resultDAO.findByUserId(userId);
    }

    @Override
    public List<Result> getResultsByExam(int examId) throws SQLException {
        return resultDAO.findByExamId(examId);
    }

    @Override
    public List<Result> getTopResultsByExam(int examId, int limit) throws SQLException {
        return resultDAO.findTopByExamId(examId, limit);
    }
}
