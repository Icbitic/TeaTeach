package org.bedrock.teateach.controllers;

import lombok.extern.slf4j.Slf4j;
import org.bedrock.teateach.beans.PlaybackVector;
import org.bedrock.teateach.services.PlaybackVectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/playback")
@Slf4j
public class PlaybackVectorController {

    private final PlaybackVectorService playbackVectorService;

    @Autowired
    public PlaybackVectorController(PlaybackVectorService playbackVectorService) {
        this.playbackVectorService = playbackVectorService;
    }

    /**
     * Record played seconds for a student's video
     */
    @PostMapping("/record")
    public ResponseEntity<?> recordPlayedSeconds(
            @RequestParam Long studentId, 
            @RequestParam Long resourceId,
            @RequestBody int[] playedSeconds) {

        log.info("Recording {} played seconds for student {} and resource {}", 
                playedSeconds.length, studentId, resourceId);

        try {
            PlaybackVector updatedVector = playbackVectorService.updatePlaybackVector(
                    studentId, resourceId, playedSeconds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("watchPercentage", updatedVector.calculatePlaybackPercentage());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recording playback data", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Get the watch percentage for a student and resource
     */
    @GetMapping("/progress")
    public ResponseEntity<?> getWatchProgress(
            @RequestParam Long studentId,
            @RequestParam Long resourceId) {

        try {
            double percentage = playbackVectorService.getWatchPercentage(studentId, resourceId);
            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "resourceId", resourceId,
                    "watchPercentage", percentage
            ));
        } catch (Exception e) {
            log.error("Error getting watch progress", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Get the heatmap data for a resource
     */
    @GetMapping("/heatmap/{resourceId}")
    public ResponseEntity<?> getHeatmap(@PathVariable Long resourceId) {
        try {
            int[] heatmap = playbackVectorService.generateHeatmap(resourceId);
            return ResponseEntity.ok(Map.of(
                    "resourceId", resourceId,
                    "heatmapData", heatmap,
                    "totalViews", heatmap.length > 0 ? 
                            playbackVectorService.getPlaybackVectorsByResource(resourceId).size() : 0
            ));
        } catch (Exception e) {
            log.error("Error generating heatmap", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
