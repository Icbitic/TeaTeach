import { apiClient } from './api'

export const submissionService = {
  // Get all submissions for a student
  getSubmissionsByStudent(studentId) {
    return apiClient.get(`/submissions/student/${studentId}`)
  },

  // Get all submissions for a specific task
  getSubmissionsByTask(taskId) {
    return apiClient.get(`/submissions/task/${taskId}`)
  },

  // Get submission by ID
  getSubmissionById(id) {
    return apiClient.get(`/submissions/${id}`)
  },

  // Create new submission
  createSubmission(submissionData) {
    return apiClient.post('/submissions', submissionData)
  },

  // Update existing submission
  updateSubmission(id, submissionData) {
    return apiClient.put(`/submissions/${id}`, submissionData)
  },

  // Delete submission
  deleteSubmission(id) {
    return apiClient.delete(`/submissions/${id}`)
  },

  // Record/update score for submission
  recordSubmissionScore(submissionId, score) {
    return apiClient.put(`/submissions/${submissionId}/score?score=${score}`)
  }
}

export default submissionService