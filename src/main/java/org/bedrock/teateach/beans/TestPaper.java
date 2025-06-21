package org.bedrock.teateach.beans;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TestPaper {
    private Long id;
    private String paperName;
    private Long courseId;
    private Long instructorId; // ID of the instructor who created it
    private List<Long> questionIds; // List of question IDs included in the paper
    private Double totalScore;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
    private String generationMethod; // e.g., "RANDOM", "BY_KNOWLEDGE_POINT", "BY_DIFFICULTY"
}
