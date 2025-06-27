<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h2><TypewriterText :text="$t('dashboard.title')" :show="true" :speed="70" @typing-complete="onDashboardTitleComplete" /></h2>
      <p class="welcome-message"><TypewriterText :text="$t('dashboard.welcome', { username: user?.username || 'Teacher' })" :show="showWelcomeMessage" :speed="50" /></p>
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
            <div class="stat-label">{{ $t('dashboard.totalStudents') }}</div>
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
            <div class="stat-label">{{ $t('dashboard.activeCourses') }}</div>
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
            <div class="stat-label">{{ $t('dashboard.pendingTasks') }}</div>
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
            <div class="stat-label">{{ $t('dashboard.activeStudents') }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-charts">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <h3>{{ $t('dashboard.recentActivity') }}</h3>
            </div>
          </template>
          <div class="activity-timeline">
            <div class="timeline-item">
              <div class="timeline-icon submission-icon"></div>
              <div class="timeline-content">
                <p class="timeline-title">{{ $t('dashboard.newSubmission') }}</p>
                <p class="timeline-desc">{{ $t('dashboard.submissionDesc') }}</p>
                <p class="timeline-time">{{ $t('dashboard.hoursAgo') }}</p>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-icon grade-icon"></div>
              <div class="timeline-content">
                <p class="timeline-title">{{ $t('dashboard.gradesUpdated') }}</p>
                <p class="timeline-desc">{{ $t('dashboard.gradesDesc') }}</p>
                <p class="timeline-time">{{ $t('dashboard.yesterday') }}</p>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-icon course-icon"></div>
              <div class="timeline-content">
                <p class="timeline-title">{{ $t('dashboard.newCourseMaterial') }}</p>
                <p class="timeline-desc">{{ $t('dashboard.materialDesc') }}</p>
                <p class="timeline-time">{{ $t('dashboard.daysAgo') }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <h3>{{ $t('dashboard.upcomingDeadlines') }}</h3>
            </div>
          </template>
          <div class="deadline-list">
            <div class="deadline-item">
              <div class="deadline-date">
                <span class="day">15</span>
                <span class="month">May</span>
              </div>
              <div class="deadline-content">
                <p class="deadline-title">{{ $t('dashboard.finalProject') }}</p>
                <p class="deadline-course">{{ $t('dashboard.advancedProgramming') }}</p>
              </div>
            </div>
            <div class="deadline-item">
              <div class="deadline-date">
                <span class="day">18</span>
                <span class="month">May</span>
              </div>
              <div class="deadline-content">
                <p class="deadline-title">{{ $t('dashboard.midtermExam') }}</p>
                <p class="deadline-course">{{ $t('dashboard.databaseSystems') }}</p>
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
</template>

<script>
import { computed, ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import studentService from '@/services/studentService'
import courseService from '@/services/courseService'
import TypewriterText from '@/components/TypewriterText.vue'

export default {
  name: 'DashboardView',
  components: {
    TypewriterText
  },
  setup() {
    const store = useStore()

    // Get user from store
    const user = computed(() => store.getters.user || { username: '', userType: '', referenceId: '' })

    // Dashboard stats
    const totalStudents = ref(0)
    const totalCourses = ref(0)
    const upcomingDeadlines = ref(0)
    const activeStudents = ref(0)

    // Typewriter effect control
    const showWelcomeMessage = ref(false)

    const onDashboardTitleComplete = () => {
      showWelcomeMessage.value = true
    }

    // Load dashboard data
    const loadDashboardData = async () => {
      try {
        // Fetch real data for students and courses
        const [studentsResponse, coursesResponse] = await Promise.all([
          studentService.getAllStudents(),
          courseService.getAllCourses()
        ])
        
        totalStudents.value = studentsResponse.data?.length || 0
        totalCourses.value = coursesResponse.data?.length || 0
        
        // Calculate active students (assuming students with recent activity)
        activeStudents.value = Math.floor(totalStudents.value * 0.85) // 85% active rate
        
        // Mock data for other stats
        upcomingDeadlines.value = 12
      } catch (error) {
        console.error('Error loading dashboard data:', error)
        ElMessage.error('Failed to load dashboard statistics')
        // Fallback values
        totalStudents.value = 0
        totalCourses.value = 0
        upcomingDeadlines.value = 12
        activeStudents.value = 0
      }
    }

    onMounted(() => {
      loadDashboardData()
    })

    return {
      user,
      totalStudents,
      totalCourses,
      upcomingDeadlines,
      activeStudents,
      showWelcomeMessage,
      onDashboardTitleComplete
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.dashboard-header {
  margin-bottom: 30px;
}

.dashboard-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.welcome-message {
  color: #909399;
  font-size: 16px;
  margin: 0;
}

.dashboard-stats {
  margin-bottom: 30px;
}

.stat-card {
  border: none;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-card .el-card__body {
  padding: 20px;
  display: flex;
  align-items: center;
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

.students-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.courses-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.tasks-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.grades-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.dashboard-charts {
  margin-bottom: 30px;
}

.chart-card {
  border: none;
  border-radius: 8px;
  height: 400px;
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.activity-timeline {
  padding: 20px 0;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.timeline-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.timeline-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 15px;
  flex-shrink: 0;
}

.submission-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.grade-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.course-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.timeline-content {
  flex: 1;
}

.timeline-title {
  margin: 0 0 5px 0;
  font-weight: 600;
  color: #303133;
}

.timeline-desc {
  margin: 0 0 5px 0;
  color: #606266;
  font-size: 14px;
}

.timeline-time {
  margin: 0;
  color: #909399;
  font-size: 12px;
}

.deadline-list {
  padding: 20px 0;
}

.deadline-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.deadline-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.deadline-item:last-child {
  margin-bottom: 0;
}

.deadline-date {
  text-align: center;
  margin-right: 20px;
  min-width: 60px;
}

.deadline-date .day {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #409EFF;
  line-height: 1;
}

.deadline-date .month {
  display: block;
  font-size: 12px;
  color: #909399;
  text-transform: uppercase;
}

.deadline-content {
  flex: 1;
}

.deadline-title {
  margin: 0 0 5px 0;
  font-weight: 600;
  color: #303133;
}

.deadline-course {
  margin: 0;
  color: #606266;
  font-size: 14px;
}
</style>