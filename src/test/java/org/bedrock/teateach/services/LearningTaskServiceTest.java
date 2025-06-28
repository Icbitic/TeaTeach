package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.beans.TaskResource;
import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.mappers.LearningTaskMapper;
import org.bedrock.teateach.mappers.TaskResourceMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningTaskServiceTest {

    @Mock
    private LearningTaskMapper learningTaskMapper;
    
    @Mock
    private TaskResourceMapper taskResourceMapper;

    @InjectMocks
    private LearningTaskService learningTaskService;

    private LearningTask testTask;
    private TaskResource testTaskResource;
    private Resource testResource;

    @BeforeEach
    void setUp() {
        testTask = new LearningTask();
        testTask.setId(1L);
        testTask.setTaskName("Homework 1");
        testTask.setCourseId(100L);
        
        testTaskResource = new TaskResource();
        testTaskResource.setId(1L);
        testTaskResource.setTaskId(1L);
        testTaskResource.setResourceId(1L);
        testTaskResource.setCreatedAt(LocalDateTime.now());
        testTaskResource.setUpdatedAt(LocalDateTime.now());
        
        testResource = new Resource();
        testResource.setId(1L);
        testResource.setResourceName("Test Resource");
        testResource.setFilePath("/path/to/resource.pdf");
        testResource.setFileType("pdf");
        testResource.setUploadedBy(1L);
        testResource.setFileSize(1024L);
        testResource.setDescription("Test resource description");
        testResource.setCreatedAt(LocalDateTime.now());
        testResource.setUpdatedAt(LocalDateTime.now());
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
    
    @Test
    void getTasksByCourseId_shouldReturnEmptyList_whenNoTasksFound() {
        // Given
        Long courseId = 999L;
        when(learningTaskMapper.findByCourseId(courseId)).thenReturn(Collections.emptyList());

        // When
        List<LearningTask> result = learningTaskService.getTasksByCourseId(courseId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(learningTaskMapper, times(1)).findByCourseId(courseId);
    }
    
    @Test
    void getAllTasks_shouldReturnAllTasks() {
        // Given
        List<LearningTask> expectedTasks = Arrays.asList(testTask, new LearningTask());
        when(learningTaskMapper.findAll()).thenReturn(expectedTasks);

        // When
        List<LearningTask> result = learningTaskService.getAllTasks();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(learningTaskMapper, times(1)).findAll();
    }
    
    @Test
    void getAllTasks_shouldReturnEmptyList_whenNoTasksExist() {
        // Given
        when(learningTaskMapper.findAll()).thenReturn(Collections.emptyList());

        // When
        List<LearningTask> result = learningTaskService.getAllTasks();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(learningTaskMapper, times(1)).findAll();
    }
    
    // Task Resource Management Tests
    
    @Test
    void addResourceToTask_shouldCreateTaskResource_whenRelationshipDoesNotExist() {
        // Given
        Long taskId = 1L;
        Long resourceId = 1L;
        when(taskResourceMapper.existsByTaskIdAndResourceId(taskId, resourceId)).thenReturn(false);
        doNothing().when(taskResourceMapper).insert(any(TaskResource.class));

        // When
        TaskResource result = learningTaskService.addResourceToTask(taskId, resourceId);

        // Then
        assertNotNull(result);
        assertEquals(taskId, result.getTaskId());
        assertEquals(resourceId, result.getResourceId());
        verify(taskResourceMapper, times(1)).existsByTaskIdAndResourceId(taskId, resourceId);
        verify(taskResourceMapper, times(1)).insert(any(TaskResource.class));
    }
    
    @Test
    void addResourceToTask_shouldThrowException_whenRelationshipAlreadyExists() {
        // Given
        Long taskId = 1L;
        Long resourceId = 1L;
        when(taskResourceMapper.existsByTaskIdAndResourceId(taskId, resourceId)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> learningTaskService.addResourceToTask(taskId, resourceId));
        
        assertEquals("Resource is already associated with this task", exception.getMessage());
        verify(taskResourceMapper, times(1)).existsByTaskIdAndResourceId(taskId, resourceId);
        verify(taskResourceMapper, never()).insert(any(TaskResource.class));
    }
    
    @Test
    void removeResourceFromTask_shouldDeleteTaskResource() {
        // Given
        Long taskId = 1L;
        Long resourceId = 1L;
        doNothing().when(taskResourceMapper).deleteByTaskIdAndResourceId(taskId, resourceId);

        // When
        learningTaskService.removeResourceFromTask(taskId, resourceId);

        // Then
        verify(taskResourceMapper, times(1)).deleteByTaskIdAndResourceId(taskId, resourceId);
    }
    
    @Test
    void getTaskResources_shouldReturnListOfResources() {
        // Given
        Long taskId = 1L;
        List<Resource> expectedResources = Arrays.asList(testResource, new Resource());
        when(taskResourceMapper.findResourcesByTaskId(taskId)).thenReturn(expectedResources);

        // When
        List<Resource> result = learningTaskService.getTaskResources(taskId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskResourceMapper, times(1)).findResourcesByTaskId(taskId);
    }
    
    @Test
    void getTaskResources_shouldReturnEmptyList_whenNoResourcesFound() {
        // Given
        Long taskId = 999L;
        when(taskResourceMapper.findResourcesByTaskId(taskId)).thenReturn(Collections.emptyList());

        // When
        List<Resource> result = learningTaskService.getTaskResources(taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskResourceMapper, times(1)).findResourcesByTaskId(taskId);
    }
    
    @Test
    void getTaskResourceRelationships_shouldReturnListOfTaskResources() {
        // Given
        Long taskId = 1L;
        List<TaskResource> expectedRelationships = Arrays.asList(testTaskResource, new TaskResource());
        when(taskResourceMapper.findByTaskId(taskId)).thenReturn(expectedRelationships);

        // When
        List<TaskResource> result = learningTaskService.getTaskResourceRelationships(taskId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskResourceMapper, times(1)).findByTaskId(taskId);
    }
    
    @Test
    void getTaskResourceRelationships_shouldReturnEmptyList_whenNoRelationshipsFound() {
        // Given
        Long taskId = 999L;
        when(taskResourceMapper.findByTaskId(taskId)).thenReturn(Collections.emptyList());

        // When
        List<TaskResource> result = learningTaskService.getTaskResourceRelationships(taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskResourceMapper, times(1)).findByTaskId(taskId);
    }
    
    @Test
    void removeAllResourcesFromTask_shouldDeleteAllTaskResources() {
        // Given
        Long taskId = 1L;
        doNothing().when(taskResourceMapper).deleteByTaskId(taskId);

        // When
        learningTaskService.removeAllResourcesFromTask(taskId);

        // Then
        verify(taskResourceMapper, times(1)).deleteByTaskId(taskId);
    }
    
    @Test
    void deleteTask_shouldCallGetTaskByIdAndDelete() {
        // Given
        Long taskId = 1L;
        when(learningTaskMapper.findById(taskId)).thenReturn(testTask);
        doNothing().when(learningTaskMapper).delete(taskId);

        // When
        learningTaskService.deleteTask(taskId);

        // Then
        verify(learningTaskMapper, times(1)).findById(taskId);
        verify(learningTaskMapper, times(1)).delete(taskId);
    }
}