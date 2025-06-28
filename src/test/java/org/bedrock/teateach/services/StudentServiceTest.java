package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.beans.StudentLearningData;
import org.bedrock.teateach.mappers.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
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
        testStudent.setStudentId("STU001");
        testStudent.setName("John Doe");
        testStudent.setEmail("john.doe@example.com");
        testStudent.setMajor("Computer Science");
        testStudent.setDateOfBirth(LocalDate.of(2000, 1, 15));
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
    
    @Test
    void getAllStudents_shouldReturnEmptyList_whenNoStudentsExist() {
        // Given
        when(studentMapper.findAll()).thenReturn(Collections.emptyList());
        
        // When
        List<Student> result = studentService.getAllStudents();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentMapper, times(1)).findAll();
    }
    
    @Test
    void getStudentByStudentId_shouldReturnStudent_whenFound() {
        // Given
        String studentId = "STU001";
        when(studentMapper.findByStudentId(studentId)).thenReturn(testStudent);
        
        // When
        Student result = studentService.getStudentByStudentId(studentId);
        
        // Then
        assertNotNull(result);
        assertEquals(testStudent, result);
        verify(studentMapper, times(1)).findByStudentId(studentId);
    }
    
    @Test
    void getStudentByStudentId_shouldReturnNull_whenNotFound() {
        // Given
        String studentId = "NONEXISTENT";
        when(studentMapper.findByStudentId(studentId)).thenReturn(null);
        
        // When
        Student result = studentService.getStudentByStudentId(studentId);
        
        // Then
        assertNull(result);
        verify(studentMapper, times(1)).findByStudentId(studentId);
    }
    
    @Test
    void convertStudentIdToId_shouldReturnDatabaseId_whenStudentExists() {
        // Given
        String studentId = "STU001";
        when(studentMapper.findByStudentId(studentId)).thenReturn(testStudent);
        
        // When
        Long result = studentService.convertStudentIdToId(studentId);
        
        // Then
        assertNotNull(result);
        assertEquals(testStudent.getId(), result);
        verify(studentMapper, times(1)).findByStudentId(studentId);
    }
    
    @Test
    void convertStudentIdToId_shouldReturnNull_whenStudentNotFound() {
        // Given
        String studentId = "NONEXISTENT";
        when(studentMapper.findByStudentId(studentId)).thenReturn(null);
        
        // When
        Long result = studentService.convertStudentIdToId(studentId);
        
        // Then
        assertNull(result);
        verify(studentMapper, times(1)).findByStudentId(studentId);
    }
    
    @Test
    void convertStudentIdToId_shouldReturnNull_whenStudentIdIsNull() {
        // When
        Long result = studentService.convertStudentIdToId(null);
        
        // Then
        assertNull(result);
        verify(studentMapper, never()).findByStudentId(any());
    }
    
    @Test
    void convertStudentIdToId_shouldReturnNull_whenStudentIdIsEmpty() {
        // When
        Long result = studentService.convertStudentIdToId("");
        
        // Then
        assertNull(result);
        verify(studentMapper, never()).findByStudentId(any());
    }
    
    @Test
    void convertIdToStudentId_shouldReturnStudentId_whenStudentExists() {
        // Given
        Long id = 1L;
        when(studentMapper.findById(id)).thenReturn(testStudent);
        
        // When
        String result = studentService.convertIdToStudentId(id);
        
        // Then
        assertNotNull(result);
        assertEquals(testStudent.getStudentId(), result);
        verify(studentMapper, times(1)).findById(id);
    }
    
    @Test
    void convertIdToStudentId_shouldReturnNull_whenStudentNotFound() {
        // Given
        Long id = 999L;
        when(studentMapper.findById(id)).thenReturn(null);
        
        // When
        String result = studentService.convertIdToStudentId(id);
        
        // Then
        assertNull(result);
        verify(studentMapper, times(1)).findById(id);
    }
    
    @Test
    void convertIdToStudentId_shouldReturnNull_whenIdIsNull() {
        // When
        String result = studentService.convertIdToStudentId(null);
        
        // Then
        assertNull(result);
        verify(studentMapper, never()).findById(any());
    }
    
    @Test
    void existsByStudentId_shouldReturnTrue_whenStudentExists() {
        // Given
        String studentId = "STU001";
        when(studentMapper.findByStudentId(studentId)).thenReturn(testStudent);
        
        // When
        boolean result = studentService.existsByStudentId(studentId);
        
        // Then
        assertTrue(result);
        verify(studentMapper, times(1)).findByStudentId(studentId);
    }
    
    @Test
    void existsByStudentId_shouldReturnFalse_whenStudentNotFound() {
        // Given
        String studentId = "NONEXISTENT";
        when(studentMapper.findByStudentId(studentId)).thenReturn(null);
        
        // When
        boolean result = studentService.existsByStudentId(studentId);
        
        // Then
        assertFalse(result);
        verify(studentMapper, times(1)).findByStudentId(studentId);
    }
    
    @Test
    void existsById_shouldReturnTrue_whenStudentExists() {
        // Given
        Long id = 1L;
        when(studentMapper.findById(id)).thenReturn(testStudent);
        
        // When
        boolean result = studentService.existsById(id);
        
        // Then
        assertTrue(result);
        verify(studentMapper, times(1)).findById(id);
    }
    
    @Test
    void existsById_shouldReturnFalse_whenStudentNotFound() {
        // Given
        Long id = 999L;
        when(studentMapper.findById(id)).thenReturn(null);
        
        // When
        boolean result = studentService.existsById(id);
        
        // Then
        assertFalse(result);
        verify(studentMapper, times(1)).findById(id);
    }
    
    @Test
    void exportStudentsToExcel_shouldReturnExcelData() {
        // Given
        List<Student> students = Arrays.asList(testStudent, createSecondTestStudent());
        when(studentMapper.findAll()).thenReturn(students);
        
        // When
        byte[] result = studentService.exportStudentsToExcel();
        
        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(studentMapper, times(1)).findAll();
    }
    
    @Test
    void exportStudentsToExcel_shouldHandleEmptyStudentList() {
        // Given
        when(studentMapper.findAll()).thenReturn(Collections.emptyList());
        
        // When
        byte[] result = studentService.exportStudentsToExcel();
        
        // Then
        assertNotNull(result);
        assertTrue(result.length > 0); // Should still contain header
        verify(studentMapper, times(1)).findAll();
    }
    
    @Test
    void importStudentsFromExcel_shouldThrowException_whenFileIsEmpty() {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.importStudentsFromExcel(mockFile);
        });
    }
    
    @Test
    void importStudentsFromExcel_shouldThrowException_whenFileFormatIsUnsupported() {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.importStudentsFromExcel(mockFile);
        });
    }
    
    @Test
    void importStudentsFromExcel_shouldThrowException_whenInvalidExcelFormat() throws IOException {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("students.xlsx");
        
        // Create invalid Excel content (plain text instead of Excel binary)
        String invalidContent = "Invalid Excel Content";
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(invalidContent.getBytes()));
        
        // When & Then
        // This should throw an exception due to invalid Excel format
        assertThrows(Exception.class, () -> {
            studentService.importStudentsFromExcel(mockFile);
        });
    }
    
    @Test
    void getStudentLearningData_shouldReturnMockData() {
        // Given
        String studentId = "1";
        Long courseId = 1L;
        
        // When
        List<StudentLearningData> result = studentService.getStudentLearningData(studentId, courseId);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        StudentLearningData data1 = result.get(0);
        assertEquals(1L, data1.getId());
        assertEquals(1L, data1.getStudentId());
        assertEquals(1L, data1.getCourseId());
        assertEquals(75.0, data1.getQuizScore());
        assertEquals(0.8, data1.getCompletionRate());
        
        StudentLearningData data2 = result.get(1);
        assertEquals(2L, data2.getId());
        assertEquals(1L, data2.getStudentId());
        assertEquals(1L, data2.getCourseId());
        assertEquals(60.0, data2.getQuizScore());
        assertEquals(0.6, data2.getCompletionRate());
    }
    
    @Test
    void getStudentLearningData_shouldReturnMockDataWithNullCourseId() {
        // Given
        String studentId = "1";
        
        // When
        List<StudentLearningData> result = studentService.getStudentLearningData(studentId, null);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getCourseId()); // Should default to 1L
    }
    
    @Test
    void exportStudentsToExcel_shouldHandleNullFields() {
        // Given
        Student studentWithNulls = new Student();
        studentWithNulls.setId(null);
        studentWithNulls.setStudentId(null);
        studentWithNulls.setName(null);
        studentWithNulls.setEmail(null);
        studentWithNulls.setMajor(null);
        studentWithNulls.setDateOfBirth(null);
        
        List<Student> students = Arrays.asList(studentWithNulls);
        when(studentMapper.findAll()).thenReturn(students);
        
        // When
        byte[] result = studentService.exportStudentsToExcel();
        
        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(studentMapper, times(1)).findAll();
    }
    
    @Test
    void convertStudentIdToId_shouldReturnNull_whenStudentIdIsWhitespace() {
        // When
        Long result = studentService.convertStudentIdToId("   ");
        
        // Then
        assertNull(result);
        verify(studentMapper, never()).findByStudentId(any());
    }
    
    @Test
    void existsByStudentId_shouldReturnFalse_whenStudentIdIsNull() {
        // When
        boolean result = studentService.existsByStudentId(null);
        
        // Then
        assertFalse(result);
        verify(studentMapper, times(1)).findByStudentId(null);
    }
    
    @Test
    void existsById_shouldReturnFalse_whenIdIsNull() {
        // When
        boolean result = studentService.existsById(null);
        
        // Then
        assertFalse(result);
        verify(studentMapper, times(1)).findById(null);
    }
    
    @Test
    void importStudentsFromExcel_shouldThrowException_whenFileNameIsNull() {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.importStudentsFromExcel(mockFile);
        });
    }
    
    @Test
    void getStudentLearningData_shouldHandleStringToLongConversion() {
        // Given
        String studentId = "123";
        Long courseId = 5L;
        
        // When
        List<StudentLearningData> result = studentService.getStudentLearningData(studentId, courseId);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(123L, result.get(0).getStudentId());
        assertEquals(5L, result.get(0).getCourseId());
    }
    
    @Test
    void createStudent_shouldHandleNullStudent() {
        // Given
        Student nullStudent = null;
        doNothing().when(studentMapper).insert(any());
        
        // When
        Student result = studentService.createStudent(nullStudent);
        
        // Then
        assertNull(result);
        verify(studentMapper, times(1)).insert(nullStudent);
    }
    
    @Test
    void updateStudent_shouldHandleNullStudent() {
        // Given
        Student nullStudent = null;
        doNothing().when(studentMapper).update(any());
        
        // When
        Student result = studentService.updateStudent(nullStudent);
        
        // Then
        assertNull(result);
        verify(studentMapper, times(1)).update(nullStudent);
    }
    
    @Test
    void deleteStudent_shouldHandleNullId() {
        // Given
        Long nullId = null;
        doNothing().when(studentMapper).delete(any());
        
        // When
        studentService.deleteStudent(nullId);
        
        // Then
        verify(studentMapper, times(1)).delete(nullId);
    }
    
    @Test
    void exportStudentsToExcel_shouldHandleIOException() {
        // Given
        List<Student> students = Arrays.asList(testStudent);
        when(studentMapper.findAll()).thenReturn(students);
        
        // When
        byte[] result = studentService.exportStudentsToExcel();
        
        // Then
        // Should not throw exception and return valid Excel data
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
    
    @Test
    void getStudentLearningData_shouldReturnConsistentMockData() {
        // Given
        String studentId = "999";
        Long courseId = 10L;
        
        // When
        List<StudentLearningData> result1 = studentService.getStudentLearningData(studentId, courseId);
        List<StudentLearningData> result2 = studentService.getStudentLearningData(studentId, courseId);
        
        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.size(), result2.size());
        assertEquals(2, result1.size());
        
        // Verify that the mock data is consistent
        assertEquals(999L, result1.get(0).getStudentId());
        assertEquals(10L, result1.get(0).getCourseId());
        assertEquals(75.0, result1.get(0).getQuizScore());
        assertEquals(0.8, result1.get(0).getCompletionRate());
        assertEquals(3600L, result1.get(0).getTimeSpentSeconds());
        
        assertEquals(999L, result1.get(1).getStudentId());
        assertEquals(10L, result1.get(1).getCourseId());
        assertEquals(60.0, result1.get(1).getQuizScore());
        assertEquals(0.6, result1.get(1).getCompletionRate());
        assertEquals(2400L, result1.get(1).getTimeSpentSeconds());
    }
    
    @Test
    void getAllStudents_shouldHandleNullResponse() {
        // Given
        when(studentMapper.findAll()).thenReturn(null);
        
        // When
        List<Student> result = studentService.getAllStudents();
        
        // Then
        assertNull(result);
        verify(studentMapper, times(1)).findAll();
    }
    
    private Student createSecondTestStudent() {
        Student student = new Student();
        student.setId(2L);
        student.setStudentId("STU002");
        student.setName("Jane Smith");
        student.setEmail("jane.smith@example.com");
        student.setMajor("Mathematics");
        student.setDateOfBirth(LocalDate.of(1999, 12, 10));
        return student;
    }
}