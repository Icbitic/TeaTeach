package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.llm.LLMService;
import org.bedrock.teateach.services.CourseEnrollmentService;
import org.bedrock.teateach.services.CourseService;
import org.bedrock.teateach.services.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentAbilityControllerTest {

    @Mock
    private LLMService llmService;

    @Mock
    private CourseService courseService;

    @Mock
    private CourseEnrollmentService courseEnrollmentService;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentAbilityController studentAbilityController;

    private Map<String, Object> testAbilityAnalysis;
    private Map<String, Object> testRecommendations;
    private List<Course> testCourses;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testAbilityAnalysis = new HashMap<>();
        Map<String, Object> abilities = new HashMap<>();
        abilities.put("mathReasoning", 85.0);
        abilities.put("languageProficiency", 78.0);
        abilities.put("codingAbility", 92.0);
        abilities.put("problemSolvingAbility", 88.0);
        abilities.put("socialKnowledge", 75.0);
        
        testAbilityAnalysis.put("abilities", abilities);
        testAbilityAnalysis.put("interestedFields", Arrays.asList("Computer Science", "Mathematics"));
        testAbilityAnalysis.put("enrolledCourses", Arrays.asList("Java Programming", "Data Structures"));
        testAbilityAnalysis.put("studentId", 1L);

        testRecommendations = new HashMap<>();
        testRecommendations.put("courses", Arrays.asList("Advanced Java", "Algorithms"));
        testRecommendations.put("books", Arrays.asList("Clean Code", "Introduction to Algorithms"));
        testRecommendations.put("videos", Arrays.asList("Java Tutorials", "Algorithm Visualization"));
        testRecommendations.put("practice", Arrays.asList("LeetCode", "HackerRank"));

        Course course1 = new Course();
        course1.setId(1L);
        course1.setCourseName("Java Programming");
        
        Course course2 = new Course();
        course2.setId(2L);
        course2.setCourseName("Data Structures");
        
        testCourses = Arrays.asList(course1, course2);
    }

    @Test
    void analyzeStudentAbilities_WhenSuccessful_ShouldReturnAnalysis() {
        // Given
        Long studentId = 1L;
        when(llmService.analyzeStudentAbilities(studentId)).thenReturn(testAbilityAnalysis);

        // When
        ResponseEntity<Map<String, Object>> response = studentAbilityController.analyzeStudentAbilities(studentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testAbilityAnalysis, response.getBody());
        verify(llmService, times(1)).analyzeStudentAbilities(studentId);
    }

    @Test
    void analyzeStudentAbilities_WhenExceptionOccurs_ShouldReturnInternalServerError() {
        // Given
        Long studentId = 1L;
        when(llmService.analyzeStudentAbilities(studentId)).thenThrow(new RuntimeException("Analysis failed"));

        // When
        ResponseEntity<Map<String, Object>> response = studentAbilityController.analyzeStudentAbilities(studentId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Analysis failed", response.getBody().get("error"));
        assertEquals("Analysis failed", response.getBody().get("message"));
        assertEquals(studentId, response.getBody().get("studentId"));
        verify(llmService, times(1)).analyzeStudentAbilities(studentId);
    }

    @Test
    void recommendResources_WithDefaultResourceType_ShouldReturnRecommendations() {
        // Given
        Long studentId = 1L;
        Map<String, Object> abilities = (Map<String, Object>) testAbilityAnalysis.get("abilities");
        List<String> interestedFields = (List<String>) testAbilityAnalysis.get("interestedFields");
        List<String> enrolledCourses = (List<String>) testAbilityAnalysis.get("enrolledCourses");
        
        when(llmService.analyzeStudentAbilities(studentId)).thenReturn(testAbilityAnalysis);
        when(llmService.recommendResources(abilities, interestedFields, enrolledCourses, "all"))
            .thenReturn(testRecommendations);

        // When
        ResponseEntity<Map<String, Object>> response = studentAbilityController.recommendResources(studentId, "all");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRecommendations, response.getBody());
        verify(llmService, times(1)).analyzeStudentAbilities(studentId);
        verify(llmService, times(1)).recommendResources(abilities, interestedFields, enrolledCourses, "all");
    }

    @Test
    void recommendResources_WithSpecificResourceType_ShouldReturnRecommendations() {
        // Given
        Long studentId = 1L;
        String resourceType = "course";
        Map<String, Object> abilities = (Map<String, Object>) testAbilityAnalysis.get("abilities");
        List<String> interestedFields = (List<String>) testAbilityAnalysis.get("interestedFields");
        List<String> enrolledCourses = (List<String>) testAbilityAnalysis.get("enrolledCourses");
        
        when(llmService.analyzeStudentAbilities(studentId)).thenReturn(testAbilityAnalysis);
        when(llmService.recommendResources(abilities, interestedFields, enrolledCourses, resourceType))
            .thenReturn(testRecommendations);

        // When
        ResponseEntity<Map<String, Object>> response = studentAbilityController.recommendResources(studentId, resourceType);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRecommendations, response.getBody());
        verify(llmService, times(1)).analyzeStudentAbilities(studentId);
        verify(llmService, times(1)).recommendResources(abilities, interestedFields, enrolledCourses, resourceType);
    }

    @Test
    void recommendResources_WhenExceptionOccurs_ShouldReturnInternalServerError() {
        // Given
        Long studentId = 1L;
        String resourceType = "course";
        when(llmService.analyzeStudentAbilities(studentId)).thenThrow(new RuntimeException("Recommendation failed"));

        // When
        ResponseEntity<Map<String, Object>> response = studentAbilityController.recommendResources(studentId, resourceType);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource recommendation failed", response.getBody().get("error"));
        assertEquals("Recommendation failed", response.getBody().get("message"));
        assertEquals(studentId, response.getBody().get("studentId"));
        assertEquals(resourceType, response.getBody().get("resourceType"));
        verify(llmService, times(1)).analyzeStudentAbilities(studentId);
    }

    @Test
    void getComprehensiveRecommendations_WhenExceptionOccurs_ShouldReturnInternalServerError() {
        // Given
        Long studentId = 1L;
        when(llmService.analyzeStudentAbilities(studentId)).thenThrow(new RuntimeException("Comprehensive recommendation failed"));

        // When
        ResponseEntity<Map<String, Object>> response = studentAbilityController.getComprehensiveRecommendations(studentId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Comprehensive recommendation failed", response.getBody().get("error"));
        assertEquals("Comprehensive recommendation failed", response.getBody().get("message"));
        assertEquals(studentId, response.getBody().get("studentId"));
        verify(llmService, times(1)).analyzeStudentAbilities(studentId);
    }

    @Test
    void getAnalysisInfo_ShouldReturnSystemInformation() {
        // When
        ResponseEntity<Map<String, Object>> response = studentAbilityController.getAnalysisInfo();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("description"));
        assertTrue(response.getBody().containsKey("abilities"));
        assertTrue(response.getBody().containsKey("resourceTypes"));
        assertTrue(response.getBody().containsKey("endpoints"));
        assertTrue(response.getBody().containsKey("dataSource"));
        assertTrue(response.getBody().containsKey("caching"));
        assertTrue(response.getBody().containsKey("features"));
        
        // Verify abilities structure
        Map<String, Object> abilities = (Map<String, Object>) response.getBody().get("abilities");
        assertTrue(abilities.containsKey("mathReasoning"));
        assertTrue(abilities.containsKey("languageProficiency"));
        assertTrue(abilities.containsKey("codingAbility"));
        assertTrue(abilities.containsKey("problemSolvingAbility"));
        assertTrue(abilities.containsKey("socialKnowledge"));
        
        // Verify resource types structure
        Map<String, Object> resourceTypes = (Map<String, Object>) response.getBody().get("resourceTypes");
        assertTrue(resourceTypes.containsKey("course"));
        assertTrue(resourceTypes.containsKey("book"));
        assertTrue(resourceTypes.containsKey("video"));
        assertTrue(resourceTypes.containsKey("practice"));
        assertTrue(resourceTypes.containsKey("all"));
    }
}