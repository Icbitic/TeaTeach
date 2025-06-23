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
    private byte[] playbackData; // Binary representation of the playback vector (0s and 1s)
    private Integer videoDuration; // Total duration in seconds
    private LocalDateTime lastUpdated;

    // Helper methods for manipulating the playback data

    // Convert a boolean array to a byte array for storage
    public void setPlaybackFromBooleanArray(boolean[] playbackArray) {
        if (playbackArray == null) {
            this.playbackData = new byte[0];
            return;
        }

        // Calculate the number of bytes needed (each byte can store 8 bits)
        int numBytes = (playbackArray.length + 7) / 8;
        byte[] bytes = new byte[numBytes];

        for (int i = 0; i < playbackArray.length; i++) {
            if (playbackArray[i]) {
                bytes[i / 8] |= (1 << (i % 8));
            }
        }

        this.playbackData = bytes;
    }

    // Convert the stored byte array to a boolean array
    public boolean[] getPlaybackAsBooleanArray() {
        if (playbackData == null || videoDuration == null) {
            return new boolean[0];
        }

        boolean[] result = new boolean[videoDuration];

        for (int i = 0; i < videoDuration && i < playbackData.length * 8; i++) {
            result[i] = (playbackData[i / 8] & (1 << (i % 8))) != 0;
        }

        return result;
    }

    // Update a specific second in the playback vector
    public void markSecondAsPlayed(int second) {
        if (playbackData == null || second < 0 || second >= videoDuration) {
            return;
        }

        // Ensure the playbackData array is large enough
        int byteIndex = second / 8;
        if (playbackData.length <= byteIndex) {
            byte[] newData = new byte[byteIndex + 1];
            System.arraycopy(playbackData, 0, newData, 0, playbackData.length);
            playbackData = newData;
        }

        // Set the bit at the specified position
        playbackData[byteIndex] |= (1 << (second % 8));
    }

    // Update multiple seconds in the playback vector
    public void markSecondsAsPlayed(int[] seconds) {
        for (int second : seconds) {
            markSecondAsPlayed(second);
        }
    }

    // Calculate the percentage of the video that has been played
    public double calculatePlaybackPercentage() {
        if (playbackData == null || videoDuration == null || videoDuration == 0) {
            return 0.0;
        }

        boolean[] playback = getPlaybackAsBooleanArray();
        int playedSeconds = 0;

        for (boolean played : playback) {
            if (played) {
                playedSeconds++;
            }
        }

        return (double) playedSeconds / videoDuration * 100.0;
    }
}
