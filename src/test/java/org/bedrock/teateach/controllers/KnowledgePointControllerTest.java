package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.services.KnowledgePointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgePointControllerTest {

    @Mock
    private KnowledgePointService knowledgePointService;

    @InjectMocks
    private KnowledgePointController knowledgePointController;

    private KnowledgePoint testKnowledgePoint;
    private List<KnowledgePoint> testKnowledgePoints;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testKnowledgePoint = new KnowledgePoint();
        testKnowledgePoint.setId(1L);
        testKnowledgePoint.setName("Test Knowledge Point");
        testKnowledgePoint.setCourseId(1L);

        KnowledgePoint knowledgePoint2 = new KnowledgePoint();
        knowledgePoint2.setId(2L);
        knowledgePoint2.setName("Another Knowledge Point");
        knowledgePoint2.setCourseId(1L);

        testKnowledgePoints = Arrays.asList(testKnowledgePoint, knowledgePoint2);
    }

    @Test
    void createKnowledgePoint_ShouldReturnCreatedKnowledgePoint() {
        // Given
        when(knowledgePointService.createKnowledgePoint(any(KnowledgePoint.class))).thenReturn(testKnowledgePoint);

        // When
        ResponseEntity<KnowledgePoint> response = knowledgePointController.createKnowledgePoint(new KnowledgePoint());

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testKnowledgePoint, response.getBody());
        verify(knowledgePointService, times(1)).createKnowledgePoint(any(KnowledgePoint.class));
    }

    @Test
    void updateKnowledgePoint_WhenIdMatches_AndKnowledgePointExists_ShouldReturnUpdatedKnowledgePoint() {
        // Given
        when(knowledgePointService.updateKnowledgePoint(any(KnowledgePoint.class))).thenReturn(testKnowledgePoint);

        // When
        ResponseEntity<KnowledgePoint> response = knowledgePointController.updateKnowledgePoint(1L, testKnowledgePoint);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testKnowledgePoint, response.getBody());
        verify(knowledgePointService, times(1)).updateKnowledgePoint(any(KnowledgePoint.class));
    }

    @Test
    void updateKnowledgePoint_WhenIdMismatch_ShouldReturnBadRequest() {
        // Given
        KnowledgePoint kp = new KnowledgePoint();
        kp.setId(2L); // Different from path ID

        // When
        ResponseEntity<KnowledgePoint> response = knowledgePointController.updateKnowledgePoint(1L, kp);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(knowledgePointService, never()).updateKnowledgePoint(any(KnowledgePoint.class));
    }

    @Test
    void updateKnowledgePoint_WhenKnowledgePointDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(knowledgePointService.updateKnowledgePoint(any(KnowledgePoint.class))).thenReturn(null);

        // When
        ResponseEntity<KnowledgePoint> response = knowledgePointController.updateKnowledgePoint(1L, testKnowledgePoint);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(knowledgePointService, times(1)).updateKnowledgePoint(any(KnowledgePoint.class));
    }

    @Test
    void deleteKnowledgePoint_ShouldReturnNoContent() {
        // Given
        doNothing().when(knowledgePointService).deleteKnowledgePoint(anyLong());

        // When
        ResponseEntity<Void> response = knowledgePointController.deleteKnowledgePoint(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(knowledgePointService, times(1)).deleteKnowledgePoint(1L);
    }

    @Test
    void getKnowledgePointById_WhenKnowledgePointExists_ShouldReturnKnowledgePoint() {
        // Given
        when(knowledgePointService.getKnowledgePointById(1L)).thenReturn(Optional.of(testKnowledgePoint));

        // When
        ResponseEntity<KnowledgePoint> response = knowledgePointController.getKnowledgePointById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testKnowledgePoint, response.getBody());
        verify(knowledgePointService, times(1)).getKnowledgePointById(1L);
    }

    @Test
    void getKnowledgePointById_WhenKnowledgePointDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(knowledgePointService.getKnowledgePointById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<KnowledgePoint> response = knowledgePointController.getKnowledgePointById(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(knowledgePointService, times(1)).getKnowledgePointById(999L);
    }

    @Test
    void getKnowledgePointsByCourseId_ShouldReturnListOfKnowledgePoints() {
        // Given
        Long courseId = 1L;
        when(knowledgePointService.getKnowledgePointsByCourseId(courseId)).thenReturn(testKnowledgePoints);

        // When
        ResponseEntity<List<KnowledgePoint>> response = knowledgePointController.getKnowledgePointsByCourseId(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testKnowledgePoints.size(), response.getBody().size());
        verify(knowledgePointService, times(1)).getKnowledgePointsByCourseId(courseId);
    }

    @Test
    void getAllKnowledgePoints_ShouldReturnListOfKnowledgePoints() {
        // Given
        when(knowledgePointService.getAllKnowledgePoints()).thenReturn(testKnowledgePoints);

        // When
        ResponseEntity<List<KnowledgePoint>> response = knowledgePointController.getAllKnowledgePoints();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testKnowledgePoints.size(), response.getBody().size());
        verify(knowledgePointService, times(1)).getAllKnowledgePoints();
    }

    @Test
    void generateKnowledgeGraph_WithValidContent_ShouldReturnGeneratedKnowledgePoints() {
        // Given
        Long courseId = 1L;
        String courseContent = "Test course content";
        Map<String, String> payload = new HashMap<>();
        payload.put("courseContent", courseContent);

        when(knowledgePointService.generateKnowledgeGraphFromContent(courseContent, courseId))
                .thenReturn(testKnowledgePoints);

        // When
        ResponseEntity<List<KnowledgePoint>> response = knowledgePointController.generateKnowledgeGraph(courseId, payload);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testKnowledgePoints.size(), response.getBody().size());
        verify(knowledgePointService, times(1)).generateKnowledgeGraphFromContent(courseContent, courseId);
    }

    @Test
    void generateKnowledgeGraph_WithEmptyContent_ShouldReturnBadRequest() {
        // Given
        Long courseId = 1L;
        Map<String, String> emptyPayload = new HashMap<>();

        // When
        ResponseEntity<List<KnowledgePoint>> response = knowledgePointController.generateKnowledgeGraph(courseId, emptyPayload);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(knowledgePointService, never()).generateKnowledgeGraphFromContent(anyString(), anyLong());
    }
}
