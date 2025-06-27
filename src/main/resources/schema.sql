-- Enhanced Question Bank and Test Paper System Schema
-- This script creates the necessary tables for the TeaTeach question bank system

-- Create questions table with enhanced fields
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text TEXT NOT NULL,
    question_type ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'FILL_IN_THE_BLANK', 'SHORT_ANSWER', 'PROGRAMMING') NOT NULL,
    options JSON, -- For multiple choice questions
    correct_answer JSON NOT NULL, -- Stores the correct answer(s)
    explanation TEXT, -- Explanation for the answer
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
    knowledge_point_ids JSON, -- Array of knowledge point IDs
    programming_language VARCHAR(50), -- For programming questions
    template_code TEXT, -- Template code for programming questions
    test_cases JSON, -- Test cases for programming questions
    points DECIMAL(5,2) DEFAULT 1.0, -- Points/score for this question
    tags JSON, -- Question tags for categorization
    created_by BIGINT, -- ID of the user who created the question
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE, -- Whether the question is active
    usage_count INT DEFAULT 0, -- How many times this question has been used
    average_score DECIMAL(5,2) DEFAULT 0.0, -- Average score students get on this question
    INDEX idx_question_type (question_type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_created_by (created_by),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
);

-- Create test_papers table
CREATE TABLE IF NOT EXISTS test_papers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paper_name VARCHAR(255) NOT NULL,
    course_id BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL, -- ID of the instructor who created it
    question_ids JSON NOT NULL, -- Array of question IDs included in the paper
    total_score DECIMAL(8,2),
    duration_minutes INT,
    generation_method ENUM('RANDOM', 'BY_KNOWLEDGE_POINT', 'BY_DIFFICULTY', 'BALANCED') DEFAULT 'RANDOM',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id),
    INDEX idx_instructor_id (instructor_id),
    INDEX idx_generation_method (generation_method),
    INDEX idx_created_at (created_at)
);

-- Create question_banks table for organizing questions into banks
CREATE TABLE IF NOT EXISTS question_banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_name VARCHAR(255) NOT NULL,
    description TEXT,
    course_id BIGINT,
    created_by BIGINT NOT NULL,
    is_public BOOLEAN DEFAULT FALSE, -- Whether other instructors can use this bank
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id),
    INDEX idx_created_by (created_by),
    INDEX idx_is_public (is_public)
);

-- Create question_bank_items table to link questions to banks
CREATE TABLE IF NOT EXISTS question_bank_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_bank_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_bank_id) REFERENCES question_banks(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY unique_bank_question (question_bank_id, question_id),
    INDEX idx_question_bank_id (question_bank_id),
    INDEX idx_question_id (question_id)
);

-- Create test_paper_attempts table to track student attempts
CREATE TABLE IF NOT EXISTS test_paper_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_paper_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    total_score DECIMAL(8,2),
    max_score DECIMAL(8,2),
    answers JSON, -- Student's answers
    is_completed BOOLEAN DEFAULT FALSE,
    time_spent_minutes INT,
    FOREIGN KEY (test_paper_id) REFERENCES test_papers(id) ON DELETE CASCADE,
    INDEX idx_test_paper_id (test_paper_id),
    INDEX idx_student_id (student_id),
    INDEX idx_start_time (start_time),
    INDEX idx_is_completed (is_completed)
);

-- Create question_statistics table for analytics
CREATE TABLE IF NOT EXISTS question_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    total_attempts INT DEFAULT 0,
    correct_attempts INT DEFAULT 0,
    average_time_seconds DECIMAL(8,2) DEFAULT 0.0,
    difficulty_rating DECIMAL(3,2) DEFAULT 0.0, -- Calculated difficulty based on performance
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY unique_question_stats (question_id),
    INDEX idx_question_id (question_id),
    INDEX idx_difficulty_rating (difficulty_rating)
);

-- Create knowledge_point_coverage table to track coverage in test papers
CREATE TABLE IF NOT EXISTS knowledge_point_coverage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_paper_id BIGINT NOT NULL,
    knowledge_point_id BIGINT NOT NULL,
    question_count INT DEFAULT 0,
    total_points DECIMAL(8,2) DEFAULT 0.0,
    FOREIGN KEY (test_paper_id) REFERENCES test_papers(id) ON DELETE CASCADE,
    UNIQUE KEY unique_paper_knowledge_point (test_paper_id, knowledge_point_id),
    INDEX idx_test_paper_id (test_paper_id),
    INDEX idx_knowledge_point_id (knowledge_point_id)
);

-- Insert some sample data for testing
INSERT IGNORE INTO questions (question_text, question_type, options, correct_answer, explanation, difficulty, points, created_by, is_active) VALUES
('What is the capital of France?', 'SINGLE_CHOICE', 
 '["Paris", "London", "Berlin", "Madrid"]', 
 '"Paris"', 
 'Paris is the capital and largest city of France.', 
 'EASY', 1.0, 1, TRUE),

('Which of the following are programming languages? (Select all that apply)', 'MULTIPLE_CHOICE',
 '["Java", "HTML", "Python", "CSS", "JavaScript"]',
 '["Java", "Python", "JavaScript"]',
 'Java, Python, and JavaScript are programming languages. HTML and CSS are markup and styling languages respectively.',
 'MEDIUM', 2.0, 1, TRUE),

('Complete the sentence: The quick brown fox _____ over the lazy dog.', 'FILL_IN_THE_BLANK',
 NULL,
 '["jumps", "jumped"]',
 'The correct word is "jumps" (present tense) or "jumped" (past tense).',
 'EASY', 1.0, 1, TRUE),

('Explain the concept of object-oriented programming and its main principles.', 'SHORT_ANSWER',
 NULL,
 '"Object-oriented programming (OOP) is a programming paradigm based on objects and classes. Main principles include encapsulation, inheritance, polymorphism, and abstraction."',
 'A good answer should mention the four main principles of OOP.',
 'HARD', 5.0, 1, TRUE),

('Write a function that calculates the factorial of a number.', 'PROGRAMMING',
 NULL,
 '{"language": "python", "code": "def factorial(n):\n    if n <= 1:\n        return 1\n    return n * factorial(n-1)"}',
 'This is a recursive implementation of factorial calculation.',
 'MEDIUM', 3.0, 1, TRUE);

-- Insert sample question bank
INSERT IGNORE INTO question_banks (bank_name, description, created_by, is_public) VALUES
('General Knowledge Bank', 'A collection of general knowledge questions for testing', 1, TRUE),
('Programming Fundamentals', 'Basic programming concepts and exercises', 1, TRUE);

-- Link questions to question banks
INSERT IGNORE INTO question_bank_items (question_bank_id, question_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(2, 2), (2, 4), (2, 5);

-- Insert sample test paper
INSERT IGNORE INTO test_papers (paper_name, course_id, instructor_id, question_ids, total_score, duration_minutes, generation_method) VALUES
('Sample Test Paper', 1, 1, '[1, 2, 3, 4, 5]', 12.0, 60, 'RANDOM');