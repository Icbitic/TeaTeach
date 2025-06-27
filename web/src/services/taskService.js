import apiClient from './api'

export const taskService = {
  // Get all tasks from all courses
  getAllTasks() {
    return apiClient.get('/learning-tasks')
  },

  // Get task by ID
  getTaskById(id) {
    return apiClient.get(`/learning-tasks/${id}`)
  },

  // Get tasks for a specific course
  getTasksForCourse(courseId) {
    return apiClient.get(`/learning-tasks/course/${courseId}`)
  },

  // Create new task
  createTask(taskData) {
    return apiClient.post('/learning-tasks', taskData)
  },

  // Update existing task
  updateTask(id, taskData) {
    return apiClient.put(`/learning-tasks/${id}`, taskData)
  },

  // Delete task
  deleteTask(id) {
    return apiClient.delete(`/learning-tasks/${id}`)
  },

  // Submit task
  submitTask(submissionData) {
    return apiClient.post('/submissions', submissionData, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
  },

  // Get submission by student and task
  getSubmissionByStudentAndTask(studentId, taskId) {
    return apiClient.get(`/submissions/student/${studentId}/task/${taskId}`)
  },

  // Update task status
  updateTaskStatus(id, status) {
    return apiClient.patch(`/learning-tasks/${id}/status`, { status })
  }
}

export default taskService