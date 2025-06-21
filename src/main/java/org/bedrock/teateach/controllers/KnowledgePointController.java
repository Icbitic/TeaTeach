package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.services.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/knowledge-points")
public class KnowledgePointController {

    private final KnowledgePointService knowledgePointService;

    @Autowired
    public KnowledgePointController(KnowledgePointService knowledgePointService) {
        this.knowledgePointService = knowledgePointService;
    }

    @PostMapping
    public ResponseEntity<KnowledgePoint> createKnowledgePoint(@RequestBody KnowledgePoint knowledgePoint) {
        KnowledgePoint createdKp = knowledgePointService.createKnowledgePoint(knowledgePoint);
        return new ResponseEntity<>(createdKp, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<KnowledgePoint> updateKnowledgePoint(@PathVariable Long id, @RequestBody KnowledgePoint knowledgePoint) {
        if (!id.equals(knowledgePoint.getId())) {
            return ResponseEntity.badRequest().body(null); // ID mismatch
        }
        KnowledgePoint updatedKp = knowledgePointService.updateKnowledgePoint(knowledgePoint);
        if (updatedKp != null) {
            return ResponseEntity.ok(updatedKp);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKnowledgePoint(@PathVariable Long id) {
        knowledgePointService.deleteKnowledgePoint(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgePoint> getKnowledgePointById(@PathVariable Long id) {
        Optional<KnowledgePoint> knowledgePoint = knowledgePointService.getKnowledgePointById(id);
        return knowledgePoint.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<KnowledgePoint>> getKnowledgePointsByCourseId(@PathVariable Long courseId) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.getKnowledgePointsByCourseId(courseId);
        return ResponseEntity.ok(knowledgePoints);
    }

    @GetMapping
    public ResponseEntity<List<KnowledgePoint>> getAllKnowledgePoints() {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.getAllKnowledgePoints();
        return ResponseEntity.ok(knowledgePoints);
    }

    /**
     * API to trigger LLM-based knowledge graph generation.
     * An administrator or course creator would use this.
     * The @RequestBody String courseContent assumes the entire text content is sent,
     * but in a real app, it might be a reference to a resource or an ID.
     */
    @PostMapping("/generate-from-content/{courseId}")
    public ResponseEntity<List<KnowledgePoint>> generateKnowledgeGraph(
            @PathVariable Long courseId,
            @RequestBody Map<String, String> payload) { // Use a map to get content from JSON body
        String courseContent = payload.get("courseContent");
        if (courseContent == null || courseContent.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        List<KnowledgePoint> generatedKPs = knowledgePointService.generateKnowledgeGraphFromContent(courseContent, courseId);
        return new ResponseEntity<>(generatedKPs, HttpStatus.CREATED);
    }
}