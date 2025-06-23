package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.GradeAnalysis;
import org.bedrock.teateach.services.GradeService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeAnalysisControllerTest {

    @Mock
    private GradeService gradeService;

    @InjectMocks
    private GradeAnalysisController gradeAnalysisController;

    private GradeAnalysis testGradeAnalysis;
    private List<GradeAnalysis> testGradeAnalysisList;
    private byte[] testExcelBytes;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testGradeAnalysis = new GradeAnalysis();
        // Set properties on testGradeAnalysis as needed

        GradeAnalysis gradeAnalysis2 = new GradeAnalysis();
        // Set properties on gradeAnalysis2 as needed

        testGradeAnalysisList = Arrays.asList(testGradeAnalysis, gradeAnalysis2);
        testExcelBytes = "mock excel content".getBytes();
    }

    @Test
    void getStudentCourseGrade_WhenGradeExists_ShouldReturnGradeAnalysis() {
        // Given
        Long studentId = 1L;
        Long courseId = 1L;
        when(gradeService.getStudentCourseGrade(studentId, courseId)).thenReturn(testGradeAnalysis);

        // When
        ResponseEntity<GradeAnalysis> response = gradeAnalysisController.getStudentCourseGrade(studentId, courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGradeAnalysis, response.getBody());
        verify(gradeService, times(1)).getStudentCourseGrade(studentId, courseId);
    }

    @Test
    void getStudentCourseGrade_WhenGradeDoesNotExist_ShouldReturnNotFound() {
        // Given
        Long studentId = 999L;
        Long courseId = 999L;
        when(gradeService.getStudentCourseGrade(studentId, courseId)).thenReturn(null);

        // When
        ResponseEntity<GradeAnalysis> response = gradeAnalysisController.getStudentCourseGrade(studentId, courseId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(gradeService, times(1)).getStudentCourseGrade(studentId, courseId);
    }

    @Test
    void getCourseGrades_ShouldReturnListOfGradeAnalyses() {
        // Given
        Long courseId = 1L;
        when(gradeService.getCourseGrades(courseId)).thenReturn(testGradeAnalysisList);

        // When
        ResponseEntity<List<GradeAnalysis>> response = gradeAnalysisController.getCourseGrades(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGradeAnalysisList.size(), response.getBody().size());
        verify(gradeService, times(1)).getCourseGrades(courseId);
    }

    @Test
    void exportGradesToExcel_ShouldReturnExcelByteArray() {
        // Given
        Long courseId = 1L;
        when(gradeService.exportGradesToExcel(courseId)).thenReturn(testExcelBytes);

        // When
        ResponseEntity<byte[]> response = gradeAnalysisController.exportGradesToExcel(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testExcelBytes.length, response.getBody().length);
        assertNotNull(response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("course_grades_" + courseId));
        verify(gradeService, times(1)).exportGradesToExcel(courseId);
    }
}
