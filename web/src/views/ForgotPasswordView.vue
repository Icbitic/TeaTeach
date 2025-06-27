<template>
  <div class="forgot-password-container">
    <GitHubButton />
    <div class="forgot-password-box">
      <div class="forgot-password-header">
        <h1>{{ $t('app.title') }}</h1>
        <p>{{ $t('forgotPassword.title') }}</p>
      </div>
      
      <el-form :model="resetForm" :rules="rules" ref="resetFormRef" class="forgot-password-form">
        <el-form-item prop="email">
          <el-input 
            v-model="resetForm.email" 
            :placeholder="$t('auth.email')"
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
            {{ $t('forgotPassword.sendResetLink') }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="forgot-password-footer">
        <p>{{ $t('forgotPassword.rememberPassword') }} <router-link to="/login">{{ $t('forgotPassword.backToLogin') }}</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { authService } from '../services/api'
import GitHubButton from '@/components/GitHubButton.vue'

export default {
  name: 'ForgotPasswordView',
  components: {
    GitHubButton
  },
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const resetFormRef = ref(null)
    const loading = ref(false)
    const emailSent = ref(false)
    
    const resetForm = reactive({
      email: ''
    })
    
    const rules = computed(() => ({
      email: [
        { required: true, message: t('auth.emailRequired'), trigger: 'blur' },
        { type: 'email', message: t('auth.emailInvalid'), trigger: 'blur' }
      ]
    }))
    
    const submitForm = () => {
      resetFormRef.value.validate(valid => {
        if (valid) {
          loading.value = true
          authService.forgotPassword(resetForm.email)
            .then(() => {
              emailSent.value = true
              ElMessage({
                message: t('forgotPassword.resetLinkSent'),
                type: 'success'
              })
              setTimeout(() => {
                router.push('/login')
              }, 3000)
            })
            .catch(error => {
              console.error('Password reset error:', error)
              ElMessage({
                message: error.response?.data?.message || t('forgotPassword.resetFailed'),
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