package org.bedrock.teateach.beans;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Resource {
    private Long id;
    private String resourceName;
    private String filePath; // Path to stored file
    private String fileType; // e.g., "pdf", "pptx", "mp4"
    private Long uploadedBy; // User ID of uploader
    private Long fileSize;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
