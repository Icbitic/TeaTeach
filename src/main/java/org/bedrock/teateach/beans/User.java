package org.bedrock.teateach.beans;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String userType; // "STUDENT" or "TEACHER"
    private String referenceId; // studentId or teacherId
    private Boolean active;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
