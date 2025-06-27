package org.bedrock.teateach.llm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.bedrock.teateach.beans.StudentLearningData;
import org.bedrock.teateach.services.StudentService;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

@RestController
@RequestMapping("/api/llm")
public class LLMController {

    private final LLMService llmService;
    private final StudentService studentService;

    @Autowired
    public LLMController(LLMService llmService, StudentService studentService) {
        this.llmService = llmService;
        this.studentService = studentService;
    }

    /**
     * Get personalized learning content recommendations for a student
     * @param request Contains student ID and performance data
     * @return Personalized learning recommendations
     */
    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendations(@RequestBody RecommendationRequest request) {
        try {
            List<String> recommendations = llmService.recommendLearningContent(
                Long.parseLong(request.getStudentId()),
                convertToLongDoubleMap(request.getPerformanceData()),
                request.getCourseId()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "recommendations", recommendations,
                "studentId", request.getStudentId(),
                "courseId", request.getCourseId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to generate recommendations: " + e.getMessage()
            ));
        }
    }

    /**
     * Get personalized learning content recommendations by student ID
     * Automatically fetches student's learning data and generates recommendations
     * @param studentId The student's ID
     * @param courseId Optional course ID to filter recommendations
     * @return Personalized learning recommendations
     */
    @GetMapping("/recommend/{studentId}")
    public ResponseEntity<Map<String, Object>> getRecommendationsByStudentId(
            @PathVariable String studentId,
            @RequestParam(required = false) Long courseId) {
        try {
            // Fetch student's learning data
            List<StudentLearningData> learningData = studentService.getStudentLearningData(studentId, courseId);
            
            // Convert learning data to performance map
            Map<Long, Double> performanceData = convertLearningDataToLongDoubleMap(learningData);
            
            List<String> recommendations = llmService.recommendLearningContent(
                Long.parseLong(studentId), 
                performanceData, 
                courseId != null ? courseId : 1L
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "recommendations", recommendations,
                "studentId", studentId,
                "courseId", courseId,
                "performanceData", performanceData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to generate recommendations: " + e.getMessage()
            ));
        }
    }

    /**
     * Convert StudentLearningData list to performance map for LLM processing
     */
    private Map<Long, Double> convertLearningDataToLongDoubleMap(List<StudentLearningData> learningData) {
        Map<Long, Double> performanceMap = new HashMap<>();
        
        if (learningData == null || learningData.isEmpty()) {
            return performanceMap;
        }
        
        // Convert each learning data record to task ID -> score mapping
        for (StudentLearningData data : learningData) {
            if (data.getTaskId() != null && data.getQuizScore() != null) {
                performanceMap.put(data.getTaskId(), data.getQuizScore());
            }
        }
        
        return performanceMap;
    }
    
    /**
     * Convert generic performance data to Long-Double map
     */
    private Map<Long, Double> convertToLongDoubleMap(Map<String, Object> performanceData) {
        Map<Long, Double> converted = new HashMap<>();
        
        if (performanceData == null) {
            return converted;
        }
        
        for (Map.Entry<String, Object> entry : performanceData.entrySet()) {
            try {
                Long key = Long.parseLong(entry.getKey());
                Double value = null;
                
                if (entry.getValue() instanceof Number) {
                    value = ((Number) entry.getValue()).doubleValue();
                } else if (entry.getValue() instanceof String) {
                    value = Double.parseDouble((String) entry.getValue());
                }
                
                if (value != null) {
                    converted.put(key, value);
                }
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }
        
        return converted;
    }

    /**
     * Request DTO for learning content recommendations
     */
    public static class RecommendationRequest {
        private String studentId;
        private Map<String, Object> performanceData;
        private Long courseId;

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public Map<String, Object> getPerformanceData() {
            return performanceData;
        }

        public void setPerformanceData(Map<String, Object> performanceData) {
            this.performanceData = performanceData;
        }
        
        public Long getCourseId() {
            return courseId;
        }
        
        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }
    }
}
