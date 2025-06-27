package org.bedrock.teateach.dto;

import lombok.Data;
import org.bedrock.teateach.enums.QuestionType;

import java.util.List;
import java.util.Map;

@Data
public class TestPaperGenerationRequest {
    private String paperName;
    private Long courseId;
    private Long instructorId;
    private String generationMethod; // "RANDOM", "BY_KNOWLEDGE_POINT", "BY_DIFFICULTY", "BALANCED"
    private Integer totalQuestions;
    private Integer durationMinutes;
    private Double totalScore;
    
    // For random generation
    private List<QuestionType> questionTypes;
    
    // For knowledge point-based generation
    private List<Long> knowledgePointIds;
    private Map<Long, Integer> knowledgePointQuestionCounts; // knowledgePointId -> question count
    
    // For difficulty-based generation
    private List<String> difficulties; // ["EASY", "MEDIUM", "HARD"]
    private Map<String, Integer> difficultyQuestionCounts; // difficulty -> question count
    
    // For balanced generation (combines multiple criteria)
    private Map<QuestionType, Integer> questionTypeDistribution; // questionType -> count
    private Map<String, Double> difficultyWeights; // difficulty -> weight (0.0-1.0)
    private Boolean includeAllKnowledgePoints; // whether to include questions from all knowledge points
    
    // Additional constraints
    private Boolean excludeUsedQuestions; // exclude questions already used in recent papers
    private Integer maxQuestionsPerKnowledgePoint;
    private Double minDifficultyScore; // minimum average difficulty score
    private Double maxDifficultyScore; // maximum average difficulty score
}