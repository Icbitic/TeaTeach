import { apiClient } from './api.js'

export const studentAbilityService = {
  // Analyze student abilities
  analyzeStudentAbilities(studentId) {
    return apiClient.get(`/student-abilities/analyze/${studentId}`)
  },

  // Get resource recommendations for a student
  getResourceRecommendations(studentId, resourceType = null) {
    const params = resourceType ? { resourceType } : {}
    return apiClient.get(`/student-abilities/recommend/${studentId}`, { params })
  },

  // Get comprehensive resource recommendations
  getComprehensiveRecommendations(studentId) {
    return apiClient.get(`/student-abilities/recommend/${studentId}/comprehensive`)
  },

  // Test RAG-enhanced recommendations directly
  testRagRecommendations(studentId, resourceType = 'all') {
    return apiClient.get(`/student-abilities/recommend/${studentId}`, {
      params: { resourceType }
    })
  },

  // Get analysis system information
  getAnalysisInfo() {
    return apiClient.get('/student-abilities/info')
  }
}

export default studentAbilityService