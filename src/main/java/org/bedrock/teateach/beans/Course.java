package org.bedrock.teateach.beans;

import lombok.Data;

@Data
public class Course {
    private Long id;
    private String courseCode;
    private String courseName;
    private String instructor;
    private Double credits;
    private Integer hours;
    private String description;
}
