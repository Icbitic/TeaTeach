package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.StudentTaskSubmission;
import org.bedrock.teateach.services.GradeService; // For recordSubmissionScore
import org.bedrock.teateach.services.StudentTaskSubmissionService; // Assuming a dedicated service for basic CRUD
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/submissions")
public class StudentTaskSubmissionController {

    private final StudentTaskSubmissionService studentTaskSubmissionService;
    private final GradeService gradeService; // For specific grading logic

    @Autowired
    public StudentTaskSubmissionController(StudentTaskSubmissionService studentTaskSubmissionService,
                                           GradeService gradeService) {
        this.studentTaskSubmissionService = studentTaskSubmissionService;
        this.gradeService = gradeService;
    }

    /**
     * Creates a new student task submission.
     * POST /api/submissions
     * @param submission The submission object to create.
     * @return The created submission with its generated ID.
     */
    @PostMapping
    public ResponseEntity<StudentTaskSubmission> createSubmission(@RequestBody StudentTaskSubmission submission) {
        StudentTaskSubmission createdSubmission = studentTaskSubmissionService.createSubmission(submission);
        return new ResponseEntity<>(createdSubmission, HttpStatus.CREATED);
    }

    /**
     * Retrieves a student task submission by its ID.
     * GET /api/submissions/{id}
     * @param id The ID of the submission to retrieve.
     * @return The submission if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentTaskSubmission> getSubmissionById(@PathVariable Long id) {
        Optional<StudentTaskSubmission> submission = studentTaskSubmissionService.getSubmissionById(id);
        return submission.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all submissions by a specific student.
     * GET /api/submissions/student/{studentId}
     * @param studentId The ID of the student.
     * @return A list of submissions by the specified student.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentTaskSubmission>> getSubmissionsByStudentId(@PathVariable Long studentId) {
        List<StudentTaskSubmission> submissions = studentTaskSubmissionService.getSubmissionsByStudentId(studentId);
        return ResponseEntity.ok(submissions);
    }

    /**
     * Retrieves all submissions for a specific task.
     * GET /api/submissions/task/{taskId}
     * @param taskId The ID of the task.
     * @return A list of submissions for the specified task.
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<StudentTaskSubmission>> getSubmissionsByTaskId(@PathVariable Long taskId) {
        List<StudentTaskSubmission> submissions = studentTaskSubmissionService.getSubmissionsByTaskId(taskId);
        return ResponseEntity.ok(submissions);
    }

    /**
     * Updates an existing student task submission.
     * PUT /api/submissions/{id}
     * @param id The ID of the submission to update.
     * @param submission The updated submission object.
     * @return The updated submission, or 404 Not Found if the ID doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentTaskSubmission> updateSubmission(@PathVariable Long id, @RequestBody StudentTaskSubmission submission) {
        if (!id.equals(submission.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Optional<StudentTaskSubmission> existingSubmission = studentTaskSubmissionService.getSubmissionById(id);
        if (existingSubmission.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        StudentTaskSubmission updatedSubmission = studentTaskSubmissionService.updateSubmission(submission);
        return ResponseEntity.ok(updatedSubmission);
    }

    /**
     * Deletes a student task submission by its ID.
     * DELETE /api/submissions/{id}
     * @param id The ID of the submission to delete.
     * @return 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        Optional<StudentTaskSubmission> existingSubmission = studentTaskSubmissionService.getSubmissionById(id);
        if (existingSubmission.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        studentTaskSubmissionService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Records/updates the score for a specific student submission.
     * PUT /api/submissions/{submissionId}/score
     * @param submissionId The ID of the submission to score.
     * @param score The score to assign.
     * @return The updated submission with score and completion status, or 404 Not Found.
     */
    @PutMapping("/{submissionId}/score")
    public ResponseEntity<StudentTaskSubmission> recordSubmissionScore(@PathVariable Long submissionId,
                                                                       @RequestParam Double score) {
        StudentTaskSubmission updatedSubmission = gradeService.recordSubmissionScore(submissionId, score);
        return updatedSubmission != null ? ResponseEntity.ok(updatedSubmission) : ResponseEntity.notFound().build();
    }
}