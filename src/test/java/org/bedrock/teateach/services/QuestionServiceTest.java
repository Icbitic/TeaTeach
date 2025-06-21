// src/test/java/org/bedrock/teateach/services/QuestionServiceTest.java
package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Question;
import org.bedrock.teateach.enums.QuestionType;
import org.bedrock.teateach.mappers.QuestionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionService questionService;

    private Question testQuestion;

    @BeforeEach
    void setUp() {
        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setQuestionText("What is 2+2?");
        testQuestion.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        testQuestion.setDifficulty("EASY");
        testQuestion.setKnowledgePointIds(Arrays.asList(101L, 102L)); // Example List<Long>
        testQuestion.setOptions("A) 3,B) 4,C) 5");
        testQuestion.setCorrectAnswer("B");
//        testQuestion.setCreatedAt(LocalDateTime.now());
//        testQuestion.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateQuestion() {
        // Correct for void insert method
        doNothing().when(questionMapper).insert(any(Question.class));

        Question createdQuestion = questionService.createQuestion(testQuestion);

        assertNotNull(createdQuestion);
        assertEquals(testQuestion.getId(), createdQuestion.getId());
        verify(questionMapper, times(1)).insert(testQuestion);
    }

    @Test
    void testUpdateQuestion() {
        // Corrected for void update method
        doNothing().when(questionMapper).update(any(Question.class));

        Question updatedQuestion = questionService.updateQuestion(testQuestion);

        assertNotNull(updatedQuestion);
        assertEquals(testQuestion.getId(), updatedQuestion.getId());
        verify(questionMapper, times(1)).update(testQuestion);
    }

    @Test
    void testDeleteQuestion() {
        doNothing().when(questionMapper).delete(anyLong());

        questionService.deleteQuestion(1L);

        verify(questionMapper, times(1)).delete(1L);
    }

    @Test
    void testGetQuestionByIdFound() {
        when(questionMapper.findById(1L)).thenReturn(testQuestion);

        Optional<Question> foundQuestion = questionService.getQuestionById(1L);

        assertTrue(foundQuestion.isPresent());
        assertEquals(testQuestion.getId(), foundQuestion.get().getId());
        verify(questionMapper, times(1)).findById(1L);
    }

    @Test
    void testGetQuestionByIdNotFound() {
        when(questionMapper.findById(anyLong())).thenReturn(null);

        Optional<Question> foundQuestion = questionService.getQuestionById(999L);

        assertFalse(foundQuestion.isPresent());
        verify(questionMapper, times(1)).findById(999L);
    }

    @Test
    void testGetQuestionsByTypeAndDifficulty() {
        List<Question> questions = Arrays.asList(testQuestion, new Question());
        when(questionMapper.findByTypeAndDifficulty("MULTIPLE_CHOICE", "EASY")).thenReturn(questions);

        List<Question> result = questionService.getQuestionsByTypeAndDifficulty("MULTIPLE_CHOICE", "EASY");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(questionMapper, times(1)).findByTypeAndDifficulty("MULTIPLE_CHOICE", "EASY");
    }

    @Test
    void testGetQuestionsByTypeAndDifficultyWithNulls() {
        List<Question> questions = Arrays.asList(testQuestion, new Question());
        // Mapper should handle nulls, so mock it for null parameters
        when(questionMapper.findByTypeAndDifficulty(null, null)).thenReturn(questions);

        List<Question> result = questionService.getQuestionsByTypeAndDifficulty(null, null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(questionMapper, times(1)).findByTypeAndDifficulty(null, null);
    }

    @Test
    void testGetAllQuestions() {
        List<Question> questions = Arrays.asList(testQuestion, new Question());
        when(questionMapper.findAll()).thenReturn(questions);

        List<Question> result = questionService.getAllQuestions();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(questionMapper, times(1)).findAll();
    }
}