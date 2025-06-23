package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.services.LearningTaskService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningTaskControllerTest {

    @Mock
    private LearningTaskService learningTaskService;

    @InjectMocks
    private LearningTaskController learningTaskController;

    private LearningTask testTask;
    private List<LearningTask> testTasks;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testTask = new LearningTask();
        testTask.setId(1L);
        testTask.setTaskName("Test Task");
        testTask.setCourseId(1L);

        LearningTask task2 = new LearningTask();
        task2.setId(2L);
        task2.setTaskName("Another Task");
        task2.setCourseId(1L);

        testTasks = Arrays.asList(testTask, task2);
    }

    @Test
    void createTask_ShouldReturnCreatedTask() {
        // Given
        when(learningTaskService.createTask(any(LearningTask.class))).thenReturn(testTask);

        // When
        ResponseEntity<LearningTask> response = learningTaskController.createTask(new LearningTask());

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testTask, response.getBody());
        verify(learningTaskService, times(1)).createTask(any(LearningTask.class));
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        // Given
        when(learningTaskService.getTaskById(1L)).thenReturn(testTask);

        // When
        ResponseEntity<LearningTask> response = learningTaskController.getTaskById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTask, response.getBody());
        verify(learningTaskService, times(1)).getTaskById(1L);
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(learningTaskService.getTaskById(anyLong())).thenReturn(null);

        // When
        ResponseEntity<LearningTask> response = learningTaskController.getTaskById(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(learningTaskService, times(1)).getTaskById(999L);
    }

    @Test
    void getTasksByCourseId_ShouldReturnListOfTasks() {
        // Given
        Long courseId = 1L;
        when(learningTaskService.getTasksByCourseId(courseId)).thenReturn(testTasks);

        // When
        ResponseEntity<List<LearningTask>> response = learningTaskController.getTasksByCourseId(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTasks.size(), response.getBody().size());
        verify(learningTaskService, times(1)).getTasksByCourseId(courseId);
    }

    @Test
    void updateTask_WhenIdMatches_AndTaskExists_ShouldReturnUpdatedTask() {
        // Given
        when(learningTaskService.getTaskById(1L)).thenReturn(testTask);
        when(learningTaskService.updateTask(any(LearningTask.class))).thenReturn(testTask);

        // When
        ResponseEntity<LearningTask> response = learningTaskController.updateTask(1L, testTask);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTask, response.getBody());
        verify(learningTaskService, times(1)).getTaskById(1L);
        verify(learningTaskService, times(1)).updateTask(testTask);
    }

    @Test
    void updateTask_WhenIdMismatch_ShouldReturnBadRequest() {
        // Given
        LearningTask task = new LearningTask();
        task.setId(2L); // Different from path ID

        // When
        ResponseEntity<LearningTask> response = learningTaskController.updateTask(1L, task);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(learningTaskService, never()).updateTask(any(LearningTask.class));
    }

    @Test
    void updateTask_WhenTaskDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(learningTaskService.getTaskById(anyLong())).thenReturn(null);

        // When
        ResponseEntity<LearningTask> response = learningTaskController.updateTask(1L, testTask);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(learningTaskService, times(1)).getTaskById(1L);
        verify(learningTaskService, never()).updateTask(any(LearningTask.class));
    }

    @Test
    void deleteTask_WhenTaskExists_ShouldReturnNoContent() {
        // Given
        when(learningTaskService.getTaskById(1L)).thenReturn(testTask);
        doNothing().when(learningTaskService).deleteTask(anyLong());

        // When
        ResponseEntity<Void> response = learningTaskController.deleteTask(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(learningTaskService, times(1)).getTaskById(1L);
        verify(learningTaskService, times(1)).deleteTask(1L);
    }

    @Test
    void deleteTask_WhenTaskDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(learningTaskService.getTaskById(anyLong())).thenReturn(null);

        // When
        ResponseEntity<Void> response = learningTaskController.deleteTask(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(learningTaskService, times(1)).getTaskById(999L);
        verify(learningTaskService, never()).deleteTask(anyLong());
    }
}
