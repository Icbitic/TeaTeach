package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.beans.CourseEnrollment;
import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.services.CourseEnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseEnrollmentControllerTest {

    @Mock
    private CourseEnrollmentService courseEnrollmentService;

    @InjectMocks
    private CourseEnrollmentController courseEnrollmentController;

    private Course testCourse;
    private Student testStudent;
    private CourseEnrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCourseName("Introduction to Programming");
        testCourse.setCourseCode("CS101");
        testCourse.setInstructor("Dr. Smith");
        testCourse.setCredits(3.0);
        testCourse.setHours(48);

        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setStudentId("STU001");
        testStudent.setName("John Doe");
        testStudent.setEmail("john.doe@example.com");
        testStudent.setMajor("Computer Science");
        testStudent.setDateOfBirth(LocalDate.of(2000, 1, 1));

        testEnrollment = new CourseEnrollment();
        testEnrollment.setId(1L);
        testEnrollment.setCourseId(1L);
        testEnrollment.setStudentId(1L);
        testEnrollment.setEnrollmentDate(LocalDateTime.now());
        testEnrollment.setStatus("ACTIVE");
    }

    @Test
    void enrollStudent_shouldReturnCreatedStatus() {
        // Given
        Map<String, Long> request = new HashMap<>();
        request.put("courseId", 1L);
        request.put("studentId", 1L);
        
        when(courseEnrollmentService.enrollStudent(1L, 1L)).thenReturn(testEnrollment);

        // When
        ResponseEntity<?> response = courseEnrollmentController.enrollStudent(request);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, ((CourseEnrollment) response.getBody()).getId());
        assertEquals(1L, ((CourseEnrollment) response.getBody()).getCourseId());
        assertEquals(1L, ((CourseEnrollment) response.getBody()).getStudentId());
        assertEquals("ACTIVE", ((CourseEnrollment) response.getBody()).getStatus());
        
        verify(courseEnrollmentService).enrollStudent(1L, 1L);
    }

    @Test
    void enrollStudent_shouldReturnBadRequestWhenMissingCourseId() {
        // Given
        Map<String, Long> request = new HashMap<>();
        request.put("studentId", 1L);

        // When
        ResponseEntity<?> response = courseEnrollmentController.enrollStudent(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Course ID and Student ID are required", response.getBody());

        verify(courseEnrollmentService, never()).enrollStudent(anyLong(), anyLong());
    }

    @Test
    void enrollStudent_shouldReturnBadRequestWhenServiceThrowsException() {
        // Given
        Map<String, Long> request = new HashMap<>();
        request.put("courseId", 1L);
        request.put("studentId", 1L);
        
        when(courseEnrollmentService.enrollStudent(1L, 1L))
                .thenThrow(new RuntimeException("Student is already enrolled"));

        // When
        ResponseEntity<?> response = courseEnrollmentController.enrollStudent(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Student is already enrolled", response.getBody());
    }

    @Test
    void enrollMultipleStudents_shouldReturnOkStatus() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("courseId", 1L);
        request.put("studentIds", Arrays.asList(1L, 2L));
        
        doNothing().when(courseEnrollmentService).enrollMultipleStudents(eq(1L), anyList());

        // When
        ResponseEntity<?> response = courseEnrollmentController.enrollMultipleStudents(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Students enrolled successfully", response.getBody());
        
        verify(courseEnrollmentService).enrollMultipleStudents(eq(1L), anyList());
    }

    @Test
    void unenrollStudent_shouldReturnOkStatus() {
        // Given
        doNothing().when(courseEnrollmentService).unenrollStudent(1L, 1L);

        // When
        ResponseEntity<?> response = courseEnrollmentController.unenrollStudent(1L, 1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Student unenrolled successfully", response.getBody());
        
        verify(courseEnrollmentService).unenrollStudent(1L, 1L);
    }

    @Test
    void unenrollStudent_shouldReturnBadRequestWhenServiceThrowsException() {
        // Given
        doThrow(new RuntimeException("Enrollment not found"))
                .when(courseEnrollmentService).unenrollStudent(1L, 1L);

        // When
        ResponseEntity<?> response = courseEnrollmentController.unenrollStudent(1L, 1L);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Enrollment not found", response.getBody());
    }

    @Test
    void updateEnrollmentStatus_shouldReturnOkStatus() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("status", "INACTIVE");
        
        doNothing().when(courseEnrollmentService).updateEnrollmentStatus(1L, "INACTIVE");

        // When
        ResponseEntity<?> response = courseEnrollmentController.updateEnrollmentStatus(1L, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Enrollment status updated successfully", response.getBody());
        
        verify(courseEnrollmentService).updateEnrollmentStatus(1L, "INACTIVE");
    }

    @Test
    void getStudentsByCourse_shouldReturnStudentList() {
        // Given
        List<Student> students = Arrays.asList(testStudent);
        when(courseEnrollmentService.getStudentsByCourseId(1L)).thenReturn(students);

        // When
        ResponseEntity<List<Student>> response = courseEnrollmentController.getStudentsByCourse(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("John Doe", response.getBody().get(0).getName());
        assertEquals("STU001", response.getBody().get(0).getStudentId());
        
        verify(courseEnrollmentService).getStudentsByCourseId(1L);
    }

    @Test
    void getCoursesByStudent_shouldReturnCourseList() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseEnrollmentService.getCoursesByStudentId(1L)).thenReturn(courses);

        // When
        ResponseEntity<List<Course>> response = courseEnrollmentController.getCoursesByStudent(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("Introduction to Programming", response.getBody().get(0).getCourseName());
        assertEquals("CS101", response.getBody().get(0).getCourseCode());
        
        verify(courseEnrollmentService).getCoursesByStudentId(1L);
    }

    @Test
    void getEnrollmentsByCourse_shouldReturnEnrollmentList() {
        // Given
        List<CourseEnrollment> enrollments = Arrays.asList(testEnrollment);
        when(courseEnrollmentService.getEnrollmentsByCourseId(1L)).thenReturn(enrollments);

        // When
        ResponseEntity<List<CourseEnrollment>> response = courseEnrollmentController.getEnrollmentsByCourse(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals(1L, response.getBody().get(0).getCourseId());
        assertEquals(1L, response.getBody().get(0).getStudentId());
        
        verify(courseEnrollmentService).getEnrollmentsByCourseId(1L);
    }

    @Test
    void getEnrollmentCount_shouldReturnCount() {
        // Given
        when(courseEnrollmentService.getActiveEnrollmentCount(1L)).thenReturn(5);

        // When
        ResponseEntity<Integer> response = courseEnrollmentController.getEnrollmentCount(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody());
        
        verify(courseEnrollmentService).getActiveEnrollmentCount(1L);
    }

    @Test
    void getAvailableStudents_shouldReturnStudentList() {
        // Given
        List<Student> students = Arrays.asList(testStudent);
        when(courseEnrollmentService.getAvailableStudentsForCourse(1L)).thenReturn(students);

        // When
        ResponseEntity<List<Student>> response = courseEnrollmentController.getAvailableStudents(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("John Doe", response.getBody().get(0).getName());
        
        verify(courseEnrollmentService).getAvailableStudentsForCourse(1L);
    }

    @Test
    void checkEnrollment_shouldReturnEnrollmentWhenExists() {
        // Given
        when(courseEnrollmentService.getEnrollmentByStudentAndCourse(1L, 1L)).thenReturn(testEnrollment);

        // When
        ResponseEntity<CourseEnrollment> response = courseEnrollmentController.checkEnrollment(1L, 1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(1L, response.getBody().getCourseId());
        assertEquals(1L, response.getBody().getStudentId());
        
        verify(courseEnrollmentService).getEnrollmentByStudentAndCourse(1L, 1L);
    }

    @Test
    void checkEnrollment_shouldReturnNotFoundWhenDoesNotExist() {
        // Given
        when(courseEnrollmentService.getEnrollmentByStudentAndCourse(1L, 1L)).thenReturn(null);

        // When
        ResponseEntity<CourseEnrollment> response = courseEnrollmentController.checkEnrollment(1L, 1L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        verify(courseEnrollmentService).getEnrollmentByStudentAndCourse(1L, 1L);
    }
}