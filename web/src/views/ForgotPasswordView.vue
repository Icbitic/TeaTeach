<template>
  <div class="forgot-password-container">
    <div class="forgot-password-box">
      <div class="forgot-password-header">
        <h1>TeaTeach</h1>
        <p>Reset Your Password</p>
      </div>
      
      <el-form :model="resetForm" :rules="rules" ref="resetFormRef" class="forgot-password-form">
        <el-form-item prop="email">
          <el-input 
            v-model="resetForm.email" 
            placeholder="Email"
            prefix-icon="el-icon-message"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="submitForm"
            class="forgot-password-button"
          >
            Send Reset Link
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="forgot-password-footer">
        <p>Remember your password? <router-link to="/login">Back to Login</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authService } from '../services/api'

export default {
  name: 'ForgotPasswordView',
  setup() {
    const router = useRouter()
    const resetFormRef = ref(null)
    const loading = ref(false)
    const emailSent = ref(false)
    
    const resetForm = reactive({
      email: ''
    })
    
    const rules = {
      email: [
        { required: true, message: 'Please enter your email', trigger: 'blur' },
        { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
      ]
    }
    
    const submitForm = () => {
      resetFormRef.value.validate(valid => {
        if (valid) {
          loading.value = true
          authService.forgotPassword(resetForm.email)
            .then(() => {
              emailSent.value = true
              ElMessage({
                message: 'Password reset link has been sent to your email',
                type: 'success'
              })
              setTimeout(() => {
                router.push('/login')
              }, 3000)
            })
            .catch(error => {
              console.error('Password reset error:', error)
              ElMessage({
                message: error.response?.data?.message || 'Failed to send reset link. Please try again.',
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
      resetForm,
      resetFormRef,
      rules,
      loading,
      emailSent,
      submitForm
    }
  }
}
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.forgot-password-box {
  width: 350px;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.forgot-password-header {
  text-align: center;
  margin-bottom: 30px;
}

.forgot-password-header h1 {
  margin: 0;
  color: #409EFF;
  font-size: 28px;
}

.forgot-password-header p {
  margin-top: 5px;
  color: #606266;
  font-size: 16px;
}

.forgot-password-form {
  margin-bottom: 20px;
}

.forgot-password-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
}

.forgot-password-footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.forgot-password-footer a {
  color: #409EFF;
  text-decoration: none;
}

.forgot-password-footer a:hover {
  text-decoration: underline;
}
</style>