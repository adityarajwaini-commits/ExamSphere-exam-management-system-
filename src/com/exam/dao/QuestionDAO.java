package com.exam.dao;

import java.sql.SQLException;
import java.util.List;
import com.exam.model.Question;

public interface QuestionDAO {
    int create(Question question) throws SQLException;

    Question findById(int questionId) throws SQLException;

    List<Question> findByExamId(int examId) throws SQLException;

    boolean update(Question question) throws SQLException;

    boolean delete(int questionId) throws SQLException;

    boolean deleteByExamId(int examId) throws SQLException;
}
