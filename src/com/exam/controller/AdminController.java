package com.exam.controller;

import com.exam.model.Exam;
import com.exam.model.Result;
import com.exam.model.User;
import com.exam.service.AuthService;
import com.exam.service.ExamService;
import com.exam.service.ResultService;
import com.exam.util.NavigationHelper;
import java.sql.SQLException;
import java.util.List;

public class AdminController {
    private final ExamService examService;
    private final ResultService resultService;
    private final AuthService authService;
    private final NavigationHelper navigation;
    private String lastError;

    public AdminController(ExamService examService, ResultService resultService,
                           AuthService authService, NavigationHelper navigation) {
        this.examService = examService;
        this.resultService = resultService;
        this.authService = authService;
        this.navigation = navigation;
    }

    public int createExam(Exam exam) throws SQLException {
        lastError = validateExam(exam);
        if (lastError != null) {
            return 0;
        }
        return examService.createExam(exam);
    }

    public boolean updateExam(Exam exam) throws SQLException {
        lastError = validateExam(exam);
        if (lastError != null) {
            return false;
        }
        return examService.updateExam(exam);
    }

    public boolean deleteExam(int examId) throws SQLException {
        if (examId <= 0) {
            lastError = "Invalid exam.";
            return false;
        }
        return examService.deleteExam(examId);
    }

    public List<Exam> getAllExams() throws SQLException {
        return examService.getAllExams();
    }

    public List<Result> getResultsByExam(int examId) throws SQLException {
        return resultService.getResultsByExam(examId);
    }

    public List<User> getAllStudents() throws SQLException {
        return authService.getAllStudents();
    }

    public void goToView(String viewName) {
        navigation.show(viewName);
    }

    public String getLastError() {
        return lastError;
    }

    public NavigationHelper getNavigation() {
        return navigation;
    }

    private String validateExam(Exam exam) {
        if (exam == null) {
            return "Exam data is required.";
        }
        if (exam.getTitle() == null || exam.getTitle().trim().isEmpty()) {
            return "Exam title is required.";
        }
        if (exam.getDurationMinutes() <= 0 || exam.getTotalQuestions() <= 0) {
            return "Duration and total questions must be positive.";
        }
        if (exam.getPassPercentage() < 0 || exam.getPassPercentage() > 100) {
            return "Pass percentage must be between 0 and 100.";
        }
        return null;
    }
}
