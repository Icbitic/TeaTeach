package org.bedrock.teateach.llm;

import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.services.CourseService;
import org.bedrock.teateach.services.KnowledgePointService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A component to trigger knowledge graph generation using the LLMService.
 * This might be triggered via an admin UI or a scheduled job.
 */
@Component
public class KnowledgeGraphGenerator {

    private final CourseService courseService;
    private final LLMService llmService;
    private final KnowledgePointService knowledgePointService;

    public KnowledgeGraphGenerator(CourseService courseService, LLMService llmService, KnowledgePointService knowledgePointService) {
        this.courseService = courseService;
        this.llmService = llmService;
        this.knowledgePointService = knowledgePointService;
    }
}