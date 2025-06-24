package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.llm.LLMService;
import org.bedrock.teateach.mappers.KnowledgePointMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgePointServiceTest {

    @Mock
    private KnowledgePointMapper knowledgePointMapper;

    @Mock
    private LLMService llmService;

    @Mock
    private KnowledgePointService selfProxy;

    @Mock
    private CourseService courseService;

    private KnowledgePointService knowledgePointService;

    private KnowledgePoint testKnowledgePoint;

    @BeforeEach
    void setUp() {
        knowledgePointService = new KnowledgePointService(knowledgePointMapper, courseService, llmService, selfProxy);
        testKnowledgePoint = new KnowledgePoint();
        testKnowledgePoint.setId(1L);
        testKnowledgePoint.setName("Test KP");
        testKnowledgePoint.setBriefDescription("A brief description.");
        testKnowledgePoint.setCourseId(100L);
    }

    @Test
    void createKnowledgePoint_shouldInsertAndReturnKnowledgePoint() {
        // Given
        doNothing().when(knowledgePointMapper).insert(testKnowledgePoint);

        // When
        KnowledgePoint result = knowledgePointService.createKnowledgePoint(testKnowledgePoint);

        // Then
        assertNotNull(result);
        assertEquals(testKnowledgePoint.getId(), result.getId());
        verify(knowledgePointMapper, times(1)).insert(testKnowledgePoint);
    }

    @Test
    void updateKnowledgePoint_shouldUpdateAndReturnKnowledgePoint() {
        // Given
        testKnowledgePoint.setName("Updated KP");
        doNothing().when(knowledgePointMapper).update(testKnowledgePoint);

        // When
        KnowledgePoint result = knowledgePointService.updateKnowledgePoint(testKnowledgePoint);

        // Then
        assertNotNull(result);
        assertEquals("Updated KP", result.getName());
        verify(knowledgePointMapper, times(1)).update(testKnowledgePoint);
    }

    @Test
    void deleteKnowledgePoint_shouldDeleteKnowledgePoint() {
        // Given
        Long kpId = 1L;
        doNothing().when(knowledgePointMapper).delete(kpId);

        // When
        knowledgePointService.deleteKnowledgePoint(kpId);

        // Then
        verify(knowledgePointMapper, times(1)).delete(kpId);
    }

    @Test
    void getKnowledgePointById_shouldReturnKnowledgePoint_whenFound() {
        // Given
        Long kpId = 1L;
        when(knowledgePointMapper.findById(kpId)).thenReturn(testKnowledgePoint);

        // When
        Optional<KnowledgePoint> result = knowledgePointService.getKnowledgePointById(kpId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testKnowledgePoint, result.get());
        verify(knowledgePointMapper, times(1)).findById(kpId);
    }

    @Test
    void getKnowledgePointById_shouldReturnEmptyOptional_whenNotFound() {
        // Given
        Long kpId = 99L;
        when(knowledgePointMapper.findById(kpId)).thenReturn(null);

        // When
        Optional<KnowledgePoint> result = knowledgePointService.getKnowledgePointById(kpId);

        // Then
        assertFalse(result.isPresent());
        verify(knowledgePointMapper, times(1)).findById(kpId);
    }

    @Test
    void getKnowledgePointsByCourseId_shouldReturnListOfKnowledgePoints() {
        // Given
        Long courseId = 100L;
        List<KnowledgePoint> expectedKPs = Arrays.asList(testKnowledgePoint, new KnowledgePoint());
        when(knowledgePointMapper.findByCourseId(courseId)).thenReturn(expectedKPs);

        // When
        List<KnowledgePoint> result = knowledgePointService.getKnowledgePointsByCourseId(courseId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(knowledgePointMapper, times(1)).findByCourseId(courseId);
    }

    @Test
    void getAllKnowledgePoints_shouldReturnAllKnowledgePoints() {
        // Given
        List<KnowledgePoint> expectedKPs = Arrays.asList(testKnowledgePoint, new KnowledgePoint());
        when(knowledgePointMapper.findAll()).thenReturn(expectedKPs);

        // When
        List<KnowledgePoint> result = knowledgePointService.getAllKnowledgePoints();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(knowledgePointMapper, times(1)).findAll();
    }

    @Test
    void generateKnowledgeGraphFromContent_shouldExtractAndPersistKPs() {
        // Given
        String courseContent = "Content about algorithms.";
        Long courseId = 200L;

        KnowledgePoint extractedKp1 = new KnowledgePoint();
        extractedKp1.setName("Extracted KP 1");
        extractedKp1.setCourseId(null); // Simulate LLM not setting courseId

        KnowledgePoint extractedKp2 = new KnowledgePoint();
        extractedKp2.setName("Extracted KP 2");
        extractedKp2.setCourseId(courseId); // Already set

        List<KnowledgePoint> existingKPs = List.of(); // Mocked getAllKnowledgePoints result
        List<KnowledgePoint> extractedKPs = List.of(extractedKp1, extractedKp2);

        when(selfProxy.getAllKnowledgePoints()).thenReturn(existingKPs);
        when(llmService.extractKnowledgePoints(courseContent, courseId, existingKPs, List.of())).thenReturn(extractedKPs);
        doNothing().when(knowledgePointMapper).insert(any(KnowledgePoint.class));

        // When
        List<KnowledgePoint> result = knowledgePointService.generateKnowledgeGraphFromContent(courseContent, courseId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(courseId, extractedKp1.getCourseId()); // Should have been set
        verify(knowledgePointMapper, times(1)).insert(extractedKp1);
        verify(knowledgePointMapper, times(1)).insert(extractedKp2);
        verify(llmService, times(1)).extractKnowledgePoints(courseContent, courseId, existingKPs, List.of());
        verify(selfProxy, times(1)).getAllKnowledgePoints();
    }
}