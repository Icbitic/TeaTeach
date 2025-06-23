package org.bedrock.teateach.llm;

import org.bedrock.teateach.beans.AbilityPoint;
import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.beans.StudentTaskSubmission;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with a Large Language Model through Spring AI.
 * This implementation uses Ollama for local inference.
 */
@Service
public class LLMService {
    private final String apiKey;
    private final ChatClient chatClient;

    public LLMService(@Value("${llm.api.key}") String apiKey, ChatClient.Builder chatClientBuilder) {
        this.apiKey = apiKey;
        this.chatClient = chatClientBuilder.build();
        System.out.println("LLMService initialized with API Key: " + (apiKey != null && !apiKey.isEmpty() ? "******" : "NONE"));
    }

    /**
     * Intelligently extracts and structures knowledge points from course content.
     *
     * @param courseContent The text content of the course (e.g., lecture notes, textbook excerpts).
     * @param courseId      The ID of the course.
     * @return A list of KnowledgePoint objects with hierarchical and logical relationships.
     */
    public List<KnowledgePoint> extractKnowledgePoints(String courseContent, Long courseId) {
        System.out.println("LLM: Extracting knowledge points from course content...");

        // Create prompt with template variables
        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("courseContent", courseContent);
        promptParams.put("courseId", courseId);

        String promptTemplate = """
                Extract key knowledge points from the following course content. 
                Identify important concepts, their relationships, and organize them hierarchically.
                
                Course ID: {courseId}
                Course Content: {courseContent}
                
                Return a JSON array of knowledge points with the following format for each point:
                [{
                  "id": [incrementing number starting from 101],
                  "name": [concept name],
                  "briefDescription": [short description],
                  "detailedContent": [more detailed explanation],
                  "prerequisiteKnowledgePointIds": [array of ids of prerequisite concepts or null],
                  "relatedKnowledgePointIds": [array of ids of related concepts or null],
                  "difficultyLevel": ["BEGINNER", "INTERMEDIATE", or "ADVANCED"],
                  "courseId": {courseId}
                }]
                
                Provide at least 3-5 knowledge points with proper relationships between them.
                """;

        // Create prompt
        PromptTemplate template = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('{').endDelimiterToken('}').build())
                .template(promptTemplate)
                .build();
        UserMessage userMessage = template.create(promptParams).getUserMessage();
        SystemMessage systemMessage = new SystemMessage("You are an educational content analyst AI that extracts structured knowledge points from educational content.");
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Execute prompt and parse results into a list of KnowledgePoint objects
        try {
            return chatClient.prompt(prompt)
                    .call()
                    .entity(new ParameterizedTypeReference<List<KnowledgePoint>>() {
                    });
        } catch (Exception e) {
            System.err.println("Error extracting knowledge points: " + e.getMessage());
            e.printStackTrace();
            // Fallback to default response if LLM fails
            return createDefaultKnowledgePoints(courseId);
        }
    }

    /**
     * Creates default knowledge points in case the LLM call fails.
     */
    private List<KnowledgePoint> createDefaultKnowledgePoints(Long courseId) {
        KnowledgePoint kp1 = new KnowledgePoint();
        kp1.setId(101L);
        kp1.setName("Introduction to Subject");
        kp1.setBriefDescription("Basic concepts and foundation.");
        kp1.setCourseId(courseId);
        kp1.setDetailedContent("Detailed learning page for Introduction to Subject");
        kp1.setDifficultyLevel("BEGINNER");

        KnowledgePoint kp2 = new KnowledgePoint();
        kp2.setId(102L);
        kp2.setName("Core Principles");
        kp2.setBriefDescription("Essential principles and methodologies.");
        kp2.setCourseId(courseId);
        kp2.setPrerequisiteKnowledgePointIds(List.of(101L));
        kp2.setDetailedContent("Detailed learning page for Core Principles");
        kp2.setDifficultyLevel("INTERMEDIATE");

        KnowledgePoint kp3 = new KnowledgePoint();
        kp3.setId(103L);
        kp3.setName("Advanced Applications");
        kp3.setBriefDescription("Applied techniques and advanced topics.");
        kp3.setCourseId(courseId);
        kp3.setPrerequisiteKnowledgePointIds(List.of(101L, 102L));
        kp3.setDetailedContent("Detailed learning page for Advanced Applications");
        kp3.setDifficultyLevel("ADVANCED");

        return List.of(kp1, kp2, kp3);
    }

    /**
     * Recommends learning content based on student performance.
     *
     * @param studentId       The ID of the student.
     * @param performanceData A map of task IDs to their scores or completion status.
     * @param courseId        The ID of the course.
     * @return A list of recommended learning materials (e.g., knowledge points, review materials).
     */
    public List<String> recommendLearningContent(Long studentId, Map<Long, Double> performanceData, Long courseId) {
        System.out.println("LLM: Recommending learning content for student " + studentId + " based on performance.");

        // Format the performance data for the prompt
        StringBuilder performanceBuilder = new StringBuilder();
        performanceData.forEach((taskId, score) -> {
            performanceBuilder.append("Task ").append(taskId).append(": ").append(score).append("\n");
        });

        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("studentId", studentId);
        promptParams.put("courseId", courseId);
        promptParams.put("performanceData", performanceBuilder.toString());

        String promptTemplate = """
                Based on the following student performance data, recommend personalized learning content.
                Analyze the strengths and weaknesses shown in the data and suggest specific learning materials.
                
                Student ID: {studentId}
                Course ID: {courseId}
                
                Performance Data:
                {performanceData}
                
                Provide a list of specific, actionable recommendations for learning content.
                Each recommendation should be clear and directly address a learning need identified from the performance data.
                Return exactly 3-5 recommendations as a JSON array of strings.
                """;

        // Create prompt
        PromptTemplate template = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('{').endDelimiterToken('}').build())
                .template(promptTemplate)
                .build();
        UserMessage userMessage = template.create(promptParams).getUserMessage();
        SystemMessage systemMessage = new SystemMessage("You are an educational recommendation AI that analyzes student performance and suggests personalized learning content.");
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Execute prompt and parse results
        try {
            return chatClient.prompt(prompt)
                    .call()
                    .entity(new ParameterizedTypeReference<List<String>>() {
                    });
        } catch (Exception e) {
            System.err.println("Error generating learning recommendations: " + e.getMessage());
            e.printStackTrace();
            // Fallback to default recommendations
            return List.of(
                    "Review foundational concepts for areas with scores below 70%.",
                    "Practice additional exercises for topics where scores are between 70-85%.",
                    "Explore advanced materials for subjects where performance is above 85%."
            );
        }
    }

    /**
     * Updates a student's ability score based on their learning behavior.
     *
     * @param studentId    The ID of the student.
     * @param abilityPoint The AbilityPoint being evaluated.
     * @param behaviorData Relevant learning behavior data (e.g., video watch patterns, task accuracy).
     * @return The updated ability score.
     */
    public Double updateAbilityScore(Long studentId, AbilityPoint abilityPoint, Map<String, Object> behaviorData) {
        System.out.println("LLM: Updating ability score for student " + studentId + " in " + abilityPoint.getName());

        // Format the behavior data for the prompt
        StringBuilder behaviorBuilder = new StringBuilder();
        behaviorData.forEach((key, value) -> {
            behaviorBuilder.append(key).append(": ").append(value).append("\n");
        });

        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("studentId", studentId);
        promptParams.put("abilityPointName", abilityPoint.getName());
        promptParams.put("abilityPointDescription", abilityPoint.getDescription());
        promptParams.put("behaviorData", behaviorBuilder.toString());

        String promptTemplate = """
                Calculate an updated ability score for a student based on their learning behavior data.
                Analyze the patterns and metrics to determine the student's proficiency in this specific ability.
                
                Student ID: {studentId}
                Ability: {abilityPointName}
                Ability Description: {abilityPointDescription}
                
                Behavior Data:
                {behaviorData}
                
                Based on this data, calculate a score between 0.0 and 100.0 that accurately reflects the student's 
                current level of proficiency in this ability. Return ONLY the numeric score as a decimal number.
                """;

        // Create prompt
        PromptTemplate template = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('{').endDelimiterToken('}').build())
                .template(promptTemplate)
                .build();
        UserMessage userMessage = template.create(promptParams).getUserMessage();
        SystemMessage systemMessage = new SystemMessage("You are an educational assessment AI that calculates precise ability scores based on learning behavior metrics.");
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Execute prompt and parse result
        try {
            String response = chatClient.prompt(prompt).call().content().trim();
            // Try to parse the response as a Double
            return Double.parseDouble(response);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing ability score from LLM response: " + e.getMessage());
            // If we can't parse a valid double from the response, use a fallback calculation
            return calculateFallbackAbilityScore(behaviorData);
        } catch (Exception e) {
            System.err.println("Error calculating ability score: " + e.getMessage());
            e.printStackTrace();
            // Fallback calculation
            return calculateFallbackAbilityScore(behaviorData);
        }
    }

    /**
     * Calculates a fallback ability score when the LLM call fails.
     */
    private Double calculateFallbackAbilityScore(Map<String, Object> behaviorData) {
        // Simple fallback algorithm that averages any numeric values in the behavior data
        double sum = 0.0;
        int count = 0;

        for (Object value : behaviorData.values()) {
            if (value instanceof Number) {
                sum += ((Number) value).doubleValue();
                count++;
            } else if (value instanceof String) {
                try {
                    double numValue = Double.parseDouble((String) value);
                    sum += numValue;
                    count++;
                } catch (NumberFormatException ignored) {
                    // Not a number, skip
                }
            }
        }

        // If we found numeric values, return their average, otherwise return a default score
        return count > 0 ? Math.min(100.0, Math.max(0.0, sum / count)) : 75.0;
    }

    /**
     * Intelligently grades a student's report/essay.
     *
     * @param submission    The student's report submission.
     * @param gradingRubric The grading criteria.
     * @return A map containing score and detailed feedback.
     */
    public Map<String, Object> gradeStudentReport(StudentTaskSubmission submission, String gradingRubric) {
        System.out.println("LLM: Intelligently grading report for submission ID: " + submission.getId());

        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("submissionId", submission.getId());
        promptParams.put("studentId", submission.getStudentId());
        promptParams.put("taskId", submission.getTaskId());
        promptParams.put("submissionContent", submission.getSubmissionContent());
        promptParams.put("gradingRubric", gradingRubric);

        String promptTemplate = """
                Grade the following student submission according to the provided rubric.
                Provide a detailed assessment with constructive feedback.
                
                Submission ID: {submissionId}
                Student ID: {studentId}
                Task ID: {taskId}
                
                SUBMISSION CONTENT:
                {submissionContent}
                
                GRADING RUBRIC:
                {gradingRubric}
                
                Evaluate the submission thoroughly against each criterion in the rubric.
                Return your assessment as a JSON object with the following structure:
                {
                  "score": [numeric score between 0-100],
                  "feedback": [detailed feedback with specific strengths and areas for improvement],
                  "criteriaBreakdown": {
                    "criterion1": [score and comments],
                    "criterion2": [score and comments],
                    ...
                  }
                }
                """;

        // Create prompt
        PromptTemplate template = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('{').endDelimiterToken('}').build())
                .template(promptTemplate)
                .build();
        UserMessage userMessage = template.create(promptParams).getUserMessage();
        SystemMessage systemMessage = new SystemMessage("You are an educational assessment AI that provides fair, consistent, and detailed grading of student submissions.");
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Execute prompt and parse results
        try {
            return chatClient.prompt(prompt)
                    .call()
                    .entity(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            System.err.println("Error grading student report: " + e.getMessage());
            e.printStackTrace();
            // Fallback to basic grading response
            return createFallbackGradingResponse(submission);
        }
    }

    /**
     * Creates a fallback grading response when the LLM call fails.
     */
    private Map<String, Object> createFallbackGradingResponse(StudentTaskSubmission submission) {
        Map<String, Object> result = new HashMap<>();
        result.put("score", 75.0);
        result.put("feedback", "Your submission has been reviewed. Due to technical limitations, detailed feedback is not available at this time. Please contact your instructor for more information.");

        Map<String, String> criteriaBreakdown = new HashMap<>();
        criteriaBreakdown.put("content", "Assessment unavailable");
        criteriaBreakdown.put("structure", "Assessment unavailable");
        criteriaBreakdown.put("presentation", "Assessment unavailable");
        result.put("criteriaBreakdown", criteriaBreakdown);

        return result;
    }

}