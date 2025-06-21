package org.bedrock.teateach.beans;

import lombok.Data;

import java.util.List;

@Data
public class AbilityPoint {
    private Long id;
    private String name;
    private String description;
    private Long courseId; // Associated with a course
    private List<Long> prerequisiteAbilityPointIds;
    private List<Long> relatedAbilityPointIds;
    // Could include target learning outcomes
}
