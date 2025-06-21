package org.bedrock.teateach.beans;

import lombok.Data;

@Data
public class StudentLearningData {
    private Long id;
    private Long studentId;
    private Long taskId; // Can be linked to a specific task
    private Long courseId; // Or overall course data
    private Double quizScore;
    private Double completionRate; // e.g., for video or reading
    private Long timeSpentSeconds; // e.g., for video or reading
    private String additionalData; // Flexible field for JSON or other serialized data
    // This bean might be more complex, potentially aggregating data from other tables
    // or representing a specific learning event.
}
