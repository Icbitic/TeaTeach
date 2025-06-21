package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.llm.LLMService;
import org.bedrock.teateach.mappers.KnowledgePointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class KnowledgePointService {

    private final KnowledgePointMapper knowledgePointMapper;
    private final LLMService llmService; // Inject the LLM service

    @Autowired
    public KnowledgePointService(KnowledgePointMapper knowledgePointMapper, LLMService llmService) {
        this.knowledgePointMapper = knowledgePointMapper;
        this.llmService = llmService;
    }

    /**
     * Creates a new knowledge point.
     * @param knowledgePoint The knowledge point to create.
     * @return The created knowledge point with its generated ID.
     */
    @Transactional
    public KnowledgePoint createKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgePointMapper.insert(knowledgePoint);
        return knowledgePoint;
    }

    /**
     * Updates an existing knowledge point.
     * @param knowledgePoint The knowledge point to update.
     * @return The updated knowledge point.
     */
    @Transactional
    public KnowledgePoint updateKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgePointMapper.update(knowledgePoint);
        return knowledgePoint;
    }

    /**
     * Deletes a knowledge point by its ID.
     * @param id The ID of the knowledge point to delete.
     */
    @Transactional
    public void deleteKnowledgePoint(Long id) {
        knowledgePointMapper.delete(id);
    }

    /**
     * Retrieves a knowledge point by its ID.
     * @param id The ID of the knowledge point.
     * @return An Optional containing the knowledge point if found, or empty otherwise.
     */
    public Optional<KnowledgePoint> getKnowledgePointById(Long id) {
        return Optional.ofNullable(knowledgePointMapper.findById(id));
    }

    /**
     * Retrieves all knowledge points associated with a specific course.
     * @param courseId The ID of the course.
     * @return A list of knowledge points for the given course.
     */
    public List<KnowledgePoint> getKnowledgePointsByCourseId(Long courseId) {
        return knowledgePointMapper.findByCourseId(courseId);
    }

    /**
     * Retrieves all knowledge points in the system.
     * @return A list of all knowledge points.
     */
    public List<KnowledgePoint> getAllKnowledgePoints() {
        return knowledgePointMapper.findAll();
    }

    /**
     * Innovative Function: Triggers intelligent extraction and storage of knowledge points
     * for a given course content using the Large Language Model.
     * This method would typically be called by an admin or course creation process.
     *
     * @param courseContent The raw text content (e.g., lecture notes, textbook chapters)
     * from which to extract knowledge points.
     * @param courseId      The ID of the course to associate these knowledge points with.
     * @return A list of the newly created KnowledgePoint objects.
     */
    @Transactional
    public List<KnowledgePoint> generateKnowledgeGraphFromContent(String courseContent, Long courseId) {
        // Use LLMService to extract knowledge points and their relationships
        List<KnowledgePoint> extractedKPs = llmService.extractKnowledgePoints(courseContent, courseId);

        // Persist the extracted knowledge points
        extractedKPs.forEach(kp -> {
            // Ensure courseId is set if not already done by LLMService
            if (kp.getCourseId() == null) {
                kp.setCourseId(courseId);
            }
            createKnowledgePoint(kp); // Call the service's create method to ensure transactionality
        });

        return extractedKPs;
    }

    // You might add more methods here, such as:
    // - getKnowledgeGraphForCourse(Long courseId): to retrieve the full graph structure
    // - validateKnowledgePointRelationships(): a utility to check graph consistency
}