<template>
  <div class="register-container">
    <GitHubButton />
    <div class="register-box">
      <div class="register-header">
        <h1>TeaTeach</h1>
        <p>Create New Account</p>
      </div>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form">
        <!-- Basic Information -->
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="Username"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="Password"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input 
            v-model="registerForm.email" 
            placeholder="Email"
            prefix-icon="el-icon-message"
          />
        </el-form-item>
        
        <el-form-item prop="name">
          <el-input 
            v-model="registerForm.name" 
            placeholder="Full Name"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        
        <el-form-item prop="userType">
          <el-select 
            v-model="registerForm.userType" 
            placeholder="Select User Type"
            style="width: 100%"
          >
            <el-option label="Student" value="STUDENT" />
            <el-option label="Teacher" value="TEACHER" />
          </el-select>
        </el-form-item>
        
        <!-- Student-specific fields -->
        <template v-if="registerForm.userType === 'STUDENT'">
          <el-form-item prop="studentId">
            <el-input 
              v-model="registerForm.studentId" 
              placeholder="Student ID"
              prefix-icon="el-icon-notebook-1"
            />
          </el-form-item>
          
          <el-form-item prop="major">
            <el-input 
              v-model="registerForm.major" 
              placeholder="Major"
              prefix-icon="el-icon-reading"
            />
          </el-form-item>
        </template>
        
        <!-- Teacher-specific fields -->
        <template v-if="registerForm.userType === 'TEACHER'">
          <el-form-item prop="teacherId">
            <el-input 
              v-model="registerForm.teacherId" 
              placeholder="Teacher ID"
              prefix-icon="el-icon-notebook-1"
            />
          </el-form-item>
          
          <el-form-item prop="department">
            <el-input 
              v-model="registerForm.department" 
              placeholder="Department"
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
            Register
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <p>Already have an account? <router-link to="/login">Login</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authService } from '../services/api'
import GitHubButton from '@/components/GitHubButton.vue'

export default {
  name: 'RegisterView',
  components: {
    GitHubButton
  },
  setup() {
    // We'll keep the store import for future use but disable the lint warning
    // eslint-disable-next-line no-unused-vars
    const store = useStore()
    const router = useRouter()
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
    
    const rules = {
      username: [
        { required: true, message: 'Please enter your username', trigger: 'blur' },
        { min: 3, message: 'Username must be at least 3 characters', trigger: 'blur' }
      ],
      password: [
        { required: true, message: 'Please enter your password', trigger: 'blur' },
        { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
      ],
      email: [
        { required: true, message: 'Please enter your email', trigger: 'blur' },
        { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
      ],
      name: [
        { required: true, message: 'Please enter your full name', trigger: 'blur' }
      ],
      userType: [
        { required: true, message: 'Please select user type', trigger: 'change' }
      ],
      studentId: [
        { required: true, message: 'Please enter your student ID', trigger: 'blur', 
          validator: (rule, value, callback) => {
            if (registerForm.userType === 'STUDENT' && !value) {
              callback(new Error('Please enter your student ID'))
            } else {
              callback()
            }
          } 
        }
      ],
      major: [
        { required: true, message: 'Please enter your major', trigger: 'blur',
          validator: (rule, value, callback) => {
            if (registerForm.userType === 'STUDENT' && !value) {
              callback(new Error('Please enter your major'))
            } else {
              callback()
            }
          }
        }
      ],
      teacherId: [
        { required: true, message: 'Please enter your teacher ID', trigger: 'blur',
          validator: (rule, value, callback) => {
            if (registerForm.userType === 'TEACHER' && !value) {
              callback(new Error('Please enter your teacher ID'))
            } else {
              callback()
            }
          }
        }
      ],
      department: [
        { required: true, message: 'Please enter your department', trigger: 'blur',
          validator: (rule, value, callback) => {
            if (registerForm.userType === 'TEACHER' && !value) {
              callback(new Error('Please enter your department'))
            } else {
              callback()
            }
          }
        }
      ]
    }
    
    const submitForm = () => {
      registerFormRef.value.validate(valid => {
        if (valid) {
          loading.value = true
          authService.register(registerForm)
            .then(() => {
              ElMessage({
                message: 'Registration successful! You can now login.',
                type: 'success'
              })
              router.push('/login')
            })
            .catch(error => {
              console.error('Registration error:', error)
              ElMessage({
                message: error.response?.data?.message || 'Registration failed. Please try again.',
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
</style>