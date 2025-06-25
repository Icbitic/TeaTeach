import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ForgotPasswordView from '../views/ForgotPasswordView.vue'
import ResetPasswordView from '../views/ResetPasswordView.vue'
import AppLayout from '../components/AppLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import StudentsView from '../views/StudentsView.vue'
import CoursesView from '../views/CoursesView.vue'
import TasksView from '../views/TasksView.vue'
import AnalyticsView from '../views/AnalyticsView.vue'
import SettingsView from '../views/SettingsView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: ForgotPasswordView
  },
  {
    path: '/reset-password',
    name: 'reset-password',
    component: ResetPasswordView
  },
  {
    path: '/',
    component: AppLayout,
    children: [
      {
        path: '',
        name: 'dashboard',
        component: DashboardView
      },
      {
        path: 'students',
        name: 'students',
        component: StudentsView
      },
      {
        path: 'courses',
        name: 'courses',
        component: CoursesView
      },
      {
        path: 'tasks',
        name: 'tasks',
        component: TasksView
      },
      {
        path: 'analytics',
        name: 'analytics',
        component: AnalyticsView
      },
      {
        path: 'settings',
        name: 'settings',
        component: SettingsView
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// Navigation guard to check for authentication
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = localStorage.getItem('user')
  const isAuthenticated = token && user
  
  // Public routes that don't require authentication
  const publicRoutes = ['/login', '/register', '/forgot-password', '/reset-password']
  const isPublicRoute = publicRoutes.includes(to.path)
  
  if (!isAuthenticated && !isPublicRoute) {
    // Redirect to login if not authenticated and trying to access protected route
    next('/login')
  } else if (isAuthenticated && isPublicRoute) {
    // Redirect to dashboard if authenticated and trying to access public route
    next('/')
  } else {
    // Allow navigation
    next()
  }
})

export default router