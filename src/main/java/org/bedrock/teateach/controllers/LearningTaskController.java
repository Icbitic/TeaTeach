package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.LearningTask;
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
}