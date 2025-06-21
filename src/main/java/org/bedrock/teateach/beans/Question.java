package org.bedrock.teateach.beans;

import lombok.Data;
import org.bedrock.teateach.enums.QuestionType;

import java.util.List;

@Data
public class Question {
    private Long id;
    private String questionText;
    private QuestionType questionType;
    private String options; // JSON string for multiple choice options
    private String correctAnswer; // JSON string for answers
    private String explanation;
    private String difficulty; // e.g., "EASY", "MEDIUM", "HARD"
    private List<Long> knowledgePointIds; // Associated knowledge points
    private String programmingLanguage; // For programming questions
    private String templateCode; // For programming questions
    private String testCases; // JSON string for test cases
}
