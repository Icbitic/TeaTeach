package org.bedrock.teateach.integration;

import org.bedrock.teateach.beans.KnowledgePoint;
import org.bedrock.teateach.mappers.KnowledgePointMapper;
import org.bedrock.teateach.services.KnowledgePointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that the KnowledgePointService.deleteKnowledgePoint method
 * properly removes references from other knowledge points.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class KnowledgePointServiceIntegrationTest {

    @Autowired
    private KnowledgePointService knowledgePointService;
    
    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Test
    public void testDeleteKnowledgePointRemovesReferences() {
        // Create and insert kp1 first to get its ID
        KnowledgePoint kp1 = createTestKnowledgePoint("Basic Math", null, null);
        knowledgePointMapper.insert(kp1);
        
        // Create other knowledge points with kp1's actual ID
        KnowledgePoint kp2 = createTestKnowledgePoint("Algebra", Arrays.asList(kp1.getId()), null);
        KnowledgePoint kp3 = createTestKnowledgePoint("Geometry", null, Arrays.asList(kp1.getId()));
        knowledgePointMapper.insert(kp2);
        knowledgePointMapper.insert(kp3);
        
        // Create kp4 with both kp1 and kp2 IDs
        KnowledgePoint kp4 = createTestKnowledgePoint("Advanced Math", Arrays.asList(kp1.getId(), kp2.getId()), Arrays.asList(kp3.getId()));
        knowledgePointMapper.insert(kp4);

        // Verify initial state
        KnowledgePoint retrievedKp2 = knowledgePointMapper.findById(kp2.getId());
        KnowledgePoint retrievedKp3 = knowledgePointMapper.findById(kp3.getId());
        KnowledgePoint retrievedKp4 = knowledgePointMapper.findById(kp4.getId());

        assertTrue(retrievedKp2.getPrerequisiteKnowledgePointIds().contains(kp1.getId()));
        assertTrue(retrievedKp3.getRelatedKnowledgePointIds().contains(kp1.getId()));
        assertTrue(retrievedKp4.getPrerequisiteKnowledgePointIds().contains(kp1.getId()));
        assertTrue(retrievedKp4.getPrerequisiteKnowledgePointIds().contains(kp2.getId()));
        assertTrue(retrievedKp4.getRelatedKnowledgePointIds().contains(kp3.getId()));

        // Delete kp1 using the service method (this should trigger the cleanup)
        knowledgePointService.deleteKnowledgePoint(kp1.getId());

        // Verify that kp1 is deleted
        assertNull(knowledgePointMapper.findById(kp1.getId()));

        // Verify that references to kp1 are removed
        retrievedKp2 = knowledgePointMapper.findById(kp2.getId());
        retrievedKp3 = knowledgePointMapper.findById(kp3.getId());
        retrievedKp4 = knowledgePointMapper.findById(kp4.getId());

        // kp2 should no longer have kp1 as prerequisite
        assertFalse(retrievedKp2.getPrerequisiteKnowledgePointIds().contains(kp1.getId()));
        
        // kp3 should no longer have kp1 as related
        assertFalse(retrievedKp3.getRelatedKnowledgePointIds().contains(kp1.getId()));
        
        // kp4 should no longer have kp1 as prerequisite, but should still have kp2
        assertFalse(retrievedKp4.getPrerequisiteKnowledgePointIds().contains(kp1.getId()));
        assertTrue(retrievedKp4.getPrerequisiteKnowledgePointIds().contains(kp2.getId()));
        assertTrue(retrievedKp4.getRelatedKnowledgePointIds().contains(kp3.getId()));
    }

    private KnowledgePoint createTestKnowledgePoint(String name, List<Long> prerequisites, List<Long> related) {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setName(name);
        kp.setBriefDescription("Test description for " + name);
        kp.setDetailedContent("Detailed content for " + name);
        kp.setCourseId(1L);
        kp.setDifficultyLevel("BEGINNER");
        kp.setPrerequisiteKnowledgePointIds(prerequisites);
        kp.setRelatedKnowledgePointIds(related);
        return kp;
    }
}