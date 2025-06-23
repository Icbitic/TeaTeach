package org.bedrock.teateach.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String userType; // "STUDENT" or "TEACHER"
    private String name;
    private String studentId; // Only for students
    private String teacherId; // Only for teachers
    private String major; // Only for students
    private String department; // Only for teachers
}
