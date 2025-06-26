package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.StudentTaskSubmission;
import org.bedrock.teateach.beans.SubmissionFile;
import org.bedrock.teateach.services.GradeService; // For recordSubmissionScore
import org.bedrock.teateach.services.StudentTaskSubmissionService; // Assuming a dedicated service for basic CRUD
import org.bedrock.teateach.services.SubmissionFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/submissions")
public class StudentTaskSubmissionController {

    private final StudentTaskSubmissionService studentTaskSubmissionService;
    private final GradeService gradeService; // For specific grading logic
    private final SubmissionFileService submissionFileService;

    @Autowired
    public StudentTaskSubmissionController(StudentTaskSubmissionService studentTaskSubmissionService,
                                           GradeService gradeService,
                                           SubmissionFileService submissionFileService) {
        this.studentTaskSubmissionService = studentTaskSubmissionService;
        this.gradeService = gradeService;
        this.submissionFileService = submissionFileService;
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
     * Retrieves a submission by student and task.
     * GET /api/submissions/student/{studentId}/task/{taskId}
     * @param studentId The ID of the student.
     * @param taskId The ID of the task.
     * @return The submission if found, or 404 Not Found.
     */
    @GetMapping("/student/{studentId}/task/{taskId}")
    public ResponseEntity<StudentTaskSubmission> getSubmissionByStudentAndTask(@PathVariable Long studentId, @PathVariable Long taskId) {
        Optional<StudentTaskSubmission> submission = studentTaskSubmissionService.getSubmissionByStudentAndTask(studentId, taskId);
        return submission.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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

    /**
     * Updates the feedback for a specific student submission.
     * PUT /api/submissions/{submissionId}/feedback
     * @param submissionId The ID of the submission to update feedback for.
     * @param feedback The feedback to assign.
     * @return The updated submission with feedback, or 404 Not Found.
     */
    @PutMapping("/{submissionId}/feedback")
    public ResponseEntity<StudentTaskSubmission> updateSubmissionFeedback(@PathVariable Long submissionId,
                                                                          @RequestParam String feedback) {
        Optional<StudentTaskSubmission> submission = studentTaskSubmissionService.getSubmissionById(submissionId);
        if (submission.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        StudentTaskSubmission existingSubmission = submission.get();
        existingSubmission.setFeedback(feedback);
        StudentTaskSubmission updatedSubmission = studentTaskSubmissionService.updateSubmission(existingSubmission);
        return ResponseEntity.ok(updatedSubmission);
    }

    /**
     * Updates both score and feedback for a specific student submission.
     * PUT /api/submissions/{submissionId}/grade
     * @param submissionId The ID of the submission to grade.
     * @param score The score to assign.
     * @param feedback The feedback to assign.
     * @return The updated submission with score and feedback, or 404 Not Found.
     */
    @PutMapping("/{submissionId}/grade")
    public ResponseEntity<StudentTaskSubmission> gradeSubmission(@PathVariable Long submissionId,
                                                                @RequestParam Double score,
                                                                @RequestParam(required = false) String feedback) {
        Optional<StudentTaskSubmission> submission = studentTaskSubmissionService.getSubmissionById(submissionId);
        if (submission.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        StudentTaskSubmission existingSubmission = submission.get();
        existingSubmission.setScore(score);
        existingSubmission.setCompletionStatus(3); // Graded
        if (feedback != null) {
            existingSubmission.setFeedback(feedback);
        }
        
        StudentTaskSubmission updatedSubmission = studentTaskSubmissionService.updateSubmission(existingSubmission);
        
        // Update overall grade
        gradeService.recordSubmissionScore(submissionId, score);
        
        return ResponseEntity.ok(updatedSubmission);
    }

    /**
     * Uploads a file for a specific submission.
     * POST /api/submissions/{submissionId}/files
     * @param submissionId The ID of the submission to upload file for.
     * @param file The file to upload.
     * @return The created SubmissionFile record.
     */
    @PostMapping("/{submissionId}/files")
    public ResponseEntity<SubmissionFile> uploadSubmissionFile(@PathVariable Long submissionId,
                                                               @RequestParam("file") MultipartFile file) {
        try {
            // Verify submission exists
            Optional<StudentTaskSubmission> submission = studentTaskSubmissionService.getSubmissionById(submissionId);
            if (submission.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SubmissionFile uploadedFile = submissionFileService.uploadFile(file, submissionId);
            return new ResponseEntity<>(uploadedFile, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets all files for a specific submission.
     * GET /api/submissions/{submissionId}/files
     * @param submissionId The ID of the submission.
     * @return List of files for the submission.
     */
    @GetMapping("/{submissionId}/files")
    public ResponseEntity<List<SubmissionFile>> getSubmissionFiles(@PathVariable Long submissionId) {
        List<SubmissionFile> files = submissionFileService.getFilesBySubmissionId(submissionId);
        return ResponseEntity.ok(files);
    }

    /**
     * Downloads a specific submission file.
     * GET /api/submissions/files/{fileId}/download
     * @param fileId The ID of the file to download.
     * @return The file content as a downloadable resource.
     */
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<ByteArrayResource> downloadSubmissionFile(@PathVariable Long fileId) {
        try {
            SubmissionFile submissionFile = submissionFileService.getFileById(fileId);
            if (submissionFile == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = submissionFileService.getFileContent(fileId);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + submissionFile.getFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, submissionFile.getMimeType() != null ? submissionFile.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a specific submission file.
     * DELETE /api/submissions/files/{fileId}
     * @param fileId The ID of the file to delete.
     * @return No content if successful.
     */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteSubmissionFile(@PathVariable Long fileId) {
        try {
            submissionFileService.deleteFile(fileId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}