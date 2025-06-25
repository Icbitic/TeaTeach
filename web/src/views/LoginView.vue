<template>
  <div class="login-container">
    <GitHubButton />
    <div class="login-box">
      <div class="login-header">
        <h1>TeaTeach</h1>
        <p>Learning Management System</p>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="Username"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="Password"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="submitForm"
            class="login-button"
          >
            Login
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>Don't have an account? <router-link to="/register">Register here</router-link></p>
        <p class="forgot-password-link"><router-link to="/forgot-password">Forgot Password?</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import GitHubButton from '@/components/GitHubButton.vue'

export default {
  name: 'LoginView',
  components: {
    GitHubButton
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const loginFormRef = ref(null)
    const loading = ref(false)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const rules = {
      username: [
        { required: true, message: 'Please enter your username', trigger: 'blur' },
        { min: 3, message: 'Username must be at least 3 characters', trigger: 'blur' }
      ],
      password: [
        { required: true, message: 'Please enter your password', trigger: 'blur' },
        { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
      ]
    }
    
    const submitForm = () => {
      loginFormRef.value.validate(valid => {
        if (valid) {
          loading.value = true
          store.dispatch('login', loginForm)
            .then(() => {
              ElMessage({
                message: 'Login successful',
                type: 'success'
              })
              router.push('/')
            })
            .catch(error => {
              console.error('Login error:', error)
              ElMessage({
                message: error.response?.data?.message || 'Login failed. Please check your credentials.',
                type: 'error'
              })
            })
            .finally(() => {
              loading.value = false
            })
        }
      })
    }
    
    return {
      loginForm,
      loginFormRef,
      rules,
      loading,
      submitForm
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 350px;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  margin: 0;
  color: #409EFF;
  font-size: 28px;
}

.login-header p {
  margin-top: 5px;
  color: #606266;
  font-size: 16px;
}

.login-form {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.login-footer a {
  color: #409EFF;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}

.forgot-password-link {
  margin-top: 10px;
  font-size: 13px;
}
</style>