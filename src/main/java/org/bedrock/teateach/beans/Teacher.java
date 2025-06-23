package org.bedrock.teateach.beans;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Teacher {
    private Long id;
    private String teacherId;
    private String name;
    private String email;
    private String department;
    private LocalDate dateOfBirth;
}
