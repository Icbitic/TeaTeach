<template>
  <div class="home-container">
    <el-container>
      <el-aside width="250px" class="sidebar">
        <div class="logo-container">
          <h1 class="logo">TeaTeach</h1>
        </div>
        <el-menu
            default-active="1"
            class="sidebar-menu"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
            :collapse="isCollapse"
            @select="handleMenuSelect">
          <el-menu-item index="1">
            <el-icon>
              <el-icon-house/>
            </el-icon>
            <span>Dashboard</span>
          </el-menu-item>
          <el-menu-item index="2">
            <el-icon>
              <el-icon-user/>
            </el-icon>
            <span>Students</span>
          </el-menu-item>
          <el-menu-item index="3">
            <el-icon>
              <el-icon-reading/>
            </el-icon>
            <span>Courses</span>
          </el-menu-item>
          <el-menu-item index="4">
            <el-icon>
              <el-icon-document/>
            </el-icon>
            <span>Tasks</span>
          </el-menu-item>
          <el-menu-item index="5">
            <el-icon>
              <el-icon-data-analysis/>
            </el-icon>
            <span>Analytics</span>
          </el-menu-item>
          <el-menu-item index="6">
            <el-icon>
              <el-icon-setting/>
            </el-icon>
            <span>Settings</span>
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
                <el-icon-fold/>
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
          <!-- Dashboard Section -->
          <div v-if="activeSection === '1'">
            <div class="dashboard-header">
              <h2>Teacher Dashboard</h2>
              <p class="welcome-message">Welcome back, {{ user?.username || 'Teacher' }}!</p>
            </div>

            <el-row :gutter="20" class="dashboard-stats">
              <el-col :xs="24" :sm="12" :md="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-icon students-icon">
                    <el-icon>
                      <el-icon-user/>
                    </el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ totalStudents }}</div>
                    <div class="stat-label">Total Students</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-icon courses-icon">
                    <el-icon>
                      <el-icon-reading/>
                    </el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ totalCourses }}</div>
                    <div class="stat-label">Active Courses</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-icon tasks-icon">
                    <el-icon>
                      <el-icon-document/>
                    </el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ upcomingDeadlines }}</div>
                    <div class="stat-label">Pending Tasks</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-icon grades-icon">
                    <el-icon>
                      <el-icon-data-line/>
                    </el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ activeStudents }}</div>
                    <div class="stat-label">Active Students</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="dashboard-charts">
              <el-col :xs="24" :md="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <h3>Recent Activity</h3>
                    </div>
                  </template>
                  <div class="activity-timeline">
                    <div class="timeline-item">
                      <div class="timeline-icon submission-icon"></div>
                      <div class="timeline-content">
                        <p class="timeline-title">New Submission</p>
                        <p class="timeline-desc">John Doe submitted Assignment #3</p>
                        <p class="timeline-time">2 hours ago</p>
                      </div>
                    </div>
                    <div class="timeline-item">
                      <div class="timeline-icon grade-icon"></div>
                      <div class="timeline-content">
                        <p class="timeline-title">Grades Updated</p>
                        <p class="timeline-desc">You graded 15 submissions for Course CS101</p>
                        <p class="timeline-time">Yesterday</p>
                      </div>
                    </div>
                    <div class="timeline-item">
                      <div class="timeline-icon course-icon"></div>
                      <div class="timeline-content">
                        <p class="timeline-title">New Course Material</p>
                        <p class="timeline-desc">You uploaded new materials for Data Structures</p>
                        <p class="timeline-time">2 days ago</p>
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <h3>Upcoming Deadlines</h3>
                    </div>
                  </template>
                  <div class="deadline-list">
                    <div class="deadline-item">
                      <div class="deadline-date">
                        <span class="day">15</span>
                        <span class="month">May</span>
                      </div>
                      <div class="deadline-content">
                        <p class="deadline-title">Final Project Submission</p>
                        <p class="deadline-course">Advanced Programming CS202</p>
                      </div>
                    </div>
                    <div class="deadline-item">
                      <div class="deadline-date">
                        <span class="day">18</span>
                        <span class="month">May</span>
                      </div>
                      <div class="deadline-content">
                        <p class="deadline-title">Midterm Exam</p>
                        <p class="deadline-course">Database Systems DB101</p>
                      </div>
                    </div>
                    <div class="deadline-item">
                      <div class="deadline-date">
                        <span class="day">22</span>
                        <span class="month">May</span>
                      </div>
                      <div class="deadline-content">
                        <p class="deadline-title">Research Paper</p>
                        <p class="deadline-course">Computer Networks CN301</p>
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- Student Management Section -->
          <div v-if="activeSection === '2'">
            <div class="section-header">
              <h2>Student Management</h2>
              <div class="section-actions">
                <el-button type="primary" @click="showAddStudentDialog">
                  <el-icon>
                    <el-icon-plus/>
                  </el-icon>
                  Add Student
                </el-button>
                <el-button type="success" @click="showImportDialog">
                  <el-icon>
                    <el-icon-upload/>
                  </el-icon>
                  Import
                </el-button>
                <el-button type="info" @click="exportStudents">
                  <el-icon>
                    <el-icon-download/>
                  </el-icon>
                  Export
                </el-button>
              </div>
            </div>

            <!-- Search and Filter -->
            <el-card shadow="hover" class="filter-card">
              <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="Search">
                  <el-input
                      v-model="searchForm.keyword"
                      placeholder="Name, ID or Email"
                      clearable
                      @keyup.enter="searchStudents"
                  >
                    <template #append>
                      <el-button @click="searchStudents">
                        <el-icon>
                          <el-icon-search/>
                        </el-icon>
                      </el-button>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="Major">
                  <el-select v-model="searchForm.major" placeholder="All Majors" clearable style="width: 200px">
                    <el-option label="Computer Science" value="Computer Science"/>
                    <el-option label="Engineering" value="Engineering"/>
                    <el-option label="Mathematics" value="Mathematics"/>
                    <el-option label="Physics" value="Physics"/>
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="searchStudents">Filter</el-button>
                  <el-button @click="resetSearch">Reset</el-button>
                </el-form-item>
              </el-form>
            </el-card>

            <!-- Students Table -->
            <el-card shadow="hover" class="data-card">
              <el-table
                  :data="students"
                  style="width: 100%"
                  v-loading="loading"
                  border
                  stripe
                  height="400"
                  @selection-change="handleSelectionChange"
                  table-layout="fixed"
              >
                <el-table-column type="selection" width="55" fixed="left"/>

                <el-table-column prop="studentId" label="Student ID" width="120" sortable/>
                <el-table-column prop="name" label="Name" min-width="150" sortable/>
                <el-table-column prop="email" label="Email" min-width="200" show-overflow-tooltip/>
                <el-table-column prop="major" label="Major" width="260"/>

                <el-table-column label="Date of Birth" width="120">
                  <template #default="scope">
                    {{ formatDate(scope.row.dateOfBirth) }}
                  </template>
                </el-table-column>

                <el-table-column label="Actions" fixed="right" width="120">
                  <template #default="scope">
                    <el-button
                        type="primary"
                        size="small"
                        @click="editStudent(scope.row)"
                        circle
                        plain>
                      <el-icon>
                        <el-icon-edit/>
                      </el-icon>
                    </el-button>
                    <el-button
                        type="danger"
                        size="small"
                        @click="confirmDeleteStudent(scope.row)"
                        circle
                        plain>
                      <el-icon>
                        <el-icon-delete/>
                      </el-icon>
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>


              <div class="pagination-container">
                <el-pagination
                    background
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="totalStudents"
                    :page-size="pageSize"
                    :current-page="currentPage"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                />
              </div>
            </el-card>
          </div>

          <!-- Other sections would be implemented similarly -->
          <div v-if="activeSection !== '1' && activeSection !== '2'">
            <el-empty description="This section is under development"/>
          </div>
        </el-main>

        <el-footer>
          <p>© 2023 TeaTeach Learning Management System</p>
        </el-footer>
      </el-container>
    </el-container>

    <!-- Add/Edit Student Dialog -->
    <el-dialog
        v-model="studentDialog.visible"
        :title="studentDialog.isEdit ? 'Edit Student' : 'Add New Student'"
        width="500px">
      <el-form
          :model="studentDialog.form"
          :rules="studentDialog.rules"
          ref="studentFormRef"
          label-width="120px">
        <el-form-item label="Student ID" prop="studentId">
          <el-input v-model="studentDialog.form.studentId" placeholder="Enter student ID"/>
        </el-form-item>
        <el-form-item label="Name" prop="name">
          <el-input v-model="studentDialog.form.name" placeholder="Enter full name"/>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="studentDialog.form.email" placeholder="Enter email address"/>
        </el-form-item>
        <el-form-item label="Major" prop="major">
          <el-select v-model="studentDialog.form.major" placeholder="Select major" style="width: 100%">
            <el-option label="Computer Science" value="Computer Science"/>
            <el-option label="Engineering" value="Engineering"/>
            <el-option label="Mathematics" value="Mathematics"/>
            <el-option label="Physics" value="Physics"/>
          </el-select>
        </el-form-item>
        <el-form-item label="Date of Birth" prop="dateOfBirth">
          <el-date-picker
              v-model="studentDialog.form.dateOfBirth"
              type="date"
              placeholder="Select date"
              style="width: 100%"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="studentDialog.visible = false">Cancel</el-button>
          <el-button type="primary" @click="submitStudentForm" :loading="studentDialog.loading">
            {{ studentDialog.isEdit ? 'Update' : 'Create' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Import Dialog -->
    <el-dialog
        v-model="importDialog.visible"
        title="Import Students"
        width="500px">
      <el-upload
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :file-list="importDialog.fileList">
        <el-icon class="el-icon--upload">
          <el-icon-upload-filled/>
        </el-icon>
        <div class="el-upload__text">
          Drop file here or <em>click to upload</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            Supported formats: .xlsx, .xls, .csv
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialog.visible = false">Cancel</el-button>
          <el-button type="primary" @click="importStudents" :loading="importDialog.loading">
            Import
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {computed, ref, reactive, onMounted} from 'vue'
import {useStore} from 'vuex'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import studentService from '@/services/studentService'

export default {
  name: 'HomeView',
  setup() {
    const store = useStore()
    const router = useRouter()
    const studentFormRef = ref(null)

    // Get user from store with a fallback to prevent null reference errors
    const user = computed(() => store.getters.user || {username: '', userType: '', referenceId: ''})

    // UI state
    const isCollapse = ref(false)
    const activeSection = ref('1')
    const loading = ref(false)

    // Pagination
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalStudents = ref(0)

    // Student data
    const students = ref([])
    const selectedStudents = ref([])

    // Search form
    const searchForm = reactive({
      keyword: '',
      major: ''
    })

    // Student dialog
    const studentDialog = reactive({
      visible: false,
      isEdit: false,
      loading: false,
      form: {
        id: null,
        studentId: '',
        name: '',
        email: '',
        major: '',
        dateOfBirth: ''
      },
      rules: {
        studentId: [
          {required: true, message: 'Please enter student ID', trigger: 'blur'},
          {min: 3, max: 20, message: 'Length should be 3 to 20 characters', trigger: 'blur'}
        ],
        name: [
          {required: true, message: 'Please enter name', trigger: 'blur'}
        ],
        email: [
          {required: true, message: 'Please enter email', trigger: 'blur'},
          {type: 'email', message: 'Please enter a valid email address', trigger: 'blur'}
        ],
        major: [
          {required: true, message: 'Please select major', trigger: 'change'}
        ]
      }
    })

    // Import dialog
    const importDialog = reactive({
      visible: false,
      loading: false,
      fileList: []
    })

    // Methods
    const toggleSidebar = () => {
      isCollapse.value = !isCollapse.value
    }

    const handleMenuSelect = (index) => {
      activeSection.value = index
      // Reset pagination when changing sections
      if (index === '2') {
        currentPage.value = 1
        fetchStudents()
      }
    }

    const logout = () => {
      router.push('/login')
      store.dispatch('logout')
          .then(() => {
            ElMessage({
              message: 'Logged out successfully',
              type: 'success'
            })
          })
    }

    // Student management methods
    const fetchStudents = () => {
      loading.value = true

      // Real API call to get all students using studentService
      studentService.getAllStudents()
          .then(response => {
            let studentData = response.data
            console.log('Student data from API:', studentData) // Debug log

            // Apply client-side filtering if search parameters are provided
            if (searchForm.keyword) {
              const keyword = searchForm.keyword.toLowerCase()
              studentData = studentData.filter(student =>
                  student.name.toLowerCase().includes(keyword) ||
                  student.studentId.toLowerCase().includes(keyword) ||
                  student.email.toLowerCase().includes(keyword)
              )
            }

            if (searchForm.major) {
              studentData = studentData.filter(student =>
                  student.major === searchForm.major
              )
            }

            // Apply pagination
            totalStudents.value = studentData.length
            const startIndex = (currentPage.value - 1) * pageSize.value
            const endIndex = startIndex + pageSize.value
            students.value = studentData.slice(startIndex, endIndex)

            loading.value = false
          })
          .catch(error => {
            console.error('Error fetching students:', error)
            ElMessage.error('Failed to load students')
            loading.value = false
          })
    }

    const searchStudents = () => {
      currentPage.value = 1
      fetchStudents()
    }

    const resetSearch = () => {
      searchForm.keyword = ''
      searchForm.major = ''
      searchStudents()
    }

    const handleSizeChange = (size) => {
      pageSize.value = size
      fetchStudents()
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchStudents()
    }

    const handleSelectionChange = (selection) => {
      selectedStudents.value = selection
    }

    const resetStudentForm = () => {
      studentDialog.form.id = null
      studentDialog.form.studentId = ''
      studentDialog.form.name = ''
      studentDialog.form.email = ''
      studentDialog.form.major = ''
      studentDialog.form.dateOfBirth = ''

      if (studentFormRef.value) {
        studentFormRef.value.resetFields()
      }
    }

    const showAddStudentDialog = () => {
      resetStudentForm()
      studentDialog.isEdit = false
      studentDialog.visible = true
    }

    const editStudent = (student) => {
      resetStudentForm()
      studentDialog.isEdit = true
      studentDialog.form.id = student.id
      studentDialog.form.studentId = student.studentId
      studentDialog.form.name = student.name
      studentDialog.form.email = student.email
      studentDialog.form.major = student.major
      // Convert dateOfBirth string to Date object if it's a string
      studentDialog.form.dateOfBirth = student.dateOfBirth ?
          (typeof student.dateOfBirth === 'string' ? new Date(student.dateOfBirth) : student.dateOfBirth) : null
      studentDialog.visible = true
    }

    const submitStudentForm = () => {
      if (!studentFormRef.value) return

      studentFormRef.value.validate((valid) => {
        if (valid) {
          studentDialog.loading = true

          // Prepare student data according to API schema
          const studentData = {
            studentId: studentDialog.form.studentId,
            name: studentDialog.form.name,
            email: studentDialog.form.email,
            major: studentDialog.form.major,
            // Format date properly for API
            dateOfBirth: studentDialog.form.dateOfBirth ?
                (studentDialog.form.dateOfBirth instanceof Date ?
                    studentDialog.form.dateOfBirth.toISOString().split('T')[0] :
                    studentDialog.form.dateOfBirth) :
                null
          }

          // Add id for update operations
          if (studentDialog.isEdit) {
            studentData.id = studentDialog.form.id
          }

          const apiCall = studentDialog.isEdit
              ? studentService.updateStudent(studentDialog.form.id, studentData)
              : studentService.createStudent(studentData)

          apiCall
              .then(() => {
                ElMessage({
                  message: `Student ${studentDialog.isEdit ? 'updated' : 'created'} successfully`,
                  type: 'success'
                })
                studentDialog.visible = false
                fetchStudents()
              })
              .catch(error => {
                console.error(`Error ${studentDialog.isEdit ? 'updating' : 'creating'} student:`, error)
                const errorMessage = error.response?.data?.message || `Failed to ${studentDialog.isEdit ? 'update' : 'create'} student`
                ElMessage.error(errorMessage)
              })
              .finally(() => {
                studentDialog.loading = false
              })
        }
      })
    }


    const deleteStudent = (student) => {
      ElMessageBox.confirm(
          `Are you sure you want to delete student "${student.name}"?`,
          'Confirm Delete',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning',
            confirmButtonClass: 'el-button--danger'
          }
      )
          .then(() => {
            // Use studentService to delete the student
            studentService.deleteStudent(student.id)
                .then(() => {
                  ElMessage({
                    type: 'success',
                    message: 'Student deleted successfully'
                  })
                  fetchStudents()
                })
                .catch(error => {
                  console.error('Error deleting student:', error)
                  const errorMessage = error.response?.data?.message || 'Failed to delete student'
                  ElMessage.error(errorMessage)
                })
          })
          .catch(() => {
            // User cancelled
          })
    }

    const showImportDialog = () => {
      importDialog.fileList = []
      importDialog.visible = true
    }

    const handleFileChange = (file) => {
      // Handle file change
      console.log('File selected:', file)
      importDialog.fileList = [file]
    }

    const importStudents = () => {
      if (importDialog.fileList.length === 0) {
        ElMessage.warning('Please select a file to import')
        return
      }

      importDialog.loading = true

      const file = importDialog.fileList[0].raw
      const formData = new FormData()
      formData.append('file', file)

      // Use studentService to import students
      studentService.importStudents(formData)
          .then(() => {
            ElMessage({
              message: 'Students imported successfully',
              type: 'success'
            })
            importDialog.visible = false
            fetchStudents()
          })
          .catch(error => {
            console.error('Error importing students:', error)
            const errorMessage = error.response?.data?.message || 'Failed to import students'
            ElMessage.error(errorMessage)
          })
          .finally(() => {
            importDialog.loading = false
          })
    }

    const exportStudents = () => {
      // Use studentService to export students
      studentService.exportStudents()
          .then(response => {
            // Create a blob from the response
            const blob = new Blob([response.data], {
              type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            })

            // Create a download link
            const url = window.URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = url
            link.download = `students_${new Date().toISOString().split('T')[0]}.xlsx`
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)
            window.URL.revokeObjectURL(url)

            ElMessage({
              message: 'Students exported successfully',
              type: 'success'
            })
          })
          .catch(error => {
            console.error('Error exporting students:', error)
            const errorMessage = error.response?.data?.message || 'Failed to export students'
            ElMessage.error(errorMessage)
          })
    }

    // Dashboard statistics
    const activeStudents = ref(0)
    const totalCourses = ref(0)
    const upcomingDeadlines = ref(0)

    // Get dashboard statistics from API
    const fetchDashboardStats = () => {
      // Update total students count from the API
      studentService.getAllStudents()
          .then(response => {
            totalStudents.value = response.data.length
            // For demo purposes, set active students as 80% of total
            activeStudents.value = Math.floor(totalStudents.value * 0.8)
          })
          .catch(error => {
            console.error('Error fetching student statistics:', error)
          })

      // For demo purposes, we'll keep these static
      // In a real app, you would fetch these from appropriate API endpoints
      totalCourses.value = 12
      upcomingDeadlines.value = 5
    }

    // Mock recent activity data
    const recentActivities = ref([
      {
        id: 1,
        type: 'assignment',
        title: 'Midterm Exam',
        date: '2023-10-15',
        description: 'Midterm exam has been scheduled'
      },
      {
        id: 2,
        type: 'course',
        title: 'New Course Added',
        date: '2023-10-12',
        description: 'Advanced Mathematics course has been added'
      },
      {
        id: 3,
        type: 'student',
        title: 'New Student Enrolled',
        date: '2023-10-10',
        description: 'John Doe has enrolled in Computer Science'
      },
      {
        id: 4,
        type: 'grade',
        title: 'Grades Updated',
        date: '2023-10-08',
        description: 'Physics 101 grades have been updated'
      }
    ])

    // Mock upcoming deadlines
    const deadlines = ref([
      {id: 1, title: 'Final Project Submission', course: 'Web Development', date: '2023-11-15'},
      {id: 2, title: 'Quiz 3', course: 'Database Systems', date: '2023-10-25'},
      {id: 3, title: 'Research Paper', course: 'Research Methods', date: '2023-11-05'}
    ])

    // Date formatting utility
    const formatDate = (dateValue) => {
      if (!dateValue) return ''

      try {
        // Handle different date formats
        let date
        if (typeof dateValue === 'string') {
          // Handle ISO date strings or other string formats
          date = new Date(dateValue)
        } else if (dateValue instanceof Date) {
          date = dateValue
        } else if (Array.isArray(dateValue) && dateValue.length >= 3) {
          // Handle array format [year, month, day]
          date = new Date(dateValue[0], dateValue[1] - 1, dateValue[2])
        } else {
          return ''
        }

        // Check if date is valid
        if (isNaN(date.getTime())) {
          console.warn('Invalid date:', dateValue)
          return ''
        }

        return date.toLocaleDateString()
      } catch (error) {
        console.error('Error formatting date:', error, dateValue)
        return ''
      }
    }

    // Initialize
    onMounted(() => {
      // Check if user is authenticated
      if (!store.getters.isAuthenticated) {
        router.push('/login')
        return
      }

      // Load initial data for dashboard
      if (user.value.userType !== 'TEACHER') {
        ElMessage.warning('This dashboard is designed for teachers')
      }

      // Fetch dashboard statistics
      fetchDashboardStats()
    })

    return {
      // State
      user,
      isCollapse,
      activeSection,
      loading,
      students,
      selectedStudents,
      currentPage,
      pageSize,
      totalStudents,
      activeStudents,
      totalCourses,
      upcomingDeadlines,
      recentActivities,
      deadlines,
      searchForm,
      studentDialog,
      studentFormRef,
      importDialog,

      // Methods
      toggleSidebar,
      handleMenuSelect,
      logout,
      fetchStudents,
      fetchDashboardStats,
      searchStudents,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      showAddStudentDialog,
      editStudent,
      submitStudentForm,
      deleteStudent,
      showImportDialog,
      handleFileChange,
      importStudents,
      exportStudents,
      formatDate
    }
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.logo {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.sidebar-menu {
  border-right: none;
}

/* Header */
.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: relative;
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.toggle-button {
  font-size: 20px;
  padding: 0;
  margin-right: 20px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 8px;
}

.user-dropdown .el-icon {
  margin-left: 8px;
}

/* Main content */
.el-main {
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
  height: calc(100vh - 120px);
}

/* Dashboard */
.dashboard-header {
  margin-bottom: 24px;
}

.welcome-message {
  color: #606266;
  margin-top: 8px;
}

.dashboard-stats {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 100px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 48px;
  margin-right: 16px;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.students-icon {
  background-color: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.courses-icon {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.tasks-icon {
  background-color: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
}

.grades-icon {
  background-color: rgba(245, 108, 108, 0.1);
  color: #F56C6C;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.dashboard-charts {
  margin-bottom: 24px;
}

.chart-card {
  height: 400px;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

/* Activity Timeline */
.activity-timeline {
  padding: 16px 0;
}

.timeline-item {
  display: flex;
  margin-bottom: 24px;
  position: relative;
}

.timeline-icon {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 16px;
  margin-top: 6px;
}

.submission-icon {
  background-color: #409EFF;
}

.grade-icon {
  background-color: #67C23A;
}

.course-icon {
  background-color: #E6A23C;
}

.timeline-content {
  flex: 1;
}

.timeline-title {
  font-weight: 600;
  margin: 0 0 4px 0;
}

.timeline-desc {
  color: #606266;
  margin: 0 0 4px 0;
}

.timeline-time {
  color: #909399;
  font-size: 12px;
  margin: 0;
}

/* Deadlines */
.deadline-list {
  padding: 16px 0;
}

.deadline-item {
  display: flex;
  margin-bottom: 24px;
  align-items: flex-start;
}

.deadline-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-right: 16px;
}

.deadline-date .day {
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
}

.deadline-date .month {
  font-size: 14px;
  color: #606266;
}

.deadline-content {
  flex: 1;
}

.deadline-title {
  font-weight: 600;
  margin: 0 0 4px 0;
}

.deadline-course {
  color: #606266;
  margin: 0;
}

/* Student Management */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin: 0;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 24px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.data-card {
  margin-bottom: 24px;
}

.data-card .el-table {
  overflow: hidden;
}

.data-card .el-table__body-wrapper {
  overflow-x: auto;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* Footer */
.el-footer {
  text-align: center;
  color: #909399;
  padding: 20px;
  font-size: 14px;
  background-color: #fff;
  border-top: 1px solid #e6e6e6;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .section-actions {
    width: 100%;
    justify-content: space-between;
  }

  .search-form {
    flex-direction: column;
    align-items: flex-start;
  }

  .el-form-item {
    margin-bottom: 12px;
    width: 100%;
  }
}
</style>