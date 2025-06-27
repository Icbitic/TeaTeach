import api from './api'

export const questionService = {
  // Get all questions with optional filters
  async getQuestions(params = {}) {
    try {
      const response = await api.get('/questions', { params })
      return response.data
    } catch (error) {
      console.error('Error fetching questions:', error)
      throw error
    }
  },

  // Get questions by type and difficulty
  async getQuestionsByFilter(type, difficulty, page = 0, size = 20) {
    try {
      const params = { page, size }
      if (type) params.type = type
      if (difficulty) params.difficulty = difficulty
      
      const response = await api.get('/questions/filter', { params })
      return response.data
    } catch (error) {
      console.error('Error fetching filtered questions:', error)
      throw error
    }
  },

  // Get a single question by ID
  async getQuestionById(id) {
    try {
      const response = await api.get(`/questions/${id}`)
      return response.data
    } catch (error) {
      console.error('Error fetching question:', error)
      throw error
    }
  },

  // Create a new question
  async createQuestion(questionData) {
    try {
      const response = await api.post('/questions', questionData)
      return response.data
    } catch (error) {
      console.error('Error creating question:', error)
      throw error
    }
  },

  // Update an existing question
  async updateQuestion(id, questionData) {
    try {
      const response = await api.put(`/questions/${id}`, questionData)
      return response.data
    } catch (error) {
      console.error('Error updating question:', error)
      throw error
    }
  },

  // Delete a question
  async deleteQuestion(id) {
    try {
      await api.delete(`/questions/${id}`)
      return true
    } catch (error) {
      console.error('Error deleting question:', error)
      throw error
    }
  },

  // Get questions by knowledge point
  async getQuestionsByKnowledgePoint(knowledgePointId, page = 0, size = 20) {
    try {
      const params = { knowledgePointId, page, size }
      const response = await api.get('/questions/by-knowledge-point', { params })
      return response.data
    } catch (error) {
      console.error('Error fetching questions by knowledge point:', error)
      throw error
    }
  },

  // Search questions
  async searchQuestions(query, page = 0, size = 20) {
    try {
      const params = { q: query, page, size }
      const response = await api.get('/questions/search', { params })
      return response.data
    } catch (error) {
      console.error('Error searching questions:', error)
      throw error
    }
  },

  // Get question statistics
  async getQuestionStatistics(id) {
    try {
      const response = await api.get(`/questions/${id}/statistics`)
      return response.data
    } catch (error) {
      console.error('Error fetching question statistics:', error)
      throw error
    }
  },

  // Bulk operations
  async bulkDeleteQuestions(questionIds) {
    try {
      await api.post('/questions/bulk-delete', { questionIds })
      return true
    } catch (error) {
      console.error('Error bulk deleting questions:', error)
      throw error
    }
  },

  async bulkUpdateQuestions(updates) {
    try {
      const response = await api.post('/questions/bulk-update', updates)
      return response.data
    } catch (error) {
      console.error('Error bulk updating questions:', error)
      throw error
    }
  },

  // Import/Export
  async exportQuestions(format = 'json', filters = {}) {
    try {
      const params = { format, ...filters }
      const response = await api.get('/questions/export', { 
        params,
        responseType: 'blob'
      })
      return response.data
    } catch (error) {
      console.error('Error exporting questions:', error)
      throw error
    }
  },

  async importQuestions(file, format = 'json') {
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('format', format)
      
      const response = await api.post('/questions/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      return response.data
    } catch (error) {
      console.error('Error importing questions:', error)
      throw error
    }
  },

  // Question validation
  async validateQuestion(questionData) {
    try {
      const response = await api.post('/questions/validate', questionData)
      return response.data
    } catch (error) {
      console.error('Error validating question:', error)
      throw error
    }
  },

  // Get question types and their configurations
  async getQuestionTypes() {
    try {
      const response = await api.get('/questions/types')
      return response.data
    } catch (error) {
      console.error('Error fetching question types:', error)
      throw error
    }
  },

  // Get difficulty levels
  async getDifficultyLevels() {
    try {
      const response = await api.get('/questions/difficulty-levels')
      return response.data
    } catch (error) {
      console.error('Error fetching difficulty levels:', error)
      throw error
    }
  },

  // Get programming languages supported
  async getProgrammingLanguages() {
    try {
      const response = await api.get('/questions/programming-languages')
      return response.data
    } catch (error) {
      console.error('Error fetching programming languages:', error)
      throw error
    }
  },

  // Question analytics
  async getQuestionAnalytics(timeRange = '30d') {
    try {
      const response = await api.get('/questions/analytics', {
        params: { timeRange }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching question analytics:', error)
      throw error
    }
  },

  // Get popular questions
  async getPopularQuestions(limit = 10) {
    try {
      const response = await api.get('/questions/popular', {
        params: { limit }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching popular questions:', error)
      throw error
    }
  },

  // Get recent questions
  async getRecentQuestions(limit = 10) {
    try {
      const response = await api.get('/questions/recent', {
        params: { limit }
      })
      return response.data
    } catch (error) {
      console.error('Error fetching recent questions:', error)
      throw error
    }
  },

  // Question templates
  async getQuestionTemplates(type) {
    try {
      const params = type ? { type } : {}
      const response = await api.get('/questions/templates', { params })
      return response.data
    } catch (error) {
      console.error('Error fetching question templates:', error)
      throw error
    }
  },

  // Duplicate question
  async duplicateQuestion(id) {
    try {
      const response = await api.post(`/questions/${id}/duplicate`)
      return response.data
    } catch (error) {
      console.error('Error duplicating question:', error)
      throw error
    }
  },

  // Archive/Unarchive question
  async archiveQuestion(id) {
    try {
      await api.post(`/questions/${id}/archive`)
      return true
    } catch (error) {
      console.error('Error archiving question:', error)
      throw error
    }
  },

  async unarchiveQuestion(id) {
    try {
      await api.post(`/questions/${id}/unarchive`)
      return true
    } catch (error) {
      console.error('Error unarchiving question:', error)
      throw error
    }
  }
}

export default questionService