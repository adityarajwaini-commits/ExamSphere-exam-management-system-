package com.exam.dao;

import java.sql.SQLException;
import java.util.List;
import com.exam.model.Exam;

public interface ExamDAO {
    int create(Exam exam) throws SQLException;

    Exam findById(int examId) throws SQLException;

    List<Exam> findAll() throws SQLException;

    boolean update(Exam exam) throws SQLException;

    boolean delete(int examId) throws SQLException;
}
