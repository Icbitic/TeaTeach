package org.bedrock.teateach.beans;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionFile {
    private Long id;
    private Long submissionId;
    private String fileName; // Original file name
    private String storedFileName; // UUID-based stored file name
    private String filePath; // Path to stored file
    private String fileType; // File extension (e.g., pdf, docx, jpg)
    private Long fileSize; // Size of the file in bytes
    private String mimeType; // MIME type of the file
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}