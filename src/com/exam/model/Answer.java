package com.exam.model;

import java.time.LocalDateTime;

public class Answer {
    private int answerId;
    private int attemptId;
    private int questionId;
    private String selectedOption;
    private boolean markedReview;
    private LocalDateTime answeredAt;

    public Answer() {
    }

    public Answer(int answerId, int attemptId, int questionId, String selectedOption,
                  boolean markedReview, LocalDateTime answeredAt) {
        this.answerId = answerId;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.markedReview = markedReview;
        this.answeredAt = answeredAt;
    }

    public Answer(int attemptId, int questionId, String selectedOption, boolean markedReview) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.markedReview = markedReview;
    }

    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public boolean isMarkedReview() {
        return markedReview;
    }

    public void setMarkedReview(boolean markedReview) {
        this.markedReview = markedReview;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}
