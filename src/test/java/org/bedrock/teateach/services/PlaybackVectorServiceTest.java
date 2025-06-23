package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.PlaybackVector;
import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.mappers.PlaybackVectorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaybackVectorServiceTest {

    @Mock
    private PlaybackVectorMapper playbackVectorMapper;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private PlaybackVectorService playbackVectorService;

    private PlaybackVector testVector;
    private Resource testResource;
    private Long studentId = 1L;
    private Long resourceId = 2L;

    @BeforeEach
    void setUp() {
        // Set up test resource
        testResource = new Resource();
        testResource.setId(resourceId);
        testResource.setFileSize(1024L * 1024L); // 1MB

        // Set up test playback vector
        testVector = new PlaybackVector();
        testVector.setId(1L);
        testVector.setStudentId(studentId);
        testVector.setResourceId(resourceId);
        testVector.setVideoDuration(120); // 2 minutes
        testVector.setPlaybackData(new int[120]); // Initialize with zeros
        testVector.setLastUpdated(LocalDateTime.now());
    }

    @Test
    void getPlaybackVector_shouldReturnVector_whenExists() {
        // Given
        when(playbackVectorMapper.findByStudentAndResource(studentId, resourceId)).thenReturn(testVector);

        // When
        PlaybackVector result = playbackVectorService.getPlaybackVector(studentId, resourceId);

        // Then
        assertNotNull(result);
        assertEquals(testVector, result);
        verify(playbackVectorMapper).findByStudentAndResource(studentId, resourceId);
    }

    @Test
    void getPlaybackVectorsByResource_shouldReturnList() {
        // Given
        List<PlaybackVector> vectors = List.of(testVector);
        when(playbackVectorMapper.findByResourceId(resourceId)).thenReturn(vectors);

        // When
        List<PlaybackVector> result = playbackVectorService.getPlaybackVectorsByResource(resourceId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(vectors, result);
        verify(playbackVectorMapper).findByResourceId(resourceId);
    }

    @Test
    void createPlaybackVector_shouldCreateNew_whenNotExists() {
        // Given
        when(playbackVectorMapper.findByStudentAndResource(studentId, resourceId)).thenReturn(null);
        when(resourceService.getResourceById(resourceId)).thenReturn(Optional.of(testResource));

        // When
        PlaybackVector result = playbackVectorService.createPlaybackVector(studentId, resourceId);

        // Then
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(resourceId, result.getResourceId());
        verify(playbackVectorMapper).insert(any(PlaybackVector.class));
    }

    @Test
    void updatePlaybackVector_shouldUpdate_whenExists() {
        // Given
        int[] playedSeconds = {10, 11, 12, 30, 31, 32};
        when(playbackVectorMapper.findByStudentAndResource(studentId, resourceId)).thenReturn(testVector);

        // When
        PlaybackVector result = playbackVectorService.updatePlaybackVector(studentId, resourceId, playedSeconds);

        // Then
        assertNotNull(result);
        verify(playbackVectorMapper).update(any(PlaybackVector.class));

        // Verify seconds were incremented
        int[] playbackData = result.getPlaybackDataAsIntArray();
        for (int second : playedSeconds) {
            assertEquals(1, playbackData[second], "Second " + second + " should have been incremented");
        }
    }

    @Test
    void updatePlaybackVector_shouldCreateAndUpdate_whenNotExists() {
        // Given
        int[] playedSeconds = {5, 6, 7};
        when(playbackVectorMapper.findByStudentAndResource(studentId, resourceId)).thenReturn(null);
        when(resourceService.getResourceById(resourceId)).thenReturn(Optional.of(testResource));

        // When
        PlaybackVector result = playbackVectorService.updatePlaybackVector(studentId, resourceId, playedSeconds);

        // Then
        assertNotNull(result);
        verify(playbackVectorMapper).insert(any(PlaybackVector.class));
        verify(playbackVectorMapper).update(any(PlaybackVector.class));
    }

    @Test
    void generateHeatmap_shouldAggregateData() {
        // Given
        PlaybackVector vector1 = new PlaybackVector();
        vector1.setVideoDuration(100);
        vector1.setPlaybackData(new int[100]);
        // Set some playback counts in vector1
        vector1.getPlaybackData()[1] = 2;  // Second 1 played twice
        vector1.getPlaybackData()[2] = 1;  // Second 2 played once
        vector1.getPlaybackData()[3] = 3;  // Second 3 played three times
        vector1.getPlaybackData()[10] = 1; // Second 10 played once
        vector1.getPlaybackData()[11] = 2; // Second 11 played twice

        PlaybackVector vector2 = new PlaybackVector();
        vector2.setVideoDuration(100);
        vector2.setPlaybackData(new int[100]);
        // Set some playback counts in vector2
        vector2.getPlaybackData()[1] = 1;  // Second 1 played once
        vector2.getPlaybackData()[3] = 2;  // Second 3 played twice
        vector2.getPlaybackData()[5] = 3;  // Second 5 played three times
        vector2.getPlaybackData()[11] = 1; // Second 11 played once
        vector2.getPlaybackData()[12] = 2; // Second 12 played twice

        List<PlaybackVector> vectors = Arrays.asList(vector1, vector2);
        when(playbackVectorMapper.findByResourceId(resourceId)).thenReturn(vectors);

        // When
        int[] heatmap = playbackVectorService.generateHeatmap(resourceId);

        // Then
        assertNotNull(heatmap);
        assertEquals(100, heatmap.length);
        assertEquals(3, heatmap[1]);  // Total: 2+1=3 times for second 1
        assertEquals(1, heatmap[2]);  // Total: 1 time for second 2
        assertEquals(5, heatmap[3]);  // Total: 3+2=5 times for second 3
        assertEquals(3, heatmap[5]);  // Total: 3 times for second 5
        assertEquals(0, heatmap[6]);  // Total: 0 times for second 6
        assertEquals(1, heatmap[10]); // Total: 1 time for second 10
        assertEquals(3, heatmap[11]); // Total: 2+1=3 times for second 11
        assertEquals(2, heatmap[12]); // Total: 2 times for second 12
    }

    @Test
    void getWatchPercentage_shouldCalculatePercentage() {
        // Given
        PlaybackVector vector = new PlaybackVector();
        vector.setVideoDuration(100);
        vector.setPlaybackData(new int[(100 + 7) / 8]);
        vector.incrementPlayCounts(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}); // 10 seconds watched

        when(playbackVectorMapper.findByStudentAndResource(studentId, resourceId)).thenReturn(vector);

        // When
        double percentage = playbackVectorService.getWatchPercentage(studentId, resourceId);

        // Then
        assertEquals(10.0, percentage);
    }

    @Test
    void getWatchPercentage_shouldReturnZero_whenNoVector() {
        // Given
        when(playbackVectorMapper.findByStudentAndResource(studentId, resourceId)).thenReturn(null);

        // When
        double percentage = playbackVectorService.getWatchPercentage(studentId, resourceId);

        // Then
        assertEquals(0.0, percentage);
    }

    @Test
    void updatePlaybackVectorWithCounts_shouldUpdateCounts() {
        // Given
        int[] existingCounts = new int[120];
        existingCounts[5] = 2;  // Second 5 already played twice
        existingCounts[10] = 1; // Second 10 already played once

        testVector.setPlaybackData(existingCounts);

        int[] newCountVector = new int[120];
        newCountVector[5] = 1;  // Second 5 played once more
        newCountVector[10] = 2; // Second 10 played twice more
        newCountVector[15] = 3; // Second 15 played three times

        when(playbackVectorMapper.findByStudentAndResource(studentId, resourceId)).thenReturn(testVector);

        // When
        PlaybackVector result = playbackVectorService.updatePlaybackVectorWithCounts(studentId, resourceId, newCountVector);

        // Then
        assertNotNull(result);
        verify(playbackVectorMapper).update(any(PlaybackVector.class));

        // Verify counts were accumulated correctly
        int[] finalCounts = result.getPlaybackDataAsIntArray();
        assertEquals(3, finalCounts[5], "Second 5 should have accumulated count of 3");
        assertEquals(3, finalCounts[10], "Second 10 should have accumulated count of 3");
        assertEquals(3, finalCounts[15], "Second 15 should have count of 3");
        assertEquals(0, finalCounts[20], "Second 20 should have count of 0");

        // Verify other helper methods
        assertEquals(9, result.getTotalPlayCount(), "Total play count should be 9");
        assertEquals(3, result.getMaxPlayCount(), "Max play count should be 3");
    }
}
