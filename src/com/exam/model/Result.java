package com.exam.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Result {
    private int resultId;
    private int attemptId;
    private int userId;
    private int examId;
    private int totalQuestions;
    private int correctCount;
    private int wrongCount;
    private int skippedCount;
    private int score;
    private BigDecimal percentage;
    private int timeTakenSeconds;
    private LocalDateTime submittedAt;
    private String studentName; // Transient/Joined field

    public Result() {
    }

    public Result(int resultId, int attemptId, int userId, int examId, int totalQuestions,
                  int correctCount, int wrongCount, int skippedCount, int score,
                  BigDecimal percentage, int timeTakenSeconds, LocalDateTime submittedAt) {
        this.resultId = resultId;
        this.attemptId = attemptId;
        this.userId = userId;
        this.examId = examId;
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.skippedCount = skippedCount;
        this.score = score;
        this.percentage = percentage;
        this.timeTakenSeconds = timeTakenSeconds;
        this.submittedAt = submittedAt;
    }

    public Result(int attemptId, int userId, int examId, int totalQuestions,
                  int correctCount, int wrongCount, int skippedCount, int score,
                  BigDecimal percentage, int timeTakenSeconds) {
        this.attemptId = attemptId;
        this.userId = userId;
        this.examId = examId;
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.skippedCount = skippedCount;
        this.score = score;
        this.percentage = percentage;
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public int getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(int timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}
