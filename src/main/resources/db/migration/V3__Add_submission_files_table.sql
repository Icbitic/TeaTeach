-- Create submission_files table to store uploaded files for task submissions
CREATE TABLE teateach.submission_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL COMMENT 'Original file name',
    stored_file_name VARCHAR(255) NOT NULL COMMENT 'UUID-based stored file name',
    file_path VARCHAR(512) NOT NULL COMMENT 'Path to stored file',
    file_type VARCHAR(50) NOT NULL COMMENT 'File extension (e.g., pdf, docx, jpg)',
    file_size BIGINT NULL COMMENT 'Size of the file in bytes',
    mime_type VARCHAR(100) NULL COMMENT 'MIME type of the file',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_submission_files_submission_id
        FOREIGN KEY (submission_id) REFERENCES teateach.student_task_submissions (id)
            ON DELETE CASCADE
);

-- Create index for faster queries
CREATE INDEX idx_submission_files_submission_id
    ON teateach.submission_files (submission_id);