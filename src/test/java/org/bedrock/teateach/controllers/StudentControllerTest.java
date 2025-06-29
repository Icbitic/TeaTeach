package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.controllers.StudentController;
import org.bedrock.teateach.services.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @Test
    void getStudentByStudentId_WhenStudentExists_ShouldReturnStudent() {
        // Given
        String studentId = "STU001";
        when(studentService.getStudentByStudentId(studentId)).thenReturn(testStudent);

        // When
        ResponseEntity<Student> response = studentController.getStudentByStudentId(studentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testStudent, response.getBody());
        verify(studentService, times(1)).getStudentByStudentId(studentId);
    }

    @Test
    void getStudentByStudentId_WhenStudentDoesNotExist_ShouldReturnNotFound() {
        // Given
        String studentId = "STU999";
        when(studentService.getStudentByStudentId(studentId)).thenReturn(null);

        // When
        ResponseEntity<Student> response = studentController.getStudentByStudentId(studentId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(studentService, times(1)).getStudentByStudentId(studentId);
    }

    @Test
    void convertStudentIdToId_WhenStudentExists_ShouldReturnId() {
        // Given
        String studentId = "STU001";
        Long expectedId = 1L;
        when(studentService.convertStudentIdToId(studentId)).thenReturn(expectedId);

        // When
        ResponseEntity<Long> response = studentController.convertStudentIdToId(studentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedId, response.getBody());
        verify(studentService, times(1)).convertStudentIdToId(studentId);
    }

    @Test
    void convertStudentIdToId_WhenStudentDoesNotExist_ShouldReturnNotFound() {
        // Given
        String studentId = "STU999";
        when(studentService.convertStudentIdToId(studentId)).thenReturn(null);

        // When
        ResponseEntity<Long> response = studentController.convertStudentIdToId(studentId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(studentService, times(1)).convertStudentIdToId(studentId);
    }

    @Test
    void convertIdToStudentId_WhenStudentExists_ShouldReturnStudentId() {
        // Given
        Long id = 1L;
        String expectedStudentId = "STU001";
        when(studentService.convertIdToStudentId(id)).thenReturn(expectedStudentId);

        // When
        ResponseEntity<String> response = studentController.convertIdToStudentId(id);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedStudentId, response.getBody());
        verify(studentService, times(1)).convertIdToStudentId(id);
    }

    @Test
    void convertIdToStudentId_WhenStudentDoesNotExist_ShouldReturnNotFound() {
        // Given
        Long id = 999L;
        when(studentService.convertIdToStudentId(id)).thenReturn(null);

        // When
        ResponseEntity<String> response = studentController.convertIdToStudentId(id);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(studentService, times(1)).convertIdToStudentId(id);
    }

    @Test
    void existsByStudentId_WhenStudentExists_ShouldReturnTrue() {
        // Given
        String studentId = "STU001";
        when(studentService.existsByStudentId(studentId)).thenReturn(true);

        // When
        ResponseEntity<Boolean> response = studentController.existsByStudentId(studentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(studentService, times(1)).existsByStudentId(studentId);
    }

    @Test
    void existsByStudentId_WhenStudentDoesNotExist_ShouldReturnFalse() {
        // Given
        String studentId = "STU999";
        when(studentService.existsByStudentId(studentId)).thenReturn(false);

        // When
        ResponseEntity<Boolean> response = studentController.existsByStudentId(studentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
        verify(studentService, times(1)).existsByStudentId(studentId);
    }

    @Test
    void existsById_WhenStudentExists_ShouldReturnTrue() {
        // Given
        Long id = 1L;
        when(studentService.existsById(id)).thenReturn(true);

        // When
        ResponseEntity<Boolean> response = studentController.existsById(id);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(studentService, times(1)).existsById(id);
    }

    @Test
    void existsById_WhenStudentDoesNotExist_ShouldReturnFalse() {
        // Given
        Long id = 999L;
        when(studentService.existsById(id)).thenReturn(false);

        // When
        ResponseEntity<Boolean> response = studentController.existsById(id);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
        verify(studentService, times(1)).existsById(id);
    }

    @Test
    void exportStudents_WhenSuccessful_ShouldReturnExcelFile() throws Exception {
        // Given
        byte[] excelData = "mock excel data".getBytes();
        when(studentService.exportStudentsToExcel()).thenReturn(excelData);

        // When
        ResponseEntity<byte[]> response = studentController.exportStudents();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(excelData, response.getBody());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("attachment"));
        assertTrue(response.getHeaders().getContentDisposition().toString().contains(".xlsx"));
        verify(studentService, times(1)).exportStudentsToExcel();
    }

    @Test
    void exportStudents_WhenExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(studentService.exportStudentsToExcel()).thenThrow(new RuntimeException("Export failed"));

        // When
        ResponseEntity<byte[]> response = studentController.exportStudents();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(studentService, times(1)).exportStudentsToExcel();
    }

    @Test
    void importStudents_WhenFileIsValid_ShouldReturnImportedStudents() throws Exception {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(studentService.importStudentsFromExcel(mockFile)).thenReturn(testStudents);

        // When
        ResponseEntity<?> response = studentController.importStudents(mockFile);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof StudentController.ImportResponse);
        StudentController.ImportResponse importResponse = (StudentController.ImportResponse) response.getBody();
        assertTrue(importResponse.isSuccess());
        assertEquals(2, importResponse.getImportedCount());
        assertEquals(testStudents, importResponse.getStudents());
        verify(studentService, times(1)).importStudentsFromExcel(mockFile);
    }

    @Test
    void importStudents_WhenFileIsEmpty_ShouldReturnBadRequest() throws IOException {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        // When
        ResponseEntity<?> response = studentController.importStudents(mockFile);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof StudentController.ImportResponse);
        StudentController.ImportResponse importResponse = (StudentController.ImportResponse) response.getBody();
        assertFalse(importResponse.isSuccess());
        assertEquals("File is empty", importResponse.getMessage());
        assertEquals(0, importResponse.getImportedCount());
        verify(studentService, never()).importStudentsFromExcel(any());
    }

    @Test
    void importStudents_WhenIllegalArgumentException_ShouldReturnBadRequest() throws Exception {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(studentService.importStudentsFromExcel(mockFile))
            .thenThrow(new IllegalArgumentException("Invalid file format"));

        // When
        ResponseEntity<?> response = studentController.importStudents(mockFile);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof StudentController.ImportResponse);
        StudentController.ImportResponse importResponse = (StudentController.ImportResponse) response.getBody();
        assertFalse(importResponse.isSuccess());
        assertEquals("Invalid file format", importResponse.getMessage());
        assertEquals(0, importResponse.getImportedCount());
        verify(studentService, times(1)).importStudentsFromExcel(mockFile);
    }

    @Test
    void importStudents_WhenGeneralException_ShouldReturnInternalServerError() throws Exception {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(studentService.importStudentsFromExcel(mockFile))
            .thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = studentController.importStudents(mockFile);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof StudentController.ImportResponse);
        StudentController.ImportResponse importResponse = (StudentController.ImportResponse) response.getBody();
        assertFalse(importResponse.isSuccess());
        assertTrue(importResponse.getMessage().contains("Failed to import students"));
        assertTrue(importResponse.getMessage().contains("Database error"));
        assertEquals(0, importResponse.getImportedCount());
        verify(studentService, times(1)).importStudentsFromExcel(mockFile);
    }
}
