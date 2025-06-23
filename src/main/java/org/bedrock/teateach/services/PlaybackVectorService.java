package org.bedrock.teateach.services;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.teateach.beans.PlaybackVector;
import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.mappers.PlaybackVectorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class PlaybackVectorService {

    private final PlaybackVectorMapper playbackVectorMapper;
    private final ResourceService resourceService;

    @Autowired
    public PlaybackVectorService(PlaybackVectorMapper playbackVectorMapper, ResourceService resourceService) {
        this.playbackVectorMapper = playbackVectorMapper;
        this.resourceService = resourceService;
    }

    /**
     * Get the playback vector for a specific student and resource
     */
    @Cacheable(value = "playbackVectors", key = "#studentId + '-' + #resourceId")
    public PlaybackVector getPlaybackVector(Long studentId, Long resourceId) {
        return playbackVectorMapper.findByStudentAndResource(studentId, resourceId);
    }

    /**
     * Get all playback vectors for a specific resource
     */
    @Cacheable(value = "resourcePlaybackVectors", key = "#resourceId")
    public List<PlaybackVector> getPlaybackVectorsByResource(Long resourceId) {
        return playbackVectorMapper.findByResourceId(resourceId);
    }

    /**
     * Get all playback vectors for a specific student
     */
    @Cacheable(value = "studentPlaybackVectors", key = "#studentId")
    public List<PlaybackVector> getPlaybackVectorsByStudent(Long studentId) {
        return playbackVectorMapper.findByStudentId(studentId);
    }

    /**
     * Create a new playback vector for a student and resource
     */
    @Transactional
    @CacheEvict(value = {"playbackVectors", "resourcePlaybackVectors", "studentPlaybackVectors"}, allEntries = true)
    public PlaybackVector createPlaybackVector(Long studentId, Long resourceId) {
        // Check if playback vector already exists
        PlaybackVector existing = playbackVectorMapper.findByStudentAndResource(studentId, resourceId);
        if (existing != null) {
            return existing;
        }

        // Get resource to determine video duration
        Resource resource = resourceService.getResourceById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));

        // Create new playback vector
        PlaybackVector playbackVector = new PlaybackVector();
        playbackVector.setStudentId(studentId);
        playbackVector.setResourceId(resourceId);
        playbackVector.setVideoDuration(getVideoDuration(resource)); // Implement this method based on resource metadata
        // Initialize with empty array that has proper length
        int[] emptyData = new int[playbackVector.getVideoDuration()];
        playbackVector.setPlaybackData(emptyData); // Initialize empty vector with zeros
        playbackVector.setLastUpdated(LocalDateTime.now());

        playbackVectorMapper.insert(playbackVector);
        return playbackVector;
    }

    /**
     * Update playback vector with seconds that were played once
     */
    @Transactional
    @CacheEvict(value = {"playbackVectors", "resourcePlaybackVectors", "studentPlaybackVectors"}, allEntries = true)
    public PlaybackVector updatePlaybackVector(Long studentId, Long resourceId, int[] playedSeconds) {
        // Get existing playback vector or create new one
        PlaybackVector playbackVector = playbackVectorMapper.findByStudentAndResource(studentId, resourceId);
        if (playbackVector == null) {
            playbackVector = createPlaybackVector(studentId, resourceId);
        }

        // Update the playback data with the played seconds
        playbackVector.incrementPlayCounts(playedSeconds);
        playbackVector.setLastUpdated(LocalDateTime.now());

        // Save the updated playback vector
        playbackVectorMapper.update(playbackVector);

        return playbackVector;
    }

    /**
     * Update playback vector with a playback count vector
     * @param studentId The student ID
     * @param resourceId The resource ID
     * @param playbackCountVector Array where each index represents a second and the value represents how many times it was played
     * @return The updated PlaybackVector
     */
    @Transactional
    @CacheEvict(value = {"playbackVectors", "resourcePlaybackVectors", "studentPlaybackVectors"}, allEntries = true)
    public PlaybackVector updatePlaybackVectorWithCounts(Long studentId, Long resourceId, int[] playbackCountVector) {
        // Get existing playback vector or create new one
        PlaybackVector playbackVector = playbackVectorMapper.findByStudentAndResource(studentId, resourceId);
        if (playbackVector == null) {
            playbackVector = createPlaybackVector(studentId, resourceId);
        }

        // Update the playback data with the count vector
        playbackVector.updatePlayCountVector(playbackCountVector);
        playbackVector.setLastUpdated(LocalDateTime.now());

        // Save the updated playback vector
        playbackVectorMapper.update(playbackVector);

        return playbackVector;
    }

    /**
     * Generate a heatmap for a resource showing which seconds are most watched
     * The heatmap represents the total count of plays for each second across all students
     */
    public int[] generateHeatmap(Long resourceId) {
        // Get all playback vectors for this resource
        List<PlaybackVector> playbackVectors = getPlaybackVectorsByResource(resourceId);
        if (playbackVectors.isEmpty()) {
            return new int[0];
        }

        // Determine the max duration
        int maxDuration = playbackVectors.stream()
                .mapToInt(PlaybackVector::getVideoDuration)
                .max()
                .orElse(0);

        // Initialize heatmap array
        int[] heatmap = new int[maxDuration];

        // Aggregate data from all playback vectors by adding play counts for each second
        for (PlaybackVector vector : playbackVectors) {
            int[] playbackData = vector.getPlaybackDataAsIntArray();
            for (int i = 0; i < playbackData.length && i < maxDuration; i++) {
                heatmap[i] += playbackData[i];
            }
        }

        return heatmap;
    }

    /**
     * Get the percentage of a video watched by a student
     */
    public double getWatchPercentage(Long studentId, Long resourceId) {
        PlaybackVector playbackVector = getPlaybackVector(studentId, resourceId);
        if (playbackVector == null) {
            return 0.0;
        }
        return playbackVector.calculatePlaybackPercentage();
    }

    /**
     * Helper method to determine video duration from resource metadata
     * In a real application, this would extract the duration from the video file
     */
    private Integer getVideoDuration(Resource resource) {
        // In a real app, you would extract this from the video file's metadata
        // For this example, we'll use a placeholder calculation based on file size
        // Assuming roughly 500KB per minute of video at standard quality
        if (resource.getFileSize() == null) {
            return 300; // Default 5 minutes (300 seconds) if size unknown
        }
        long sizeInKB = resource.getFileSize() / 1024;
        int estimatedSeconds = (int) (sizeInKB / 500 * 60);
        return Math.max(estimatedSeconds, 60); // At least 1 minute
    }
}
