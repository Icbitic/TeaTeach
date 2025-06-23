package org.bedrock.teateach.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bedrock.teateach.beans.PlaybackVector;
import org.bedrock.teateach.services.PlaybackVectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlaybackVectorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlaybackVectorService playbackVectorService;

    @InjectMocks
    private PlaybackVectorController controller;

    private ObjectMapper objectMapper;
    private Long studentId = 1L;
    private Long resourceId = 2L;
    private PlaybackVector testVector;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        // Set up a test playback vector
        testVector = new PlaybackVector();
        testVector.setId(1L);
        testVector.setStudentId(studentId);
        testVector.setResourceId(resourceId);
        testVector.setVideoDuration(120); // 2 minutes
        testVector.setPlaybackData(new int[15]); // Initialize with empty data
        testVector.setLastUpdated(LocalDateTime.now());

        // Mark 20 seconds as played (approx 16.7%)
        for (int i = 0; i < 20; i++) {
            testVector.incrementSecondPlayCount(i);
        }
    }

    @Test
    void recordPlayedSeconds_shouldReturnSuccess() throws Exception {
        // Given
        int[] playedSeconds = {30, 31, 32, 33, 34};
        when(playbackVectorService.updatePlaybackVector(eq(studentId), eq(resourceId), any(int[].class)))
                .thenReturn(testVector);

        // When & Then
        mockMvc.perform(post("/api/playback/record")
                .param("studentId", studentId.toString())
                .param("resourceId", resourceId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playedSeconds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.watchPercentage").exists());
    }

    @Test
    void getWatchProgress_shouldReturnPercentage() throws Exception {
        // Given
        double expectedPercentage = 16.7;
        when(playbackVectorService.getWatchPercentage(studentId, resourceId))
                .thenReturn(expectedPercentage);

        // When & Then
        mockMvc.perform(get("/api/playback/progress")
                .param("studentId", studentId.toString())
                .param("resourceId", resourceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId", is(studentId.intValue())))
                .andExpect(jsonPath("$.resourceId", is(resourceId.intValue())))
                .andExpect(jsonPath("$.watchPercentage", is(expectedPercentage)));
    }

    @Test
    void getHeatmap_shouldReturnHeatmapData() throws Exception {
        // Given
        int[] heatmapData = new int[120];
        // Simulate some heat at different points
        heatmapData[10] = 5;
        heatmapData[20] = 8;
        heatmapData[30] = 3;
        heatmapData[60] = 10;

        when(playbackVectorService.generateHeatmap(resourceId)).thenReturn(heatmapData);
        when(playbackVectorService.getPlaybackVectorsByResource(resourceId)).thenReturn(java.util.List.of(testVector));

        // When & Then
        mockMvc.perform(get("/api/playback/heatmap/{resourceId}", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId", is(resourceId.intValue())))
                .andExpect(jsonPath("$.heatmapData").isArray())
                .andExpect(jsonPath("$.totalViews", is(1)));
    }

    @Test
    void getWatchProgress_shouldHandleException() throws Exception {
        // Given
        when(playbackVectorService.getWatchPercentage(studentId, resourceId))
                .thenThrow(new RuntimeException("Test error"));

        // When & Then
        mockMvc.perform(get("/api/playback/progress")
                .param("studentId", studentId.toString())
                .param("resourceId", resourceId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void recordPlaybackCounts_shouldReturnSuccess() throws Exception {
        // Given
        int[] playbackCountVector = {1, 2, 2, 2, 1, 0, 0, 3, 1}; // Example count vector

        // Mock vector with some statistics
        testVector.setPlaybackData(playbackCountVector);
        double expectedPercentage = 77.8; // 7 out of 9 seconds have non-zero count
        int expectedTotalCount = 12; // Sum of all counts
        int expectedMaxCount = 3; // Maximum count

        // Create a stub for the PlaybackVector that will be returned by the service
        PlaybackVector mockedVector = mock(PlaybackVector.class);
        when(mockedVector.calculatePlaybackPercentage()).thenReturn(expectedPercentage);
        when(mockedVector.getTotalPlayCount()).thenReturn(expectedTotalCount);
        when(mockedVector.getMaxPlayCount()).thenReturn(expectedMaxCount);

        // Mock the service call to return our mocked vector
        when(playbackVectorService.updatePlaybackVectorWithCounts(eq(studentId), eq(resourceId), any(int[].class)))
                .thenReturn(mockedVector);

        // When & Then
        mockMvc.perform(post("/api/playback/record-counts")
                .param("studentId", studentId.toString())
                .param("resourceId", resourceId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playbackCountVector)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.watchPercentage", is(expectedPercentage)))
                .andExpect(jsonPath("$.totalPlayCount", is(expectedTotalCount)))
                .andExpect(jsonPath("$.maxPlayCount", is(expectedMaxCount)));
    }
}
