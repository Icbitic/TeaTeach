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
        testVector.setPlaybackData(new byte[15]); // Initialize with empty data
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

        // Verify seconds were marked as played
        boolean[] playback = result.getPlaybackAsBooleanArray();
        for (int second : playedSeconds) {
            assertTrue(playback[second], "Second " + second + " should be marked as played");
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
        vector1.setPlaybackData(new byte[(100 + 7) / 8]);
        vector1.markSecondsAsPlayed(new int[]{1, 2, 3, 10, 11});

        PlaybackVector vector2 = new PlaybackVector();
        vector2.setVideoDuration(100);
        vector2.setPlaybackData(new byte[(100 + 7) / 8]);
        vector2.markSecondsAsPlayed(new int[]{1, 3, 5, 11, 12});

        List<PlaybackVector> vectors = Arrays.asList(vector1, vector2);
        when(playbackVectorMapper.findByResourceId(resourceId)).thenReturn(vectors);

        // When
        int[] heatmap = playbackVectorService.generateHeatmap(resourceId);

        // Then
        assertNotNull(heatmap);
        assertEquals(100, heatmap.length);
        assertEquals(2, heatmap[1]); // Both watched second 1
        assertEquals(1, heatmap[2]); // Only vector1 watched second 2
        assertEquals(2, heatmap[3]); // Both watched second 3
        assertEquals(1, heatmap[5]); // Only vector2 watched second 5
        assertEquals(0, heatmap[6]); // Nobody watched second 6
        assertEquals(1, heatmap[10]); // Only vector1 watched second 10
        assertEquals(2, heatmap[11]); // Both watched second 11
        assertEquals(1, heatmap[12]); // Only vector2 watched second 12
    }

    @Test
    void getWatchPercentage_shouldCalculatePercentage() {
        // Given
        PlaybackVector vector = new PlaybackVector();
        vector.setVideoDuration(100);
        vector.setPlaybackData(new byte[(100 + 7) / 8]);
        vector.markSecondsAsPlayed(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}); // 10 seconds watched

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
}
