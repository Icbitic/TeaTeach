import axios from 'axios'

// Determine API URL based on environment
const API_URL = process.env.VUE_APP_API_URL || (
  process.env.NODE_ENV === 'production' 
    ? '/api'  // In production, use relative path (nginx will proxy to backend)
    : 'http://172.18.140.60:8080/api'  // In development, use direct backend URL
)

// Create axios instance with base URL
const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add request interceptor to include auth token
apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Add response interceptor to handle common errors
apiClient.interceptors.response.use(
  response => response,
  error => {
    const { status } = error.response || {}
    
    if (status === 401) {
      // Unauthorized - clear token and redirect to login
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    
    return Promise.reject(error)
  }
)

export { apiClient }

export const authService = {
  login(credentials) {
    return apiClient.post('/auth/login', credentials)
  },
  register(userData) {
    return apiClient.post('/auth/register', userData)
  },
  forgotPassword(email) {
    return apiClient.post('/auth/forgot-password', { email })
  },
  resetPassword(data) {
    return apiClient.post('/auth/reset-password', data)
  },
  logout() {
    // Clear authentication data from localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    // Return a resolved promise to maintain consistency with other methods
    return Promise.resolve()
  }
}

export default apiClient