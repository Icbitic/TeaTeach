<template>
  <div class="register-container">
    <LanguageSwitcher class="language-switcher-top" />
    <GitHubButton />
    <div class="register-box">
      <div class="register-header">
        <h1>{{ $t('app.title') }}</h1>
        <p>{{ $t('auth.createNewAccount') }}</p>
      </div>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form">
        <!-- Basic Information -->
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            :placeholder="$t('auth.username')"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            :placeholder="$t('auth.password')"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input 
            v-model="registerForm.email" 
            :placeholder="$t('auth.email')"
            prefix-icon="el-icon-message"
          />
        </el-form-item>
        
        <el-form-item prop="name">
          <el-input 
            v-model="registerForm.name" 
            :placeholder="$t('common.fullName')"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        
        <el-form-item prop="userType">
          <el-select 
            v-model="registerForm.userType" 
            :placeholder="$t('common.selectUserType')"
            style="width: 100%"
          >
            <el-option :label="$t('common.student')" value="STUDENT" />
            <el-option :label="$t('common.teacher')" value="TEACHER" />
          </el-select>
        </el-form-item>
        
        <!-- Student-specific fields -->
        <template v-if="registerForm.userType === 'STUDENT'">
          <el-form-item prop="studentId">
            <el-input 
              v-model="registerForm.studentId" 
              :placeholder="$t('common.studentId')"
              prefix-icon="el-icon-notebook-1"
            />
          </el-form-item>
          
          <el-form-item prop="major">
            <el-input 
              v-model="registerForm.major" 
              :placeholder="$t('common.major')"
              prefix-icon="el-icon-reading"
            />
          </el-form-item>
        </template>
        
        <!-- Teacher-specific fields -->
        <template v-if="registerForm.userType === 'TEACHER'">
          <el-form-item prop="teacherId">
            <el-input 
              v-model="registerForm.teacherId" 
              :placeholder="$t('common.teacherId')"
              prefix-icon="el-icon-notebook-1"
            />
          </el-form-item>
          
          <el-form-item prop="department">
            <el-input 
              v-model="registerForm.department" 
              :placeholder="$t('common.department')"
              prefix-icon="el-icon-office-building"
            />
          </el-form-item>
        </template>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="submitForm"
            class="register-button"
          >
            {{ $t('auth.register') }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <p>{{ $t('auth.alreadyHaveAccount') }} <router-link to="/login">{{ $t('auth.login') }}</router-link></p>
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
import { authService } from '../services/api'
import GitHubButton from '@/components/GitHubButton.vue'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

export default {
  name: 'RegisterView',
  components: {
    GitHubButton,
    LanguageSwitcher
  },
  setup() {
    // We'll keep the store import for future use but disable the lint warning
    // eslint-disable-next-line no-unused-vars
    const store = useStore()
    const router = useRouter()
    const { t } = useI18n()
    const registerFormRef = ref(null)
    const loading = ref(false)
    
    const registerForm = reactive({
      username: '',
      password: '',
      email: '',
      name: '',
      userType: '',
      studentId: '',
      teacherId: '',
      major: '',
      department: ''
    })
    
    const rules = computed(() => ({
      username: [
        { required: true, message: t('auth.usernameRequired'), trigger: 'blur' },
        { min: 3, message: t('auth.usernameMinLength'), trigger: 'blur' }
      ],
      password: [
        { required: true, message: t('auth.passwordRequired'), trigger: 'blur' },
        { min: 6, message: t('auth.passwordMinLength'), trigger: 'blur' }
      ],
      email: [
        { required: true, message: t('auth.emailRequired'), trigger: 'blur' },
        { type: 'email', message: t('auth.emailInvalid'), trigger: 'blur' }
      ],
      name: [
        { required: true, message: t('auth.nameRequired'), trigger: 'blur' }
      ],
      userType: [
        { required: true, message: t('auth.userTypeRequired'), trigger: 'change' }
      ],
      studentId: [
        { required: true, message: t('auth.studentIdRequired'), trigger: 'blur', 
          validator: (rule, value, callback) => {
            if (registerForm.userType === 'STUDENT' && !value) {
              callback(new Error(t('auth.studentIdRequired')))
            } else {
              callback()
            }
          } 
        }
      ],
      major: [
        { required: true, message: t('auth.majorRequired'), trigger: 'blur',
          validator: (rule, value, callback) => {
            if (registerForm.userType === 'STUDENT' && !value) {
              callback(new Error(t('auth.majorRequired')))
            } else {
              callback()
            }
          }
        }
      ],
      teacherId: [
        { required: true, message: t('auth.teacherIdRequired'), trigger: 'blur',
          validator: (rule, value, callback) => {
            if (registerForm.userType === 'TEACHER' && !value) {
              callback(new Error(t('auth.teacherIdRequired')))
            } else {
              callback()
            }
          }
        }
      ],
      department: [
        { required: true, message: t('auth.departmentRequired'), trigger: 'blur',
          validator: (rule, value, callback) => {
            if (registerForm.userType === 'TEACHER' && !value) {
              callback(new Error(t('auth.departmentRequired')))
            } else {
              callback()
            }
          }
        }
      ]
    }))
    
    const submitForm = () => {
      registerFormRef.value.validate(valid => {
        if (valid) {
          loading.value = true
          authService.register(registerForm)
            .then(() => {
              ElMessage({
                message: t('auth.registrationSuccess'),
                type: 'success'
              })
              router.push('/login')
            })
            .catch(error => {
              console.error('Registration error:', error)
              ElMessage({
                message: error.response?.data?.message || t('auth.registrationFailed'),
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
      registerForm,
      registerFormRef,
      rules,
      loading,
      submitForm
    }
  }
}
</script>

<style scoped>
.register-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.register-box {
  width: 380px;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  margin: 0;
  color: #409EFF;
  font-size: 28px;
}

.register-header p {
  margin-top: 5px;
  color: #606266;
  font-size: 16px;
}

.register-form {
  margin-bottom: 20px;
}

.register-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
}

.register-footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.register-footer a {
  color: #409EFF;
  text-decoration: none;
}

.register-footer a:hover {
  text-decoration: underline;
}

.language-switcher-top {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 1000;
}
</style>