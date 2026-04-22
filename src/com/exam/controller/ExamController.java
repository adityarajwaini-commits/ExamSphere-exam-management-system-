package com.exam.controller;

import com.exam.model.Answer;
import com.exam.model.Exam;
import com.exam.model.Question;
import com.exam.model.Result;
import com.exam.service.AttemptService;
import com.exam.service.ExamService;
import com.exam.service.ResultService;
import com.exam.util.NavigationHelper;
import java.sql.SQLException;
import java.util.List;

public class ExamController {
    private final ExamService examService;
    private final AttemptService attemptService;
    private final ResultService resultService;
    private final NavigationHelper navigation;
    private String lastError;

    public ExamController(ExamService examService, AttemptService attemptService,
                          ResultService resultService, NavigationHelper navigation) {
        this.examService = examService;
        this.attemptService = attemptService;
        this.resultService = resultService;
        this.navigation = navigation;
    }

    public List<Question> loadQuestions(int examId, boolean randomize) throws SQLException {
        if (examId <= 0) {
            lastError = "Invalid exam.";
            return null;
        }
        return examService.getQuestionsForExam(examId, randomize);
    }

    public int createExam(Exam exam) throws SQLException {
        if (exam == null) {
            lastError = "Exam data is required.";
            return 0;
        }
        return examService.createExam(exam);
    }

    public boolean updateExam(Exam exam) throws SQLException {
        if (exam == null || exam.getExamId() <= 0) {
            lastError = "Invalid exam.";
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

    public int createQuestion(Question question) throws SQLException {
        if (question == null) {
            lastError = "Question data is required.";
            return 0;
        }
        return examService.createQuestion(question);
    }

    public boolean updateQuestion(Question question) throws SQLException {
        if (question == null || question.getQuestionId() <= 0) {
            lastError = "Invalid question.";
            return false;
        }
        return examService.updateQuestion(question);
    }

    public boolean deleteQuestion(int questionId) throws SQLException {
        if (questionId <= 0) {
            lastError = "Invalid question.";
            return false;
        }
        return examService.deleteQuestion(questionId);
    }

    public List<Exam> getAllExams() throws SQLException {
        return examService.getAllExams();
    }

    public int startAttempt(int userId, int examId) throws SQLException {
        if (userId <= 0 || examId <= 0) {
            lastError = "Invalid student or exam.";
            return 0;
        }
        return attemptService.startAttempt(userId, examId);
    }

    public Answer saveAnswer(Answer answer) throws SQLException {
        if (answer == null || answer.getAttemptId() <= 0 || answer.getQuestionId() <= 0) {
            lastError = "Invalid answer.";
            return null;
        }
        return attemptService.saveAnswer(answer);
    }

    public Result calculateResult(int attemptId, int userId, int examId,
                                  List<Question> questions, List<Answer> answers,
                                  int timeTakenSeconds) {
        return resultService.calculateResult(attemptId, userId, examId,
                questions, answers, timeTakenSeconds);
    }

    public int saveResult(Result result) throws SQLException {
        if (result == null) {
            lastError = "Result data is required.";
            return 0;
        }
        return resultService.saveResult(result);
    }

    public List<Result> getLeaderboard(int examId) throws SQLException {
        return resultService.getResultsByExam(examId);
    }

    public void goToView(String viewName) {
        navigation.show(viewName);
    }

    public String getLastError() {
        return lastError;
    }
}
