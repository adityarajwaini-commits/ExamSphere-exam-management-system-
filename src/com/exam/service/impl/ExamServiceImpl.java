package com.exam.service.impl;

import com.exam.dao.ExamDAO;
import com.exam.dao.QuestionDAO;
import com.exam.model.Exam;
import com.exam.model.Question;
import com.exam.service.ExamService;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ExamServiceImpl implements ExamService {
    private final ExamDAO examDAO;
    private final QuestionDAO questionDAO;

    public ExamServiceImpl(ExamDAO examDAO, QuestionDAO questionDAO) {
        this.examDAO = examDAO;
        this.questionDAO = questionDAO;
    }

    @Override
    public int createExam(Exam exam) throws SQLException {
        return examDAO.create(exam);
    }

    @Override
    public boolean updateExam(Exam exam) throws SQLException {
        return examDAO.update(exam);
    }

    @Override
    public boolean deleteExam(int examId) throws SQLException {
        return examDAO.delete(examId);
    }

    @Override
    public Exam getExamById(int examId) throws SQLException {
        return examDAO.findById(examId);
    }

    @Override
    public List<Exam> getAllExams() throws SQLException {
        return examDAO.findAll();
    }

    @Override
    public List<Question> getQuestionsForExam(int examId, boolean randomize) throws SQLException {
        List<Question> questions = questionDAO.findByExamId(examId);
        if (randomize) {
            Collections.shuffle(questions);
        }
        return questions;
    }

    @Override
    public int createQuestion(Question question) throws SQLException {
        return questionDAO.create(question);
    }

    @Override
    public boolean updateQuestion(Question question) throws SQLException {
        return questionDAO.update(question);
    }

    @Override
    public boolean deleteQuestion(int questionId) throws SQLException {
        return questionDAO.delete(questionId);
    }
}
