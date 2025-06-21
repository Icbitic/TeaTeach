package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.mappers.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setName("John Doe");
        testStudent.setEmail("john.doe@example.com");
    }

    @Test
    void createStudent_shouldInsertAndReturnStudent() {
        // Given
        doNothing().when(studentMapper).insert(testStudent);

        // When
        Student result = studentService.createStudent(testStudent);

        // Then
        assertNotNull(result);
        assertEquals(testStudent.getId(), result.getId());
        verify(studentMapper, times(1)).insert(testStudent);
    }

    @Test
    void updateStudent_shouldUpdateAndReturnStudent() {
        // Given
        testStudent.setName("Jane Doe");
        doNothing().when(studentMapper).update(testStudent);

        // When
        Student result = studentService.updateStudent(testStudent);

        // Then
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        verify(studentMapper, times(1)).update(testStudent);
    }

    @Test
    void deleteStudent_shouldDeleteStudent() {
        // Given
        Long studentId = 1L;
        doNothing().when(studentMapper).delete(studentId);

        // When
        studentService.deleteStudent(studentId);

        // Then
        verify(studentMapper, times(1)).delete(studentId);
    }

    @Test
    void getStudentById_shouldReturnStudent_whenFound() {
        // Given
        Long studentId = 1L;
        when(studentMapper.findById(studentId)).thenReturn(testStudent);

        // When
        Student result = studentService.getStudentById(studentId);

        // Then
        assertNotNull(result);
        assertEquals(testStudent, result);
        verify(studentMapper, times(1)).findById(studentId);
    }

    @Test
    void getStudentById_shouldReturnNull_whenNotFound() {
        // Given
        Long studentId = 99L;
        when(studentMapper.findById(studentId)).thenReturn(null);

        // When
        Student result = studentService.getStudentById(studentId);

        // Then
        assertNull(result);
        verify(studentMapper, times(1)).findById(studentId);
    }

    @Test
    void getAllStudents_shouldReturnAllStudents() {
        // Given
        List<Student> expectedStudents = Arrays.asList(testStudent, new Student());
        when(studentMapper.findAll()).thenReturn(expectedStudents);

        // When
        List<Student> result = studentService.getAllStudents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(studentMapper, times(1)).findAll();
    }
}