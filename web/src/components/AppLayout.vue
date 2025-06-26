<template>
  <div class="app-layout">
    <el-container class="layout-container">
      <el-aside :width="isCollapse ? '64px' : '250px'" class="sidebar">
        <div class="logo-container">
          <h1 class="logo" v-show="!isCollapse">
            <TypewriterText :text="'TeaTeach'" :show="!isCollapse" :speed="100" />
          </h1>
          <h1 class="logo-collapsed" v-show="isCollapse">
            <TypewriterText :text="'T'" :show="isCollapse" :speed="130" />
          </h1>
        </div>
        <el-menu
          :default-active="currentMenuKey"
          class="sidebar-menu"
          :collapse="isCollapse"
          background-color="#2c3e50"
          text-color="#ecf0f1"
          active-text-color="#3498db"
          @select="handleMenuSelect"
        >
          <!-- Teacher Menu -->
          <template v-if="user.userType === 'TEACHER'">
            <el-menu-item index="dashboard">
              <el-icon><el-icon-data-board /></el-icon>
              <span><TypewriterText :text="'Dashboard'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="students">
              <el-icon><el-icon-user /></el-icon>
              <span><TypewriterText :text="'Students'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="courses">
              <el-icon><el-icon-reading /></el-icon>
              <span><TypewriterText :text="'Courses'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="tasks">
              <el-icon><el-icon-document /></el-icon>
              <span><TypewriterText :text="'Tasks'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="analytics">
              <el-icon><el-icon-data-analysis /></el-icon>
              <span><TypewriterText :text="'Analytics'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="video-heatmap">
              <el-icon><i class="el-icon-video-camera"></i></el-icon>
              <span><TypewriterText :text="'Video Heatmap'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="submissions">
              <el-icon><el-icon-files /></el-icon>
              <span><TypewriterText :text="'Submissions'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
          </template>
          
          <!-- Student Menu -->
          <template v-else-if="user.userType === 'STUDENT'">
            <el-menu-item index="student-dashboard">
              <el-icon><el-icon-data-board /></el-icon>
              <span><TypewriterText :text="'Dashboard'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="my-courses">
              <el-icon><el-icon-reading /></el-icon>
              <span><TypewriterText :text="'My Courses'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="course-materials">
              <el-icon><el-icon-folder /></el-icon>
              <span><TypewriterText :text="'Course Materials'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="my-tasks">
              <el-icon><el-icon-document /></el-icon>
              <span><TypewriterText :text="'My Tasks'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
            <el-menu-item index="my-grades">
              <el-icon><el-icon-data-analysis /></el-icon>
              <span><TypewriterText :text="'My Grades'" :show="!isCollapse" :speed="50" /></span>
            </el-menu-item>
          </template>
          
          <!-- Common Menu Items -->
          <el-menu-item index="settings">
            <el-icon><el-icon-setting /></el-icon>
            <span><TypewriterText :text="'Settings'" :show="!isCollapse" :speed="50" /></span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-content">
            <el-button
                type="text"
                @click="toggleSidebar"
                class="toggle-button">
              <el-icon>
                <el-icon-fold v-if="!isCollapse"/>
                <el-icon-expand v-else/>
              </el-icon>
            </el-button>
            <div class="user-info">
              <el-dropdown trigger="click">
                <span class="user-dropdown">
                  <span v-if="user">{{ user.username }}</span>
                  <el-icon><el-icon-arrow-down/></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>Profile</el-dropdown-item>
                    <el-dropdown-item divided @click="logout">Logout</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main>
          <router-view />
        </el-main>

        <el-footer>
          <p>© 2025 TeaTeach Learning Management System</p>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import TypewriterText from './TypewriterText.vue'

export default {
  name: 'AppLayout',
  components: {
    TypewriterText
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()

    // Get user from store with a fallback to prevent null reference errors
    const user = computed(() => store.getters.user || { username: '', userType: '', referenceId: '' })

    // UI state
    const isCollapse = ref(false)

    // Methods
    const toggleSidebar = () => {
      isCollapse.value = !isCollapse.value
    }

    const logout = async () => {
      try {
        await store.dispatch('logout')
        ElMessage.success('Logged out successfully')
        router.push('/login')
      } catch (error) {
        console.error('Logout error:', error)
        ElMessage.error('Logout failed')
      }
    }

    const handleMenuSelect = (key) => {
      const routeMap = {
        'dashboard': '/',
        'student-dashboard': '/student-dashboard',
        'students': '/students', 
        'courses': '/courses',
        'my-courses': '/my-courses',
        'course-materials': '/course-materials',
        'tasks': '/tasks',
        'my-tasks': '/my-tasks',
        'analytics': '/analytics',
        'video-heatmap': '/video-heatmap',
        'submissions': '/submissions',
        'my-grades': '/student-dashboard', // For now, redirect to student dashboard
        'settings': '/settings'
      }
      
      if (routeMap[key]) {
        router.push(routeMap[key])
      }
    }

    const currentMenuKey = computed(() => {
      const routeToKey = {
        '/': 'dashboard',
        '/student-dashboard': 'student-dashboard',
        '/my-courses': 'my-courses',
        '/course-materials': 'course-materials',
        '/students': 'students',
        '/courses': 'courses', 
        '/tasks': 'tasks',
        '/my-tasks': 'my-tasks',
        '/analytics': 'analytics',
        '/video-heatmap': 'video-heatmap',
        '/submissions': 'submissions',
        '/settings': 'settings'
      }
      return routeToKey[route.path] || (user.value.userType === 'STUDENT' ? 'student-dashboard' : 'dashboard')
    })

    return {
      user,
      isCollapse,
      toggleSidebar,
      logout,
      handleMenuSelect,
      currentMenuKey
    }
  }
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
  overflow: hidden;
}

.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #2c3e50;
  color: white;
  transition: width 0.3s ease;
  overflow: hidden;
}

.logo-container {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #34495e;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  color: #3498db;
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  transition: opacity 0.3s ease;
}

.logo-collapsed {
  color: #3498db;
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.sidebar-menu {
  border: none;
  background-color: #2c3e50 !important;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  border-radius: 0;
}

.sidebar-menu .el-menu-item:hover {
  background-color: #34495e !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #3498db !important;
  color: #ffffff !important;
}

.sidebar-menu.el-menu--collapse {
  width: 64px;
}

.sidebar-menu.el-menu--collapse .el-menu-item {
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.toggle-button {
  font-size: 18px;
  color: #606266;
  border: none;
  background: none;
  cursor: pointer;
  padding: 8px;
}

.toggle-button:hover {
  background-color: #f5f7fa;
  border-radius: 4px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.user-dropdown span {
  margin-right: 8px;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

.el-footer {
  background-color: #fff;
  border-top: 1px solid #e6e6e6;
  text-align: center;
  color: #999;
  padding: 10px;
}
</style>