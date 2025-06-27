package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.llm.LLMService;
import org.bedrock.teateach.mappers.KnowledgePointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KnowledgePointService {

    private final KnowledgePointMapper knowledgePointMapper;
    private final CourseService courseService;
    private final LLMService llmService; // Inject the LLM service
    private final KnowledgePointService selfProxy;

    @Autowired
    public KnowledgePointService(KnowledgePointMapper knowledgePointMapper,
                                 CourseService courseService,
                                 LLMService llmService,
                                 @Lazy KnowledgePointService selfProxy) {
        this.knowledgePointMapper = knowledgePointMapper;
        this.courseService = courseService;
        this.llmService = llmService;
        this.selfProxy = selfProxy;
    }

    /**
     * Creates a new knowledge point.
     *
     * @param knowledgePoint The knowledge point to create.
     * @return The created knowledge point with its generated ID.
     */
    @Transactional
    @CacheEvict(value = {"allKnowledgePoints", "courseKnowledgePoints"}, allEntries = true)
    public KnowledgePoint createKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgePointMapper.insert(knowledgePoint);
        return knowledgePoint;
    }

    /**
     * Updates an existing knowledge point.
     *
     * @param knowledgePoint The knowledge point to update.
     * @return The updated knowledge point.
     */
    @Transactional
    @Caching(put = {@CachePut(value = "knowledgePoints", key = "#knowledgePoint.id")},
            evict = {
                    @CacheEvict(value = "allKnowledgePoints", allEntries = true),
                    @CacheEvict(value = "courseKnowledgePoints", key = "#knowledgePoint.courseId")
            })
    public KnowledgePoint updateKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgePointMapper.update(knowledgePoint);
        return knowledgePoint;
    }

    /**
     * Deletes a knowledge point by its ID.
     * Also removes the deleted knowledge point ID from prerequisite and related fields of other knowledge points.
     *
     * @param id The ID of the knowledge point to delete.
     */
    @Transactional
    @CacheEvict(value = {"knowledgePoints", "allKnowledgePoints", "courseKnowledgePoints"},
            allEntries = true)
    public void deleteKnowledgePoint(Long id) {
        // First, remove the knowledge point ID from prerequisite fields of other knowledge points
        knowledgePointMapper.removeFromPrerequisiteFields(id);
        
        // Then, remove the knowledge point ID from related fields of other knowledge points
        knowledgePointMapper.removeFromRelatedFields(id);
        
        // Finally, delete the knowledge point itself
        knowledgePointMapper.delete(id);
    }

    /**
     * Retrieves a knowledge point by its ID.
     *
     * @param id The ID of the knowledge point.
     * @return An Optional containing the knowledge point if found, or empty otherwise.
     */
    @Cacheable(value = "knowledgePoints", key = "#id")
    public Optional<KnowledgePoint> getKnowledgePointById(Long id) {
        return Optional.ofNullable(knowledgePointMapper.findById(id));
    }

    /**
     * Retrieves all knowledge points associated with a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of knowledge points for the given course.
     */
    @Cacheable(value = "courseKnowledgePoints", key = "#courseId")
    public List<KnowledgePoint> getKnowledgePointsByCourseId(Long courseId) {
        return knowledgePointMapper.findByCourseId(courseId);
    }

    /**
     * Retrieves all knowledge points in the system.
     *
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
     *                      from which to extract knowledge points.
     * @param courseId      The ID of the course to associate these knowledge points with.
     * @return A list of the newly created KnowledgePoint objects.
     */
    @Transactional
    public List<KnowledgePoint> generateKnowledgeGraphFromContent(String courseContent, Long courseId) {
        // Use LLMService to extract knowledge points and their relationships
        List<KnowledgePoint> extractedKPs = llmService.extractKnowledgePoints(courseContent, courseId, selfProxy.getAllKnowledgePoints());

        // Create ID mapping from temporary IDs (10001+) to real database IDs
        Map<Long, Long> idMapping = new HashMap<>();
        
        // First pass: Create knowledge points without relationships and build ID mapping
        for (KnowledgePoint kp : extractedKPs) {
            Long tempId = kp.getId();
            
            // Ensure courseId is set if not already done by LLMService
            if (kp.getCourseId() == null) {
                kp.setCourseId(courseId);
            }
            
            // Clear relationships temporarily to avoid referencing non-existent IDs
            List<Long> originalPrerequisites = kp.getPrerequisiteKnowledgePointIds();
            List<Long> originalRelated = kp.getRelatedKnowledgePointIds();
            kp.setPrerequisiteKnowledgePointIds(null);
            kp.setRelatedKnowledgePointIds(null);
            
            // Create the knowledge point (database will assign real ID)
            createKnowledgePoint(kp);
            
            // Map temporary ID to real database ID
            if (tempId != null && tempId >= 10001) {
                idMapping.put(tempId, kp.getId());
            }
            
            // Restore original relationships for second pass
            kp.setPrerequisiteKnowledgePointIds(originalPrerequisites);
            kp.setRelatedKnowledgePointIds(originalRelated);
        }
        
        // Second pass: Update relationships with mapped IDs
        for (KnowledgePoint kp : extractedKPs) {
            boolean needsUpdate = false;
            
            // Map prerequisite IDs
            if (kp.getPrerequisiteKnowledgePointIds() != null) {
                List<Long> mappedPrerequisites = kp.getPrerequisiteKnowledgePointIds().stream()
                    .map(id -> {
                        if (id >= 10001 && idMapping.containsKey(id)) {
                            return idMapping.get(id);
                        }
                        return id; // Keep existing IDs that are not temporary
                    })
                    .collect(java.util.stream.Collectors.toList());
                kp.setPrerequisiteKnowledgePointIds(mappedPrerequisites);
                needsUpdate = true;
            }
            
            // Map related IDs
            if (kp.getRelatedKnowledgePointIds() != null) {
                List<Long> mappedRelated = kp.getRelatedKnowledgePointIds().stream()
                    .map(id -> {
                        if (id >= 10001 && idMapping.containsKey(id)) {
                            return idMapping.get(id);
                        }
                        return id; // Keep existing IDs that are not temporary
                    })
                    .collect(java.util.stream.Collectors.toList());
                kp.setRelatedKnowledgePointIds(mappedRelated);
                needsUpdate = true;
            }
            
            // Update the knowledge point with mapped relationships
            if (needsUpdate) {
                updateKnowledgePoint(kp);
            }
        }

        return extractedKPs;
    }

    // You might add more methods here, such as:
    // - getKnowledgeGraphForCourse(Long courseId): to retrieve the full graph structure
    // - validateKnowledgePointRelationships(): a utility to check graph consistency
}