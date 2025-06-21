package org.bedrock.teateach.beans;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Student {
    private Long id;
    private String studentId;
    private String name;
    private String email;
    private String major;
    private LocalDate dateOfBirth;
}