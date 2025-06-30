# TeaTeach 学习管理系统

TeaTeach 是一个全面的学习管理系统，后端使用 Spring Boot，前端使用 Vue.js 构建。

## 项目结构

该项目由两个主要组件组成：

1. **后端 (Spring Boot)** - 位于根目录
2. **前端 (Vue.js)** - 位于 `/web` 目录

## 后端 (Spring Boot)

### 使用的技术

- Java 17
- Spring Boot 3.5.3
- 带 JWT 身份验证的 Spring Security
- 用于数据库访问的 MyBatis
- MySQL 数据库
- 用于 AI 功能的 Spring AI

### 运行后端

```bash
# 使用 Maven 运行
./mvnw spring-boot:run
```

后端服务器将在 http://localhost:8080 启动

## 前端 (Vue.js)

### 使用的技术

- Vue.js 3
- Element Plus UI 框架
- 用于 API 通信的 Axios
- 用于导航的 Vue Router
- 用于状态管理的 Vuex

### 运行前端

```bash
# 导航到 web 目录
cd web

# 安装依赖项
npm install

# 启动开发服务器
npm run serve
```

前端开发服务器将在 http://localhost:8081 启动。

## 功能

- 用户身份验证（登录/注册/密码重置）
- 课程管理
- 学习任务管理
- 资源管理
- AI学生进度跟踪
- 基于大模型知识点映射
- AI成绩分析

## API 文档

后端运行时，API 文档可在 http://localhost:8080/swagger-ui.html 获取。
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
