import apiClient from './api.js'

export const playbackService = {
  // Record played seconds for a video (increment count by 1 for each second)
  recordPlayedSeconds(studentId, resourceId, playedSeconds) {
    return apiClient.post('/playback/record', playedSeconds, {
      params: {
        studentId,
        resourceId
      }
    })
  },

  // Record playback count vector where each index represents a second and the value is how many times it was played
  recordPlaybackCounts(studentId, resourceId, playbackCountVector) {
    return apiClient.post('/playback/record-counts', playbackCountVector, {
      params: {
        studentId,
        resourceId
      }
    })
  },

  // Get the watch percentage for a student and resource
  getWatchProgress(studentId, resourceId) {
    return apiClient.get('/playback/progress', {
      params: {
        studentId,
        resourceId
      }
    })
  },

  // Get the heatmap data for a resource
  getHeatmap(resourceId) {
    return apiClient.get(`/playback/heatmap/${resourceId}`)
  },

  // Get all playback vectors for a resource
  getPlaybackVectors(resourceId) {
    return apiClient.get(`/playback/vectors/${resourceId}`)
  },

  // Get playback statistics for a resource
  getPlaybackStats(resourceId) {
    return apiClient.get(`/playback/stats/${resourceId}`)
  },

  // Get engagement analytics for a course
  getCourseEngagement(courseId) {
    return apiClient.get(`/playback/engagement/course/${courseId}`)
  },

  // Get student engagement data
  getStudentEngagement(studentId, resourceId = null) {
    const url = resourceId 
      ? `/playback/engagement/student/${studentId}/${resourceId}`
      : `/playback/engagement/student/${studentId}`
    return apiClient.get(url)
  },

  // Get aggregated analytics
  getAggregatedAnalytics(params = {}) {
    return apiClient.get('/playback/analytics', { params })
  },

  // Export playback data
  exportPlaybackData(resourceId, format = 'csv') {
    return apiClient.get(`/playback/export/${resourceId}`, {
      params: { format },
      responseType: 'blob'
    })
  }
}

export default playbackService