package org.bedrock.teateach.beans;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentTaskSubmission {
    private Long id;
    private Long taskId;
    private Long studentId;
    private String submissionContent; // e.g., file path, text answer
    private LocalDateTime submissionTime;
    private Double score; // Nullable until graded
    private String feedback; // For teacher feedback or automated feedback
    private Integer completionStatus; // e.g., 0: not started, 1: in progress, 2: submitted, 3: graded
}
