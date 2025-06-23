package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Question;
import org.bedrock.teateach.enums.QuestionType;
import org.bedrock.teateach.services.QuestionService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private QuestionController questionController;

    private Question testQuestion;
    private List<Question> testQuestions;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setQuestionText("Test question content");
        testQuestion.setQuestionType(QuestionType.SINGLE_CHOICE);
        testQuestion.setDifficulty("EASY");

        Question question2 = new Question();
        question2.setId(2L);
        question2.setQuestionText("Another question content");
        question2.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        question2.setDifficulty("MEDIUM");

        testQuestions = Arrays.asList(testQuestion, question2);
    }

    @Test
    void createQuestion_ShouldReturnCreatedQuestion() {
        // Given
        when(questionService.createQuestion(any(Question.class))).thenReturn(testQuestion);

        // When
        ResponseEntity<Question> response = questionController.createQuestion(new Question());

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testQuestion, response.getBody());
        verify(questionService, times(1)).createQuestion(any(Question.class));
    }

    @Test
    void getQuestionById_WhenQuestionExists_ShouldReturnQuestion() {
        // Given
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(testQuestion));

        // When
        ResponseEntity<Question> response = questionController.getQuestionById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testQuestion, response.getBody());
        verify(questionService, times(1)).getQuestionById(1L);
    }

    @Test
    void getQuestionById_WhenQuestionDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(questionService.getQuestionById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Question> response = questionController.getQuestionById(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(questionService, times(1)).getQuestionById(999L);
    }

    @Test
    void getQuestionsByFilters_WithTypeAndDifficulty_ShouldReturnFilteredQuestions() {
        // Given
        String type = "SINGLE_CHOICE";
        String difficulty = "EASY";
        when(questionService.getQuestionsByTypeAndDifficulty(type, difficulty)).thenReturn(List.of(testQuestion));

        // When
        ResponseEntity<List<Question>> response = questionController.getQuestionsByFilters(type, difficulty);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testQuestion, response.getBody().get(0));
        verify(questionService, times(1)).getQuestionsByTypeAndDifficulty(type, difficulty);
    }

    @Test
    void getQuestionsByFilters_WithTypeOnly_ShouldReturnFilteredQuestions() {
        // Given
        String type = "SINGLE_CHOICE";
        when(questionService.getQuestionsByTypeAndDifficulty(type, null)).thenReturn(List.of(testQuestion));

        // When
        ResponseEntity<List<Question>> response = questionController.getQuestionsByFilters(type, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(questionService, times(1)).getQuestionsByTypeAndDifficulty(type, null);
    }

    @Test
    void updateQuestion_WhenIdMatches_AndQuestionExists_ShouldReturnUpdatedQuestion() {
        // Given
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(testQuestion));
        when(questionService.updateQuestion(any(Question.class))).thenReturn(testQuestion);

        // When
        ResponseEntity<Question> response = questionController.updateQuestion(1L, testQuestion);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testQuestion, response.getBody());
        verify(questionService, times(1)).getQuestionById(1L);
        verify(questionService, times(1)).updateQuestion(testQuestion);
    }

    @Test
    void updateQuestion_WhenIdMismatch_ShouldReturnBadRequest() {
        // Given
        Question question = new Question();
        question.setId(2L); // Different from path ID

        // When
        ResponseEntity<Question> response = questionController.updateQuestion(1L, question);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(questionService, never()).updateQuestion(any(Question.class));
    }

    @Test
    void updateQuestion_WhenQuestionDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(questionService.getQuestionById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Question> response = questionController.updateQuestion(1L, testQuestion);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(questionService, times(1)).getQuestionById(1L);
        verify(questionService, never()).updateQuestion(any(Question.class));
    }

    @Test
    void deleteQuestion_WhenQuestionExists_ShouldReturnNoContent() {
        // Given
        when(questionService.getQuestionById(1L)).thenReturn(Optional.of(testQuestion));
        doNothing().when(questionService).deleteQuestion(anyLong());

        // When
        ResponseEntity<Void> response = questionController.deleteQuestion(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(questionService, times(1)).getQuestionById(1L);
        verify(questionService, times(1)).deleteQuestion(1L);
    }

    @Test
    void deleteQuestion_WhenQuestionDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(questionService.getQuestionById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = questionController.deleteQuestion(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(questionService, times(1)).getQuestionById(999L);
        verify(questionService, never()).deleteQuestion(anyLong());
    }
}
