package com.exam.service;

import com.exam.model.Exam;
import com.exam.model.Question;
import java.sql.SQLException;
import java.util.List;

public interface ExamService {
    int createExam(Exam exam) throws SQLException;

    boolean updateExam(Exam exam) throws SQLException;

    boolean deleteExam(int examId) throws SQLException;

    Exam getExamById(int examId) throws SQLException;

    List<Exam> getAllExams() throws SQLException;

    List<Question> getQuestionsForExam(int examId, boolean randomize) throws SQLException;

    int createQuestion(Question question) throws SQLException;

    boolean updateQuestion(Question question) throws SQLException;

    boolean deleteQuestion(int questionId) throws SQLException;
}
