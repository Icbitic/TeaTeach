import { apiClient } from './api.js'

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
  },

  // Update feedback for submission
  updateSubmissionFeedback(submissionId, feedback) {
    return apiClient.put(`/submissions/${submissionId}/feedback?feedback=${encodeURIComponent(feedback)}`)
  },

  // Grade submission (update both score and feedback)
  gradeSubmission(submissionId, score, feedback) {
    let url = `/submissions/${submissionId}/grade?score=${score}`
    if (feedback) {
      url += `&feedback=${encodeURIComponent(feedback)}`
    }
    return apiClient.put(url)
  },

  // Upload file for submission
  uploadSubmissionFile(submissionId, file) {
    const formData = new FormData()
    formData.append('file', file)

    return apiClient.post(`/submissions/${submissionId}/files`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Get files for submission
  getSubmissionFiles(submissionId) {
    return apiClient.get(`/submissions/${submissionId}/files`)
  },

  // Download submission file
  downloadSubmissionFile(fileId) {
    return apiClient.get(`/submissions/files/${fileId}/download`, {
      responseType: 'blob'
    })
  },

  // Delete submission file
  deleteSubmissionFile(fileId) {
    return apiClient.delete(`/submissions/files/${fileId}`)
  },

  // LLM-based grading
  llmGradeSubmission(submissionId, gradingRubric = null) {
    let url = `/submissions/${submissionId}/llm-grade`
    const params = {}
    if (gradingRubric) {
      params.gradingRubric = gradingRubric
    }
    return apiClient.post(url, null, { params })
  },

  // LLM-based feedback generation
  llmGenerateFeedback(submissionId, feedbackPrompt = null) {
    let url = `/submissions/${submissionId}/llm-feedback`
    const params = {}
    if (feedbackPrompt) {
      params.feedbackPrompt = feedbackPrompt
    }
    return apiClient.post(url, null, { params })
  },

  // Batch LLM grading
  batchLlmGrade(submissionIds, gradingRubric = null) {
    const data = {
      submissionIds: submissionIds
    }
    if (gradingRubric) {
      data.gradingRubric = gradingRubric
    }
    return apiClient.post('/submissions/batch-llm-grade', data)
  }
}

export default submissionService