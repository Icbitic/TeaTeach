package org.bedrock.teateach.beans;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class GradeAnalysis {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Double overallGrade;
    private Map<String, Double> taskGrades; // e.g., {"Quiz 1": 85.0, "Report": 92.5}
    private Map<LocalDate, Double> gradeTrend; // For historical grade tracking
}
