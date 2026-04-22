package com.exam.model;

import java.time.LocalDateTime;

public class Exam {
    private int examId;
    private String title;
    private int durationMinutes;
    private int totalQuestions;
    private int passPercentage;
    private boolean randomizeQuestions;
    private int createdBy;
    private LocalDateTime createdAt;

    public Exam() {
    }

    public Exam(int examId, String title, int durationMinutes, int totalQuestions,
                int passPercentage, boolean randomizeQuestions, int createdBy,
                LocalDateTime createdAt) {
        this.examId = examId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.totalQuestions = totalQuestions;
        this.passPercentage = passPercentage;
        this.randomizeQuestions = randomizeQuestions;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Exam(String title, int durationMinutes, int totalQuestions, int passPercentage,
                boolean randomizeQuestions, int createdBy) {
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.totalQuestions = totalQuestions;
        this.passPercentage = passPercentage;
        this.randomizeQuestions = randomizeQuestions;
        this.createdBy = createdBy;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(int passPercentage) {
        this.passPercentage = passPercentage;
    }

    public boolean isRandomizeQuestions() {
        return randomizeQuestions;
    }

    public void setRandomizeQuestions(boolean randomizeQuestions) {
        this.randomizeQuestions = randomizeQuestions;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title == null ? ("Exam " + examId) : title;
    }
}
