package org.bedrock.teateach.beans;

import lombok.Builder;
import lombok.Data;
import org.bedrock.teateach.enums.TaskType;

import java.time.LocalDateTime;

@Data
public class LearningTask {
    private Long id;
    private Long courseId;
    private String taskName;
    private TaskType taskType; // Enum for different task types
    private String taskDescription;
    private LocalDateTime deadline;
    private String submissionMethod; // e.g., "upload"
}
