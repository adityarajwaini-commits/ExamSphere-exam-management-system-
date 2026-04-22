package com.exam.controller;

import com.exam.model.Answer;
import com.exam.model.Attempt;
import com.exam.model.Result;
import com.exam.service.AttemptService;
import com.exam.service.ResultService;
import com.exam.util.NavigationHelper;
import java.sql.SQLException;
import java.util.List;

public class StudentController {
    private final AttemptService attemptService;
    private final ResultService resultService;
    private final NavigationHelper navigation;
    private String lastError;

    public StudentController(AttemptService attemptService, ResultService resultService,
                             NavigationHelper navigation) {
        this.attemptService = attemptService;
        this.resultService = resultService;
        this.navigation = navigation;
    }

    public int startAttempt(int userId, int examId) throws SQLException {
        if (userId <= 0 || examId <= 0) {
            lastError = "Invalid student or exam.";
            return 0;
        }
        return attemptService.startAttempt(userId, examId);
    }

    public Attempt getActiveAttempt(int userId, int examId) throws SQLException {
        return attemptService.getActiveAttempt(userId, examId);
    }

    public List<Result> getMyResults(int userId) throws SQLException {
        return resultService.getResultsByUser(userId);
    }

    public List<Answer> getAnswers(int attemptId) throws SQLException {
        return attemptService.getAnswersForAttempt(attemptId);
    }

    public void goToView(String viewName) {
        navigation.show(viewName);
    }

    public String getLastError() {
        return lastError;
    }
}
