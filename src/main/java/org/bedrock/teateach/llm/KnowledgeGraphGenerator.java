package org.bedrock.teateach.llm;

import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.services.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A component to trigger knowledge graph generation using the LLMService.
 * This might be triggered via an admin UI or a scheduled job.
 */
@Component
public class KnowledgeGraphGenerator {

    private final LLMService llmService;
    private final KnowledgePointService knowledgePointService;

    @Autowired
    public KnowledgeGraphGenerator(LLMService llmService, KnowledgePointService knowledgePointService) {
        this.llmService = llmService;
        this.knowledgePointService = knowledgePointService;
    }

    /**
     * Generates and persists a knowledge graph for a given course.
     * @param courseContent The raw content of the course.
     * @param courseId The ID of the course.
     */
    public void generateKnowledgeGraphForCourse(String courseContent, Long courseId) {
        System.out.println("Initiating knowledge graph generation for course " + courseId);
        List<KnowledgePoint> knowledgePoints = llmService.extractKnowledgePoints(courseContent, courseId);

        // Persist the extracted knowledge points to the database
        knowledgePoints.forEach(kp -> {
            knowledgePointService.createKnowledgePoint(kp);
            System.out.println("Created Knowledge Point: " + kp.getName());
        });
        System.out.println("Knowledge graph generation complete for course " + courseId);
    }
}