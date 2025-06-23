package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.services.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private Student testStudent;
    private List<Student> testStudents;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setName("John Doe");
        testStudent.setEmail("john.doe@example.com");

        Student student2 = new Student();
        student2.setId(2L);
        student2.setName("Jane Smith");
        student2.setEmail("jane.smith@example.com");

        testStudents = Arrays.asList(testStudent, student2);
    }

    @Test
    void createStudent_ShouldReturnCreatedStudent() {
        // Given
        when(studentService.createStudent(any(Student.class))).thenReturn(testStudent);

        // When
        ResponseEntity<Student> response = studentController.createStudent(new Student());

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testStudent, response.getBody());
        verify(studentService, times(1)).createStudent(any(Student.class));
    }

    @Test
    void updateStudent_WhenStudentExists_ShouldReturnUpdatedStudent() {
        // Given
        when(studentService.updateStudent(any(Student.class))).thenReturn(testStudent);

        // When
        ResponseEntity<Student> response = studentController.updateStudent(1L, testStudent);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testStudent, response.getBody());
        assertEquals(1L, testStudent.getId()); // Verify ID was set from path
        verify(studentService, times(1)).updateStudent(testStudent);
    }

    @Test
    void updateStudent_WhenStudentDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(studentService.updateStudent(any(Student.class))).thenReturn(null);

        // When
        ResponseEntity<Student> response = studentController.updateStudent(1L, testStudent);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(studentService, times(1)).updateStudent(any(Student.class));
    }

    @Test
    void deleteStudent_ShouldReturnNoContent() {
        // Given
        doNothing().when(studentService).deleteStudent(anyLong());

        // When
        ResponseEntity<Void> response = studentController.deleteStudent(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(studentService, times(1)).deleteStudent(1L);
    }

    @Test
    void getStudentById_WhenStudentExists_ShouldReturnStudent() {
        // Given
        when(studentService.getStudentById(1L)).thenReturn(testStudent);

        // When
        ResponseEntity<Student> response = studentController.getStudentById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testStudent, response.getBody());
        verify(studentService, times(1)).getStudentById(1L);
    }

    @Test
    void getStudentById_WhenStudentDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(studentService.getStudentById(anyLong())).thenReturn(null);

        // When
        ResponseEntity<Student> response = studentController.getStudentById(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(studentService, times(1)).getStudentById(999L);
    }

    @Test
    void getAllStudents_ShouldReturnListOfStudents() {
        // Given
        when(studentService.getAllStudents()).thenReturn(testStudents);

        // When
        ResponseEntity<List<Student>> response = studentController.getAllStudents();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testStudents.size(), response.getBody().size());
        verify(studentService, times(1)).getAllStudents();
    }
}
