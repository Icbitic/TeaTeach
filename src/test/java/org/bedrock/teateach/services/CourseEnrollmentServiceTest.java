package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.beans.CourseEnrollment;
import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.mappers.CourseEnrollmentMapper;
import org.bedrock.teateach.mappers.CourseMapper;
import org.bedrock.teateach.mappers.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseEnrollmentServiceTest {

    @Mock
    private CourseEnrollmentMapper courseEnrollmentMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private CourseEnrollmentService courseEnrollmentService;

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
    void enrollStudent_shouldCreateEnrollmentSuccessfully() {
        // Given
        when(courseMapper.findById(1L)).thenReturn(testCourse);
        when(studentMapper.findById(1L)).thenReturn(testStudent);
        when(courseEnrollmentMapper.findByStudentAndCourse(1L, 1L)).thenReturn(null);
        doNothing().when(courseEnrollmentMapper).insert(any(CourseEnrollment.class));

        // When
        CourseEnrollment result = courseEnrollmentService.enrollStudent(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getCourseId());
        assertEquals(1L, result.getStudentId());
        assertEquals("ACTIVE", result.getStatus());
        verify(courseEnrollmentMapper).insert(any(CourseEnrollment.class));
    }

    @Test
    void enrollStudent_shouldThrowExceptionWhenCourseNotFound() {
        // Given
        when(courseMapper.findById(1L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> courseEnrollmentService.enrollStudent(1L, 1L));
        assertEquals("Course not found with id: 1", exception.getMessage());
        verify(courseEnrollmentMapper, never()).insert(any(CourseEnrollment.class));
    }

    @Test
    void enrollStudent_shouldThrowExceptionWhenStudentNotFound() {
        // Given
        when(courseMapper.findById(1L)).thenReturn(testCourse);
        when(studentMapper.findById(1L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> courseEnrollmentService.enrollStudent(1L, 1L));
        assertEquals("Student not found with id: 1", exception.getMessage());
        verify(courseEnrollmentMapper, never()).insert(any(CourseEnrollment.class));
    }

    @Test
    void enrollStudent_shouldThrowExceptionWhenAlreadyEnrolled() {
        // Given
        when(courseMapper.findById(1L)).thenReturn(testCourse);
        when(studentMapper.findById(1L)).thenReturn(testStudent);
        when(courseEnrollmentMapper.findByStudentAndCourse(1L, 1L)).thenReturn(testEnrollment);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> courseEnrollmentService.enrollStudent(1L, 1L));
        assertEquals("Student is already enrolled in this course", exception.getMessage());
        verify(courseEnrollmentMapper, never()).insert(any(CourseEnrollment.class));
    }

    @Test
    void unenrollStudent_shouldDeleteEnrollmentSuccessfully() {
        // Given
        when(courseEnrollmentMapper.findByStudentAndCourse(1L, 1L)).thenReturn(testEnrollment);
        doNothing().when(courseEnrollmentMapper).deleteByStudentAndCourse(1L, 1L);

        // When
        courseEnrollmentService.unenrollStudent(1L, 1L);

        // Then
        verify(courseEnrollmentMapper).deleteByStudentAndCourse(1L, 1L);
    }

    @Test
    void unenrollStudent_shouldThrowExceptionWhenEnrollmentNotFound() {
        // Given
        when(courseEnrollmentMapper.findByStudentAndCourse(1L, 1L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> courseEnrollmentService.unenrollStudent(1L, 1L));
        assertEquals("Enrollment not found", exception.getMessage());
        verify(courseEnrollmentMapper, never()).deleteByStudentAndCourse(anyLong(), anyLong());
    }

    @Test
    void updateEnrollmentStatus_shouldUpdateStatusSuccessfully() {
        // Given
        when(courseEnrollmentMapper.findById(1L)).thenReturn(testEnrollment);
        doNothing().when(courseEnrollmentMapper).update(any(CourseEnrollment.class));

        // When
        courseEnrollmentService.updateEnrollmentStatus(1L, "INACTIVE");

        // Then
        verify(courseEnrollmentMapper).update(any(CourseEnrollment.class));
    }

    @Test
    void updateEnrollmentStatus_shouldThrowExceptionWhenEnrollmentNotFound() {
        // Given
        when(courseEnrollmentMapper.findById(1L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> courseEnrollmentService.updateEnrollmentStatus(1L, "INACTIVE"));
        assertEquals("Enrollment not found with id: 1", exception.getMessage());
        verify(courseEnrollmentMapper, never()).update(any(CourseEnrollment.class));
    }

    @Test
    void getStudentsByCourseId_shouldReturnStudentList() {
        // Given
        List<Student> expectedStudents = Arrays.asList(testStudent);
        when(courseEnrollmentMapper.findStudentsByCourseId(1L)).thenReturn(expectedStudents);

        // When
        List<Student> result = courseEnrollmentService.getStudentsByCourseId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStudent.getId(), result.get(0).getId());
        verify(courseEnrollmentMapper).findStudentsByCourseId(1L);
    }

    @Test
    void getCoursesByStudentId_shouldReturnCourseList() {
        // Given
        List<Course> expectedCourses = Arrays.asList(testCourse);
        when(courseEnrollmentMapper.findCoursesByStudentId(1L)).thenReturn(expectedCourses);

        // When
        List<Course> result = courseEnrollmentService.getCoursesByStudentId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCourse.getId(), result.get(0).getId());
        verify(courseEnrollmentMapper).findCoursesByStudentId(1L);
    }

    @Test
    void getActiveEnrollmentCount_shouldReturnCorrectCount() {
        // Given
        when(courseEnrollmentMapper.countActiveEnrollmentsByCourseId(1L)).thenReturn(5);

        // When
        int result = courseEnrollmentService.getActiveEnrollmentCount(1L);

        // Then
        assertEquals(5, result);
        verify(courseEnrollmentMapper).countActiveEnrollmentsByCourseId(1L);
    }

    @Test
    void getAvailableStudentsForCourse_shouldReturnAvailableStudents() {
        // Given
        List<Student> expectedStudents = Arrays.asList(testStudent);
        when(courseEnrollmentMapper.findAvailableStudentsForCourse(1L)).thenReturn(expectedStudents);

        // When
        List<Student> result = courseEnrollmentService.getAvailableStudentsForCourse(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStudent.getId(), result.get(0).getId());
        verify(courseEnrollmentMapper).findAvailableStudentsForCourse(1L);
    }

    @Test
    void enrollMultipleStudents_shouldEnrollAllValidStudents() {
        // Given
        List<Long> studentIds = Arrays.asList(1L, 2L);
        when(courseMapper.findById(1L)).thenReturn(testCourse);
        when(studentMapper.findById(1L)).thenReturn(testStudent);
        when(studentMapper.findById(2L)).thenReturn(testStudent);
        when(courseEnrollmentMapper.findByStudentAndCourse(1L, 1L)).thenReturn(null);
        when(courseEnrollmentMapper.findByStudentAndCourse(1L, 2L)).thenReturn(null);
        doNothing().when(courseEnrollmentMapper).insert(any(CourseEnrollment.class));

        // When
        courseEnrollmentService.enrollMultipleStudents(1L, studentIds);

        // Then
        verify(courseEnrollmentMapper, times(2)).insert(any(CourseEnrollment.class));
    }

    @Test
    void getEnrollmentByStudentAndCourse_shouldReturnEnrollment() {
        // Given
        when(courseEnrollmentMapper.findByStudentAndCourse(1L, 1L)).thenReturn(testEnrollment);

        // When
        CourseEnrollment result = courseEnrollmentService.getEnrollmentByStudentAndCourse(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(testEnrollment.getId(), result.getId());
        verify(courseEnrollmentMapper).findByStudentAndCourse(1L, 1L);
    }
}