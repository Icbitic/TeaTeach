<template>
  <div class="home-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>TeaTeach</h1>
          <div class="user-info">
            <span v-if="user">Welcome, {{ user.username }}</span>
            <el-button type="text" @click="logout">Logout</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>Welcome to TeaTeach</h2>
            </div>
          </template>
          <div class="card-content">
            <p>You have successfully logged in to the TeaTeach Learning Management System.</p>
            <p v-if="user">User Type: {{ user.userType }}</p>
            <p v-if="user">Reference ID: {{ user.referenceId }}</p>
          </div>
        </el-card>
      </el-main>
      
      <el-footer>
        <p>© 2023 TeaTeach Learning Management System</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'HomeView',
  setup() {
    const store = useStore()
    const router = useRouter()
    
    // Get user from store with a fallback to prevent null reference errors
    const user = computed(() => store.getters.user || { username: '', userType: '', referenceId: '' })
    
    const logout = () => {
      // First navigate to login page, then logout
      router.push('/login')
      // Then dispatch logout action
      store.dispatch('logout')
        .then(() => {
          ElMessage({
            message: 'Logged out successfully',
            type: 'success'
          })
        })
    }
    
    return {
      user,
      logout
    }
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.el-header {
  background-color: #409EFF;
  color: white;
  line-height: 60px;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: left;
  line-height: 1.6;
}

.el-footer {
  text-align: center;
  color: #909399;
  padding: 20px;
  font-size: 14px;
}
</style>