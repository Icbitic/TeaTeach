import apiClient  from './api.js'

export const resourceService = {
  // Upload a resource file
  uploadResource(file, resourceName, description = null) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('resourceName', resourceName)
    if (description) {
      formData.append('description', description)
    }

    return apiClient.post('/resources/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Get all resources
  getAllResources() {
    return apiClient.get('/resources')
  },

  // Get resource by ID
  getResourceById(id) {
    return apiClient.get(`/resources/${id}`)
  },





  // Download a resource
  downloadResource(id) {
    return apiClient.get(`/resources/${id}/download`, {
      responseType: 'blob'
    })
  },

  // Update resource metadata
  updateResource(id, resourceData) {
    return apiClient.put(`/resources/${id}`, resourceData)
  },

  // Delete resource
  deleteResource(id) {
    return apiClient.delete(`/resources/${id}`)
  },

  // Get resource metadata without downloading
  getResourceMetadata(id) {
    return apiClient.get(`/resources/${id}/metadata`)
  },

  // Task Resource Management
  
  // Get all resources for a specific task
  getTaskResources(taskId) {
    return apiClient.get(`/learning-tasks/${taskId}/resources`)
  },

  // Associate a resource with a task
  addResourceToTask(taskId, resourceId) {
    return apiClient.post(`/learning-tasks/${taskId}/resources/${resourceId}`)
  },

  // Remove a resource from a task
  removeResourceFromTask(taskId, resourceId) {
    return apiClient.delete(`/learning-tasks/${taskId}/resources/${resourceId}`)
  },

  // Remove all resources from a task
  removeAllResourcesFromTask(taskId) {
    return apiClient.delete(`/learning-tasks/${taskId}/resources`)
  }
}