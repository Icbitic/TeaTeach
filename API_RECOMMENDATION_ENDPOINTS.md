# Resource Recommendation API Endpoints

The TeaTeach application now includes RAG-powered learning content recommendation endpoints that provide personalized suggestions based on student performance data.

## Endpoints

### 1. POST /api/llm/recommend

Get personalized learning content recommendations with custom performance data.

**Request Body:**
```json
{
  "studentId": "123",
  "courseId": 1,
  "performanceData": {
    "1": 75.0,
    "2": 60.0,
    "3": 85.0
  }
}
```

**Response:**
```json
{
  "success": true,
  "recommendations": [
    "Focus on improving Physics concepts",
    "Review Mathematics fundamentals",
    "Practice more advanced problems"
  ],
  "studentId": "123",
  "courseId": 1
}
```

### 2. GET /api/llm/recommend/{studentId}

Get personalized learning content recommendations by automatically fetching student's learning data.

**Parameters:**
- `studentId` (path): The student's ID
- `courseId` (query, optional): Course ID to filter recommendations

**Example Request:**
```
GET /api/llm/recommend/123?courseId=1
```

**Response:**
```json
{
  "success": true,
  "recommendations": [
    "Focus on improving Physics concepts",
    "Review Mathematics fundamentals",
    "Practice more advanced problems"
  ],
  "studentId": "123",
  "courseId": 1,
  "performanceData": {
    "1": 75.0,
    "2": 60.0
  }
}
```

## How RAG Works

1. **Performance Analysis**: The system analyzes student performance data to identify weak areas
2. **Resource Retrieval**: RAG searches the vector store for educational resources most relevant to the student's needs
3. **Enhanced Recommendations**: The LLM generates personalized learning recommendations using both the student's context and relevant retrieved resources
4. **Automatic Indexing**: When teachers upload new resources, they're automatically added to the vector store for future recommendations

## Testing the Endpoints

You can test these endpoints using curl:

```bash
# Test POST endpoint
curl -X POST http://localhost:8080/api/llm/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "123",
    "courseId": 1,
    "performanceData": {
      "1": 75.0,
      "2": 60.0
    }
  }'

# Test GET endpoint
curl http://localhost:8080/api/llm/recommend/123?courseId=1
```

## Features

- **RAG Integration**: Uses vector store to find relevant educational resources
- **Performance Analysis**: Automatically identifies areas where students need improvement
- **Personalized Recommendations**: Tailored suggestions based on individual student data
- **Automatic Resource Indexing**: New uploads are automatically added to the recommendation system
- **Flexible Input**: Supports both custom performance data and automatic data fetching

## Implementation Notes

- The system currently uses mock student learning data for demonstration
- Vector store is initialized with existing resources on application startup
- Recommendations are generated using the Ollama LLM with RAG enhancement
- Error handling ensures graceful degradation if recommendation generation fails