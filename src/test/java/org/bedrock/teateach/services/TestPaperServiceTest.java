package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Question;
import org.bedrock.teateach.beans.TestPaper;
import org.bedrock.teateach.dto.TestPaperGenerationRequest;
import org.bedrock.teateach.enums.QuestionType;
import org.bedrock.teateach.mappers.TestPaperMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestPaperServiceTest {

    @Mock
    private TestPaperMapper testPaperMapper;

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private TestPaperService testPaperService;

    private TestPaper testTestPaper;
    private TestPaperGenerationRequest testRequest;
    private List<Question> testQuestions;

    @BeforeEach
    void setUp() {
        testTestPaper = new TestPaper();
        testTestPaper.setId(1L);
        testTestPaper.setPaperName("Test Paper");
        testTestPaper.setCourseId(1L);
        testTestPaper.setInstructorId(1L);
        testTestPaper.setQuestionIds(Arrays.asList(1L, 2L, 3L));
        testTestPaper.setTotalScore(100.0);
        testTestPaper.setDurationMinutes(60);
        testTestPaper.setGenerationMethod("RANDOM");
        testTestPaper.setCreatedAt(LocalDateTime.now());

        testRequest = new TestPaperGenerationRequest();
        testRequest.setPaperName("Generated Test Paper");
        testRequest.setCourseId(1L);
        testRequest.setInstructorId(1L);
        testRequest.setGenerationMethod("RANDOM");
        testRequest.setTotalQuestions(5);
        testRequest.setTotalScore(100.0);
        testRequest.setDurationMinutes(60);

        testQuestions = createTestQuestions();
    }

    private List<Question> createTestQuestions() {
        List<Question> questions = new ArrayList<>();
        
        Question q1 = new Question();
        q1.setId(1L);
        q1.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        q1.setDifficulty("EASY");
        q1.setKnowledgePointIds(Arrays.asList(1L, 2L));
        questions.add(q1);
        
        Question q2 = new Question();
        q2.setId(2L);
        q2.setQuestionType(QuestionType.FILL_IN_THE_BLANK);
        q2.setDifficulty("MEDIUM");
        q2.setKnowledgePointIds(Arrays.asList(2L, 3L));
        questions.add(q2);
        
        Question q3 = new Question();
        q3.setId(3L);
        q3.setQuestionType(QuestionType.SHORT_ANSWER);
        q3.setDifficulty("HARD");
        q3.setKnowledgePointIds(Arrays.asList(1L, 3L));
        questions.add(q3);
        
        Question q4 = new Question();
        q4.setId(4L);
        q4.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        q4.setDifficulty("EASY");
        q4.setKnowledgePointIds(Arrays.asList(1L));
        questions.add(q4);
        
        Question q5 = new Question();
        q5.setId(5L);
        q5.setQuestionType(QuestionType.PROGRAMMING);
        q5.setDifficulty("HARD");
        q5.setKnowledgePointIds(Arrays.asList(2L));
        questions.add(q5);
        
        return questions;
    }

    @Test
    void createTestPaper_shouldCreateTestPaper() {
        // Given
        TestPaper newTestPaper = new TestPaper();
        newTestPaper.setPaperName("New Test Paper");
        newTestPaper.setCourseId(1L);
        
        doNothing().when(testPaperMapper).insert(any(TestPaper.class));
        
        // When
        TestPaper result = testPaperService.createTestPaper(newTestPaper);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertEquals("New Test Paper", result.getPaperName());
        assertEquals(1L, result.getCourseId());
        verify(testPaperMapper).insert(newTestPaper);
    }

    @Test
    void generateTestPaper_shouldGenerateRandomTestPaper() {
        // Given
        when(questionService.getAllQuestions()).thenReturn(testQuestions);
        doNothing().when(testPaperMapper).insert(any(TestPaper.class));
        
        // When
        TestPaper result = testPaperService.generateTestPaper(testRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("Generated Test Paper", result.getPaperName());
        assertEquals(1L, result.getCourseId());
        assertEquals(1L, result.getInstructorId());
        assertEquals(100.0, result.getTotalScore());
        assertEquals(60, result.getDurationMinutes());
        assertEquals("RANDOM", result.getGenerationMethod());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getQuestionIds());
        assertTrue(result.getQuestionIds().size() <= 5);
        verify(testPaperMapper).insert(result);
    }

    @Test
    void generateTestPaper_shouldGenerateByKnowledgePoints() {
        // Given
        testRequest.setGenerationMethod("BY_KNOWLEDGE_POINT");
        testRequest.setKnowledgePointIds(Arrays.asList(1L, 2L));
        testRequest.setKnowledgePointQuestionCounts(Map.of(1L, 2, 2L, 1));
        
        when(questionService.getAllQuestions()).thenReturn(testQuestions);
        doNothing().when(testPaperMapper).insert(any(TestPaper.class));
        
        // When
        TestPaper result = testPaperService.generateTestPaper(testRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("BY_KNOWLEDGE_POINT", result.getGenerationMethod());
        assertNotNull(result.getQuestionIds());
        verify(testPaperMapper).insert(result);
    }

    @Test
    void generateTestPaper_shouldGenerateByDifficulty() {
        // Given
        testRequest.setGenerationMethod("BY_DIFFICULTY");
        testRequest.setDifficulties(Arrays.asList("EASY", "MEDIUM"));
        testRequest.setDifficultyQuestionCounts(Map.of("EASY", 2, "MEDIUM", 1));
        
        when(questionService.getAllQuestions()).thenReturn(testQuestions);
        doNothing().when(testPaperMapper).insert(any(TestPaper.class));
        
        // When
        TestPaper result = testPaperService.generateTestPaper(testRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("BY_DIFFICULTY", result.getGenerationMethod());
        assertNotNull(result.getQuestionIds());
        verify(testPaperMapper).insert(result);
    }

    @Test
    void generateTestPaper_shouldGenerateBalanced() {
        // Given
        testRequest.setGenerationMethod("BALANCED");
        testRequest.setQuestionTypeDistribution(Map.of(
            QuestionType.MULTIPLE_CHOICE, 2,
            QuestionType.FILL_IN_THE_BLANK, 1
        ));
        testRequest.setDifficultyWeights(Map.of("EASY", 0.8, "MEDIUM", 0.6, "HARD", 0.4));
        
        when(questionService.getAllQuestions()).thenReturn(testQuestions);
        doNothing().when(testPaperMapper).insert(any(TestPaper.class));
        
        // When
        TestPaper result = testPaperService.generateTestPaper(testRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("BALANCED", result.getGenerationMethod());
        assertNotNull(result.getQuestionIds());
        verify(testPaperMapper).insert(result);
    }

    @Test
    void generateTestPaper_shouldThrowException_whenInvalidGenerationMethod() {
        // Given
        testRequest.setGenerationMethod("INVALID_METHOD");
        when(questionService.getAllQuestions()).thenReturn(testQuestions);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            testPaperService.generateTestPaper(testRequest);
        });
    }

    @Test
    void getTestPaperById_shouldReturnTestPaper_whenExists() {
        // Given
        when(testPaperMapper.findById(1L)).thenReturn(testTestPaper);
        
        // When
        Optional<TestPaper> result = testPaperService.getTestPaperById(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testTestPaper, result.get());
        verify(testPaperMapper).findById(1L);
    }

    @Test
    void getTestPaperById_shouldReturnEmpty_whenNotExists() {
        // Given
        when(testPaperMapper.findById(1L)).thenReturn(null);
        
        // When
        Optional<TestPaper> result = testPaperService.getTestPaperById(1L);
        
        // Then
        assertFalse(result.isPresent());
        verify(testPaperMapper).findById(1L);
    }

    @Test
    void getTestPapersByCourse_shouldReturnTestPapers() {
        // Given
        List<TestPaper> testPapers = Arrays.asList(testTestPaper);
        when(testPaperMapper.findByCourseId(1L)).thenReturn(testPapers);
        
        // When
        List<TestPaper> result = testPaperService.getTestPapersByCourse(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTestPaper, result.get(0));
        verify(testPaperMapper).findByCourseId(1L);
    }

    @Test
    void getTestPapersByInstructor_shouldReturnTestPapers() {
        // Given
        List<TestPaper> testPapers = Arrays.asList(testTestPaper);
        when(testPaperMapper.findByInstructorId(1L)).thenReturn(testPapers);
        
        // When
        List<TestPaper> result = testPaperService.getTestPapersByInstructor(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTestPaper, result.get(0));
        verify(testPaperMapper).findByInstructorId(1L);
    }

    @Test
    void getAllTestPapers_shouldReturnPaginatedResults_withoutSearch() {
        // Given
        List<TestPaper> testPapers = Arrays.asList(testTestPaper);
        when(testPaperMapper.findAllWithPagination(0, 10)).thenReturn(testPapers);
        
        // When
        List<TestPaper> result = testPaperService.getAllTestPapers(0, 10, null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(testPaperMapper).findAllWithPagination(0, 10);
        verify(testPaperMapper, never()).findAllWithPaginationAndSearch(anyInt(), anyInt(), anyString());
    }

    @Test
    void getAllTestPapers_shouldReturnPaginatedResults_withSearch() {
        // Given
        List<TestPaper> testPapers = Arrays.asList(testTestPaper);
        when(testPaperMapper.findAllWithPaginationAndSearch(0, 10, "test")).thenReturn(testPapers);
        
        // When
        List<TestPaper> result = testPaperService.getAllTestPapers(0, 10, "test");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(testPaperMapper).findAllWithPaginationAndSearch(0, 10, "test");
        verify(testPaperMapper, never()).findAllWithPagination(anyInt(), anyInt());
    }

    @Test
    void getAllTestPapers_shouldReturnPaginatedResults_withEmptySearch() {
        // Given
        List<TestPaper> testPapers = Arrays.asList(testTestPaper);
        when(testPaperMapper.findAllWithPagination(0, 10)).thenReturn(testPapers);
        
        // When
        List<TestPaper> result = testPaperService.getAllTestPapers(0, 10, "");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(testPaperMapper).findAllWithPagination(0, 10);
        verify(testPaperMapper, never()).findAllWithPaginationAndSearch(anyInt(), anyInt(), anyString());
    }

    @Test
    void updateTestPaper_shouldUpdateTestPaper() {
        // Given
        doNothing().when(testPaperMapper).update(testTestPaper);
        
        // When
        TestPaper result = testPaperService.updateTestPaper(testTestPaper);
        
        // Then
        assertNotNull(result);
        assertEquals(testTestPaper, result);
        verify(testPaperMapper).update(testTestPaper);
    }

    @Test
    void deleteTestPaper_shouldDeleteTestPaper() {
        // Given
        doNothing().when(testPaperMapper).delete(1L);
        
        // When
        testPaperService.deleteTestPaper(1L);
        
        // Then
        verify(testPaperMapper).delete(1L);
    }

    @Test
    void previewQuestions_shouldReturnSelectedQuestions() {
        // Given
        when(questionService.getAllQuestions()).thenReturn(testQuestions);
        
        // When
        List<Question> result = testPaperService.previewQuestions(testRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.size() <= testRequest.getTotalQuestions());
        verify(questionService).getAllQuestions();
    }

    @Test
    void generateTestPaper_shouldHandleKnowledgePointsWithNullCounts() {
        // Given
        testRequest.setGenerationMethod("BY_KNOWLEDGE_POINT");
        testRequest.setKnowledgePointIds(Arrays.asList(1L, 2L));
        testRequest.setKnowledgePointQuestionCounts(null); // null counts
        
        when(questionService.getAllQuestions()).thenReturn(testQuestions);
        doNothing().when(testPaperMapper).insert(any(TestPaper.class));
        
        // When
        TestPaper result = testPaperService.generateTestPaper(testRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("BY_KNOWLEDGE_POINT", result.getGenerationMethod());
        verify(testPaperMapper).insert(result);
    }

    @Test
    void generateTestPaper_shouldHandleBalancedWithKnowledgePointCoverage() {
        // Given
        testRequest.setGenerationMethod("BALANCED");
        testRequest.setQuestionTypeDistribution(Map.of(QuestionType.MULTIPLE_CHOICE, 1));
        testRequest.setIncludeAllKnowledgePoints(true);
        testRequest.setKnowledgePointIds(Arrays.asList(1L, 2L, 3L));
        
        when(questionService.getAllQuestions()).thenReturn(testQuestions);
        doNothing().when(testPaperMapper).insert(any(TestPaper.class));
        
        // When
        TestPaper result = testPaperService.generateTestPaper(testRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("BALANCED", result.getGenerationMethod());
        verify(testPaperMapper).insert(result);
    }

    @Test
    void generateTestPaper_shouldHandleRandomWithQuestionTypeFilter() {
        // Given
        testRequest.setGenerationMethod("RANDOM");
        testRequest.setQuestionTypes(Arrays.asList(QuestionType.MULTIPLE_CHOICE, QuestionType.FILL_IN_THE_BLANK));
        
        when(questionService.getAllQuestions()).thenReturn(testQuestions);
        doNothing().when(testPaperMapper).insert(any(TestPaper.class));
        
        // When
        TestPaper result = testPaperService.generateTestPaper(testRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("RANDOM", result.getGenerationMethod());
        verify(testPaperMapper).insert(result);
    }

    @Test
    void generateTestPaper_shouldHandleEmptyQuestionsList() {
        // Given
        when(questionService.getAllQuestions()).thenReturn(new ArrayList<>());
        doNothing().when(testPaperMapper).insert(any(TestPaper.class));
        
        // When
        TestPaper result = testPaperService.generateTestPaper(testRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getQuestionIds().isEmpty());
        verify(testPaperMapper).insert(result);
    }
}