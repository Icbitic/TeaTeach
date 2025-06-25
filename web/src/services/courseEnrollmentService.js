import apiClient from './api'

export const courseEnrollmentService = {
  // Enroll a single student in a course
  enrollStudent(courseId, studentId) {
    return apiClient.post('/course-enrollments/enroll', {
      courseId,
      studentId
    })
  },

  // Enroll multiple students in a course
  enrollMultipleStudents(courseId, studentIds) {
    return apiClient.post('/course-enrollments/enroll-multiple', {
      courseId,
      studentIds
    })
  },

  // Unenroll a student from a course
  unenrollStudent(courseId, studentId) {
    return apiClient.delete('/course-enrollments/unenroll', {
      params: {
        courseId,
        studentId
      }
    })
  },

  // Update enrollment status
  updateEnrollmentStatus(enrollmentId, status) {
    return apiClient.put(`/course-enrollments/${enrollmentId}/status`, {
      status
    })
  },

  // Get students enrolled in a course
  getStudentsByCourse(courseId) {
    return apiClient.get(`/course-enrollments/course/${courseId}/students`)
  },

  // Get courses a student is enrolled in
  getCoursesByStudent(studentId) {
    return apiClient.get(`/course-enrollments/student/${studentId}/courses`)
  },

  // Get all enrollments for a course
  getEnrollmentsByCourse(courseId) {
    return apiClient.get(`/course-enrollments/course/${courseId}/enrollments`)
  },

  // Get enrollment count for a course
  getEnrollmentCount(courseId) {
    return apiClient.get(`/course-enrollments/course/${courseId}/count`)
  },

  // Get available students for a course (not yet enrolled)
  getAvailableStudents(courseId) {
    return apiClient.get(`/course-enrollments/course/${courseId}/available-students`)
  },

  // Check if a student is enrolled in a course
  checkEnrollment(courseId, studentId) {
    return apiClient.get('/course-enrollments/check-enrollment', {
      params: {
        courseId,
        studentId
      }
    })
  }
}

export default courseEnrollmentService