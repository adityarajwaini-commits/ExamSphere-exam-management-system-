-- Online Examination System (MySQL 8.x)
-- UTF8MB4 for proper text handling
CREATE DATABASE IF NOT EXISTS online_exam
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE online_exam;

-- 1) USERS
CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(80) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed default admin (idempotent)
INSERT INTO users (full_name, email, password_hash, role, status)
SELECT 'Admin User', 'admin@gmail.com', 'admin123', 'ADMIN', 'ACTIVE'
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE email = 'admin@gmail.com'
);

-- 2) EXAMS
CREATE TABLE exams (
  exam_id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  duration_minutes INT NOT NULL,
  total_questions INT NOT NULL,
  pass_percentage INT NOT NULL,
  randomize_questions TINYINT(1) NOT NULL DEFAULT 1,
  created_by INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_exam_admin FOREIGN KEY (created_by)
    REFERENCES users(user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 3) QUESTIONS
CREATE TABLE questions (
  question_id INT AUTO_INCREMENT PRIMARY KEY,
  exam_id INT NOT NULL,
  question_text TEXT NOT NULL,
  option_a VARCHAR(255) NOT NULL,
  option_b VARCHAR(255) NOT NULL,
  option_c VARCHAR(255) NOT NULL,
  option_d VARCHAR(255) NOT NULL,
  correct_option CHAR(1) NOT NULL,
  marks INT NOT NULL DEFAULT 1,
  CONSTRAINT fk_question_exam FOREIGN KEY (exam_id)
    REFERENCES exams(exam_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ck_question_correct_option CHECK (correct_option IN ('A','B','C','D'))
);

-- 4) ATTEMPTS
CREATE TABLE attempts (
  attempt_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  exam_id INT NOT NULL,
  start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  end_time TIMESTAMP NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
  CONSTRAINT fk_attempt_user FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_attempt_exam FOREIGN KEY (exam_id)
    REFERENCES exams(exam_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);

-- 5) ANSWERS
CREATE TABLE answers (
  answer_id INT AUTO_INCREMENT PRIMARY KEY,
  attempt_id INT NOT NULL,
  question_id INT NOT NULL,
  selected_option CHAR(1) NULL,
  is_marked_review TINYINT(1) NOT NULL DEFAULT 0,
  answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id)
    REFERENCES attempts(attempt_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_answer_question FOREIGN KEY (question_id)
    REFERENCES questions(question_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ck_answer_selected_option CHECK (
    selected_option IN ('A','B','C','D') OR selected_option IS NULL
  ),
  UNIQUE KEY uq_attempt_question (attempt_id, question_id)
);

-- 6) RESULTS
CREATE TABLE results (
  result_id INT AUTO_INCREMENT PRIMARY KEY,
  attempt_id INT NOT NULL UNIQUE,
  user_id INT NOT NULL,
  exam_id INT NOT NULL,
  total_questions INT NOT NULL,
  correct_count INT NOT NULL,
  wrong_count INT NOT NULL,
  skipped_count INT NOT NULL,
  score INT NOT NULL,
  percentage DECIMAL(5,2) NOT NULL,
  time_taken_seconds INT NOT NULL,
  submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_result_attempt FOREIGN KEY (attempt_id)
    REFERENCES attempts(attempt_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_result_user FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_result_exam FOREIGN KEY (exam_id)
    REFERENCES exams(exam_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ck_result_counts CHECK (
    total_questions >= 0 AND correct_count >= 0 AND wrong_count >= 0 AND skipped_count >= 0
  )
);

-- Indexes for faster lookups
CREATE INDEX idx_questions_exam ON questions(exam_id);
CREATE INDEX idx_attempts_user ON attempts(user_id);
CREATE INDEX idx_attempts_exam ON attempts(exam_id);
CREATE INDEX idx_answers_attempt ON answers(attempt_id);
CREATE INDEX idx_results_user ON results(user_id);
CREATE INDEX idx_results_exam ON results(exam_id);
CREATE INDEX idx_results_score ON results(score);
