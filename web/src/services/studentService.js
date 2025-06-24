import apiClient from './api.js'

export const studentService = {
  // Get all students
  getAllStudents() {
    return apiClient.get('/students')
  },

  // Get student by ID
  getStudentById(id) {
    return apiClient.get(`/students/${id}`)
  },

  // Create new student
  createStudent(studentData) {
    return apiClient.post('/students', studentData)
  },

  // Update existing student
  updateStudent(id, studentData) {
    return apiClient.put(`/students/${id}`, studentData)
  },

  // Delete student
  deleteStudent(id) {
    return apiClient.delete(`/students/${id}`)
  },

  // Import students from file (if backend supports it)
  importStudents(formData) {
    return apiClient.post('/students/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Export students to Excel (if backend supports it)
  exportStudents() {
    return apiClient.get('/students/export', {
      responseType: 'blob'
    })
  }
}

export default studentService