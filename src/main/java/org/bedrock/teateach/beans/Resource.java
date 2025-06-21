package org.bedrock.teateach.beans;

import lombok.Data;

@Data
public class Resource {
    private Long id;
    private Long courseId;
    private Long taskId; // Optional: if resource is tied to a specific task
    private String resourceName;
    private String filePath; // Path to stored file
    private String fileType; // e.g., "pdf", "pptx", "mp4"
    private String uploadedBy; // User ID of uploader
    private Long fileSize;
    private String description;
}
