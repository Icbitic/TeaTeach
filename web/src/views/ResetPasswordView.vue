<template>
  <div class="reset-password-container">
    <div class="reset-password-box">
      <div class="reset-password-header">
        <h1>TeaTeach</h1>
        <p>Set New Password</p>
      </div>
      
      <div v-if="invalidToken" class="token-error">
        <el-alert
          title="Invalid or Expired Token"
          type="error"
          description="The password reset link is invalid or has expired. Please request a new password reset."
          show-icon
          :closable="false"
        />
        <div class="back-link">
          <router-link to="/forgot-password">Request New Reset Link</router-link>
        </div>
      </div>
      
      <el-form v-else :model="resetForm" :rules="rules" ref="resetFormRef" class="reset-password-form">
        <el-form-item prop="password">
          <el-input 
            v-model="resetForm.password" 
            type="password" 
            placeholder="New Password"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="resetForm.confirmPassword" 
            type="password" 
            placeholder="Confirm New Password"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="submitForm"
            class="reset-password-button"
          >
            Reset Password
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="reset-password-footer">
        <p>Remember your password? <router-link to="/login">Back to Login</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authService } from '../services/api'

export default {
  name: 'ResetPasswordView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const resetFormRef = ref(null)
    const loading = ref(false)
    const invalidToken = ref(false)
    const token = ref('')
    
    const resetForm = reactive({
      password: '',
      confirmPassword: ''
    })
    
    const validatePasswordMatch = (rule, value, callback) => {
      if (value !== resetForm.password) {
        callback(new Error('Passwords do not match'))
      } else {
        callback()
      }
    }
    
    const rules = {
      password: [
        { required: true, message: 'Please enter your new password', trigger: 'blur' },
        { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: 'Please confirm your new password', trigger: 'blur' },
        { validator: validatePasswordMatch, trigger: 'blur' }
      ]
    }
    
    onMounted(() => {
      // Get token from URL query parameter
      token.value = route.query.token
      
      if (!token.value) {
        invalidToken.value = true
        ElMessage({
          message: 'Invalid password reset link',
          type: 'error'
        })
      }
    })
    
    const submitForm = () => {
      resetFormRef.value.validate(valid => {
        if (valid) {
          loading.value = true
          
          const resetData = {
            token: token.value,
            password: resetForm.password
          }
          
          authService.resetPassword(resetData)
            .then(() => {
              ElMessage({
                message: 'Password has been reset successfully',
                type: 'success'
              })
              setTimeout(() => {
                router.push('/login')
              }, 2000)
            })
            .catch(error => {
              console.error('Password reset error:', error)
              if (error.response?.status === 400) {
                invalidToken.value = true
              }
              ElMessage({
                message: error.response?.data?.message || 'Failed to reset password. Please try again.',
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
      invalidToken,
      submitForm
    }
  }
}
</script>

<style scoped>
.reset-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.reset-password-box {
  width: 350px;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.reset-password-header {
  text-align: center;
  margin-bottom: 30px;
}

.reset-password-header h1 {
  margin: 0;
  color: #409EFF;
  font-size: 28px;
}

.reset-password-header p {
  margin-top: 5px;
  color: #606266;
  font-size: 16px;
}

.reset-password-form {
  margin-bottom: 20px;
}

.reset-password-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
}

.reset-password-footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.reset-password-footer a,
.back-link a {
  color: #409EFF;
  text-decoration: none;
}

.reset-password-footer a:hover,
.back-link a:hover {
  text-decoration: underline;
}

.token-error {
  margin-bottom: 20px;
}

.back-link {
  text-align: center;
  margin-top: 20px;
}
</style>