package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.llm.LLMService;
import org.bedrock.teateach.mappers.KnowledgePointMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgePointIdMappingTest {

    @Mock
    private KnowledgePointMapper knowledgePointMapper;

    @Mock
    private CourseService courseService;

    @Mock
    private LLMService llmService;

    private KnowledgePointService knowledgePointService;

    @BeforeEach
    void setUp() {
        knowledgePointService = new KnowledgePointService(knowledgePointMapper, courseService, llmService, null);
        // Set self proxy to the service itself for testing
        ReflectionTestUtils.setField(knowledgePointService, "selfProxy", knowledgePointService);
    }

    @Test
    void testIdMappingInGenerateKnowledgeGraphFromContent() {
        // Arrange
        Long courseId = 1L;
        String courseContent = "Test content";
        
        // Create mock knowledge points with temporary IDs (10001+)
        KnowledgePoint kp1 = new KnowledgePoint();
        kp1.setId(10001L);
        kp1.setName("Concept 1");
        kp1.setCourseId(courseId);
        kp1.setPrerequisiteKnowledgePointIds(null);
        kp1.setRelatedKnowledgePointIds(Arrays.asList(10002L));
        
        KnowledgePoint kp2 = new KnowledgePoint();
        kp2.setId(10002L);
        kp2.setName("Concept 2");
        kp2.setCourseId(courseId);
        kp2.setPrerequisiteKnowledgePointIds(Arrays.asList(10001L));
        kp2.setRelatedKnowledgePointIds(null);
        
        List<KnowledgePoint> extractedKPs = Arrays.asList(kp1, kp2);
        
        // Mock LLM service to return our test knowledge points
        when(llmService.extractKnowledgePoints(eq(courseContent), eq(courseId), any()))
            .thenReturn(extractedKPs);
        
        // Mock mapper insert to simulate database ID assignment
        doAnswer(invocation -> {
            KnowledgePoint kp = invocation.getArgument(0);
            if (kp.getName().equals("Concept 1")) {
                kp.setId(100L); // Real database ID
            } else if (kp.getName().equals("Concept 2")) {
                kp.setId(200L); // Real database ID
            }
            return null;
        }).when(knowledgePointMapper).insert(any(KnowledgePoint.class));
        
        // Act
        List<KnowledgePoint> result = knowledgePointService.generateKnowledgeGraphFromContent(courseContent, courseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify that insert was called twice (first pass)
        verify(knowledgePointMapper, times(2)).insert(any(KnowledgePoint.class));
        
        // Verify that update was called twice (second pass for relationship mapping)
        verify(knowledgePointMapper, times(2)).update(any(KnowledgePoint.class));
        
        // Check that IDs were properly mapped
        KnowledgePoint resultKp1 = result.stream().filter(kp -> kp.getName().equals("Concept 1")).findFirst().orElse(null);
        KnowledgePoint resultKp2 = result.stream().filter(kp -> kp.getName().equals("Concept 2")).findFirst().orElse(null);
        
        assertNotNull(resultKp1);
        assertNotNull(resultKp2);
        
        // Verify that the relationships now reference real database IDs
        assertEquals(Arrays.asList(200L), resultKp1.getRelatedKnowledgePointIds());
        assertEquals(Arrays.asList(100L), resultKp2.getPrerequisiteKnowledgePointIds());
    }
    
    @Test
    void testIdMappingWithExistingIds() {
        // Test that existing IDs (< 10001) are preserved
        Long courseId = 1L;
        String courseContent = "Test content";
        
        KnowledgePoint kp1 = new KnowledgePoint();
        kp1.setId(10001L);
        kp1.setName("New Concept");
        kp1.setCourseId(courseId);
        kp1.setPrerequisiteKnowledgePointIds(Arrays.asList(50L)); // Existing ID
        kp1.setRelatedKnowledgePointIds(Arrays.asList(10002L)); // Temporary ID
        
        KnowledgePoint kp2 = new KnowledgePoint();
        kp2.setId(10002L);
        kp2.setName("Another Concept");
        kp2.setCourseId(courseId);
        
        List<KnowledgePoint> extractedKPs = Arrays.asList(kp1, kp2);
        
        when(llmService.extractKnowledgePoints(eq(courseContent), eq(courseId), any()))
            .thenReturn(extractedKPs);
        
        doAnswer(invocation -> {
            KnowledgePoint kp = invocation.getArgument(0);
            if (kp.getName().equals("New Concept")) {
                kp.setId(300L);
            } else if (kp.getName().equals("Another Concept")) {
                kp.setId(400L);
            }
            return null;
        }).when(knowledgePointMapper).insert(any(KnowledgePoint.class));
        
        // Act
        List<KnowledgePoint> result = knowledgePointService.generateKnowledgeGraphFromContent(courseContent, courseId);
        
        // Assert
        KnowledgePoint resultKp1 = result.stream().filter(kp -> kp.getName().equals("New Concept")).findFirst().orElse(null);
        assertNotNull(resultKp1);
        
        // Verify that existing ID (50L) is preserved and temporary ID (10002L) is mapped to real ID (400L)
        assertEquals(Arrays.asList(50L), resultKp1.getPrerequisiteKnowledgePointIds());
        assertEquals(Arrays.asList(400L), resultKp1.getRelatedKnowledgePointIds());
    }
}