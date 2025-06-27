package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Question;
import org.bedrock.teateach.services.QuestionService; // Assuming a QuestionService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Creates a new question.
     * POST /api/questions
     * @param question The question object to create.
     * @return The created question with its generated ID.
     */
    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody Question question) {
        Question createdQuestion = questionService.createQuestion(question);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    /**
     * Retrieves a question by its ID.
     * GET /api/questions/{id}
     * @param id The ID of the question to retrieve.
     * @return The question if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id) {
        Optional<Question> question = questionService.getQuestionById(id);
        return question.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all questions with pagination and search.
     * GET /api/questions?page=0&size=12&search=
     * @param page The page number (default: 0).
     * @param size The page size (default: 12).
     * @param search Optional search term.
     * @return A list of questions matching the criteria.
     */
    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "") String search) {
        List<Question> questions = questionService.getAllQuestions(page, size, search);
        return ResponseEntity.ok(questions);
    }

    /**
     * Retrieves questions by their type and difficulty (optional filters).
     * GET /api/questions/filter?type=SINGLE_CHOICE&difficulty=EASY
     * @param type Optional: The type of question (e.g., SINGLE_CHOICE).
     * @param difficulty Optional: The difficulty level (e.g., EASY).
     * @return A list of questions matching the criteria.
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Question>> getQuestionsByFilters(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty) {
        List<Question> questions = questionService.getQuestionsByTypeAndDifficulty(type, difficulty);
        return ResponseEntity.ok(questions);
    }

    /**
     * Updates an existing question.
     * PUT /api/questions/{id}
     * @param id The ID of the question to update.
     * @param question The updated question object.
     * @return The updated question, or 404 Not Found if the ID doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        if (!id.equals(question.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Question> existingQuestion = questionService.getQuestionById(id);
        if (existingQuestion.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Question updatedQuestion = questionService.updateQuestion(question);
        return ResponseEntity.ok(updatedQuestion);
    }

    /**
     * Deletes a question by its ID.
     * DELETE /api/questions/{id}
     * @param id The ID of the question to delete.
     * @return 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        Optional<Question> existingQuestion = questionService.getQuestionById(id);
        if (existingQuestion.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}