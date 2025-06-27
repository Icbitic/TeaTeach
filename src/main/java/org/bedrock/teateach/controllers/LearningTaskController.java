package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.beans.TaskResource;
import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.services.LearningTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-tasks")
public class LearningTaskController {

    private final LearningTaskService learningTaskService;

    @Autowired
    public LearningTaskController(LearningTaskService learningTaskService) {
        this.learningTaskService = learningTaskService;
    }

    /**
     * Creates a new learning task.
     * POST /api/learning-tasks
     * @param learningTask The learning task object to create.
     * @return The created learning task with its generated ID.
     */
    @PostMapping
    public ResponseEntity<LearningTask> createTask(@RequestBody LearningTask learningTask) {
        LearningTask createdTask = learningTaskService.createTask(learningTask);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * Retrieves a learning task by its ID.
     * GET /api/learning-tasks/{id}
     * @param id The ID of the learning task to retrieve.
     * @return The learning task if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LearningTask> getTaskById(@PathVariable Long id) {
        LearningTask task = learningTaskService.getTaskById(id);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves all learning tasks.
     * GET /api/learning-tasks
     * @return A list of all learning tasks.
     */
    @GetMapping
    public ResponseEntity<List<LearningTask>> getAllTasks() {
        List<LearningTask> tasks = learningTaskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Retrieves all learning tasks for a specific course.
     * GET /api/learning-tasks/course/{courseId}
     * @param courseId The ID of the course.
     * @return A list of learning tasks for the specified course.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LearningTask>> getTasksByCourseId(@PathVariable Long courseId) {
        List<LearningTask> tasks = learningTaskService.getTasksByCourseId(courseId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Updates an existing learning task.
     * PUT /api/learning-tasks/{id}
     * @param id The ID of the learning task to update.
     * @param learningTask The updated learning task object.
     * @return The updated learning task, or 404 Not Found if the ID doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LearningTask> updateTask(@PathVariable Long id, @RequestBody LearningTask learningTask) {
        // Ensure the ID in the path matches the ID in the request body for consistency
        if (!id.equals(learningTask.getId())) {
            return ResponseEntity.badRequest().build();
        }
        LearningTask existingTask = learningTaskService.getTaskById(id);
        if (existingTask == null) {
            return ResponseEntity.notFound().build();
        }
        LearningTask updatedTask = learningTaskService.updateTask(learningTask);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Deletes a learning task by its ID.
     * DELETE /api/learning-tasks/{id}
     * @param id The ID of the learning task to delete.
     * @return 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        LearningTask existingTask = learningTaskService.getTaskById(id);
        if (existingTask == null) {
            return ResponseEntity.notFound().build();
        }
        learningTaskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // Task Resource Management Endpoints

    /**
     * Gets all resources associated with a task.
     * GET /api/learning-tasks/{taskId}/resources
     * @param taskId The ID of the task.
     * @return List of resources associated with the task.
     */
    @GetMapping("/{taskId}/resources")
    public ResponseEntity<List<Resource>> getTaskResources(@PathVariable Long taskId) {
        LearningTask task = learningTaskService.getTaskById(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        List<Resource> resources = learningTaskService.getTaskResources(taskId);
        return ResponseEntity.ok(resources);
    }

    /**
     * Associates a resource with a task.
     * POST /api/learning-tasks/{taskId}/resources/{resourceId}
     * @param taskId The ID of the task.
     * @param resourceId The ID of the resource.
     * @return The created TaskResource relationship.
     */
    @PostMapping("/{taskId}/resources/{resourceId}")
    public ResponseEntity<TaskResource> addResourceToTask(@PathVariable Long taskId, @PathVariable Long resourceId) {
        try {
            LearningTask task = learningTaskService.getTaskById(taskId);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }
            TaskResource taskResource = learningTaskService.addResourceToTask(taskId, resourceId);
            return new ResponseEntity<>(taskResource, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Removes a resource from a task.
     * DELETE /api/learning-tasks/{taskId}/resources/{resourceId}
     * @param taskId The ID of the task.
     * @param resourceId The ID of the resource.
     * @return 204 No Content if successful.
     */
    @DeleteMapping("/{taskId}/resources/{resourceId}")
    public ResponseEntity<Void> removeResourceFromTask(@PathVariable Long taskId, @PathVariable Long resourceId) {
        LearningTask task = learningTaskService.getTaskById(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        learningTaskService.removeResourceFromTask(taskId, resourceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Removes all resources from a task.
     * DELETE /api/learning-tasks/{taskId}/resources
     * @param taskId The ID of the task.
     * @return 204 No Content if successful.
     */
    @DeleteMapping("/{taskId}/resources")
    public ResponseEntity<Void> removeAllResourcesFromTask(@PathVariable Long taskId) {
        LearningTask task = learningTaskService.getTaskById(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        learningTaskService.removeAllResourcesFromTask(taskId);
        return ResponseEntity.noContent().build();
    }
}