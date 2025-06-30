package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.llm.LLMService;
import org.bedrock.teateach.services.CourseEnrollmentService;
import org.bedrock.teateach.services.CourseService;
import org.bedrock.teateach.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for student ability analysis endpoints.
 * Provides AI-powered assessment of student abilities based on their academic performance.
 */
@RestController
@RequestMapping("/api/student-abilities")
public class StudentAbilityController {

    private final LLMService llmService;
    private final CourseService courseService;
    private final CourseEnrollmentService courseEnrollmentService;
    private final StudentService studentService;

    @Autowired
    public StudentAbilityController(LLMService llmService, CourseService courseService, CourseEnrollmentService courseEnrollmentService, StudentService studentService) {
        this.llmService = llmService;
        this.courseService = courseService;
        this.courseEnrollmentService = courseEnrollmentService;
        this.studentService = studentService;
    }

    /**
     * Analyzes student abilities based on their submission data, enrolled courses, and knowledge points.
     * Results are cached to avoid heavy LLM load.
     *
     * @param studentId The ID of the student to analyze
     * @return A comprehensive ability assessment including scores and interested fields
     */
    @GetMapping("/analyze/{studentId}")
    public ResponseEntity<Map<String, Object>> analyzeStudentAbilities(@PathVariable Long studentId) {
        try {
            Map<String, Object> analysis = llmService.analyzeStudentAbilities(studentId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Analysis failed",
                    "message", e.getMessage(),
                    "studentId", studentId
                ));
        }
    }

    /**
     * Recommends personalized learning resources based on student ability analysis.
     * Uses AI with RAG to suggest relevant courses, books, videos, and practice materials.
     *
     * @param studentId The ID of the student
     * @param resourceType Optional filter for resource type (course, book, video, practice, all)
     * @return Personalized resource recommendations with learning paths
     */
    @GetMapping("/recommend/{studentId}")
    public ResponseEntity<Map<String, Object>> recommendResources(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "all") String resourceType) {
        try {
            // Get student abilities, interested fields, and enrolled courses
            Map<String, Object> studentAbilities = llmService.analyzeStudentAbilities(studentId);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> abilities = (Map<String, Object>) studentAbilities.get("abilities");
            
            @SuppressWarnings("unchecked")
            List<String> interestedFields = (List<String>) studentAbilities.get("interestedFields");
            
            @SuppressWarnings("unchecked")
            List<String> enrolledCourses = (List<String>) studentAbilities.get("enrolledCourses");
            
            // Use the new RAG-based recommendation method
            Map<String, Object> recommendations = llmService.recommendResources(
                abilities, interestedFields, enrolledCourses, resourceType);
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Resource recommendation failed",
                    "message", e.getMessage(),
                    "studentId", studentId,
                    "resourceType", resourceType
                ));
        }
    }

    /**
     * Get resource recommendations for multiple resource types.
     *
     * @param studentId The ID of the student
     * @return Comprehensive recommendations across all resource types
     */
    @GetMapping("/recommend/{studentId}/comprehensive")
    public ResponseEntity<Map<String, Object>> getComprehensiveRecommendations(@PathVariable Long studentId) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Get student abilities, interested fields, and enrolled courses once
            Map<String, Object> studentAbilities = llmService.analyzeStudentAbilities(studentId);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> abilities = (Map<String, Object>) studentAbilities.get("abilities");
            
            @SuppressWarnings("unchecked")
            List<String> interestedFields = (List<String>) studentAbilities.get("interestedFields");
            
            @SuppressWarnings("unchecked")
//            List<String> enrolledCourses = (List<String>) studentAbilities.get("enrolledCourses");
            List<String> enrolledCourses = new java.util.ArrayList<>(List.of());
            var tmp = courseEnrollmentService.getCoursesByStudentId(studentService.convertStudentIdToId(studentId.toString()));
            for(Course course : tmp) {
                enrolledCourses.add(course.getCourseName());
            }

//            // Get recommendations for each resource type using RAG
//            String[] resourceTypes = {"video"};
//
//            for (String type : resourceTypes) {
//                Map<String, Object> typeRecommendations = llmService.recommendResources(
//                    abilities, interestedFields, enrolledCourses, type);
//                result.put(type + "Recommendations", typeRecommendations);
//            }
            
            // Get overall recommendations using RAG
            Map<String, Object> overallRecommendations = llmService.recommendResources(
                abilities, interestedFields, enrolledCourses, "all");
            result.put("overallRecommendations", overallRecommendations);
            result.put("studentId", studentId);
            result.put("generatedAt", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Comprehensive recommendation failed",
                    "message", e.getMessage(),
                    "studentId", studentId
                ));
        }
    }

    /**
     * Get a brief explanation of the ability analysis system.
     *
     * @return Information about the analysis capabilities
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAnalysisInfo() {
        return ResponseEntity.ok(Map.of(
            "description", "AI-powered student ability analysis and resource recommendation system",
            "abilities", Map.of(
                "mathReasoning", "Mathematical and logical reasoning skills (0-100)",
                "languageProficiency", "Language comprehension and communication skills (0-100)",
                "codingAbility", "Programming and computational thinking skills (0-100)",
                "problemSolvingAbility", "General problem-solving and analytical skills (0-100)",
                "socialKnowledge", "Social sciences and humanities knowledge (0-100)"
            ),
            "resourceTypes", Map.of(
                "course", "Structured learning courses and curricula",
                "book", "Textbooks, reference materials, and reading resources",
                "video", "Educational videos, tutorials, and multimedia content",
                "practice", "Exercises, problem sets, and hands-on activities",
                "all", "Comprehensive recommendations across all types"
            ),
            "endpoints", Map.of(
                "analyze", "/api/student-abilities/analyze/{studentId} - Get ability analysis",
                "recommend", "/api/student-abilities/recommend/{studentId}?resourceType=type - Get resource recommendations",
                "comprehensive", "/api/student-abilities/recommend/{studentId}/comprehensive - Get all recommendation types"
            ),
            "dataSource", "Analysis based on graded submissions, enrolled courses, and knowledge points",
            "caching", "Results cached for 5 seconds (development) / 1 hour (production)",
            "features", java.util.List.of(
                "Comprehensive ability scoring",
                "Interest field identification",
                "Personalized resource recommendations",
                "Learning path generation",
                "Study strategy suggestions",
                "Performance trend analysis",
                "Intelligent caching system"
            )
        ));
    }
}