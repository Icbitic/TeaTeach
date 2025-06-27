package org.bedrock.teateach.beans;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgePoint {
    private Long id;
    private String name;
    private String briefDescription;
    private String detailedContent;
    private List<Long> prerequisiteKnowledgePointIds; // For graph relationships
    private List<Long> relatedKnowledgePointIds;
    private String difficultyLevel; // e.g., "BEGINNER", "INTERMEDIATE", "ADVANCED"
    private Long courseId; // Associated with a course
    private String courseName; // Course name for display purposes
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
