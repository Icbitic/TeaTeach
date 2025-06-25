package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.beans.TaskResource;
import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.mappers.LearningTaskMapper;
import org.bedrock.teateach.mappers.TaskResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LearningTaskService {

    private final LearningTaskMapper learningTaskMapper;
    private final TaskResourceMapper taskResourceMapper;

    @Autowired
    public LearningTaskService(LearningTaskMapper learningTaskMapper, TaskResourceMapper taskResourceMapper) {
        this.learningTaskMapper = learningTaskMapper;
        this.taskResourceMapper = taskResourceMapper;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "courseTasks", key = "#task.courseId"),
        @CacheEvict(value = "allTasks", allEntries = true)
    })
    public LearningTask createTask(LearningTask task) {
        learningTaskMapper.insert(task);
        return task;
    }

    @Transactional
    @Caching(put = { @CachePut(value = "learningTasks", key = "#task.id") },
             evict = { 
                 @CacheEvict(value = "courseTasks", key = "#task.courseId"),
                 @CacheEvict(value = "allTasks", allEntries = true)
             })
    public LearningTask updateTask(LearningTask task) {
        learningTaskMapper.update(task);
        return task;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "learningTasks", key = "#id"),
        @CacheEvict(value = "courseTasks", allEntries = true),
        @CacheEvict(value = "allTasks", allEntries = true)
    })
    public void deleteTask(Long id) {
        // Get the task first to know which course cache to invalidate
        LearningTask task = getTaskById(id);
        learningTaskMapper.delete(id);
    }

    @Cacheable(value = "learningTasks", key = "#id")
    public LearningTask getTaskById(Long id) {
        return learningTaskMapper.findById(id);
    }

    @Cacheable(value = "courseTasks", key = "#courseId")
    public List<LearningTask> getTasksByCourseId(Long courseId) {
        return learningTaskMapper.findByCourseId(courseId);
    }

    @Cacheable(value = "allTasks")
    public List<LearningTask> getAllTasks() {
        return learningTaskMapper.findAll();
    }

    // Task Resource Management Methods

    /**
     * Associates a resource with a task.
     *
     * @param taskId The ID of the task.
     * @param resourceId The ID of the resource.
     * @return The created TaskResource relationship.
     */
    @Transactional
    @CacheEvict(value = {"taskResources"}, key = "#taskId")
    public TaskResource addResourceToTask(Long taskId, Long resourceId) {
        // Check if relationship already exists
        if (taskResourceMapper.existsByTaskIdAndResourceId(taskId, resourceId)) {
            throw new IllegalArgumentException("Resource is already associated with this task");
        }
        
        TaskResource taskResource = new TaskResource(taskId, resourceId);
        taskResourceMapper.insert(taskResource);
        return taskResource;
    }

    /**
     * Removes a resource from a task.
     *
     * @param taskId The ID of the task.
     * @param resourceId The ID of the resource.
     */
    @Transactional
    @CacheEvict(value = {"taskResources"}, key = "#taskId")
    public void removeResourceFromTask(Long taskId, Long resourceId) {
        taskResourceMapper.deleteByTaskIdAndResourceId(taskId, resourceId);
    }

    /**
     * Gets all resources associated with a task.
     *
     * @param taskId The ID of the task.
     * @return List of resources associated with the task.
     */
    @Cacheable(value = "taskResources", key = "#taskId")
    public List<Resource> getTaskResources(Long taskId) {
        return taskResourceMapper.findResourcesByTaskId(taskId);
    }

    /**
     * Gets all task-resource relationships for a task.
     *
     * @param taskId The ID of the task.
     * @return List of TaskResource relationships.
     */
    public List<TaskResource> getTaskResourceRelationships(Long taskId) {
        return taskResourceMapper.findByTaskId(taskId);
    }

    /**
     * Removes all resources from a task.
     *
     * @param taskId The ID of the task.
     */
    @Transactional
    @CacheEvict(value = {"taskResources"}, key = "#taskId")
    public void removeAllResourcesFromTask(Long taskId) {
        taskResourceMapper.deleteByTaskId(taskId);
    }
}