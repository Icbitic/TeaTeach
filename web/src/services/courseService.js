import apiClient from './api.js'

export const courseService = {
  // Get all courses
  getAllCourses() {
    return apiClient.get('/courses')
  },

  // Get course by ID
  getCourseById(id) {
    return apiClient.get(`/courses/${id}`)
  },

  // Create new course
  createCourse(courseData) {
    return apiClient.post('/courses', courseData)
  },

  // Update existing course
  updateCourse(id, courseData) {
    return apiClient.put(`/courses/${id}`, courseData)
  },

  // Delete course
  deleteCourse(id) {
    return apiClient.delete(`/courses/${id}`)
  },

  // Get tasks for a specific course
  getTasksForCourse(courseId) {
    return apiClient.get(`/courses/${courseId}/tasks`)
  }
}

export default courseService