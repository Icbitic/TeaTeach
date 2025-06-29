package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.StudentTaskSubmission;
import org.bedrock.teateach.services.GradeService;
import org.bedrock.teateach.services.StudentTaskSubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentTaskSubmissionControllerTest {

    @Mock
    private StudentTaskSubmissionService studentTaskSubmissionService;

    @Mock
    private GradeService gradeService;

    @InjectMocks
    private StudentTaskSubmissionController studentTaskSubmissionController;

    private StudentTaskSubmission testSubmission;
    private List<StudentTaskSubmission> testSubmissions;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testSubmission = new StudentTaskSubmission();
        testSubmission.setId(1L);
        testSubmission.setStudentId(101L);
        testSubmission.setTaskId(201L);
        testSubmission.setSubmissionContent("Test submission content");
        testSubmission.setScore(85.0);
        testSubmission.setCompletionStatus(3); // 3 means scored

        StudentTaskSubmission submission2 = new StudentTaskSubmission();
        submission2.setId(2L);
        submission2.setStudentId(102L);
        submission2.setTaskId(201L);
        submission2.setSubmissionContent("Another submission content");
        submission2.setScore(90.0);
        submission2.setCompletionStatus(3); // 3 means scored

        testSubmissions = Arrays.asList(testSubmission, submission2);
    }

    @Test
    void createSubmission_ShouldReturnCreatedSubmission() {
        // Given
        when(studentTaskSubmissionService.createSubmission(any(StudentTaskSubmission.class))).thenReturn(testSubmission);

        // When
        ResponseEntity<StudentTaskSubmission> response = studentTaskSubmissionController.createSubmission(new StudentTaskSubmission());

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testSubmission, response.getBody());
        verify(studentTaskSubmissionService, times(1)).createSubmission(any(StudentTaskSubmission.class));
    }

    @Test
    void getSubmissionById_WhenSubmissionExists_ShouldReturnSubmission() {
        // Given
        when(studentTaskSubmissionService.getSubmissionById(1L)).thenReturn(Optional.of(testSubmission));

        // When
        ResponseEntity<StudentTaskSubmission> response = studentTaskSubmissionController.getSubmissionById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testSubmission, response.getBody());
        verify(studentTaskSubmissionService, times(1)).getSubmissionById(1L);
    }

    @Test
    void getSubmissionById_WhenSubmissionDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(studentTaskSubmissionService.getSubmissionById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<StudentTaskSubmission> response = studentTaskSubmissionController.getSubmissionById(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(studentTaskSubmissionService, times(1)).getSubmissionById(999L);
    }

    @Test
    void getSubmissionsByStudentId_ShouldReturnListOfSubmissions() {
        // Given
        Long studentId = 101L;
        when(studentTaskSubmissionService.getSubmissionsByStudentId(studentId)).thenReturn(List.of(testSubmission));

        // When
        ResponseEntity<List<StudentTaskSubmission>> response = studentTaskSubmissionController.getSubmissionsByStudentId(studentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testSubmission, response.getBody().get(0));
        verify(studentTaskSubmissionService, times(1)).getSubmissionsByStudentId(studentId);
    }

    @Test
    void getSubmissionsByTaskId_ShouldReturnListOfSubmissions() {
        // Given
        Long taskId = 201L;
        when(studentTaskSubmissionService.getSubmissionsByTaskId(taskId)).thenReturn(testSubmissions);

        // When
        ResponseEntity<List<StudentTaskSubmission>> response = studentTaskSubmissionController.getSubmissionsByTaskId(taskId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testSubmissions.size(), response.getBody().size());
        verify(studentTaskSubmissionService, times(1)).getSubmissionsByTaskId(taskId);
    }

    @Test
    void updateSubmission_WhenIdMatches_AndSubmissionExists_ShouldReturnUpdatedSubmission() {
        // Given
        when(studentTaskSubmissionService.getSubmissionById(1L)).thenReturn(Optional.of(testSubmission));
        when(studentTaskSubmissionService.updateSubmission(any(StudentTaskSubmission.class))).thenReturn(testSubmission);

        // When
        ResponseEntity<StudentTaskSubmission> response = studentTaskSubmissionController.updateSubmission(1L, testSubmission);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testSubmission, response.getBody());
        verify(studentTaskSubmissionService, times(1)).getSubmissionById(1L);
        verify(studentTaskSubmissionService, times(1)).updateSubmission(testSubmission);
    }

    @Test
    void updateSubmission_WhenIdMismatch_ShouldReturnBadRequest() {
        // Given
        StudentTaskSubmission submission = new StudentTaskSubmission();
        submission.setId(2L); // Different from path ID

        // When
        ResponseEntity<StudentTaskSubmission> response = studentTaskSubmissionController.updateSubmission(1L, submission);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(studentTaskSubmissionService, never()).updateSubmission(any(StudentTaskSubmission.class));
    }

    @Test
    void updateSubmission_WhenSubmissionDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(studentTaskSubmissionService.getSubmissionById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<StudentTaskSubmission> response = studentTaskSubmissionController.updateSubmission(1L, testSubmission);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studentTaskSubmissionService, times(1)).getSubmissionById(1L);
        verify(studentTaskSubmissionService, never()).updateSubmission(any(StudentTaskSubmission.class));
    }

    @Test
    void deleteSubmission_WhenSubmissionExists_ShouldReturnNoContent() {
        // Given
        when(studentTaskSubmissionService.getSubmissionById(1L)).thenReturn(Optional.of(testSubmission));
        doNothing().when(studentTaskSubmissionService).deleteSubmission(anyLong());

        // When
        ResponseEntity<Void> response = studentTaskSubmissionController.deleteSubmission(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(studentTaskSubmissionService, times(1)).getSubmissionById(1L);
        verify(studentTaskSubmissionService, times(1)).deleteSubmission(1L);
    }

    @Test
    void deleteSubmission_WhenSubmissionDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(studentTaskSubmissionService.getSubmissionById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = studentTaskSubmissionController.deleteSubmission(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studentTaskSubmissionService, times(1)).getSubmissionById(999L);
        verify(studentTaskSubmissionService, never()).deleteSubmission(anyLong());
    }

    @Test
    void recordSubmissionScore_WhenSuccessful_ShouldReturnUpdatedSubmission() {
        // Given
        Long submissionId = 1L;
        Double score = 95.0;
        when(gradeService.recordSubmissionScore(submissionId, score)).thenReturn(testSubmission);

        // When
        ResponseEntity<StudentTaskSubmission> response = studentTaskSubmissionController.recordSubmissionScore(submissionId, score);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testSubmission, response.getBody());
        verify(gradeService, times(1)).recordSubmissionScore(submissionId, score);
    }

    @Test
    void recordSubmissionScore_WhenSubmissionNotFound_ShouldReturnNotFound() {
        // Given
        Long submissionId = 999L;
        Double score = 95.0;
        when(gradeService.recordSubmissionScore(submissionId, score)).thenReturn(null);

        // When
        ResponseEntity<StudentTaskSubmission> response = studentTaskSubmissionController.recordSubmissionScore(submissionId, score);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(gradeService, times(1)).recordSubmissionScore(submissionId, score);
    }


    @Test
    void testGradeSubmissionWithLLM_SubmissionNotFound() {
        // Given
        Long submissionId = 1L;
        String gradingRubric = "Test rubric";

        when(studentTaskSubmissionService.getSubmissionById(submissionId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = studentTaskSubmissionController.gradeSubmissionWithLLM(submissionId, gradingRubric);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studentTaskSubmissionService).getSubmissionById(submissionId);
    }


    @Test
    void testGenerateFeedbackWithLLM_SubmissionNotFound() {
        // Given
        Long submissionId = 1L;
        String feedbackPrompt = "Provide detailed feedback";

        when(studentTaskSubmissionService.getSubmissionById(submissionId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = studentTaskSubmissionController.generateFeedbackWithLLM(submissionId, feedbackPrompt);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(studentTaskSubmissionService).getSubmissionById(submissionId);
    }


    @Test
    void batchGradeSubmissions_WhenException_ShouldReturnInternalServerError() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("submissionIds", Arrays.asList(1L));
        request.put("gradingRubric", "Test rubric");

        when(studentTaskSubmissionService.getSubmissionById(1L))
                .thenThrow(new RuntimeException("Batch grading failed"));

        // When
        ResponseEntity<?> response = studentTaskSubmissionController.batchGradeSubmissionsWithLLM(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(studentTaskSubmissionService, times(1)).getSubmissionById(1L);
    }
}
