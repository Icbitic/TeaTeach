package org.bedrock.teateach.beans;

import lombok.Data;

@Data
public class VideoLearningProgress {
    private Long id;
    private Long studentId;
    private Long resourceId;
    private Long currentPlaybackTimeSeconds;
    private Double completionPercentage;
    private String skippedSegments;
    private String repeatedSegments;
    private Double totalPlayDurationSeconds;
}
