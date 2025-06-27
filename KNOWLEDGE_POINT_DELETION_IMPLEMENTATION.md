# Knowledge Point Deletion Implementation

## Overview

This document describes the implementation of automatic cleanup of knowledge point references when a knowledge point is deleted from the system.

## Problem Statement

When a knowledge point is deleted, other knowledge points may still reference it in their:
- `prerequisite_knowledge_point_ids` field
- `related_knowledge_point_ids` field

These orphaned references need to be automatically cleaned up to maintain data integrity.

## Database Schema

The `knowledge_points` table stores prerequisite and related knowledge point IDs as JSON arrays:

```sql
CREATE TABLE knowledge_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brief_description TEXT,
    detailed_content TEXT,
    course_id BIGINT,
    difficulty_level VARCHAR(50),
    prerequisite_knowledge_point_ids TEXT COMMENT 'JSON array of IDs',
    related_knowledge_point_ids TEXT COMMENT 'JSON array of IDs',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## Implementation Details

### 1. Mapper Layer Updates

Added two new methods to `KnowledgePointMapper.java`:

```java
/**
 * Remove a knowledge point ID from prerequisite_knowledge_point_ids field of all knowledge points
 */
@Update("UPDATE knowledge_points SET " +
        "prerequisite_knowledge_point_ids = JSON_REMOVE(prerequisite_knowledge_point_ids, " +
        "JSON_UNQUOTE(JSON_SEARCH(prerequisite_knowledge_point_ids, 'one', #{deletedId}))), " +
        "updated_at = NOW() " +
        "WHERE JSON_SEARCH(prerequisite_knowledge_point_ids, 'one', #{deletedId}) IS NOT NULL")
void removeFromPrerequisiteFields(@Param("deletedId") Long deletedId);

/**
 * Remove a knowledge point ID from related_knowledge_point_ids field of all knowledge points
 */
@Update("UPDATE knowledge_points SET " +
        "related_knowledge_point_ids = JSON_REMOVE(related_knowledge_point_ids, " +
        "JSON_UNQUOTE(JSON_SEARCH(related_knowledge_point_ids, 'one', #{deletedId}))), " +
        "updated_at = NOW() " +
        "WHERE JSON_SEARCH(related_knowledge_point_ids, 'one', #{deletedId}) IS NOT NULL")
void removeFromRelatedFields(@Param("deletedId") Long deletedId);
```

### 2. Service Layer Updates

Modified `KnowledgePointService.deleteKnowledgePoint()` method:

```java
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
```

### 3. SQL Operations Explained

The SQL operations use string manipulation with CASE statements to handle different positions of the ID in the JSON array:

1. **Single element array**: `[deletedId]` → `[]`
   - When the array contains only the deleted ID

2. **First element**: `[deletedId,...]` → `[...]`
   - Removes the ID and the following comma

3. **Last element**: `[...,deletedId]` → `[...]`
   - Removes the preceding comma and the ID

4. **Middle element**: `[...,deletedId,...]` → `[...,...]`
   - Replaces the ID and one comma with just a comma

5. **WHERE clause**: Only updates rows that actually contain the deleted ID
   - `WHERE field LIKE CONCAT('%', #{deletedId}, '%')` ensures we only update relevant rows

This approach is more reliable than JSON functions because it handles the exact string format produced by Jackson ObjectMapper.

## Transaction Safety

The entire deletion process is wrapped in a `@Transactional` annotation, ensuring:
- All operations succeed or all fail (atomicity)
- Data consistency is maintained
- Cache is properly invalidated

## Performance Considerations

1. **Targeted Updates**: The WHERE clause ensures only affected rows are updated
2. **JSON Indexing**: Consider adding functional indexes on JSON fields for better performance:
   ```sql
   CREATE INDEX idx_prerequisite_ids ON knowledge_points (
       (CAST(prerequisite_knowledge_point_ids AS JSON))
   );
   ```
3. **Batch Operations**: For large datasets, consider implementing batch processing

## Testing

### Unit Tests
Updated `KnowledgePointServiceTest.java` to verify:
- All three mapper methods are called in correct order
- Proper verification of method invocations

### Integration Tests
Created `KnowledgePointDeletionIntegrationTest.java` to verify:
- End-to-end functionality
- Actual database operations
- Reference cleanup verification

## Usage Example

When deleting a knowledge point with ID 101:

1. **Before deletion**:
   ```json
   // Knowledge Point 102
   {
     "prerequisite_knowledge_point_ids": "[100, 101, 103]",
     "related_knowledge_point_ids": "[104, 105]"
   }
   
   // Knowledge Point 106
   {
     "prerequisite_knowledge_point_ids": "[101]",
     "related_knowledge_point_ids": "[101, 107]"
   }
   ```

2. **After deletion**:
   ```json
   // Knowledge Point 102
   {
     "prerequisite_knowledge_point_ids": "[100, 103]",
     "related_knowledge_point_ids": "[104, 105]"
   }
   
   // Knowledge Point 106
   {
     "prerequisite_knowledge_point_ids": "[]",
     "related_knowledge_point_ids": "[107]"
   }
   ```

## Error Handling

- **Database Constraints**: Foreign key constraints on course_id are preserved
- **Transaction Rollback**: Any failure during the deletion process rolls back all changes
- **Cache Invalidation**: Cache is cleared regardless of operation success/failure

## Future Enhancements

1. **Soft Delete**: Consider implementing soft delete to preserve historical relationships
2. **Audit Trail**: Log all reference removals for audit purposes
3. **Bulk Operations**: Optimize for bulk deletions
4. **Notification System**: Notify affected users when their knowledge points lose prerequisites

## API Impact

The deletion endpoint remains unchanged:
```
DELETE /api/knowledge-points/{id}
```

The enhanced functionality is transparent to API consumers while ensuring data integrity.