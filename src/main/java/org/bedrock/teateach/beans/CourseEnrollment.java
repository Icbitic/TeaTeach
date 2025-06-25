package org.bedrock.teateach.beans;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseEnrollment {
    private Long id;
    private Long courseId;
    private Long studentId;
    private LocalDateTime enrollmentDate;
    private String status; // ACTIVE, INACTIVE, DROPPED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for joined queries
    private Course course;
    private Student student;
}