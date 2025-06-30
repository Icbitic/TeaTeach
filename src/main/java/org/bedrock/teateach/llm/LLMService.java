package org.bedrock.teateach.llm;

import org.bedrock.teateach.beans.*;
import org.bedrock.teateach.services.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for interacting with a Large Language Model through Spring AI.
 * This implementation uses Ollama for local inference.
 */
@Service
public class LLMService {
    private final String apiKey;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ResourceService resourceService;
    private final StudentTaskSubmissionService submissionService;
    private final CourseEnrollmentService courseEnrollmentService;
    private final KnowledgePointService knowledgePointService;
    private final StudentService studentService;
    private final CourseService courseService;

    @Autowired
    public LLMService(@Value("${llm.api.key}") String apiKey,
                      ChatClient.Builder chatClientBuilder,
                      VectorStore vectorStore,
                      ResourceService resourceService,
                      StudentTaskSubmissionService submissionService,
                      CourseEnrollmentService courseEnrollmentService,
                      @Lazy KnowledgePointService knowledgePointService,
                      @Lazy StudentService studentService,
                      @Lazy CourseService courseService) {
        this.apiKey = apiKey;
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.resourceService = resourceService;
        this.submissionService = submissionService;
        this.courseEnrollmentService = courseEnrollmentService;
        this.knowledgePointService = knowledgePointService;
        this.studentService = studentService;
        this.courseService = courseService;
        System.out.println("LLMService initialized with API Key: " + (apiKey != null && !apiKey.isEmpty() ? "******" : "NONE"));
    }

    /**
     * Initializes the vector store with all existing resources in the system.
     * This method is called after the bean is constructed.
     */
    @PostConstruct
    public void initializeVectorStore() {
        try {
            System.out.println("Initializing vector store with existing resources...");
            List<Resource> allResources = resourceService.getAllResources();

            for (Resource resource : allResources) {
                addResourceToVectorStore(resource);
            }

            System.out.println("Vector store initialized with " + allResources.size() + " resources.");
        } catch (Exception e) {
            System.err.println("Error initializing vector store: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds a single resource to the vector store.
     * This method should be called whenever a new resource is uploaded.
     *
     * @param resource The resource to add to the vector store
     */
    public void addResourceToVectorStore(Resource resource) {
        try {
            String content = extractResourceContent(resource);
            if (content != null && !content.trim().isEmpty()) {
                Document document = new Document(content);
                document.getMetadata().put("resourceId", resource.getId().toString());
                document.getMetadata().put("resourceName", resource.getResourceName());
                document.getMetadata().put("fileType", resource.getFileType());
                document.getMetadata().put("description", resource.getDescription() != null ? resource.getDescription() : "");

                vectorStore.add(List.of(document));
                System.out.println("Added resource to vector store: " + resource.getResourceName());
            }
        } catch (Exception e) {
            System.err.println("Error adding resource to vector store: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extracts text content from a resource file for embedding.
     * Currently supports basic text extraction for common file types.
     *
     * @param resource The resource to extract content from
     * @return The extracted text content, or null if extraction fails
     */
    private String extractResourceContent(Resource resource) {
        try {
            String filePath = resource.getFilePath();
            String fileType = resource.getFileType().toLowerCase();

            // For now, we'll create a searchable representation using metadata
            // In a production system, you'd want to use proper document parsers
            StringBuilder content = new StringBuilder();
            content.append("Resource Name: ").append(resource.getResourceName()).append("\n");
            content.append("File Type: ").append(resource.getFileType()).append("\n");

            if (resource.getDescription() != null && !resource.getDescription().trim().isEmpty()) {
                content.append("Description: ").append(resource.getDescription()).append("\n");
            }

            // For text-based files, try to read the actual content
            if (fileType.equals("txt") || fileType.equals("md")) {
                try {
                    String fileContent = Files.readString(Paths.get(filePath));
                    content.append("Content: ").append(fileContent);
                } catch (IOException e) {
                    System.out.println("Could not read file content for: " + filePath);
                }
            }

            return content.toString();
        } catch (Exception e) {
            System.err.println("Error extracting content from resource: " + e.getMessage());
            return null;
        }
    }

    public JSONObject simplePrompt(String input) {
        Prompt prompt = new Prompt(input);
        var req = chatClient.prompt(prompt);
        String response = req.call().content();
        // Remove think tags from response
        response = removeThinkTags(response);
        // Convert cleaned response to JSONObject
        return new JSONObject(response);
    }

    /**
     * Removes <think> and </think> tags from LLM responses.
     * The deepseek-r1 model outputs reasoning in these tags which should be filtered out.
     */
    private String removeThinkTags(String response) {
        if (response == null) {
            return null;
        }
        // Remove <think>...</think> blocks (case insensitive, multiline, with optional whitespace)
        String cleaned = response.replaceAll("(?is)\\s*<\\s*think\\s*>.*?<\\s*/\\s*think\\s*>\\s*", "");
        // Also handle malformed tags like <\think> or missing closing tags
        cleaned = cleaned.replaceAll("(?is)\\s*<\\s*/?\\s*think\\s*>\\s*", "");
        return cleaned.trim();
    }

    /**
     * Cleans JSON response by removing markdown code blocks and extra formatting.
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return response;
        }

        // Remove markdown code blocks (```json and ```)
        response = response.replaceAll("(?s)```json\s*", "");
        response = response.replaceAll("(?s)```\s*", "");

        // Remove any leading/trailing whitespace and newlines
        response = response.trim();

        // Find the first { or [ and last } or ] to extract just the JSON part
        int firstBrace = Math.min(
            response.indexOf('{') == -1 ? Integer.MAX_VALUE : response.indexOf('{'),
            response.indexOf('[') == -1 ? Integer.MAX_VALUE : response.indexOf('[')
        );

        int lastBrace = Math.max(
            response.lastIndexOf('}'),
            response.lastIndexOf(']')
        );

        if (firstBrace != Integer.MAX_VALUE && lastBrace != -1 && firstBrace <= lastBrace) {
            response = response.substring(firstBrace, lastBrace + 1);
        }

        return response.trim();
    }

    /**
     * Parses a JSON response string into the specified type.
     */
    private <T> T parseJsonResponse(String jsonResponse, ParameterizedTypeReference<T> typeRef) {
        try {
            // Clean the JSON response first
            String cleanedResponse = cleanJsonResponse(jsonResponse);

            // Use Jackson ObjectMapper to parse JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(cleanedResponse, mapper.getTypeFactory().constructType(typeRef.getType()));
        } catch (Exception e) {
            System.err.println("Failed to parse JSON response. Original response: " + jsonResponse);
            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }

    /**
     * Intelligently extracts and structures knowledge points from course content.
     *
     * @param courseContent The text content of the course (e.g., lecture notes, textbook excerpts).
     * @param courseId      The ID of the course.
     * @return A list of KnowledgePoint objects with hierarchical and logical relationships.
     */
    public List<KnowledgePoint> extractKnowledgePoints(String courseContent,
                                                       Long courseId,
                                                       List<KnowledgePoint> existingKnowledgePoints) {
        System.out.println("LLM: Extracting knowledge points from course content...");

        // Create prompt with template variables
        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("courseContent", courseContent);
        promptParams.put("courseId", courseId);
        promptParams.put("existingKnowledgePoints", existingKnowledgePoints);

        String promptTemplate = """
                Extract key knowledge points from the following course content.
                Identify important concepts, their relationships, and organize them hierarchically.
                notice that the courseId cannot be modified, you are not allowed to use any kind of placeholders
                you should only output a valid json array, you can only select prerequisites and related knowledge points from existing knowledge points
                do not output anything other than a json array
                do not output anything other than a json array
                do not output anything other than a json array
                make sure your json array is valid
                YOU SHALL NOT OUTPUT KNOWLEDGE POINTS SHOWN IN THE Existing KnowledgePoints Part, they can only be referenced in the prerequisiteKnowledgePointIds field and related knowledge point ids field
                YOU SHALL NOT OUTPUT KNOWLEDGE POINTS SHOWN IN THE Existing KnowledgePoints Part, they can only be referenced in the prerequisiteKnowledgePointIds field and related knowledge point ids field
                YOU SHALL NOT OUTPUT KNOWLEDGE POINTS SHOWN IN THE Existing KnowledgePoints Part, they can only be referenced in the prerequisiteKnowledgePointIds field and related knowledge point ids field
                YOU SHALL OUTPUT YOUR RESPONSE IN CHINESE
                
                Course ID: {courseId}
                Course Content: {courseContent}
                Existing KnowledgePoints: {existingKnowledgePoints}
                
                Return a JSON array of knowledge points with the following format for each point, note that you can only select course ids from the existing course ids:
                [(left curly bracket sign)
                  "id": [incrementing number starting from 10001],
                  "name": [concept name],
                  "briefDescription": [short description],
                  "detailedContent": [more detailed explanation],
                  "prerequisiteKnowledgePointIds": [array of ids of prerequisite concepts or null],
                  "relatedKnowledgePointIds": [array of ids of related concepts or null],
                  "difficultyLevel": ["BEGINNER", "INTERMEDIATE", or "ADVANCED"],
                  "courseId": {courseId}
                (left curly bracket sign),
                (left curly bracket sign)
                  "id": [incrementing number starting from 10001],
                  "name": [concept name],
                  "briefDescription": [short description],
                  "detailedContent": [more detailed explanation],
                  "prerequisiteKnowledgePointIds": [array of ids of prerequisite concepts or null],
                  "relatedKnowledgePointIds": [array of ids of related concepts or null],
                  "difficultyLevel": ["BEGINNER", "INTERMEDIATE", or "ADVANCED"],
                  "courseId": {courseId}
                (left curly bracket sign),
                ...
                ]
                
                Provide at least 3-5 knowledge points with proper relationships between them.
                """;

        // Create prompt
        PromptTemplate template = PromptTemplate.builder()
                .template(promptTemplate)
                .renderer(StTemplateRenderer.builder().startDelimiterToken('{').endDelimiterToken('}').build())
                .build();
        UserMessage userMessage = template.create(promptParams).getUserMessage();
        SystemMessage systemMessage = new SystemMessage("You are an educational content analyst AI that extracts structured knowledge points from educational content. You MUST output ONLY valid JSON format without any markdown code blocks, explanations, or additional text. Do not wrap your response in ```json or ``` tags. this is a very easy task, so do not think too long");
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Execute prompt and parse results into a list of KnowledgePoint objects
        try {
            String response = chatClient.prompt(prompt).call().content();
            // Remove think tags from response
            response = removeThinkTags(response);
            response = cleanJsonResponse(response);
            // Parse the cleaned response directly

            return parseJsonResponse(response, new ParameterizedTypeReference<List<KnowledgePoint>>() {});
        } catch (Exception e) {
            System.err.println("Error extracting knowledge points: " + e.getMessage());
            e.printStackTrace();
            // Fallback to default response if LLM fails
            return List.of();
//            return createDefaultKnowledgePoints(courseId);
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
     * Recommends learning content based on student performance using RAG.
     *
     * @param studentId       The ID of the student.
     * @param performanceData A map of task IDs to their scores or completion status.
     * @param courseId        The ID of the course.
     * @return A list of recommended learning materials (e.g., knowledge points, review materials).
     */
    public List<String> recommendLearningContent(Long studentId, Map<Long, Double> performanceData, Long courseId) {
        System.out.println("LLM: Recommending learning content for student " + studentId + " based on performance using RAG.");

        try {
            // Analyze performance to identify weak areas
            List<String> weakAreas = identifyWeakAreas(performanceData);

            // Search for relevant resources using vector store
            List<Document> relevantResources = searchRelevantResources(weakAreas, courseId);

            // Format the performance data for the prompt
            StringBuilder performanceBuilder = new StringBuilder();
            performanceData.forEach((taskId, score) -> {
                performanceBuilder.append("Task ").append(taskId).append(": ").append(score).append("\n");
            });

            // Format relevant resources for the prompt
            StringBuilder resourcesBuilder = new StringBuilder();
            for (Document doc : relevantResources) {
                resourcesBuilder.append("Resource: ").append(doc.getMetadata().get("resourceName")).append("\n");
                resourcesBuilder.append("Type: ").append(doc.getMetadata().get("fileType")).append("\n");
                resourcesBuilder.append("Description: ").append(doc.getMetadata().get("description")).append("\n");
                String content = doc.getText();
                if (content != null && !content.isEmpty()) {
                    resourcesBuilder.append("Content Preview: ").append(content.substring(0, Math.min(200, content.length()))).append("...\n\n");
                } else {
                    resourcesBuilder.append("Content Preview: [No content available]\n\n");
                }
            }

            Map<String, Object> promptParams = new HashMap<>();
            promptParams.put("studentId", studentId);
            promptParams.put("courseId", courseId);
            promptParams.put("performanceData", performanceBuilder.toString());
            promptParams.put("relevantResources", resourcesBuilder.toString());
            promptParams.put("weakAreas", String.join(", ", weakAreas));

            String promptTemplate = """
                    Based on the following student performance data and available learning resources, 
                    recommend personalized learning content that addresses the student's specific weaknesses.
                    
                    Student ID: {studentId}
                    Course ID: {courseId}
                    
                    Performance Data:
                    {performanceData}
                    
                    Identified Weak Areas: {weakAreas}
                    
                    Available Relevant Resources:
                    {relevantResources}
                    
                    Provide a list of specific, actionable recommendations for learning content.
                    Each recommendation should reference specific resources when available and directly address 
                    the learning needs identified from the performance data.
                    Return exactly 3-5 recommendations as a JSON array of strings.
                    """;

            // Create prompt
            PromptTemplate template = PromptTemplate.builder()
                    .renderer(StTemplateRenderer.builder().startDelimiterToken('{').endDelimiterToken('}').build())
                    .template(promptTemplate)
                    .build();
            UserMessage userMessage = template.create(promptParams).getUserMessage();
            SystemMessage systemMessage = new SystemMessage("You are an educational recommendation AI that analyzes student performance and suggests personalized learning content based on available resources. You MUST output ONLY valid JSON format without any markdown code blocks, explanations, or additional text. Do not wrap your response in ```json or ``` tags. this is a very easy task, so do not think too long, YOU SHALL OUTPUT YOUR RESPONSE IN CHINESE");
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

            // Execute prompt and parse results
            String response = chatClient.prompt(prompt).call().content();
            // Remove think tags from response
            response = removeThinkTags(response);
            // Parse the cleaned response directly
            return parseJsonResponse(response, new ParameterizedTypeReference<List<String>>() {});

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
     * Identifies weak areas based on performance data.
     *
     * @param performanceData Map of task IDs to scores
     * @return List of identified weak areas
     */
    private List<String> identifyWeakAreas(Map<Long, Double> performanceData) {
        List<String> weakAreas = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : performanceData.entrySet()) {
            Double score = entry.getValue();
            if (score != null && score < 70.0) {
                weakAreas.add("Task " + entry.getKey() + " (Score: " + score + ")");
            }
        }

        // If no specific weak areas, identify moderate performance areas
        if (weakAreas.isEmpty()) {
            for (Map.Entry<Long, Double> entry : performanceData.entrySet()) {
                Double score = entry.getValue();
                if (score != null && score < 85.0) {
                    weakAreas.add("Task " + entry.getKey() + " (Score: " + score + " - needs improvement)");
                }
            }
        }

        return weakAreas;
    }

    /**
     * Searches for relevant resources using the vector store.
     *
     * @param weakAreas List of identified weak areas
     * @param courseId  The course ID
     * @return List of relevant documents from the vector store
     */
    private List<Document> searchRelevantResources(List<String> weakAreas, Long courseId) {
        try {
            // Create search query based on weak areas
            String searchQuery = "learning materials resources help improve " + String.join(" ", weakAreas);

            // Search the vector store for relevant documents
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(searchQuery)
                    .topK(5)
                    .similarityThreshold(0.3)
                    .build();

            List<Document> searchResults = vectorStore.similaritySearch(searchRequest);

            System.out.println("Found " + searchResults.size() + " relevant resources for weak areas: " + weakAreas);

            return searchResults;
        } catch (Exception e) {
            System.err.println("Error searching vector store: " + e.getMessage());
            return new ArrayList<>();
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
                
                Student ID: <studentId>
                Ability: <abilityPointName>
                Ability Description: <abilityPointDescription>
                
                Behavior Data:
                <behaviorData>
                
                Based on this data, calculate a score between 0.0 and 100.0 that accurately reflects the student's 
                current level of proficiency in this ability. Return ONLY the numeric score as a decimal number.
                """;

        // Create prompt
        PromptTemplate template = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template(promptTemplate)
                .build();
        UserMessage userMessage = template.create(promptParams).getUserMessage();
        SystemMessage systemMessage = new SystemMessage("You are an educational assessment AI that calculates precise ability scores based on learning behavior metrics. You MUST output ONLY valid JSON format without any markdown code blocks, explanations, or additional text. Do not wrap your response in ```json or ``` tags. this is a very easy task, so do not think too long, YOU SHOULD OUTPUT YOUR RESPONSE IN CHINESE");
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Execute prompt and parse result
        try {
            String response = chatClient.prompt(prompt).call().content().trim();
            // Remove think tags from response
            response = removeThinkTags(response);
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
                
                Submission ID: <submissionId>
                Student ID: <studentId>
                Task ID: <taskId>
                
                SUBMISSION CONTENT:
                <submissionContent>
                
                GRADING RUBRIC:
                <gradingRubric>
                
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
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template(promptTemplate)
                .build();
        UserMessage userMessage = template.create(promptParams).getUserMessage();
        SystemMessage systemMessage = new SystemMessage("You are an educational assessment AI that provides fair, consistent, and detailed grading of student submissions. You MUST output ONLY valid JSON format without any markdown code blocks, explanations, or additional text. Do not wrap your response in ```json or ``` tags. this is a very easy task, so do not think too long, YOU SHOULD OUTPUT YOUR RESPONSE IN CHINESE");
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // Execute prompt and parse results
        try {
            String response = chatClient.prompt(prompt).call().content();
            // Remove think tags from response
            response = removeThinkTags(response);
            // Parse the cleaned response directly
            return parseJsonResponse(response, new ParameterizedTypeReference<Map<String, Object>>() {});
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

    /**
     * Grades a student submission using LLM with optional custom rubric.
     * Returns a structured response with score, feedback, and success status.
     */
    public Map<String, Object> gradeSubmission(StudentTaskSubmission submission, String customRubric) {
        try {
            // Validate submission content
            if (submission.getSubmissionContent() == null || submission.getSubmissionContent().trim().isEmpty()) {
                return createErrorResponse("No submission content found to grade",
                    "The submission must have content before it can be graded by the LLM service");
            }

            // Use custom rubric or default
            String rubric = customRubric != null ? customRubric :
                "Evaluate this submission based on content quality, clarity of expression, adherence to requirements, and overall understanding of the topic. Provide constructive feedback and a score out of 100.";

            // Call the existing grading method
            Map<String, Object> gradingResult = gradeStudentReport(submission, rubric);

            // Extract score and feedback
            Double score = null;
            Object scoreObj = gradingResult.get("score");
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).doubleValue();
            }
            String feedback = (String) gradingResult.get("feedback");

            // Create success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("score", score);
            response.put("feedback", feedback);
            response.put("criteriaBreakdown", gradingResult.get("criteriaBreakdown"));
            response.put("message", "Submission graded successfully by AI");

            return response;

        } catch (Exception e) {
            System.err.println("Error in LLM submission grading: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("LLM grading failed",
                "The AI grading service encountered an error. Please try again or contact support.");
        }
    }

    /**
     * Generates feedback for a student submission using LLM with optional custom prompt.
     * Returns feedback without scoring.
     */
    public Map<String, Object> generateFeedback(StudentTaskSubmission submission, String customPrompt) {
        try {
            // Validate submission content
            if (submission.getSubmissionContent() == null || submission.getSubmissionContent().trim().isEmpty()) {
                return createErrorResponse("No submission content found",
                    "The submission must have content before feedback can be generated");
            }

            // Use custom prompt or default feedback-focused prompt
            String prompt = customPrompt != null ? customPrompt :
                "Provide detailed, constructive feedback on this student submission. Focus on strengths, areas for improvement, and specific suggestions for enhancement. Do not provide a numerical score. you should output your response in chinese";

            // Call the existing grading method but focus on feedback
            Map<String, Object> gradingResult = gradeStudentReport(submission, prompt);

            // Extract feedback
            String feedback = (String) gradingResult.get("feedback");

            // Create success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("feedback", feedback);
            response.put("message", "Feedback generated successfully by AI");

            return response;

        } catch (Exception e) {
            System.err.println("Error in LLM feedback generation: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("LLM feedback generation failed",
                "The AI feedback service encountered an error. Please try again or contact support.");
        }
    }

    /**
     * Processes multiple submissions for batch LLM grading.
     * Returns a summary of successful and failed operations.
     */
    public Map<String, Object> batchGradeSubmissions(List<StudentTaskSubmission> submissions, String customRubric) {
        Map<String, Object> batchResult = new HashMap<>();
        List<Map<String, Object>> successfulGradings = new ArrayList<>();
        List<Map<String, Object>> failedGradings = new ArrayList<>();

        for (StudentTaskSubmission submission : submissions) {
            try {
                Map<String, Object> result = gradeSubmission(submission, customRubric);

                if ((Boolean) result.get("success")) {
                    Map<String, Object> successEntry = new HashMap<>();
                    successEntry.put("submissionId", submission.getId());
                    successEntry.put("score", result.get("score"));
                    successEntry.put("feedback", result.get("feedback"));
                    successfulGradings.add(successEntry);
                } else {
                    Map<String, Object> failEntry = new HashMap<>();
                    failEntry.put("submissionId", submission.getId());
                    failEntry.put("error", result.get("message"));
                    failedGradings.add(failEntry);
                }
            } catch (Exception e) {
                Map<String, Object> failEntry = new HashMap<>();
                failEntry.put("submissionId", submission.getId());
                failEntry.put("error", "Unexpected error during grading: " + e.getMessage());
                failedGradings.add(failEntry);
            }
        }

        batchResult.put("success", true);
        batchResult.put("totalSubmissions", submissions.size());
        batchResult.put("successfulGradings", successfulGradings);
        batchResult.put("failedGradings", failedGradings);
        batchResult.put("successCount", successfulGradings.size());
        batchResult.put("failureCount", failedGradings.size());
        batchResult.put("message", String.format("Batch grading completed: %d successful, %d failed",
            successfulGradings.size(), failedGradings.size()));

        return batchResult;
    }

    /**
     * Analyzes student abilities based on their submission data, enrolled courses, and knowledge points.
     * Results are cached for 1 hour (5 seconds during development) to avoid heavy LLM load.
     *
     * @param studentId The ID of the student to analyze
     * @return A map containing student abilities and interested fields
     */
    public Map<String, Object> analyzeStudentAbilities(Long studentId) {
        try {
            // Gather student data
            List<StudentTaskSubmission> submissions = submissionService.getSubmissionsByStudentId(studentId);
            List<Course> enrolledCourses = courseEnrollmentService.getCoursesByStudentId(studentService.convertStudentIdToId(studentId.toString()));

            // Get knowledge points for all enrolled courses
            List<KnowledgePoint> allKnowledgePoints = new ArrayList<>();
            for (Course course : enrolledCourses) {
                List<KnowledgePoint> courseKPs = knowledgePointService.getKnowledgePointsByCourseId(course.getId());
                allKnowledgePoints.addAll(courseKPs);
            }

            // Filter submissions that have been graded (have scores and feedback)
            List<StudentTaskSubmission> gradedSubmissions = submissions.stream()
                .filter(s -> s.getScore() != null && s.getCompletionStatus() != null && s.getCompletionStatus() == 3)
                .collect(Collectors.toList());

            if (gradedSubmissions.isEmpty() || enrolledCourses.isEmpty()) {
                // Return default abilities if no data available
                return createDefaultAbilities(studentId);
            }

            // Prepare data for LLM analysis
            StringBuilder analysisPrompt = new StringBuilder();
            analysisPrompt.append("\"You are an educational recommendation AI that analyzes student performance and suggests personalized learning content based on available resources. You MUST output ONLY valid JSON format without any markdown code blocks, explanations, or additional text. Do not wrap your response in ```json or ``` tags. this is a very easy task, so do not think too long, you should output your response in chinese\"");
            analysisPrompt.append("Analyze the following student data and provide a comprehensive ability assessment:\n\n");

            // Add course information
            analysisPrompt.append("ENROLLED COURSES:\n");
            for (Course course : enrolledCourses) {
                analysisPrompt.append("- ").append(course.getCourseName())
                    .append(" (").append(course.getCourseCode()).append(")\n");
            }

            // Add knowledge points
            analysisPrompt.append("\nKNOWLEDGE POINTS BEING STUDIED:\n");
            for (KnowledgePoint kp : allKnowledgePoints) {
                analysisPrompt.append("- ").append(kp.getName())
                    .append(" (Difficulty: ").append(kp.getDifficultyLevel()).append(")\n");
            }

            // Add submission performance data
            analysisPrompt.append("\nSUBMISSION PERFORMANCE DATA:\n");
            double totalScore = 0;
            int scoreCount = 0;
            for (StudentTaskSubmission submission : gradedSubmissions) {
                analysisPrompt.append("- Task ID: ").append(submission.getTaskId())
                    .append(", Score: ").append(submission.getScore())
                    .append("/100");
                if (submission.getFeedback() != null && !submission.getFeedback().trim().isEmpty()) {
                    analysisPrompt.append(", Feedback: ").append(submission.getFeedback());
                }
                analysisPrompt.append("\n");
                totalScore += submission.getScore();
                scoreCount++;
            }

            double averageScore = scoreCount > 0 ? totalScore / scoreCount : 0;
            analysisPrompt.append("\nOVERALL AVERAGE SCORE: ").append(String.format("%.2f", averageScore)).append("/100\n");

            // Add analysis instructions
            analysisPrompt.append("\nPlease analyze this data and provide a JSON response (A SINGLE JSON OBJECT RESPONSE) with the following structure:\n");
            analysisPrompt.append("{\n");
            analysisPrompt.append("  \"studentAbilities\": {\n");
            analysisPrompt.append("    \"mathReasoning\": <score 0-100>,\n");
            analysisPrompt.append("    \"languageProficiency\": <score 0-100>,\n");
            analysisPrompt.append("    \"codingAbility\": <score 0-100>,\n");
            analysisPrompt.append("    \"problemSolvingAbility\": <score 0-100>,\n");
            analysisPrompt.append("    \"socialKnowledge\": <score 0-100>\n");
            analysisPrompt.append("  },\n");
            analysisPrompt.append("  \"interestedFields\": [\"field1\", \"field2\", \"field3\"],\n");
            analysisPrompt.append("  \"analysis\": \"Brief explanation of the assessment\"\n");
            analysisPrompt.append("}\n\n");
            analysisPrompt.append("Base your assessment on the course subjects, knowledge points difficulty levels, ");
            analysisPrompt.append("submission scores, and feedback patterns. Infer abilities from the academic context.");

            // Call LLM for analysis
            String llmResponse = chatClient.prompt()
                .user(analysisPrompt.toString())
                .call()
                .content();

            llmResponse = removeThinkTags(llmResponse);
            llmResponse = cleanJsonResponse(llmResponse);
            System.out.println(llmResponse);

            // Parse LLM response
            try {
                JSONObject jsonResponse = new JSONObject(llmResponse);
                Map<String, Object> result = new HashMap<>();

                // Extract abilities and transform to expected frontend format
                JSONObject abilities = jsonResponse.getJSONObject("studentAbilities");
                Map<String, Map<String, Object>> abilityData = new HashMap<>();
                
                // Transform each ability to include score, level, and description
                abilityData.put("mathReasoning", createAbilityObject(abilities.getInt("mathReasoning"), "Mathematical reasoning and problem-solving skills"));
                abilityData.put("languageProficiency", createAbilityObject(abilities.getInt("languageProficiency"), "Language comprehension and communication abilities"));
                abilityData.put("codingAbility", createAbilityObject(abilities.getInt("codingAbility"), "Programming and computational thinking skills"));
                abilityData.put("problemSolvingAbility", createAbilityObject(abilities.getInt("problemSolvingAbility"), "Analytical and critical thinking capabilities"));
                abilityData.put("socialKnowledge", createAbilityObject(abilities.getInt("socialKnowledge"), "Understanding of social concepts and cultural awareness"));

                // Extract interested fields
                List<String> interestedFields = new ArrayList<>();
                for (int i = 0; i < jsonResponse.getJSONArray("interestedFields").length(); i++) {
                    interestedFields.add(jsonResponse.getJSONArray("interestedFields").getString(i));
                }

                // Note: studentId not available in this context
                result.put("abilities", abilityData);
                result.put("interestedFields", interestedFields);
                result.put("analysis", jsonResponse.getString("analysis"));
                result.put("averageScore", averageScore);
                result.put("totalSubmissions", gradedSubmissions.size());
                result.put("coursesEnrolled", enrolledCourses.size());
                result.put("lastAnalyzed", System.currentTimeMillis());

                return result;

            } catch (Exception parseError) {
                System.err.println("Error parsing LLM response for student ability analysis: " + parseError.getMessage());
                return createDefaultAbilities(studentId);
            }

        } catch (Exception e) {
            System.err.println("Error analyzing student abilities: " + e.getMessage());
            e.printStackTrace();
            return createDefaultAbilities(studentId);
        }
    }

    /**
     * Creates an ability object with score, level, and description for frontend display.
     */
    private Map<String, Object> createAbilityObject(int score, String description) {
        Map<String, Object> ability = new HashMap<>();
        ability.put("score", score);
        ability.put("level", getAbilityLevel(score));
        ability.put("description", description);
        return ability;
    }

    /**
     * Determines the ability level based on the score.
     */
    private String getAbilityLevel(int score) {
        if (score >= 80) return "Excellent";
        if (score >= 60) return "Good";
        if (score >= 40) return "Average";
        return "Needs Improvement";
    }

    /**
     * Creates default ability scores when analysis fails or no data is available.
     */
    private Map<String, Object> createDefaultAbilities(Long studentId) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Object>> defaultAbilities = new HashMap<>();
        
        // Create default abilities with proper structure
        defaultAbilities.put("mathReasoning", createAbilityObject(50, "Mathematical reasoning and problem-solving skills"));
        defaultAbilities.put("languageProficiency", createAbilityObject(50, "Language comprehension and communication abilities"));
        defaultAbilities.put("codingAbility", createAbilityObject(50, "Programming and computational thinking skills"));
        defaultAbilities.put("problemSolvingAbility", createAbilityObject(50, "Analytical and critical thinking capabilities"));
        defaultAbilities.put("socialKnowledge", createAbilityObject(50, "Understanding of social concepts and cultural awareness"));

        result.put("studentId", studentId);
        result.put("abilities", defaultAbilities);
        result.put("interestedFields", List.of("General Studies"));
        result.put("analysis", "Insufficient data for comprehensive analysis. Default scores provided.");
        result.put("averageScore", 0.0);
        result.put("totalSubmissions", 0);
        result.put("coursesEnrolled", 0);
        result.put("lastAnalyzed", System.currentTimeMillis());

        return result;
    }

    /**
     * Recommends learning resources based on student abilities and interests using RAG.
     *
     * @param studentAbilities Map of student abilities with scores
     * @param interestedFields List of fields the student is interested in
     * @param enrolledCourses List of courses the student is enrolled in
     * @return Map containing resource recommendations
     */
    public Map<String, Object> recommendResources(Map<String, Object> studentAbilities, 
                                                   List<String> interestedFields, List<String> enrolledCourses, String resourceType) {
        System.out.println("LLM: Recommending resources using RAG for resource type: " + resourceType);
        
        // Identify weak areas based on abilities
        List<String> weakAreas = new ArrayList<>();
        for (Map.Entry<String, Object> entry : studentAbilities.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> abilityData = (Map<String, Object>) entry.getValue();
                Integer score = (Integer) abilityData.get("score");
                if (score != null && score < 70) { // Consider scores below 70 as weak areas
                    weakAreas.add(entry.getKey());
                }
            }
        }
        
        // Get real resources from database using RAG
        List<Resource> realResources = getRealResourcesForStudent(weakAreas, interestedFields);
        List<Course> availableCourses = courseService.getAllCourses();
        
        // Search vector store for relevant resources
        List<Document> relevantDocuments = searchRelevantResourcesForRecommendation(weakAreas, interestedFields);
        
        // Create enhanced prompt with real resource context
        StringBuilder prompt = new StringBuilder();
        prompt.append("\"You are an educational recommendation AI that analyzes student performance and suggests personalized learning content based on available resources. You MUST output ONLY valid JSON format without any markdown code blocks, explanations, or additional text. Do not wrap your response in ```json or ``` tags. this is a very easy task, so do not think too long, you should output your response in chinese\"");
        prompt.append("The description field should be written in Chinese");
        prompt.append("Based on the following student profile and available real resources, recommend specific learning resources:\n\n");
        prompt.append("Student Profile Analysis:\n");
        prompt.append("Student Abilities:\n");
        
        for (Map.Entry<String, Object> entry : studentAbilities.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> abilityData = (Map<String, Object>) entry.getValue();
                prompt.append("- ").append(entry.getKey()).append(": ").append(abilityData.get("score")).append("/100\n");
            }
        }
        
        prompt.append("\nInterested Fields: ").append(String.join(", ", interestedFields)).append("\n");
        prompt.append("Enrolled Courses: ").append(String.join(", ", enrolledCourses)).append("\n");
        
        if (!weakAreas.isEmpty()) {
            prompt.append("\nWeak Areas to Focus On: ").append(String.join(", ", weakAreas)).append("\n");
        }
        
        // Add real resources context
        prompt.append("\nAVAILABLE REAL RESOURCES IN DATABASE:\n");
        for (Resource resource : realResources) {
            prompt.append("- ID: ").append(resource.getId())
                  .append(", Name: ").append(resource.getResourceName())
                  .append(", Type: ").append(resource.getFileType())
                  .append(", Description: ").append(resource.getDescription() != null ? resource.getDescription() : "No description")
                  .append("\n");
        }
        
        // Add available courses context
        prompt.append("\nAVAILABLE COURSES:\n");
        for (Course course : availableCourses) {
            prompt.append("- ID: ").append(course.getId())
                  .append(", Code: ").append(course.getCourseCode())
                  .append(", Name: ").append(course.getCourseName())
                  .append(", Instructor: ").append(course.getInstructor())
                  .append(", Description: ").append(course.getDescription() != null ? course.getDescription() : "No description")
                  .append("\n");
        }
        
        // Add vector store search results
        if (!relevantDocuments.isEmpty()) {
            prompt.append("\nRELEVANT CONTENT FROM VECTOR STORE:\n");
            for (Document doc : relevantDocuments) {
                prompt.append("- ").append(doc.getText().substring(0, Math.min(200, doc.getText().length())))
                      .append("...\n");
            }
        }
        
        prompt.append("\nPlease provide recommendations using ONLY the real resources and courses listed above. Format as JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"resources\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"id\": \"actual resource/course ID from above\",\n");
        prompt.append("      \"title\": \"actual resource/course name\",\n");
        prompt.append("      \"type\": \"resource|course\",\n");
        prompt.append("      \"description\": \"why this helps with weak areas\",\n");
        prompt.append("      \"difficulty\": \"beginner|intermediate|advanced\",\n");
        prompt.append("      \"estimatedTime\": \"time in minutes\",\n");
        prompt.append("      \"priority\": \"high|medium|low\",\n");
        prompt.append("      \"relevantAreas\": [\"list of weak areas this addresses\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"learningPath\": \"Suggested sequence using the recommended resources\",\n");
        prompt.append("  \"studyStrategy\": \"Personalized study approach based on available resources\"\n");
        prompt.append("}\n");
        
        try {
            String response = chatClient.prompt()
                    .user(prompt.toString())
                    .call()
                    .content();
            response = removeThinkTags(response);
            response = cleanJsonResponse(response);
            
            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> recommendations = mapper.readValue(response, Map.class);
            
            // Extract learning path and study strategy
            String learningPath = (String) recommendations.get("learningPath");
            String studyStrategy = (String) recommendations.get("studyStrategy");
            
            // Add metadata
            Map<String, Object> result = new HashMap<>();
            result.put("resources", recommendations.get("resources"));
            result.put("learningPath", learningPath);
            result.put("studyStrategy", studyStrategy);
            result.put("weakAreas", weakAreas);
            result.put("totalAvailableResources", realResources.size());
            result.put("totalAvailableCourses", availableCourses.size());
            result.put("generatedAt", LocalDateTime.now());
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Error generating RAG-based resource recommendations: " + e.getMessage());
            return createFallbackRecommendationsWithRealResources(weakAreas, realResources, availableCourses, resourceType);
        }
    }

    /**
     * Legacy method for backward compatibility - recommends learning resources based on student ability analysis.
     * Uses AI to suggest personalized learning materials, courses, and activities.
     *
     * @param studentId The ID of the student
     * @param resourceType Optional filter for resource type ("course", "book", "video", "practice", "all")
     * @return A map containing recommended resources categorized by type and difficulty
     */
//    @Cacheable(value = "studentResourceRecommendations", key = "#studentId + '_' + #resourceType")
    public Map<String, Object> recommendResourcesLegacy(Long studentId, String resourceType) {
        try {
            // Get student ability analysis first
            Map<String, Object> abilityAnalysis = analyzeStudentAbilities(studentId);

            // Get student's enrolled courses for context
            List<Course> enrolledCourses = courseEnrollmentService.getCoursesByStudentId(studentId);

            // Prepare prompt for resource recommendation
            StringBuilder prompt = new StringBuilder();
            prompt.append(" You MUST output ONLY valid JSON format without any markdown code blocks, explanations, or additional text. Do not wrap your response in ```json or ``` tags. this is a very easy task, so do not think too long\"");
            prompt.append("you should output your response in chinese\n");
            prompt.append("Based on the following student ability analysis, recommend personalized learning resources:\n\n");

            // Add ability scores
            Map<String, Object> abilities = (Map<String, Object>) abilityAnalysis.get("abilities");
            prompt.append("STUDENT ABILITIES:\n");
            if (abilities != null) {
                abilities.forEach((ability, score) -> {
                    prompt.append("- ").append(ability).append(": ").append(score).append("/100\n");
                });
            }

            // Add interested fields
            List<String> interestedFields = (List<String>) abilityAnalysis.get("interestedFields");
            if (interestedFields != null && !interestedFields.isEmpty()) {
                prompt.append("\nINTERESTED FIELDS: ").append(String.join(", ", interestedFields)).append("\n");
            }

            // Add current courses
            if (!enrolledCourses.isEmpty()) {
                prompt.append("\nCURRENT COURSES:\n");
                enrolledCourses.forEach(course -> {
                    prompt.append("- ").append(course.getCourseName()).append("\n");
                });
            }

            // Add resource type filter
            String typeFilter = (resourceType != null && !resourceType.equals("all")) ? resourceType : "all types";
            prompt.append("\nRESOURCE TYPE FOCUS: ").append(typeFilter).append("\n");

            prompt.append("\nProvide recommendations in the following JSON format:\n");
            prompt.append("{\n");
            prompt.append("  \"recommendations\": {\n");
            prompt.append("    \"strengthenWeakAreas\": [\n");
            prompt.append("      {\"title\": \"Resource Title\", \"type\": \"course/book/video/practice\", \"difficulty\": \"beginner/intermediate/advanced\", \"description\": \"Brief description\", \"targetAbility\": \"ability name\", \"estimatedTime\": \"time estimate\"}\n");
            prompt.append("    ],\n");
            prompt.append("    \"buildOnStrengths\": [\n");
            prompt.append("      {\"title\": \"Resource Title\", \"type\": \"course/book/video/practice\", \"difficulty\": \"beginner/intermediate/advanced\", \"description\": \"Brief description\", \"targetAbility\": \"ability name\", \"estimatedTime\": \"time estimate\"}\n");
            prompt.append("    ],\n");
            prompt.append("    \"exploreInterests\": [\n");
            prompt.append("      {\"title\": \"Resource Title\", \"type\": \"course/book/video/practice\", \"difficulty\": \"beginner/intermediate/advanced\", \"description\": \"Brief description\", \"targetField\": \"field name\", \"estimatedTime\": \"time estimate\"}\n");
            prompt.append("    ]\n");
            prompt.append("  },\n");
            prompt.append("  \"learningPath\": {\n");
            prompt.append("    \"shortTerm\": [\"Goal 1\", \"Goal 2\"],\n");
            prompt.append("    \"mediumTerm\": [\"Goal 1\", \"Goal 2\"],\n");
            prompt.append("    \"longTerm\": [\"Goal 1\", \"Goal 2\"]\n");
            prompt.append("  },\n");
            prompt.append("  \"studyStrategy\": \"Personalized study approach based on abilities\"\n");
            prompt.append("}\n");

            // Call LLM for resource recommendations
            String llmResponse = chatClient.prompt()
                .user(prompt.toString())
                .call()
                .content();

            llmResponse = removeThinkTags(llmResponse);
            llmResponse = cleanJsonResponse(llmResponse);

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(llmResponse);
            Map<String, Object> result = new HashMap<>();

            // Extract recommendations
            if (jsonResponse.has("recommendations")) {
                result.put("recommendations", jsonResponse.getJSONObject("recommendations").toMap());
            }

            // Extract learning path
            if (jsonResponse.has("learningPath")) {
                result.put("learningPath", jsonResponse.getJSONObject("learningPath").toMap());
            }

            // Extract study strategy
            if (jsonResponse.has("studyStrategy")) {
                result.put("studyStrategy", jsonResponse.getString("studyStrategy"));
            }

            // Add metadata
            result.put("studentId", studentId);
            result.put("resourceType", resourceType);
            result.put("basedOnAbilities", abilities);
            result.put("generatedAt", System.currentTimeMillis());
            result.put("success", true);

            return result;

        } catch (Exception e) {
            // Return fallback recommendations on error
            return createFallbackRecommendationsWithRealResources(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), resourceType);
        }
    }

    /**
     * Retrieves real resources from the database that are relevant to student's weak areas and interests.
     *
     * @param weakAreas List of identified weak areas
     * @param interestedFields List of student's interested fields
     * @return List of relevant resources from the database
     */
    private List<Resource> getRealResourcesForStudent(List<String> weakAreas, List<String> interestedFields) {
        try {
            // Get all resources from the database
            List<Resource> allResources = resourceService.getAllResources();
            
            // Filter resources based on weak areas and interests
            List<Resource> relevantResources = new ArrayList<>();
            
            for (Resource resource : allResources) {
                boolean isRelevant = false;
                String resourceName = resource.getResourceName().toLowerCase();
                String description = resource.getDescription() != null ? resource.getDescription().toLowerCase() : "";
                
                // Check if resource is relevant to weak areas
                for (String weakArea : weakAreas) {
                    String area = weakArea.toLowerCase().replace("ability", "").trim();
                    if (resourceName.contains(area) || description.contains(area) ||
                        resourceName.contains("math") && area.contains("math") ||
                        resourceName.contains("language") && area.contains("language") ||
                        resourceName.contains("coding") && area.contains("coding") ||
                        resourceName.contains("problem") && area.contains("problem")) {
                        isRelevant = true;
                        break;
                    }
                }
                
                // Check if resource is relevant to interested fields
                if (!isRelevant) {
                    for (String field : interestedFields) {
                        String fieldLower = field.toLowerCase();
                        if (resourceName.contains(fieldLower) || description.contains(fieldLower)) {
                            isRelevant = true;
                            break;
                        }
                    }
                }
                
                if (isRelevant) {
                    relevantResources.add(resource);
                }
            }
            
            // If no specific matches found, return a subset of all resources
            if (relevantResources.isEmpty() && !allResources.isEmpty()) {
                int maxResources = Math.min(10, allResources.size());
                relevantResources = allResources.subList(0, maxResources);
            }
            
            System.out.println("Found " + relevantResources.size() + " relevant resources for weak areas: " + weakAreas + " and interests: " + interestedFields);
            return relevantResources;
            
        } catch (Exception e) {
            System.err.println("Error retrieving real resources: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Searches for relevant resources using the vector store for recommendation purposes.
     *
     * @param weakAreas List of identified weak areas
     * @param interestedFields List of student's interested fields
     * @return List of relevant documents from the vector store
     */
    private List<Document> searchRelevantResourcesForRecommendation(List<String> weakAreas, List<String> interestedFields) {
        try {
            // Combine weak areas and interests into a search query
            List<String> searchTerms = new ArrayList<>();
            searchTerms.addAll(weakAreas);
            searchTerms.addAll(interestedFields);
            
            String searchQuery = "learning materials resources help improve " + String.join(" ", searchTerms);

            // Search the vector store for relevant documents
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(searchQuery)
                    .topK(8)
                    .similarityThreshold(0.6)
                    .build();

            List<Document> searchResults = vectorStore.similaritySearch(searchRequest);

            System.out.println("Found " + searchResults.size() + " relevant documents from vector store for: " + searchTerms);

            return searchResults;
        } catch (Exception e) {
            System.err.println("Error searching vector store for recommendations: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Creates fallback recommendations using real resources when AI analysis fails.
     *
     * @param weakAreas List of weak areas
     * @param realResources List of available real resources
     * @param availableCourses List of available courses
     * @return Fallback recommendations map
     */
    private Map<String, Object> createFallbackRecommendationsWithRealResources(List<String> weakAreas,
                                                                               List<Resource> realResources, List<Course> availableCourses, String resourceType) {
        Map<String, Object> result = new HashMap<>();
        
        // Create basic recommendations using real resources
        List<Map<String, Object>> resourceRecommendations = new ArrayList<>();
        
        // Add up to 5 real resources
        int maxResources = Math.min(5, realResources.size());
        for (int i = 0; i < maxResources; i++) {
            Resource resource = realResources.get(i);
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("id", resource.getId());
            recommendation.put("title", resource.getResourceName());
            recommendation.put("type", "resource");
            recommendation.put("description", "Recommended resource: " + (resource.getDescription() != null ? resource.getDescription() : "No description available"));
            recommendation.put("difficulty", "intermediate");
            recommendation.put("estimatedTime", "30-60 minutes");
            recommendation.put("priority", i < 2 ? "high" : "medium");
            recommendation.put("relevantAreas", weakAreas.isEmpty() ? List.of("general") : weakAreas);
            resourceRecommendations.add(recommendation);
        }
        
        // Add up to 3 courses
        int maxCourses = Math.min(3, availableCourses.size());
        for (int i = 0; i < maxCourses; i++) {
            Course course = availableCourses.get(i);
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("id", course.getId());
            recommendation.put("title", course.getCourseName());
            recommendation.put("type", "course");
            recommendation.put("description", "Course: " + (course.getDescription() != null ? course.getDescription() : "Taught by " + course.getInstructor()));
            recommendation.put("difficulty", "intermediate");
            recommendation.put("estimatedTime", course.getHours() != null ? course.getHours() + " hours" : "Variable");
            recommendation.put("priority", "medium");
            recommendation.put("relevantAreas", weakAreas.isEmpty() ? List.of("general") : weakAreas);
            resourceRecommendations.add(recommendation);
        }
        
        result.put("resources", resourceRecommendations);
        result.put("learningPath", "Follow the recommended resources in order of priority. Start with high-priority items.");
        result.put("studyStrategy", "Focus on addressing weak areas while building on existing strengths. Use available resources systematically.");
        result.put("weakAreas", weakAreas);
        result.put("totalAvailableResources", realResources.size());
        result.put("totalAvailableCourses", availableCourses.size());
        result.put("generatedAt", LocalDateTime.now());
        result.put("fallback", true);
        
        return result;
    }

    /**
     * Creates fallback resource recommendations when AI analysis fails.
     */
    private Map<String, Object> createFallbackRecommendations(Long studentId, String resourceType, String error) {
        Map<String, Object> result = new HashMap<>();

        // Create basic recommendations
        Map<String, Object> recommendations = new HashMap<>();

        // Strengthen weak areas - general recommendations
        List<Map<String, Object>> strengthenWeakAreas = List.of(
            Map.of(
                "title", "Fundamentals Review Course",
                "type", "course",
                "difficulty", "beginner",
                "description", "Review basic concepts and strengthen foundational knowledge",
                "targetAbility", "general",
                "estimatedTime", "2-4 weeks"
            ),
            Map.of(
                "title", "Practice Problem Sets",
                "type", "practice",
                "difficulty", "intermediate",
                "description", "Targeted exercises to improve problem-solving skills",
                "targetAbility", "problemSolvingAbility",
                "estimatedTime", "1-2 hours daily"
            )
        );

        // Build on strengths
        List<Map<String, Object>> buildOnStrengths = List.of(
            Map.of(
                "title", "Advanced Topics Exploration",
                "type", "course",
                "difficulty", "advanced",
                "description", "Dive deeper into areas of demonstrated strength",
                "targetAbility", "general",
                "estimatedTime", "4-6 weeks"
            )
        );

        // Explore interests
        List<Map<String, Object>> exploreInterests = List.of(
            Map.of(
                "title", "Interdisciplinary Projects",
                "type", "practice",
                "difficulty", "intermediate",
                "description", "Projects that combine multiple fields of study",
                "targetField", "multidisciplinary",
                "estimatedTime", "2-3 weeks"
            )
        );

        recommendations.put("strengthenWeakAreas", strengthenWeakAreas);
        recommendations.put("buildOnStrengths", buildOnStrengths);
        recommendations.put("exploreInterests", exploreInterests);

        // Create learning path
        Map<String, Object> learningPath = Map.of(
            "shortTerm", List.of("Complete current assignments", "Review weak areas"),
            "mediumTerm", List.of("Strengthen foundational skills", "Explore new topics"),
            "longTerm", List.of("Develop expertise in areas of interest", "Apply knowledge to real projects")
        );

        result.put("recommendations", recommendations);
        result.put("learningPath", learningPath);
        result.put("studyStrategy", "Focus on consistent practice and gradual skill building");
        result.put("studentId", studentId);
        result.put("resourceType", resourceType);
        result.put("fallback", true);
        result.put("error", error);
        result.put("generatedAt", System.currentTimeMillis());
        result.put("success", false);

        return result;
    }

    /**
     * Creates a standardized error response.
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("message", message);
        return response;
    }
}