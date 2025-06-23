package org.bedrock.teateach.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackVector {
    private Long id;
    private Long studentId;
    private Long resourceId;
    private int[] playbackData; // Integer array representing the count of times each second is played
    private Integer videoDuration; // Total duration in seconds
    private LocalDateTime lastUpdated;

    // Helper methods for manipulating the playback data

    /**
     * Initialize the playback array with zeros
     */
    public void initializePlaybackData() {
        if (videoDuration == null || videoDuration <= 0) {
            this.playbackData = new int[0];
            return;
        }
        this.playbackData = new int[videoDuration];
    }

    /**
     * Set the playback data from an integer array
     */
    public void setPlaybackData(int[] playbackArray) {
        if (playbackArray == null) {
            this.playbackData = new int[0];
            return;
        }

        // If existing data is null or not the right size, initialize it
        if (this.playbackData == null || this.playbackData.length != playbackArray.length) {
            this.playbackData = new int[playbackArray.length];
        }

        // Copy the data
        System.arraycopy(playbackArray, 0, this.playbackData, 0, playbackArray.length);
    }

    /**
     * Get the playback data as an integer array
     */
    public int[] getPlaybackDataAsIntArray() {
        if (playbackData == null || videoDuration == null) {
            return new int[0];
        }

        // Return a copy to avoid external modification
        int[] result = new int[videoDuration];
        System.arraycopy(playbackData, 0, result, 0, Math.min(playbackData.length, videoDuration));
        return result;
    }

    /**
     * Increment the play count for a specific second
     */
    public void incrementSecondPlayCount(int second) {
        if (playbackData == null || second < 0 || second >= videoDuration) {
            return;
        }

        // Ensure the playbackData array is initialized
        if (playbackData.length <= second) {
            int[] newData = new int[videoDuration];
            System.arraycopy(playbackData, 0, newData, 0, playbackData.length);
            playbackData = newData;
        }

        // Increment the count at the specified position
        playbackData[second]++;
    }

    /**
     * Increment play counts for multiple seconds
     */
    public void incrementPlayCounts(int[] seconds) {
        for (int second : seconds) {
            incrementSecondPlayCount(second);
        }
    }

    /**
     * Update play counts from a vector representing the count for each second
     * e.g., [1, 2, 2, 2, 1] means 1st second played once, 2nd-4th seconds played twice, etc.
     */
    public void updatePlayCountVector(int[] countVector) {
        if (playbackData == null || countVector == null) {
            return;
        }

        // Ensure the playbackData array is initialized to the right size
        if (playbackData.length < countVector.length) {
            int[] newData = new int[Math.max(videoDuration, countVector.length)];
            System.arraycopy(playbackData, 0, newData, 0, playbackData.length);
            playbackData = newData;
        }

        // Add the new counts to the existing counts
        for (int i = 0; i < countVector.length; i++) {
            if (i < playbackData.length) {
                playbackData[i] += countVector[i];
            }
        }
    }

    /**
     * Calculate the percentage of the video that has been played at least once
     */
    public double calculatePlaybackPercentage() {
        if (playbackData == null || videoDuration == null || videoDuration == 0) {
            return 0.0;
        }

        int playedSeconds = 0;

        for (int i = 0; i < Math.min(playbackData.length, videoDuration); i++) {
            if (playbackData[i] > 0) {
                playedSeconds++;
            }
        }

        return (double) playedSeconds / videoDuration * 100.0;
    }

    /**
     * Get the total number of times any second in the video has been played
     */
    public int getTotalPlayCount() {
        if (playbackData == null) {
            return 0;
        }

        int total = 0;
        for (int count : playbackData) {
            total += count;
        }
        return total;
    }

    /**
     * Get the maximum play count for any second in the video
     */
    public int getMaxPlayCount() {
        if (playbackData == null || playbackData.length == 0) {
            return 0;
        }

        int max = 0;
        for (int count : playbackData) {
            if (count > max) {
                max = count;
            }
        }
        return max;
    }
}
