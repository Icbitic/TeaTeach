import api from './api'

export const testPaperService = {
  // Get all test papers with optional filters
  async getTestPapers(params = {}) {
    try {
      const response = await api.get('/test-papers', { params })
      return response.data
    } catch (error) {
      console.error('Error fetching test papers:', error)
      throw error
    }
  },

  // Get a single test paper by ID
  async getTestPaperById(id) {
    try {
      const response = await api.get(`/test-papers/${id}`)
      return response.data
    } catch (error) {
      console.error('Error fetching test paper:', error)
      throw error
    }
  },

  // Create a new test paper manually
  async createTestPaper(testPaperData) {
    try {
      const response = await api.post('/test-papers', testPaperData)
      return response.data
    } catch (error) {
      console.error('Error creating test paper:', error)
      throw error
    }
  },

  // Generate a test paper automatically
  async generateTestPaper(generationRequest) {
    try {
      const response = await api.post('/test-papers/generate', generationRequest)
      return response.data
    } catch (error) {
      console.error('Error generating test paper:', error)
      throw error
    }
  },

  // Preview questions for test paper generation
  async previewQuestions(generationRequest) {
    try {
      const response = await api.post('/test-papers/preview', generationRequest)
      return response.data
    } catch (error) {
      console.error('Error previewing questions:', error)
      throw error
    }
  },

  // Update an existing test paper
  async updateTestPaper(id, testPaperData) {
    try {
      const response = await api.put(`/test-papers/${id}`, testPaperData)
      return response.data
    } catch (error) {
      console.error('Error updating test paper:', error)
      throw error
    }
  },

  // Delete a test paper
  async deleteTestPaper(id) {
    try {
      await api.delete(`/test-papers/${id}`)
      return true
    } catch (error) {
      console.error('Error deleting test paper:', error)
      throw error
    }
  },

  // Get test papers by course
  async getTestPapersByCourse(courseId, page = 0, size = 20) {
    try {
      const params = { page, size }
      const response = await api.get(`/test-papers/course/${courseId}`, { params })
      return response.data
    } catch (error) {
      console.error('Error fetching test papers by course:', error)
      throw error
    }
  },

  // Get test papers by instructor
  async getTestPapersByInstructor(instructorId, page = 0, size = 20) {
    try {
      const params = { page, size }
      const response = await api.get(`/test-papers/instructor/${instructorId}`, { params })
      return response.data
    } catch (error) {
      console.error('Error fetching test papers by instructor:', error)
      throw error
    }
  },

  // Search test papers
  async searchTestPapers(query, page = 0, size = 20) {
    try {
      const params = { q: query, page, size }
      const response = await api.get('/test-papers/search', { params })
      return response.data
    } catch (error) {
      console.error('Error searching test papers:', error)
      throw error
    }
  },

  // Get test paper statistics
  async getTestPaperStatistics(id) {
    try {
      const response = await api.get(`/test-papers/${id}/statistics`)
      return response.data
    } catch (error) {
      console.error('Error fetching test paper statistics:', error)
      throw error
    }
  },

  // Get test paper analytics
  async getTestPaperAnalytics(id) {
    try {
      const response = await api.get(`/test-papers/${id}/analytics`)
      return response.data
    } catch (error) {
      console.error('Error fetching test paper analytics:', error)
      throw error
    }
  },

  // Duplicate a test paper
  async duplicateTestPaper(id, newName) {
    try {
      const response = await api.post(`/test-papers/${id}/duplicate`, { newName })
      return response.data
    } catch (error) {
      console.error('Error duplicating test paper:', error)
      throw error
    }
  },

  // Archive/Unarchive test paper
  async archiveTestPaper(id) {
    try {
      await api.post(`/test-papers/${id}/archive`)
      return true
    } catch (error) {
      console.error('Error archiving test paper:', error)
      throw error
    }
  },

  async unarchiveTestPaper(id) {
    try {
      await api.post(`/test-papers/${id}/unarchive`)
      return true
    } catch (error) {
      console.error('Error unarchiving test paper:', error)
      throw error
    }
  },

  // Bulk operations
  async bulkDeleteTestPapers(testPaperIds) {
    try {
      await api.post('/test-papers/bulk-delete', { testPaperIds })
      return true
    } catch (error) {
      console.error('Error bulk deleting test papers:', error)
      throw error
    }
  },

  async bulkArchiveTestPapers(testPaperIds) {
    try {
      await api.post('/test-papers/bulk-archive', { testPaperIds })
      return true
    } catch (error) {
      console.error('Error bulk archiving test papers:', error)
      throw error
    }
  },

  // Import/Export
  async exportTestPaper(id, format = 'pdf') {
    try {
      const response = await api.get(`/test-papers/${id}/export`, {
        params: { format },
        responseType: 'blob'
      })
      return response.data
    } catch (error) {
      console.error('Error exporting test paper:', error)
      throw error
    }
  },

  async exportTestPapers(testPaperIds, format = 'pdf') {
    try {
      const response = await api.post('/api/test-papers/export', 
        { testPaperIds, format },
        { responseType: 'blob' }
      )
      return response.data
    } catch (error) {
      console.error('Error exporting test papers:', error)
      throw error
    }
  },

  async importTestPaper(file, format = 'json') {
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('format', format)
      
      const response = await api.post('/api/test-papers/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      return response.data
    } catch (error) {
      console.error('Error importing test paper:', error)
      throw error
    }
  },

  // Test paper validation
  async validateTestPaper(testPaperData) {
    try {
      const response = await api.post('/api/test-papers/validate', testPaperData)
      return response.data
    } catch (error) {
      console.error('Error validating test paper:', error)
      throw error
    }
  },

  // Get generation methods and their configurations
  async getGenerationMethods() {
    try {
      const response = await api.get('/api/test-papers/generation-methods')
      return response.data
    } catch (error) {
      console.error('Error fetching generation methods:', error)
      throw error
    }
  },

  // Test paper templates
  async getTestPaperTemplates() {
    try {
      const response = await api.get('/api/test-papers/templates')
      return response.data
    } catch (error) {
      console.error('Error fetching test paper templates:', error)
      throw error
    }
  },

  async createTestPaperFromTemplate(templateId, customizations = {}) {
    try {
      const response = await api.post(`/api/test-papers/templates/${templateId}/create`, customizations)
      return response.data
    } catch (error) {
      console.error('Error creating test paper from template:', error)
      throw error
    }
  },

  // Test paper scheduling
  async scheduleTestPaper(id, scheduleData) {
    try {
      const response = await api.post(`/api/test-papers/${id}/schedule`, scheduleData)
      return response.data
    } catch (error) {
      console.error('Error scheduling test paper:', error)
      throw error
    }
  },

  async getScheduledTestPapers(params = {}) {
    try {
      const response = await api.get('/api/test-papers/scheduled', { params })
      return response.data
    } catch (error) {
      console.error('Error fetching scheduled test papers:', error)
      throw error
    }
  },

  // Test paper attempts management
  async getTestPaperAttempts(testPaperId, params = {}) {
    try {
      const response = await api.get(`/api/test-papers/${testPaperId}/attempts`, { params })
      return response.data
    } catch (error) {
      console.error('Error fetching test paper attempts:', error)
      throw error
    }
  },

  async getAttemptById(attemptId) {
    try {
      const response = await api.get(`/api/test-paper-attempts/${attemptId}`)
      return response.data
    } catch (error) {
      console.error('Error fetching attempt:', error)
      throw error
    }
  },

  async startAttempt(testPaperId, studentId) {
    try {
      const response = await api.post(`/api/test-papers/${testPaperId}/start`, { studentId })
      return response.data
    } catch (error) {
      console.error('Error starting attempt:', error)
      throw error
    }
  },

  async submitAttempt(attemptId, answers) {
    try {
      const response = await api.post(`/api/test-paper-attempts/${attemptId}/submit`, { answers })
      return response.data
    } catch (error) {
      console.error('Error submitting attempt:', error)
      throw error
    }
  },

  async saveAttemptProgress(attemptId, answers) {
    try {
      const response = await api.post(`/api/test-paper-attempts/${attemptId}/save`, { answers })
      return response.data
    } catch (error) {
      console.error('Error saving attempt progress:', error)
      throw error
    }
  },

  // Test paper grading
  async gradeAttempt(attemptId, gradingData) {
    try {
      const response = await api.post(`/api/test-paper-attempts/${attemptId}/grade`, gradingData)
      return response.data
    } catch (error) {
      console.error('Error grading attempt:', error)
      throw error
    }
  },

  async autoGradeAttempt(attemptId) {
    try {
      const response = await api.post(`/api/test-paper-attempts/${attemptId}/auto-grade`)
      return response.data
    } catch (error) {
      console.error('Error auto-grading attempt:', error)
      throw error
    }
  },

  async bulkGradeAttempts(attemptIds, gradingCriteria) {
    try {
      const response = await api.post('/api/test-paper-attempts/bulk-grade', {
        attemptIds,
        gradingCriteria
      })
      return response.data
    } catch (error) {
      console.error('Error bulk grading attempts:', error)
      throw error
    }
  },

  // Test paper reports
  async getTestPaperReport(id, reportType = 'summary') {
    try {
      const response = await api.get(`/api/test-papers/${id}/report`, {
        params: { type: reportType }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching test paper report:', error)
      throw error
    }
  },

  async getPerformanceAnalysis(id) {
    try {
      const response = await api.get(`/api/test-papers/${id}/performance-analysis`)
      return response.data
    } catch (error) {
      console.error('Error fetching performance analysis:', error)
      throw error
    }
  },

  async getDifficultyAnalysis(id) {
    try {
      const response = await api.get(`/api/test-papers/${id}/difficulty-analysis`)
      return response.data
    } catch (error) {
      console.error('Error fetching difficulty analysis:', error)
      throw error
    }
  },

  // Test paper sharing and collaboration
  async shareTestPaper(id, shareData) {
    try {
      const response = await api.post(`/api/test-papers/${id}/share`, shareData)
      return response.data
    } catch (error) {
      console.error('Error sharing test paper:', error)
      throw error
    }
  },

  async getSharedTestPapers(params = {}) {
    try {
      const response = await api.get('/api/test-papers/shared', { params })
      return response.data
    } catch (error) {
      console.error('Error fetching shared test papers:', error)
      throw error
    }
  },

  async revokeTestPaperShare(id, shareId) {
    try {
      await api.delete(`/api/test-papers/${id}/share/${shareId}`)
      return true
    } catch (error) {
      console.error('Error revoking test paper share:', error)
      throw error
    }
  },

  // Test paper versioning
  async getTestPaperVersions(id) {
    try {
      const response = await api.get(`/api/test-papers/${id}/versions`)
      return response.data
    } catch (error) {
      console.error('Error fetching test paper versions:', error)
      throw error
    }
  },

  async createTestPaperVersion(id, versionData) {
    try {
      const response = await api.post(`/api/test-papers/${id}/versions`, versionData)
      return response.data
    } catch (error) {
      console.error('Error creating test paper version:', error)
      throw error
    }
  },

  async restoreTestPaperVersion(id, versionId) {
    try {
      const response = await api.post(`/api/test-papers/${id}/versions/${versionId}/restore`)
      return response.data
    } catch (error) {
      console.error('Error restoring test paper version:', error)
      throw error
    }
  },

  // Test paper settings and configuration
  async getTestPaperSettings(id) {
    try {
      const response = await api.get(`/api/test-papers/${id}/settings`)
      return response.data
    } catch (error) {
      console.error('Error fetching test paper settings:', error)
      throw error
    }
  },

  async updateTestPaperSettings(id, settings) {
    try {
      const response = await api.put(`/api/test-papers/${id}/settings`, settings)
      return response.data
    } catch (error) {
      console.error('Error updating test paper settings:', error)
      throw error
    }
  },

  // Recent and popular test papers
  async getRecentTestPapers(limit = 10) {
    try {
      const response = await api.get('/api/test-papers/recent', {
        params: { limit }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching recent test papers:', error)
      throw error
    }
  },

  async getPopularTestPapers(limit = 10) {
    try {
      const response = await api.get('/api/test-papers/popular', {
        params: { limit }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching popular test papers:', error)
      throw error
    }
  }
}

export default testPaperService