package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.mappers.LearningTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningTaskServiceTest {

    @Mock
    private LearningTaskMapper learningTaskMapper;

    @InjectMocks
    private LearningTaskService learningTaskService;

    private LearningTask testTask;

    @BeforeEach
    void setUp() {
        testTask = new LearningTask();
        testTask.setId(1L);
        testTask.setTaskName("Homework 1");
        testTask.setCourseId(100L);
    }

    @Test
    void createTask_shouldInsertAndReturnTask() {
        // Given
        doNothing().when(learningTaskMapper).insert(testTask);

        // When
        LearningTask result = learningTaskService.createTask(testTask);

        // Then
        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        verify(learningTaskMapper, times(1)).insert(testTask);
    }

    @Test
    void updateTask_shouldUpdateAndReturnTask() {
        // Given
        testTask.setTaskName("Updated Homework 1");
        doNothing().when(learningTaskMapper).update(testTask);

        // When
        LearningTask result = learningTaskService.updateTask(testTask);

        // Then
        assertNotNull(result);
        assertEquals("Updated Homework 1", result.getTaskName());
        verify(learningTaskMapper, times(1)).update(testTask);
    }

    @Test
    void deleteTask_shouldDeleteTask() {
        // Given
        Long taskId = 1L;
        doNothing().when(learningTaskMapper).delete(taskId);

        // When
        learningTaskService.deleteTask(taskId);

        // Then
        verify(learningTaskMapper, times(1)).delete(taskId);
    }

    @Test
    void getTaskById_shouldReturnTask_whenFound() {
        // Given
        Long taskId = 1L;
        when(learningTaskMapper.findById(taskId)).thenReturn(testTask);

        // When
        LearningTask result = learningTaskService.getTaskById(taskId);

        // Then
        assertNotNull(result);
        assertEquals(testTask, result);
        verify(learningTaskMapper, times(1)).findById(taskId);
    }

    @Test
    void getTaskById_shouldReturnNull_whenNotFound() {
        // Given
        Long taskId = 99L;
        when(learningTaskMapper.findById(taskId)).thenReturn(null);

        // When
        LearningTask result = learningTaskService.getTaskById(taskId);

        // Then
        assertNull(result);
        verify(learningTaskMapper, times(1)).findById(taskId);
    }

    @Test
    void getTasksByCourseId_shouldReturnListOfTasks() {
        // Given
        Long courseId = 100L;
        List<LearningTask> expectedTasks = Arrays.asList(testTask, new LearningTask());
        when(learningTaskMapper.findByCourseId(courseId)).thenReturn(expectedTasks);

        // When
        List<LearningTask> result = learningTaskService.getTasksByCourseId(courseId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(learningTaskMapper, times(1)).findByCourseId(courseId);
    }
}