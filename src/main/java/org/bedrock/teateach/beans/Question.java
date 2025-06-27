package org.bedrock.teateach.beans;

import lombok.Data;
import org.bedrock.teateach.enums.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Question {
    private Long id;
    private String questionText;
    private QuestionType questionType;
    private String[] options; // Array for multiple choice options
    private String correctAnswer; // String or JSON string for answers
    private String explanation;
    private String difficulty; // e.g., "EASY", "MEDIUM", "HARD"
    private List<Long> knowledgePointIds; // Associated knowledge points
    private String programmingLanguage; // For programming questions
    private String templateCode; // For programming questions
    private String testCases; // JSON string for test cases
    private Double points; // Points/score for this question
    private String[] tags; // Array for question tags
    private Long createdBy; // ID of the user who created the question
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive; // Whether the question is active/available
    private Integer usageCount; // How many times this question has been used
    private Double averageScore; // Average score students get on this question
}
