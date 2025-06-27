# Student ID Conversion Guide

This guide explains how to use the new student ID conversion functionality in the TeaTeach application.

## Overview

The Student entity has two types of identifiers:
- **`id`** (Long): Database primary key, auto-generated
- **`studentId`** (String): Human-readable student identifier (e.g., "STU001", "2023001")

## Service Methods

The `StudentService` now provides the following conversion methods:

### Core Conversion Methods

```java
// Convert student ID string to database ID
Long convertStudentIdToId(String studentId)

// Convert database ID to student ID string
String convertIdToStudentId(Long id)
```

### Additional Helper Methods

```java
// Get student by student ID string
Student getStudentByStudentId(String studentId)

// Check existence by student ID
boolean existsByStudentId(String studentId)

// Check existence by database ID
boolean existsById(Long id)
```

## REST API Endpoints

The `StudentController` provides these new endpoints:

### Get Student by Student ID
```
GET /api/students/by-student-id/{studentId}
```
Example: `GET /api/students/by-student-id/STU001`

### Convert Student ID to Database ID
```
GET /api/students/convert/student-id-to-id/{studentId}
```
Example: `GET /api/students/convert/student-id-to-id/STU001`
Response: `123` (Long)

### Convert Database ID to Student ID
```
GET /api/students/convert/id-to-student-id/{id}
```
Example: `GET /api/students/convert/id-to-student-id/123`
Response: `"STU001"` (String)

### Check Existence
```
GET /api/students/exists/by-student-id/{studentId}
GET /api/students/exists/by-id/{id}
```
Response: `true` or `false`

## Usage Examples

### In Service Layer

```java
@Autowired
private StudentService studentService;

// Convert student ID to database ID for queries
String studentIdFromFrontend = "STU001";
Long databaseId = studentService.convertStudentIdToId(studentIdFromFrontend);
if (databaseId != null) {
    // Use databaseId for database operations
    // e.g., courseEnrollmentService.enrollStudent(databaseId, courseId);
}

// Convert database ID back to student ID for display
Long idFromDatabase = 123L;
String displayStudentId = studentService.convertIdToStudentId(idFromDatabase);
// displayStudentId = "STU001"
```

### In Frontend (JavaScript)

```javascript
// Get student by student ID
const response = await fetch('/api/students/by-student-id/STU001');
const student = await response.json();

// Convert student ID to database ID
const idResponse = await fetch('/api/students/convert/student-id-to-id/STU001');
const databaseId = await idResponse.json(); // 123

// Convert database ID to student ID
const studentIdResponse = await fetch('/api/students/convert/id-to-student-id/123');
const studentId = await studentIdResponse.text(); // "STU001"
```

## Error Handling

- All conversion methods return `null` if the student is not found
- REST endpoints return `404 Not Found` if the student doesn't exist
- Input validation: `null` or empty strings are handled gracefully

## Caching

- Student lookups are cached using Spring Cache
- Cache keys are differentiated between ID types:
  - Database ID: `students:123`
  - Student ID: `students:studentId:STU001`

## Best Practices

1. **Use student ID (string) for user-facing operations** (forms, URLs, displays)
2. **Use database ID (Long) for internal operations** (foreign keys, joins)
3. **Always check for null returns** when converting between ID types
4. **Cache conversion results** when performing multiple operations
5. **Validate input** before calling conversion methods

## Integration with Existing Code

This functionality is particularly useful for:
- **Frontend-Backend communication**: Frontend can use readable student IDs
- **API consistency**: Some endpoints use Long IDs, others use String IDs
- **Data import/export**: Excel files typically use readable student IDs
- **User authentication**: Login systems often use student ID strings

## Example Integration

```java
// Before: Frontend had to know database IDs
@GetMapping("/student-abilities/analyze/{studentId}")
public ResponseEntity<?> analyzeAbilities(@PathVariable Long studentId) {
    // ...
}

// After: Frontend can use readable student IDs
@GetMapping("/student-abilities/analyze/{studentId}")
public ResponseEntity<?> analyzeAbilities(@PathVariable String studentId) {
    Long databaseId = studentService.convertStudentIdToId(studentId);
    if (databaseId == null) {
        return ResponseEntity.notFound().build();
    }
    // Continue with analysis using databaseId
}
```