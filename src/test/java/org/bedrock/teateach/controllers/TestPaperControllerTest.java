package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Question;
import org.bedrock.teateach.beans.TestPaper;
import org.bedrock.teateach.controllers.TestPaperController;
import org.bedrock.teateach.dto.TestPaperGenerationRequest;
import org.bedrock.teateach.enums.QuestionType;
import org.bedrock.teateach.services.TestPaperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestPaperControllerTest {

    @Mock
    private TestPaperService testPaperService;

    @InjectMocks
    private TestPaperController testPaperController;

    private TestPaper testPaper;
    private List<TestPaper> testPapers;
    private TestPaperGenerationRequest generationRequest;
    private List<Question> testQuestions;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testPaper = new TestPaper();
        testPaper.setId(1L);
        testPaper.setPaperName("Java Programming Test");
        testPaper.setCourseId(101L);
        testPaper.setInstructorId(201L);
        testPaper.setQuestionIds(Arrays.asList(1L, 2L, 3L));
        testPaper.setTotalScore(100.0);
        testPaper.setDurationMinutes(120);
        testPaper.setGenerationMethod("MANUAL");
        testPaper.setCreatedAt(LocalDateTime.now());

        TestPaper testPaper2 = new TestPaper();
        testPaper2.setId(2L);
        testPaper2.setPaperName("Data Structures Test");
        testPaper2.setCourseId(102L);
        testPaper2.setInstructorId(201L);
        testPaper2.setQuestionIds(Arrays.asList(4L, 5L, 6L));
        testPaper2.setTotalScore(80.0);
        testPaper2.setDurationMinutes(90);
        testPaper2.setGenerationMethod("RANDOM");
        testPaper2.setCreatedAt(LocalDateTime.now());

        testPapers = Arrays.asList(testPaper, testPaper2);

        // Initialize generation request
        generationRequest = new TestPaperGenerationRequest();
        // Note: Set properties based on actual TestPaperGenerationRequest structure

        // Initialize test questions
        Question question1 = new Question();
        question1.setId(1L);
        question1.setQuestionText("What is polymorphism in Java?");
        question1.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        question1.setOptions(new String[]{"Inheritance", "Encapsulation", "Multiple forms", "Abstraction"});
        question1.setCorrectAnswer("Multiple forms");
        question1.setExplanation("Polymorphism allows objects to take multiple forms");
        question1.setDifficulty("MEDIUM");
        question1.setPoints(10.0);
        question1.setCreatedBy(1L);
        question1.setCreatedAt(LocalDateTime.now());
        question1.setIsActive(true);

        Question question2 = new Question();
        question2.setId(2L);
        question2.setQuestionText("Explain the concept of inheritance.");
        question2.setQuestionType(QuestionType.SHORT_ANSWER);
        question2.setCorrectAnswer("Inheritance allows a class to inherit properties and methods from another class");
        question2.setExplanation("Inheritance is a fundamental OOP concept");
        question2.setDifficulty("HARD");
        question2.setPoints(20.0);
        question2.setCreatedBy(1L);
        question2.setCreatedAt(LocalDateTime.now());
        question2.setIsActive(true);

        testQuestions = Arrays.asList(question1, question2);
    }

    @Test
    void createTestPaper_ShouldReturnCreatedTestPaper() {
        // Given
        when(testPaperService.createTestPaper(any(TestPaper.class))).thenReturn(testPaper);

        // When
        ResponseEntity<TestPaper> response = testPaperController.createTestPaper(new TestPaper());

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testPaper, response.getBody());
        verify(testPaperService, times(1)).createTestPaper(any(TestPaper.class));
    }

    @Test
    void generateTestPaper_ShouldReturnGeneratedTestPaper() {
        // Given
        when(testPaperService.generateTestPaper(any(TestPaperGenerationRequest.class))).thenReturn(testPaper);

        // When
        ResponseEntity<TestPaper> response = testPaperController.generateTestPaper(generationRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testPaper, response.getBody());
        verify(testPaperService, times(1)).generateTestPaper(generationRequest);
    }

    @Test
    void getTestPaperById_WhenTestPaperExists_ShouldReturnTestPaper() {
        // Given
        when(testPaperService.getTestPaperById(1L)).thenReturn(Optional.of(testPaper));

        // When
        ResponseEntity<TestPaper> response = testPaperController.getTestPaperById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPaper, response.getBody());
        verify(testPaperService, times(1)).getTestPaperById(1L);
    }

    @Test
    void getTestPaperById_WhenTestPaperDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(testPaperService.getTestPaperById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<TestPaper> response = testPaperController.getTestPaperById(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(testPaperService, times(1)).getTestPaperById(999L);
    }

    @Test
    void getAllTestPapers_WithDefaultParameters_ShouldReturnTestPapers() {
        // Given
        when(testPaperService.getAllTestPapers(0, 12, "")).thenReturn(testPapers);

        // When
        ResponseEntity<List<TestPaper>> response = testPaperController.getAllTestPapers(0, 12, "");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPapers, response.getBody());
        verify(testPaperService, times(1)).getAllTestPapers(0, 12, "");
    }

    @Test
    void getAllTestPapers_WithCustomParameters_ShouldReturnTestPapers() {
        // Given
        int page = 1;
        int size = 5;
        String search = "Java";
        when(testPaperService.getAllTestPapers(page, size, search)).thenReturn(testPapers);

        // When
        ResponseEntity<List<TestPaper>> response = testPaperController.getAllTestPapers(page, size, search);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPapers, response.getBody());
        verify(testPaperService, times(1)).getAllTestPapers(page, size, search);
    }

    @Test
    void getTestPapersByCourse_ShouldReturnTestPapersForCourse() {
        // Given
        Long courseId = 101L;
        when(testPaperService.getTestPapersByCourse(courseId)).thenReturn(List.of(testPaper));

        // When
        ResponseEntity<List<TestPaper>> response = testPaperController.getTestPapersByCourse(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testPaper, response.getBody().get(0));
        verify(testPaperService, times(1)).getTestPapersByCourse(courseId);
    }

    @Test
    void getTestPapersByInstructor_ShouldReturnTestPapersForInstructor() {
        // Given
        Long instructorId = 201L;
        when(testPaperService.getTestPapersByInstructor(instructorId)).thenReturn(testPapers);

        // When
        ResponseEntity<List<TestPaper>> response = testPaperController.getTestPapersByInstructor(instructorId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPapers, response.getBody());
        verify(testPaperService, times(1)).getTestPapersByInstructor(instructorId);
    }

    @Test
    void updateTestPaper_WhenIdMatches_AndTestPaperExists_ShouldReturnUpdatedTestPaper() {
        // Given
        when(testPaperService.getTestPaperById(1L)).thenReturn(Optional.of(testPaper));
        when(testPaperService.updateTestPaper(any(TestPaper.class))).thenReturn(testPaper);

        // When
        ResponseEntity<TestPaper> response = testPaperController.updateTestPaper(1L, testPaper);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPaper, response.getBody());
        verify(testPaperService, times(1)).getTestPaperById(1L);
        verify(testPaperService, times(1)).updateTestPaper(testPaper);
    }

    @Test
    void updateTestPaper_WhenIdMismatch_ShouldReturnBadRequest() {
        // Given
        TestPaper testPaperWithDifferentId = new TestPaper();
        testPaperWithDifferentId.setId(2L); // Different from path ID

        // When
        ResponseEntity<TestPaper> response = testPaperController.updateTestPaper(1L, testPaperWithDifferentId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(testPaperService, never()).updateTestPaper(any(TestPaper.class));
    }

    @Test
    void updateTestPaper_WhenTestPaperDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(testPaperService.getTestPaperById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<TestPaper> response = testPaperController.updateTestPaper(1L, testPaper);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(testPaperService, times(1)).getTestPaperById(1L);
        verify(testPaperService, never()).updateTestPaper(any(TestPaper.class));
    }

    @Test
    void deleteTestPaper_WhenTestPaperExists_ShouldReturnNoContent() {
        // Given
        when(testPaperService.getTestPaperById(1L)).thenReturn(Optional.of(testPaper));
        doNothing().when(testPaperService).deleteTestPaper(anyLong());

        // When
        ResponseEntity<Void> response = testPaperController.deleteTestPaper(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(testPaperService, times(1)).getTestPaperById(1L);
        verify(testPaperService, times(1)).deleteTestPaper(1L);
    }

    @Test
    void deleteTestPaper_WhenTestPaperDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(testPaperService.getTestPaperById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = testPaperController.deleteTestPaper(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(testPaperService, times(1)).getTestPaperById(999L);
        verify(testPaperService, never()).deleteTestPaper(anyLong());
    }

    @Test
    void previewQuestions_ShouldReturnQuestions() {
        // Given
        when(testPaperService.previewQuestions(any(TestPaperGenerationRequest.class))).thenReturn(testQuestions);

        // When
        ResponseEntity<List<Question>> response = testPaperController.previewQuestions(generationRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testQuestions, response.getBody());
        verify(testPaperService, times(1)).previewQuestions(generationRequest);
    }

    @Test
    void previewQuestions_WithEmptyResult_ShouldReturnEmptyList() {
        // Given
        when(testPaperService.previewQuestions(any(TestPaperGenerationRequest.class))).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<Question>> response = testPaperController.previewQuestions(generationRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(testPaperService, times(1)).previewQuestions(generationRequest);
    }
}