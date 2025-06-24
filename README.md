# TeaTeach Learning Management System

TeaTeach is a comprehensive Learning Management System built with Spring Boot for the backend and Vue.js for the frontend.

## Project Structure

The project is organized into two main components:

1. **Backend (Spring Boot)** - Located in the root directory
2. **Frontend (Vue.js)** - Located in the `/web` directory

## Backend (Spring Boot)

### Technologies Used

- Java 17
- Spring Boot 3.5.3
- Spring Security with JWT Authentication
- MyBatis for database access
- MySQL Database
- Spring AI for AI-powered features

### Running the Backend

```bash
# Run with Maven
./mvnw spring-boot:run
```

The backend server will start on http://localhost:8080

## Frontend (Vue.js)

### Technologies Used

- Vue.js 3
- Element Plus UI Framework
- Axios for API communication
- Vue Router for navigation
- Vuex for state management

### Running the Frontend

```bash
# Navigate to the web directory
cd web

# Install dependencies
npm install

# Start development server
npm run serve
```

The frontend development server will start on http://localhost:8081

## Features

- User authentication (login/register/password reset)
- Course management
- Learning task management
- Resource management
- Student progress tracking
- Knowledge point mapping
- Grade analysis

## API Documentation

API documentation is available at http://localhost:8080/swagger-ui.html when the backend is running.

## License

This project is proprietary and confidential.