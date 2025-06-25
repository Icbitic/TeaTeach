-- SQL Script to Construct Tables for 'teateach' Schema in MySQL

-- 1. Create the schema if it doesn't exist and set it as the current database
CREATE SCHEMA IF NOT EXISTS teateach;
USE teateach;

-- 2. Drop tables if they exist (to allow re-running the script for development)
--    Drop in reverse order of creation due to foreign key constraints
DROP TABLE IF EXISTS grade_analysis;
DROP TABLE IF EXISTS student_task_submissions;
DROP TABLE IF EXISTS test_papers;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS video_learning_progress;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS knowledge_points;
DROP TABLE IF EXISTS learning_tasks;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS ability_points; -- Assuming ability_points might exist independently or as a base

-- -----------------------------------------------------
-- Table `teateach`.`students`
-- Stores basic student information
-- -----------------------------------------------------
CREATE TABLE students (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          student_id VARCHAR(50) UNIQUE NOT NULL COMMENT 'Unique student identification number',
                          name VARCHAR(255) NOT NULL,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          major VARCHAR(255),
                          date_of_birth DATE,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Table `teateach`.`courses`
-- Stores basic course information
-- -----------------------------------------------------
CREATE TABLE courses (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         course_code VARCHAR(100) UNIQUE NOT NULL COMMENT 'Unique code for the course (e.g., CS101)',
                         course_name VARCHAR(255) NOT NULL,
                         instructor VARCHAR(255),
                         credits DOUBLE,
                         hours INT COMMENT 'Total learning hours for the course',
                         description TEXT,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Table `teateach`.`learning_tasks`
-- Manages learning tasks within a course
-- -----------------------------------------------------
CREATE TABLE learning_tasks (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                course_id BIGINT NOT NULL,
                                task_name VARCHAR(255) NOT NULL,
                                task_type VARCHAR(50) NOT NULL COMMENT 'e.g., CHAPTER_HOMEWORK, EXAM_QUIZ, VIDEO_WATCH, REPORT_UPLOAD',
                                task_description TEXT,
                                deadline DATETIME,
                                submission_method VARCHAR(100) COMMENT 'e.g., upload, online_quiz, text_input',
                                resource_id BIGINT COMMENT 'Optional: Reference to associated resource (video, PDF, etc.)',
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE SET NULL
);

-- -----------------------------------------------------
-- Table `teateach`.`student_task_submissions`
-- Records student submissions for learning tasks
-- -----------------------------------------------------
CREATE TABLE student_task_submissions (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          task_id BIGINT NOT NULL,
                                          student_id BIGINT NOT NULL,
                                          submission_content TEXT COMMENT 'Path to file, text answer, or JSON data',
                                          submission_time DATETIME NOT NULL,
                                          score DOUBLE COMMENT 'Nullable until graded',
                                          feedback TEXT COMMENT 'Teacher feedback or automated feedback',
                                          completion_status INT NOT NULL DEFAULT 0 COMMENT '0: Not Started, 1: In Progress, 2: Submitted, 3: Graded',
                                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          FOREIGN KEY (task_id) REFERENCES learning_tasks(id) ON DELETE CASCADE,
                                          FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                                          UNIQUE (task_id, student_id) -- Ensures one submission per student per task (can be removed for multiple attempts)
);

-- -----------------------------------------------------
-- Table `teateach`.`grade_analysis`
-- Stores aggregated and analyzed grade data for students in courses
-- -----------------------------------------------------
CREATE TABLE grade_analysis (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                student_id BIGINT NOT NULL,
                                course_id BIGINT NOT NULL,
                                overall_grade DOUBLE COMMENT 'Overall calculated grade for the student in this course',
                                task_grades TEXT COMMENT 'JSON string of task grades (e.g., {"Quiz 1": 85.0, "Report": 92.5})',
                                grade_trend TEXT COMMENT 'JSON string of grade changes over time (e.g., {"2024-01-15": 70.5, "2024-02-20": 78.0})',
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                                FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                UNIQUE (student_id, course_id) -- Ensures only one analysis record per student per course
);

-- -----------------------------------------------------
-- Table `teateach`.`resources`
-- Stores information about uploaded course resources (PPT, PDF, Videos etc.)
-- -----------------------------------------------------
CREATE TABLE resources (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           task_id BIGINT COMMENT 'Optional: if resource is tied to a specific task',
                           resource_name VARCHAR(255) NOT NULL,
                           file_path VARCHAR(512) NOT NULL COMMENT 'Path to stored file or external URL',
                           file_type VARCHAR(50) NOT NULL COMMENT 'e.g., pdf, pptx, mp4, docx',
                           uploaded_by BIGINT COMMENT 'ID of the user (e.g., teacher) who uploaded it',
                           file_size BIGINT COMMENT 'Size of the file in bytes',
                           description TEXT,
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (task_id) REFERENCES learning_tasks(id) ON DELETE SET NULL
    -- ON DELETE SET NULL: if task is deleted, resource remains but its task_id is set to NULL
);

-- -----------------------------------------------------
-- Table `teateach`.`video_learning_progress`
-- Records detailed student progress in video resources
-- -----------------------------------------------------
CREATE TABLE video_learning_progress (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         student_id BIGINT NOT NULL,
                                         resource_id BIGINT NOT NULL COMMENT 'ID of the video resource',
                                         current_playback_time_seconds BIGINT COMMENT 'Current position in video',
                                         completion_percentage DOUBLE COMMENT 'Percentage of video completed',
                                         skipped_segments TEXT COMMENT 'JSON array of [start_time, end_time] pairs',
                                         repeated_segments TEXT COMMENT 'JSON array of [start_time, end_time] pairs',
                                         total_play_duration_seconds BIGINT COMMENT 'Total time student spent playing the video',
                                         last_accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                         FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                                         FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
                                         UNIQUE (student_id, resource_id) -- Ensures one progress record per student per video
);

-- -----------------------------------------------------
-- Table `teateach`.`knowledge_points`
-- Stores knowledge points for building a knowledge graph
-- -----------------------------------------------------
CREATE TABLE knowledge_points (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  name VARCHAR(255) NOT NULL COMMENT 'Name of the knowledge point',
                                  brief_description TEXT,
                                  detailed_content TEXT COMMENT 'More extensive content or link to learning material',
                                  course_id BIGINT,
                                  difficulty_level VARCHAR(50) COMMENT 'e.g., BEGINNER, INTERMEDIATE, ADVANCED',
                                  prerequisite_knowledge_point_ids TEXT COMMENT 'JSON array of IDs of prerequisite KPs',
                                  related_knowledge_point_ids TEXT COMMENT 'JSON array of IDs of related KPs',
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL
);

-- -----------------------------------------------------
-- Table `teateach`.`questions`
-- Stores questions for quizzes and tests
-- -----------------------------------------------------
CREATE TABLE questions (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           question_text TEXT NOT NULL,
                           question_type VARCHAR(50) NOT NULL COMMENT 'e.g., SINGLE_CHOICE, MULTIPLE_CHOICE, SHORT_ANSWER, PROGRAMMING',
                           options TEXT COMMENT 'JSON string for multiple choice options',
                           correct_answer TEXT COMMENT 'JSON string for correct answer(s)',
                           explanation TEXT,
                           difficulty VARCHAR(50) COMMENT 'e.g., EASY, MEDIUM, HARD',
                           knowledge_point_ids TEXT COMMENT 'JSON array of IDs of associated knowledge points',
                           programming_language VARCHAR(50),
                           template_code TEXT,
                           test_cases TEXT COMMENT 'JSON string for programming question test cases',
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Table `teateach`.`test_papers`
-- Stores information about generated test papers
-- -----------------------------------------------------
CREATE TABLE test_papers (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             paper_name VARCHAR(255) NOT NULL,
                             course_id BIGINT NOT NULL,
                             instructor_id BIGINT COMMENT 'ID of the user (teacher) who created this paper',
                             question_ids TEXT NOT NULL COMMENT 'JSON array of IDs of questions included in this paper',
                             total_score DOUBLE,
                             duration_minutes INT,
                             generation_method VARCHAR(100) COMMENT 'e.g., RANDOM, BY_KNOWLEDGE_POINT, BY_DIFFICULTY',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
    -- FOREIGN KEY (instructor_id) REFERENCES users(id) -- Assuming a 'users' table if instructors are users
);

-- -----------------------------------------------------
-- Table `teateach`.`ability_points`
-- Stores predefined ability points for courses
-- -----------------------------------------------------
CREATE TABLE ability_points (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                name VARCHAR(255) NOT NULL,
                                description TEXT,
                                course_id BIGINT,
                                prerequisite_ability_point_ids TEXT COMMENT 'JSON array of IDs of prerequisite APs',
                                related_ability_point_ids TEXT COMMENT 'JSON array of IDs of related APs',
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Optional: Initial data or indexes
-- -----------------------------------------------------

-- Add indexes for common lookup fields to improve performance
CREATE INDEX idx_students_email ON students(email);
CREATE INDEX idx_courses_code ON courses(course_code);
CREATE INDEX idx_tasks_course_id ON learning_tasks(course_id);
CREATE INDEX idx_submissions_student_task ON student_task_submissions(student_id, task_id);
CREATE INDEX idx_grades_student_course ON grade_analysis(student_id, course_id);
CREATE INDEX idx_resources_course_id ON resources(course_id);
CREATE INDEX idx_video_progress_student_resource ON video_learning_progress(student_id, resource_id);
CREATE INDEX idx_knowledge_points_course_id ON knowledge_points(course_id);
CREATE INDEX idx_questions_type_difficulty ON questions(question_type, difficulty);
CREATE INDEX idx_test_papers_course_id ON test_papers(course_id);
CREATE INDEX idx_ability_points_course_id ON ability_points(course_id);

-- Commit changes
-- For DDL statements, auto-commit is often enabled by default.
-- If not, you might need a COMMIT statement.
-- COMMIT;