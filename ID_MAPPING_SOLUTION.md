# Knowledge Point ID Mapping Solution

## Problem
When the LLM service (`LLMService.extractKnowledgePoints`) generates knowledge points, it assigns temporary IDs starting from 10001. However, when these knowledge points are saved to the database, the database assigns real auto-generated IDs. This creates a mismatch where `prerequisiteKnowledgePointIds` and `relatedKnowledgePointIds` still reference the temporary IDs (10001+) instead of the actual database IDs.

## Solution
Implemented a two-pass ID mapping system in the `KnowledgePointService.generateKnowledgeGraphFromContent` method:

### First Pass: Create Knowledge Points and Build ID Mapping
1. **Extract temporary IDs**: Store the LLM-generated temporary ID (≥10001) before database insertion
2. **Clear relationships**: Temporarily remove prerequisite and related knowledge point IDs to avoid foreign key issues
3. **Insert into database**: Save the knowledge point, allowing the database to assign a real ID
4. **Build mapping**: Create a mapping from temporary ID to real database ID
5. **Restore relationships**: Put back the original relationship lists for the second pass

### Second Pass: Update Relationships with Mapped IDs
1. **Map prerequisite IDs**: Convert any temporary IDs (≥10001) to their corresponding real database IDs
2. **Map related IDs**: Convert any temporary IDs (≥10001) to their corresponding real database IDs
3. **Preserve existing IDs**: Keep any existing IDs (<10001) unchanged
4. **Update database**: Save the knowledge points with corrected relationship IDs

## Code Changes

### Modified Files
- `src/main/java/org/bedrock/teateach/services/KnowledgePointService.java`
  - Added imports for `HashMap` and `Map`
  - Completely rewrote `generateKnowledgeGraphFromContent` method with two-pass approach

### Added Files
- `src/test/java/org/bedrock/teateach/services/KnowledgePointIdMappingTest.java`
  - Unit tests to verify ID mapping functionality
  - Tests both temporary ID mapping and preservation of existing IDs

## Key Features

1. **Automatic ID Mapping**: Temporary IDs (≥10001) are automatically mapped to real database IDs
2. **Existing ID Preservation**: Existing knowledge point IDs (<10001) are preserved unchanged
3. **Transactional Safety**: All operations are wrapped in a transaction to ensure data consistency
4. **Comprehensive Testing**: Unit tests verify both mapping scenarios and edge cases

## Usage

The solution is transparent to existing code. When calling:
```java
List<KnowledgePoint> result = knowledgePointService.generateKnowledgeGraphFromContent(courseContent, courseId);
```

The returned knowledge points will have:
- Real database IDs (not temporary ones)
- Correctly mapped prerequisite and related knowledge point IDs
- All relationships pointing to valid, existing knowledge point IDs

## Example

**Before (LLM Output):**
```json
[
  {
    "id": 10001,
    "name": "Basic Concepts",
    "relatedKnowledgePointIds": [10002]
  },
  {
    "id": 10002,
    "name": "Advanced Topics", 
    "prerequisiteKnowledgePointIds": [10001]
  }
]
```

**After (Database Storage):**
```json
[
  {
    "id": 150,
    "name": "Basic Concepts",
    "relatedKnowledgePointIds": [151]
  },
  {
    "id": 151,
    "name": "Advanced Topics",
    "prerequisiteKnowledgePointIds": [150]
  }
]
```

The temporary IDs (10001, 10002) are mapped to real database IDs (150, 151), and all relationships are updated accordingly.