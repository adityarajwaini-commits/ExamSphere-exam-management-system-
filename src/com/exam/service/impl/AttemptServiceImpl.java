package com.exam.service.impl;

import com.exam.dao.AnswerDAO;
import com.exam.dao.AttemptDAO;
import com.exam.model.Answer;
import com.exam.model.Attempt;
import com.exam.service.AttemptService;
import java.sql.SQLException;
import java.util.List;

public class AttemptServiceImpl implements AttemptService {
    private final AttemptDAO attemptDAO;
    private final AnswerDAO answerDAO;

    public AttemptServiceImpl(AttemptDAO attemptDAO, AnswerDAO answerDAO) {
        this.attemptDAO = attemptDAO;
        this.answerDAO = answerDAO;
    }

    @Override
    public int startAttempt(int userId, int examId) throws SQLException {
        Attempt attempt = new Attempt(userId, examId, "IN_PROGRESS");
        return attemptDAO.create(attempt);
    }

    @Override
    public Attempt getActiveAttempt(int userId, int examId) throws SQLException {
        return attemptDAO.findActiveAttempt(userId, examId);
    }

    @Override
    public boolean updateAttemptStatus(int attemptId, String status) throws SQLException {
        return attemptDAO.updateStatus(attemptId, status);
    }

    @Override
    public Answer saveAnswer(Answer answer) throws SQLException {
        Answer existing = answerDAO.findByAttemptAndQuestion(answer.getAttemptId(), answer.getQuestionId());
        if (existing == null) {
            int id = answerDAO.create(answer);
            answer.setAnswerId(id);
            return answer;
        }
        answer.setAnswerId(existing.getAnswerId());
        answerDAO.update(answer);
        return answer;
    }

    @Override
    public List<Answer> getAnswersForAttempt(int attemptId) throws SQLException {
        return answerDAO.findByAttemptId(attemptId);
    }
}
