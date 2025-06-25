package org.bedrock.teateach.beans;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskResource {
    private Long id;
    private Long taskId;
    private Long resourceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor
    public TaskResource() {}
    
    public TaskResource(Long taskId, Long resourceId) {
        this.taskId = taskId;
        this.resourceId = resourceId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}