<template>
  <div class="my-courses-container">
    <div class="courses-header">
      <h2><TypewriterText :text="$t('myCourses.title')" :show="true" :speed="70" @typing-complete="onTitleComplete" /></h2>
      <p class="subtitle"><TypewriterText :text="$t('myCourses.subtitle')" :show="showSubtitle" :speed="50" /></p>
    </div>

    <!-- Course Stats -->>
    <el-row :gutter="20" class="course-stats">
      <el-col :xs="24" :sm="8" :md="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon enrolled-icon">
            <el-icon>
              <el-icon-reading/>
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ enrolledCourses.length }}</div>
            <div class="stat-label">{{ $t('myCourses.enrolledCourses') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" :md="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon progress-icon">
            <el-icon>
              <el-icon-trophy/>
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ completedCourses }}</div>
            <div class="stat-label">{{ $t('myCourses.completed') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" :md="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon active-icon">
            <el-icon>
              <el-icon-clock/>
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ activeCourses }}</div>
            <div class="stat-label">{{ $t('myCourses.inProgress') }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Search and Filter -->
    <el-row :gutter="20" class="filter-section">
      <el-col :span="24">
        <el-card shadow="hover">
          <el-form :inline="true" class="search-form">
            <el-form-item :label="$t('common.search')">
              <el-input
                v-model="searchKeyword"
                :placeholder="$t('myCourses.searchCoursesPlaceholder')"
                clearable
                @input="filterCourses"
                style="width: 300px"
              >
                <template #prefix>
                  <el-icon><el-icon-search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item :label="$t('common.status')">
              <el-select
                v-model="statusFilter"
                :placeholder="$t('myCourses.allStatus')"
                clearable
                @change="filterCourses"
                style="width: 150px"
              >
                <el-option :label="$t('common.all')" value="" />
                <el-option :label="$t('myCourses.active')" value="ACTIVE" />
                <el-option :label="$t('myCourses.completed')" value="COMPLETED" />
                <el-option :label="$t('myCourses.paused')" value="PAUSED" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- Courses Grid -->
    <el-row :gutter="20" class="courses-grid">
      <el-col 
        v-for="course in filteredCourses" 
        :key="course.id" 
        :xs="24" 
        :sm="12" 
        :md="8" 
        :lg="6"
        class="course-col"
      >
        <el-card shadow="hover" class="course-card" @click="viewCourseDetails(course)">
          <div class="course-header">
            <div class="course-code">{{ course.courseCode }}</div>
            <el-tag 
              :type="getStatusTagType(course.enrollmentStatus)" 
              size="small"
            >
              {{ getEnrollmentStatusLabel(course.enrollmentStatus) }}
            </el-tag>
          </div>
          
          <div class="course-title">
            <h3>{{ course.courseName }}</h3>
          </div>
          
          <div class="course-info">
            <div class="info-item">
              <el-icon><el-icon-user /></el-icon>
              <span>{{ course.instructor }}</span>
            </div>
            <div class="info-item">
              <el-icon><el-icon-star /></el-icon>
              <span>{{ $t('myCourses.creditsCount', { count: course.credits }) }}</span>
            </div>
            <div class="info-item">
              <el-icon><el-icon-clock /></el-icon>
              <span>{{ $t('myCourses.hoursCount', { count: course.hours }) }}</span>
            </div>
          </div>
          
          <div class="course-progress">
            <div class="progress-label">
              <span>{{ $t('myCourses.progress') }}</span>
              <span>{{ getProgressPercentage(course) }}%</span>
            </div>
            <el-progress 
              :percentage="getProgressPercentage(course)" 
              :stroke-width="8"
              :show-text="false"
            />
          </div>
          
          <div class="course-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click.stop="continueLearning(course)"
            >
              {{ $t('myCourses.continueLearning') }}
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click.stop="viewCourseDetails(course)"
            >
              {{ $t('common.details') }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Empty State -->
    <div v-if="filteredCourses.length === 0 && !loading" class="empty-state">
      <el-empty :description="$t('myCourses.noCoursesFound')">
        <el-button type="primary" @click="exploreAllCourses">{{ $t('myCourses.exploreCourses') }}</el-button>
      </el-empty>
    </div>

    <!-- Course Details Dialog -->
    <el-dialog
      v-model="courseDetailVisible"
      :title="selectedCourse?.courseName"
      width="60%"
      :before-close="handleDetailClose"
    >
      <div v-if="selectedCourse" class="course-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="$t('courses.courseCode')">
            {{ selectedCourse.courseCode }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('courses.instructor')">
            {{ selectedCourse.instructor }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('courses.credits')">
            {{ selectedCourse.credits }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('courses.hours')">
            {{ selectedCourse.hours }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('common.status')">
            <el-tag :type="getStatusTagType(selectedCourse.enrollmentStatus)">
              {{ getEnrollmentStatusLabel(selectedCourse.enrollmentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('myCourses.progress')">
            <el-progress 
              :percentage="getProgressPercentage(selectedCourse)" 
              :stroke-width="6"
            />
          </el-descriptions-item>
          <el-descriptions-item :label="$t('common.description')" :span="2">
            {{ selectedCourse.description }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="course-tasks" v-if="courseTasks.length > 0">
          <h4>{{ $t('myCourses.courseTasks') }}</h4>
          <el-table :data="courseTasks" stripe>
            <el-table-column prop="taskName" :label="$t('tasks.taskName')" />
            <el-table-column prop="taskType" :label="$t('tasks.type')" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getTaskTypeLabel(row.taskType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deadline" :label="$t('tasks.deadline')" width="160">
              <template #default="{ row }">
                {{ formatDeadline(row.deadline) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.status')" width="100">
              <template #default="{ row }">
                <el-tag :type="getTaskStatusColor(row)" size="small">
                  {{ getTaskStatus(row) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="courseDetailVisible = false">{{ $t('common.close') }}</el-button>
          <el-button type="primary" @click="continueLearning(selectedCourse)">{{ $t('myCourses.continueLearning') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { courseEnrollmentService } from '@/services/courseEnrollmentService'
import { taskService } from '@/services/taskService'
import TypewriterText from '@/components/TypewriterText.vue'

export default {
  name: 'MyCoursesView',
  components: {
    TypewriterText
  },
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const loading = ref(false)
    const showSubtitle = ref(false)
    const enrolledCourses = ref([])
    const filteredCourses = ref([])
    const searchKeyword = ref('')
    const statusFilter = ref('')
    const courseDetailVisible = ref(false)
    const selectedCourse = ref(null)
    const courseTasks = ref([])
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    
    // Computed properties
    const completedCourses = computed(() => {
      return enrolledCourses.value.filter(course => 
        course.enrollmentStatus === 'COMPLETED'
      ).length
    })
    
    const activeCourses = computed(() => {
      return enrolledCourses.value.filter(course => 
        course.enrollmentStatus === 'ACTIVE' || !course.enrollmentStatus
      ).length
    })
    
    // Methods
    const onTitleComplete = () => {
      showSubtitle.value = true
    }
    
    const fetchEnrolledCourses = async () => {
      try {
        loading.value = true
        const response = await courseEnrollmentService.getCoursesByStudent(user.referenceId)
        enrolledCourses.value = response.data || []
        filteredCourses.value = enrolledCourses.value
      } catch (error) {
        console.error(t('errors.errorFetchingEnrolledCourses'), error)
        ElMessage.error(t('errors.failedToLoadCourses'))
      } finally {
        loading.value = false
      }
    }
    
    const filterCourses = () => {
      let filtered = enrolledCourses.value
      
      // Filter by search keyword
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        filtered = filtered.filter(course => 
          course.courseName.toLowerCase().includes(keyword) ||
          course.courseCode.toLowerCase().includes(keyword) ||
          course.instructor.toLowerCase().includes(keyword)
        )
      }
      
      // Filter by status
      if (statusFilter.value) {
        filtered = filtered.filter(course => 
          course.enrollmentStatus === statusFilter.value
        )
      }
      
      filteredCourses.value = filtered
    }
    
    const getStatusTagType = (status) => {
      switch (status) {
        case 'COMPLETED': return 'success'
        case 'PAUSED': return 'warning'
        case 'ACTIVE': return 'primary'
        default: return 'primary'
      }
    }
    
    const getEnrollmentStatusLabel = (status) => {
      const statusKey = status || 'ACTIVE'
      const labels = {
        'ACTIVE': t('myCourses.status.active'),
        'COMPLETED': t('myCourses.status.completed'),
        'PAUSED': t('myCourses.status.paused')
      }
      return labels[statusKey] || labels['ACTIVE']
    }
    
    const getProgressPercentage = (course) => {
      // This would typically come from the backend
      // For now, return a mock percentage based on course status
      if (course.enrollmentStatus === 'COMPLETED') return 100
      if (course.enrollmentStatus === 'PAUSED') return Math.floor(Math.random() * 50) + 25
      return Math.floor(Math.random() * 80) + 10
    }
    
    const viewCourseDetails = async (course) => {
      selectedCourse.value = course
      courseDetailVisible.value = true
      
      // Fetch course tasks
      try {
        const response = await taskService.getTasksForCourse(course.id)
        courseTasks.value = response.data || []
      } catch (error) {
        console.error(t('errors.errorFetchingCourseTasks'), error)
        ElMessage.error(t('errors.failedToLoadCourseTasks'))
      }
    }
    
    const continueLearning = (course) => {
      // Navigate to course learning page or dashboard with course filter
      router.push({
        name: 'student-dashboard',
        query: { courseId: course.id }
      })
    }
    
    const exploreAllCourses = () => {
      // This would navigate to a course catalog page
      ElMessage.info(t('info.courseCatalogComingSoon'))
    }
    
    const handleDetailClose = () => {
      courseDetailVisible.value = false
      selectedCourse.value = null
      courseTasks.value = []
    }
    
    const getTaskTypeLabel = (taskType) => {
      const labels = {
        'READING_MATERIAL': t('tasks.types.reading'),
        'VIDEO_WATCH': t('tasks.types.video'),
        'CHAPTER_HOMEWORK': t('tasks.types.homework'),
        'REPORT_UPLOAD': t('tasks.types.report'),
        'EXAM_QUIZ': t('tasks.types.quiz'),
        'PRACTICE_PROJECT': t('tasks.types.project')
      }
      return labels[taskType] || taskType
    }
    
    const formatDeadline = (deadline) => {
      if (!deadline) return t('tasks.noDeadline')
      return new Date(deadline).toLocaleDateString()
    }
    
    const getTaskStatus = (task) => {
      // Mock task status logic
      if (task.submissionDate) return t('tasks.status.completed')
      if (new Date(task.deadline) < new Date()) return t('tasks.status.overdue')
      return t('tasks.status.pending')
    }
    
    const getTaskStatusColor = (task) => {
      if (task.submissionDate) return 'success'
      if (new Date(task.deadline) < new Date()) return 'danger'
      return 'warning'
    }
    
    onMounted(() => {
      fetchEnrolledCourses()
    })
    
    return {
      loading,
      showSubtitle,
      enrolledCourses,
      filteredCourses,
      searchKeyword,
      statusFilter,
      courseDetailVisible,
      selectedCourse,
      courseTasks,
      completedCourses,
      activeCourses,
      onTitleComplete,
      filterCourses,
      getStatusTagType,
      getEnrollmentStatusLabel,
      getProgressPercentage,
      viewCourseDetails,
      continueLearning,
      exploreAllCourses,
      handleDetailClose,
      getTaskTypeLabel,
      formatDeadline,
      getTaskStatus,
      getTaskStatusColor
    }
  }
}
</script>

<style scoped>
.my-courses-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.courses-header {
  margin-bottom: 30px;
  text-align: center;
}

.courses-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 2.5em;
  font-weight: 300;
}

.subtitle {
  color: #7f8c8d;
  font-size: 1.1em;
  margin: 0;
}

.course-stats {
  margin-bottom: 30px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.stat-card .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.enrolled-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.progress-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.active-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 2.5em;
  font-weight: bold;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  color: #7f8c8d;
  font-size: 0.9em;
  margin-top: 5px;
}

.filter-section {
  margin-bottom: 30px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 20px;
}

.courses-grid {
  margin-bottom: 30px;
}

.course-col {
  margin-bottom: 20px;
}

.course-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.course-code {
  font-weight: bold;
  color: #3498db;
  font-size: 0.9em;
}

.course-title h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.2em;
  line-height: 1.4;
}

.course-info {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #7f8c8d;
  font-size: 0.9em;
}

.info-item .el-icon {
  margin-right: 8px;
  color: #bdc3c7;
}

.course-progress {
  margin-bottom: 20px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.9em;
  color: #7f8c8d;
}

.course-actions {
  display: flex;
  gap: 10px;
}

.course-actions .el-button {
  flex: 1;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.course-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.course-tasks {
  margin-top: 30px;
}

.course-tasks h4 {
  color: #2c3e50;
  margin-bottom: 15px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .my-courses-container {
    padding: 15px;
  }
  
  .courses-header h2 {
    font-size: 2em;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .course-actions {
    flex-direction: column;
  }
}
</style>