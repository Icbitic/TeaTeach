import apiClient from './api.js'

export const knowledgePointService = {
  // Get all knowledge points
  getAllKnowledgePoints() {
    return apiClient.get('/knowledge-points')
  },

  // Get knowledge point by ID
  getKnowledgePointById(id) {
    return apiClient.get(`/knowledge-points/${id}`)
  },

  // Get knowledge points by course ID
  getKnowledgePointsByCourseId(courseId) {
    return apiClient.get(`/knowledge-points/course/${courseId}`)
  },

  // Create new knowledge point
  createKnowledgePoint(knowledgePointData) {
    return apiClient.post('/knowledge-points', knowledgePointData)
  },

  // Update existing knowledge point
  updateKnowledgePoint(id, knowledgePointData) {
    return apiClient.put(`/knowledge-points/${id}`, knowledgePointData)
  },

  // Delete knowledge point
  deleteKnowledgePoint(id) {
    return apiClient.delete(`/knowledge-points/${id}`)
  },

  // Generate knowledge graph from content
  generateKnowledgeGraphFromContent(courseId, courseContent) {
    return apiClient.post(`/knowledge-points/generate-from-content/${courseId}`, {
      courseContent: courseContent
    })
  }
}

export default knowledgePointService