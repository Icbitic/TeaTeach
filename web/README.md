# TeaTeach Web Frontend

This is the web frontend for the TeaTeach Learning Management System. It's built with Vue.js 3, Element Plus UI, and communicates with the Spring Boot backend.

## Project Setup

```
npm install
```

### Compiles and hot-reloads for development

```
npm run serve
```

### Compiles and minifies for production

```
npm run build
```

### Lints and fixes files

```
npm run lint
```

## Features

- User authentication (login/logout)
- JWT token-based security
- Responsive UI with Element Plus
- Vue Router for navigation
- Vuex for state management

## Configuration

The application is configured to connect to the backend at `http://localhost:8080`. If your backend is running on a different URL, update the `API_URL` in `src/services/api.js`.

## Backend Integration

This frontend is designed to work with the TeaTeach Spring Boot backend. Make sure the backend server is running before using this application.

## Browser Support

The application is compatible with all modern browsers.