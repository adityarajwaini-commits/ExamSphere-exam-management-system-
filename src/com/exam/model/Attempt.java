package com.exam.model;

import java.time.LocalDateTime;

public class Attempt {
    private int attemptId;
    private int userId;
    private int examId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public Attempt() {
    }

    public Attempt(int attemptId, int userId, int examId, LocalDateTime startTime,
                   LocalDateTime endTime, String status) {
        this.attemptId = attemptId;
        this.userId = userId;
        this.examId = examId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Attempt(int userId, int examId, String status) {
        this.userId = userId;
        this.examId = examId;
        this.status = status;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
