# Video Playback Heatmap Functionality

This module implements functionality to track student video viewing patterns and generate heatmaps showing the most-watched sections of educational videos.

## Core Components

### PlaybackVector

The `PlaybackVector` is the core data model that tracks which seconds of a video have been watched by a specific student. It stores:

- A binary representation of video seconds (1 = watched, 0 = not watched)
- Student ID and Resource ID to link the data to specific users and videos
- The total duration of the video
- Last updated timestamp

The model includes helper methods for manipulating the binary playback data efficiently, using bit operations to minimize storage requirements.

### PlaybackVectorService

The service layer handles:

- Creating and updating playback vectors
- Generating heatmaps by aggregating data from multiple students
- Calculating watch percentages for individual students

### PlaybackVectorController

The REST API provides endpoints for:

- Recording played seconds incrementally (e.g., when a student watches parts of a video)
- Retrieving watch progress for a student
- Getting heatmap data for instructor analysis

## Technical Implementation

### Efficient Storage

The playback data is stored as a bit vector to minimize space requirements:

- Each second of video requires only 1 bit of storage
- A 1-hour video requires only 3.6KB of storage per student

### Data Structure

The implementation uses a byte array to store the playback data, with bit manipulation to set and check individual seconds:

```java
// To mark a second as played:
bytes[second / 8] |= (1 << (second % 8));

// To check if a second was played:
boolean played = (bytes[second / 8] & (1 << (second % 8))) != 0;
```

## Usage

### Frontend Integration

Integrate with the frontend by making API calls:

1. When a student watches a video section, call `/api/playback/record` with the played seconds
2. To display a progress bar, call `/api/playback/progress`
3. For instructor dashboards, use `/api/playback/heatmap/{resourceId}` to get visualization data

### Sample Heatmap Visualization

The heatmap data can be visualized as a color-coded overlay on the video progress bar:

- Darker/more intense colors indicate more student views
- Helps instructors identify which parts of a video are most engaging or confusing
- Can guide instructional design improvements

## Next Steps

- Add analytics for video engagement patterns
- Implement recommendations based on viewing behavior
- Integrate with learning outcomes assessment
