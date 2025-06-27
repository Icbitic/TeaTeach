package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.TestPaper;
import org.bedrock.teateach.dto.TestPaperGenerationRequest;
import org.bedrock.teateach.services.TestPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test-papers")
public class TestPaperController {

    private final TestPaperService testPaperService;

    @Autowired
    public TestPaperController(TestPaperService testPaperService) {
        this.testPaperService = testPaperService;
    }

    /**
     * Creates a new test paper manually.
     * POST /api/test-papers
     */
    @PostMapping
    public ResponseEntity<TestPaper> createTestPaper(@RequestBody TestPaper testPaper) {
        TestPaper createdTestPaper = testPaperService.createTestPaper(testPaper);
        return new ResponseEntity<>(createdTestPaper, HttpStatus.CREATED);
    }

    /**
     * Generates a test paper automatically based on criteria.
     * POST /api/test-papers/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<TestPaper> generateTestPaper(@RequestBody TestPaperGenerationRequest request) {
        TestPaper generatedTestPaper = testPaperService.generateTestPaper(request);
        return new ResponseEntity<>(generatedTestPaper, HttpStatus.CREATED);
    }

    /**
     * Retrieves a test paper by its ID.
     * GET /api/test-papers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestPaper> getTestPaperById(@PathVariable Long id) {
        Optional<TestPaper> testPaper = testPaperService.getTestPaperById(id);
        return testPaper.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all test papers with pagination and search.
     * GET /api/test-papers?page=0&size=12&search=
     * @param page The page number (default: 0).
     * @param size The page size (default: 12).
     * @param search Optional search term.
     * @return A list of test papers matching the criteria.
     */
    @GetMapping
    public ResponseEntity<List<TestPaper>> getAllTestPapers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "") String search) {
        List<TestPaper> testPapers = testPaperService.getAllTestPapers(page, size, search);
        return ResponseEntity.ok(testPapers);
    }

    /**
     * Retrieves all test papers for a specific course.
     * GET /api/test-papers/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TestPaper>> getTestPapersByCourse(@PathVariable Long courseId) {
        List<TestPaper> testPapers = testPaperService.getTestPapersByCourse(courseId);
        return ResponseEntity.ok(testPapers);
    }

    /**
     * Retrieves all test papers created by a specific instructor.
     * GET /api/test-papers/instructor/{instructorId}
     */
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<TestPaper>> getTestPapersByInstructor(@PathVariable Long instructorId) {
        List<TestPaper> testPapers = testPaperService.getTestPapersByInstructor(instructorId);
        return ResponseEntity.ok(testPapers);
    }

    /**
     * Updates an existing test paper.
     * PUT /api/test-papers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TestPaper> updateTestPaper(@PathVariable Long id, @RequestBody TestPaper testPaper) {
        if (!id.equals(testPaper.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Optional<TestPaper> existingTestPaper = testPaperService.getTestPaperById(id);
        if (existingTestPaper.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TestPaper updatedTestPaper = testPaperService.updateTestPaper(testPaper);
        return ResponseEntity.ok(updatedTestPaper);
    }

    /**
     * Deletes a test paper by its ID.
     * DELETE /api/test-papers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestPaper(@PathVariable Long id) {
        Optional<TestPaper> existingTestPaper = testPaperService.getTestPaperById(id);
        if (existingTestPaper.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        testPaperService.deleteTestPaper(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Previews questions for test paper generation based on criteria.
     * POST /api/test-papers/preview
     */
    @PostMapping("/preview")
    public ResponseEntity<List<org.bedrock.teateach.beans.Question>> previewQuestions(@RequestBody TestPaperGenerationRequest request) {
        List<org.bedrock.teateach.beans.Question> questions = testPaperService.previewQuestions(request);
        return ResponseEntity.ok(questions);
    }
}