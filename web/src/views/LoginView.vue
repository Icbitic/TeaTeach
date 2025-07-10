<template>
  <div class="login-container">
    <GitHubButton />
    <LanguageSwitcher class="language-switcher-top" />
    <div class="login-box">
      <div class="login-header">
        <h1>{{ $t('app.title') }}</h1>
        <p>{{ $t('app.subtitle') }}</p>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            :placeholder="$t('auth.username')"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            :placeholder="$t('auth.password')"
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
            {{ $t('auth.login') }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>{{ $t('auth.dontHaveAccount') }} <router-link to="/register">{{ $t('auth.registerHere') }}</router-link></p>
        <p class="forgot-password-link"><router-link to="/forgot-password">{{ $t('auth.forgotPassword') }}</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import GitHubButton from '@/components/GitHubButton.vue'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

export default {
  name: 'LoginView',
  components: {
    GitHubButton,
    LanguageSwitcher
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const { t } = useI18n()
    const loginFormRef = ref(null)
    const loading = ref(false)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const rules = computed(() => ({
      username: [
        { required: true, message: t('validation.required.username'), trigger: 'blur' },
        { min: 3, message: t('validation.minLength.username'), trigger: 'blur' }
      ],
      password: [
        { required: true, message: t('validation.required.password'), trigger: 'blur' },
        { min: 6, message: t('validation.minLength.password'), trigger: 'blur' }
      ]
    }))
    
    const submitForm = () => {
      loginFormRef.value.validate(valid => {
        if (valid) {
          loading.value = true
          store.dispatch('login', loginForm)
            .then(() => {
              ElMessage({
                message: t('auth.loginSuccessful'),
                type: 'success'
              })
              router.push('/')
            })
            .catch(error => {
              console.error(t('errors.loginError'), error)
              ElMessage({
                message: error.response?.data?.message || t('auth.loginFailed'),
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
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(2px);
  z-index: 0;
}

.language-switcher-top {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 1000;
}

.login-box {
  width: 370px;
  padding: 48px 40px 36px 40px;
  background-color: rgba(255,255,255,0.95);
  border-radius: 18px;
  box-shadow: 0 8px 32px rgba(76, 110, 245, 0.18), 0 1.5px 8px rgba(0,0,0,0.08);
  position: relative;
  z-index: 1;
  transition: box-shadow 0.3s, transform 0.3s;
}
.login-box:hover {
  box-shadow: 0 16px 48px rgba(76, 110, 245, 0.22), 0 3px 16px rgba(0,0,0,0.10);
  transform: translateY(-4px) scale(1.015);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  margin: 0;
  color: #4c6ef5;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 1px;
}

.login-header p {
  margin-top: 7px;
  color: #606266;
  font-size: 16px;
  opacity: 0.85;
}

.login-form {
  margin-bottom: 20px;
}

.el-input__wrapper {
  border-radius: 8px !important;
  box-shadow: 0 1px 4px rgba(76, 110, 245, 0.04);
  transition: box-shadow 0.2s, border-color 0.2s;
}
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px #a5b4fc, 0 1px 8px rgba(76, 110, 245, 0.10);
  border-color: #4c6ef5 !important;
}

.login-button {
  width: 100%;
  padding: 13px 0;
  font-size: 17px;
  border-radius: 8px;
  background: linear-gradient(90deg, #4c6ef5 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(76, 110, 245, 0.10);
  transition: background 0.2s, box-shadow 0.2s, transform 0.2s;
}
.login-button:hover {
  background: linear-gradient(90deg, #5f8cff 0%, #a084e8 100%);
  box-shadow: 0 4px 16px rgba(76, 110, 245, 0.18);
  transform: translateY(-2px) scale(1.01);
}

.login-footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
}

.login-footer a {
  color: #4c6ef5;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.login-footer a:hover {
  color: #764ba2;
  text-decoration: underline;
}

.forgot-password-link {
  margin-top: 10px;
  font-size: 13px;
}
</style>