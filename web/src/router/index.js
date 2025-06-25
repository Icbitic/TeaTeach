import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ForgotPasswordView from '../views/ForgotPasswordView.vue'
import ResetPasswordView from '../views/ResetPasswordView.vue'
import AppLayout from '../components/AppLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import StudentDashboardView from '../views/StudentDashboardView.vue'
import MyCoursesView from '../views/MyCoursesView.vue'
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
        component: DashboardView,
        meta: { requiresTeacher: true }
      },
      {
        path: 'student-dashboard',
        name: 'student-dashboard',
        component: StudentDashboardView,
        meta: { requiresStudent: true }
      },
      {
        path: 'my-courses',
        name: 'my-courses',
        component: MyCoursesView,
        meta: { requiresStudent: true }
      },
      {
        path: 'students',
        name: 'students',
        component: StudentsView,
        meta: { requiresTeacher: true }
      },
      {
        path: 'courses',
        name: 'courses',
        component: CoursesView,
        meta: { requiresTeacher: true }
      },
      {
        path: 'tasks',
        name: 'tasks',
        component: TasksView,
        meta: { requiresTeacher: true }
      },
      {
        path: 'analytics',
        name: 'analytics',
        component: AnalyticsView,
        meta: { requiresTeacher: true }
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

// Navigation guard to check for authentication and user type
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  const isAuthenticated = token && userStr
  
  // Public routes that don't require authentication
  const publicRoutes = ['/login', '/register', '/forgot-password', '/reset-password']
  const isPublicRoute = publicRoutes.includes(to.path)
  
  if (!isAuthenticated && !isPublicRoute) {
    // Redirect to login if not authenticated and trying to access protected route
    next('/login')
  } else if (isAuthenticated && isPublicRoute) {
    // Redirect to appropriate dashboard based on user type
    const user = JSON.parse(userStr)
    if (user.userType === 'STUDENT') {
      next('/student-dashboard')
    } else {
      next('/')
    }
  } else if (isAuthenticated) {
    // Check user type permissions
    const user = JSON.parse(userStr)
    
    // Redirect to appropriate dashboard if accessing root
    if (to.path === '/') {
      if (user.userType === 'STUDENT') {
        next('/student-dashboard')
        return
      }
    }
    
    // Check route permissions
    if (to.meta.requiresTeacher && user.userType === 'STUDENT') {
      next('/student-dashboard')
    } else if (to.meta.requiresStudent && user.userType === 'TEACHER') {
      next('/')
    } else {
      next()
    }
  } else {
    // Allow navigation
    next()
  }
})

export default router