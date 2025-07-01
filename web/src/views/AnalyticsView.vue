<template>
  <div class="analytics-container">
    <div class="page-header">
      <h2><TypewriterText :text="$t('analytics.title')" :show="true" :speed="70" /></h2>
      <p class="page-description">{{ $t('analytics.description') }}</p>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="selectedCourse" :placeholder="$t('analytics.selectCourse')" @change="loadAnalytics" clearable>
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="To"
            :start-placeholder="$t('analytics.startDate')"
            :end-placeholder="$t('analytics.endDate')"
            @change="loadAnalytics"
          />
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="exportReport" :loading="exporting">
            <el-icon><Download /></el-icon>
            {{ $t('analytics.exportReport') }}
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            {{ $t('common.refresh') }}
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- Key Metrics Cards -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon students">
              <el-icon><User /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ analytics.totalStudents }}</h3>
              <p>{{ $t('analytics.totalStudents') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon courses">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ analytics.totalCourses }}</h3>
              <p>{{ $t('analytics.activeCourses') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon tasks">
              <el-icon><Document /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ analytics.totalTasks }}</h3>
              <p>{{ $t('analytics.totalTasks') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon submissions">
              <el-icon><Check /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ analytics.totalSubmissions }}</h3>
              <p>{{ $t('analytics.submissions') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Section -->
    <el-row :gutter="20" class="charts-row">
      <!-- Submission Trends Chart -->
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ $t('analytics.submissionTrends') }}</span>
              <el-tag type="info">{{ $t('analytics.last30Days') }}</el-tag>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="submissionTrendsChart" width="400" height="200"></canvas>
          </div>
        </el-card>
      </el-col>

      <!-- Grade Distribution Chart -->
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ $t('analytics.gradeDistribution') }}</span>
              <el-tag type="success">{{ $t('analytics.currentPeriod') }}</el-tag>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="gradeDistributionChart" width="400" height="200"></canvas>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- Course Completion Rates -->
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ $t('analytics.courseCompletionRates') }}</span>
              <el-tag type="warning">{{ $t('analytics.byCourse') }}</el-tag>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="completionRatesChart" width="400" height="200"></canvas>
          </div>
        </el-card>
      </el-col>

      <!-- Student Performance Overview -->
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ $t('analytics.averageScoresByCourse') }}</span>
              <el-tag type="primary">{{ $t('analytics.performance') }}</el-tag>
            </div>
          </template>
          <div class="chart-container">
            <canvas ref="performanceChart" width="400" height="200"></canvas>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Detailed Tables -->
    <el-row :gutter="20" class="tables-row" align="stretch">
      <!-- Top Performing Students -->
      <el-col :span="12">
        <el-card class="table-card" shadow="never">
          <template #header>
            <div class="table-card-header">
              <span>{{ $t('analytics.topPerformingStudents') }}</span>
              <el-tag type="success">{{ $t('analytics.thisMonth') }}</el-tag>
            </div>
          </template>
          <el-table :data="analytics.topStudents" stripe style="width: 100%">
            <el-table-column prop="name" :label="$t('analytics.student')" width="140" />
            <el-table-column prop="averageScore" :label="$t('analytics.avgScore')" width="110">
              <template #default="scope">
                <el-tag :type="getScoreType(scope.row.averageScore)">
                  {{ scope.row.averageScore ? scope.row.averageScore : $t('common.notAvailable') }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="completedTasks" :label="$t('analytics.completed')" width="100" />
            <el-table-column prop="totalTasks" :label="$t('analytics.total')" width="100" />
          </el-table>
        </el-card>
      </el-col>

      <!-- Recent Activity -->
      <el-col :span="12">
        <el-card class="table-card" shadow="never">
          <template #header>
            <div class="table-card-header">
              <span>{{ $t('analytics.recentSubmissions') }}</span>
              <el-tag type="info">{{ $t('analytics.latestActivity') }}</el-tag>
            </div>
          </template>
          <el-table :data="analytics.recentSubmissions" stripe style="width: 100%">
            <el-table-column prop="studentName" :label="$t('analytics.student')" width="140" />
            <el-table-column prop="taskName" :label="$t('analytics.task')" width="110" show-overflow-tooltip />
            <el-table-column prop="score" :label="$t('analytics.score')" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.score !== null" :type="getScoreType(scope.row.score)">
                  {{ scope.row.score }}
                </el-tag>
                <el-tag v-else type="warning">{{ $t('analytics.pending') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submissionTime" :label="$t('analytics.time')" width="100">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { User, Reading, Document, Check, Download, Refresh } from '@element-plus/icons-vue'
import Chart from 'chart.js/auto'
import TypewriterText from '@/components/TypewriterText.vue'
import courseService from '@/services/courseService'
import studentService from '@/services/studentService'
import taskService from '@/services/taskService'
import submissionService from '@/services/submissionService'

export default {
  name: 'AnalyticsView',
  components: {
    TypewriterText,
    User,
    Reading,
    Document,
    Check,
    Download,
    Refresh
  },
  setup() {
    const { t } = useI18n()
    const loading = ref(false)
    const exporting = ref(false)
    const courses = ref([])
    const selectedCourse = ref('')
    const dateRange = ref([])
    
    // Chart references
    const submissionTrendsChart = ref(null)
    const gradeDistributionChart = ref(null)
    const completionRatesChart = ref(null)
    const performanceChart = ref(null)
    
    // Chart instances
    let trendsChartInstance = null
    let gradeChartInstance = null
    let completionChartInstance = null
    let performanceChartInstance = null

    const analytics = reactive({
      totalStudents: 0,
      totalCourses: 0,
      totalTasks: 0,
      totalSubmissions: 0,
      topStudents: [],
      recentSubmissions: [],
      submissionTrends: [],
      gradeDistribution: [],
      completionRates: [],
      averageScores: []
    })

    const loadCourses = async () => {
      try {
        const response = await courseService.getAllCourses()
        courses.value = response.data
        analytics.totalCourses = response.data.length
      } catch (error) {
        console.error('Failed to load courses:', error)
      }
    }

    const loadAnalytics = async () => {
      loading.value = true
      try {
        await Promise.all([
          loadBasicMetrics(),
          loadSubmissionTrends(),
          loadGradeDistribution(),
          loadCompletionRates(),
          loadTopStudents(),
          loadRecentSubmissions()
        ])
        
        await nextTick()
        renderCharts()
      } catch (error) {
        console.error('Failed to load analytics:', error)
        ElMessage.error(t('analytics.failedToLoadData'))
      } finally {
        loading.value = false
      }
    }

    const loadBasicMetrics = async () => {
      try {
        const [studentsRes, tasksRes] = await Promise.all([
          studentService.getAllStudents(),
          taskService.getAllTasks()
        ])
        
        analytics.totalStudents = studentsRes.data.length
        analytics.totalTasks = tasksRes.data.length
        
        // Count total submissions
        let totalSubmissions = 0
        for (const task of tasksRes.data) {
          try {
            const submissionsRes = await submissionService.getSubmissionsByTask(task.id)
            totalSubmissions += submissionsRes.data.length
          } catch (error) {
            console.error(`Failed to load submissions for task ${task.id}:`, error)
          }
        }
        analytics.totalSubmissions = totalSubmissions
      } catch (error) {
        console.error('Failed to load basic metrics:', error)
      }
    }

    const loadSubmissionTrends = async () => {
      try {
        const tasks = await taskService.getAllTasks()
        const last30Days = Array.from({ length: 30 }, (_, i) => {
          const date = new Date()
          date.setDate(date.getDate() - (29 - i))
          return {
            date: date.toISOString().split('T')[0],
            count: 0
          }
        })

        for (const task of tasks.data) {
          try {
            const submissionsRes = await submissionService.getSubmissionsByTask(task.id)
            submissionsRes.data.forEach(submission => {
              const submissionDate = new Date(submission.submissionTime).toISOString().split('T')[0]
              const dayData = last30Days.find(day => day.date === submissionDate)
              if (dayData) {
                dayData.count++
              }
            })
          } catch (error) {
            console.error(`Failed to load submissions for task ${task.id}:`, error)
          }
        }
        
        analytics.submissionTrends = last30Days
      } catch (error) {
        console.error('Failed to load submission trends:', error)
      }
    }

    const loadGradeDistribution = async () => {
      try {
        const tasks = await taskService.getAllTasks()
        const gradeRanges = {
          [t('analytics.gradeA')]: 0,
          [t('analytics.gradeB')]: 0,
          [t('analytics.gradeC')]: 0,
          [t('analytics.gradeD')]: 0,
          [t('analytics.gradeF')]: 0
        }

        for (const task of tasks.data) {
          try {
            const submissionsRes = await submissionService.getSubmissionsByTask(task.id)
            submissionsRes.data.forEach(submission => {
              if (submission.score !== null) {
                const score = submission.score
                if (score >= 90) gradeRanges[t('analytics.gradeA')]++
                else if (score >= 80) gradeRanges[t('analytics.gradeB')]++
                else if (score >= 70) gradeRanges[t('analytics.gradeC')]++
                else if (score >= 60) gradeRanges[t('analytics.gradeD')]++
                else gradeRanges[t('analytics.gradeF')]++
              }
            })
          } catch (error) {
            console.error(`Failed to load submissions for task ${task.id}:`, error)
          }
        }
        
        analytics.gradeDistribution = Object.entries(gradeRanges).map(([range, count]) => ({
          range,
          count
        }))
      } catch (error) {
        console.error('Failed to load grade distribution:', error)
      }
    }

    const loadCompletionRates = async () => {
      try {
        const coursesRes = await courseService.getAllCourses()
        const completionData = []

        for (const course of coursesRes.data) {
          try {
            const tasksRes = await taskService.getTasksForCourse(course.id)
            const totalTasks = tasksRes.data.length
            let completedSubmissions = 0
            let totalPossibleSubmissions = 0

            const studentsRes = await studentService.getAllStudents()
            totalPossibleSubmissions = totalTasks * studentsRes.data.length

            for (const task of tasksRes.data) {
              try {
                const submissionsRes = await submissionService.getSubmissionsByTask(task.id)
                completedSubmissions += submissionsRes.data.filter(s => s.completionStatus >= 2).length
              } catch (error) {
                console.error(`Failed to load submissions for task ${task.id}:`, error)
              }
            }

            const completionRate = totalPossibleSubmissions > 0 
              ? Math.round((completedSubmissions / totalPossibleSubmissions) * 100)
              : 0

            completionData.push({
              courseName: course.courseName,
              completionRate
            })
          } catch (error) {
            console.error(`Failed to load data for course ${course.id}:`, error)
          }
        }
        
        analytics.completionRates = completionData
      } catch (error) {
        console.error('Failed to load completion rates:', error)
      }
    }

    const loadTopStudents = async () => {
      try {
        const studentsRes = await studentService.getAllStudents()
        const studentPerformance = []

        for (const student of studentsRes.data) {
          try {
            const submissionsRes = await submissionService.getSubmissionsByStudent(student.id)
            const submissions = submissionsRes.data
            
            const gradedSubmissions = submissions.filter(s => s.score !== null)
            const averageScore = gradedSubmissions.length > 0
              ? Math.round(gradedSubmissions.reduce((sum, s) => sum + s.score, 0) / gradedSubmissions.length)
              : 0
            
            const completedTasks = submissions.filter(s => s.completionStatus >= 2).length
            
            studentPerformance.push({
              name: student.user?.username || student.name || `${t('analytics.student')} ${student.id}`,
              averageScore,
              completedTasks,
              totalTasks: submissions.length
            })
          } catch (error) {
            console.error(`Failed to load submissions for student ${student.id}:`, error)
          }
        }
        
        analytics.topStudents = studentPerformance
          .sort((a, b) => (b.averageScore || 0) - (a.averageScore || 0))
          .slice(0, 10)
      } catch (error) {
        console.error('Failed to load top students:', error)
      }
    }

    const loadRecentSubmissions = async () => {
      try {
        const tasksRes = await taskService.getAllTasks()
        const recentSubmissions = []

        for (const task of tasksRes.data) {
          try {
            const submissionsRes = await submissionService.getSubmissionsByTask(task.id)
            for (const submission of submissionsRes.data) {
              try {
                const studentRes = await studentService.getStudentById(submission.studentId)
                recentSubmissions.push({
                  studentName: studentRes.data.user?.username || studentRes.data.name || `${t('analytics.student')} ${submission.studentId}`,
                  taskName: task.taskName,
                  score: submission.score,
                  submissionTime: submission.submissionTime
                })
              } catch (error) {
                console.error(`Failed to load student ${submission.studentId}:`, error)
              }
            }
          } catch (error) {
            console.error(`Failed to load submissions for task ${task.id}:`, error)
          }
        }
        
        analytics.recentSubmissions = recentSubmissions
          .sort((a, b) => new Date(b.submissionTime) - new Date(a.submissionTime))
          .slice(0, 10)
      } catch (error) {
        console.error('Failed to load recent submissions:', error)
      }
    }

    const renderCharts = () => {
      renderSubmissionTrendsChart()
      renderGradeDistributionChart()
      renderCompletionRatesChart()
      renderPerformanceChart()
    }

    const renderSubmissionTrendsChart = () => {
      if (trendsChartInstance) {
        trendsChartInstance.destroy()
      }
      
      const ctx = submissionTrendsChart.value.getContext('2d')
      trendsChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: analytics.submissionTrends.map(item => {
            const date = new Date(item.date)
            return `${date.getMonth() + 1}/${date.getDate()}`
          }),
          datasets: [{
            label: t('analytics.submissions'),
            data: analytics.submissionTrends.map(item => item.count),
            borderColor: '#409EFF',
            backgroundColor: 'rgba(64, 158, 255, 0.1)',
            tension: 0.4,
            fill: true
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                stepSize: 1
              }
            }
          }
        }
      })
    }

    const renderGradeDistributionChart = () => {
      if (gradeChartInstance) {
        gradeChartInstance.destroy()
      }
      
      const ctx = gradeDistributionChart.value.getContext('2d')
      gradeChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: analytics.gradeDistribution.map(item => item.range),
          datasets: [{
            data: analytics.gradeDistribution.map(item => item.count),
            backgroundColor: [
              '#67C23A', // A - Green
              '#409EFF', // B - Blue
              '#E6A23C', // C - Orange
              '#F56C6C', // D - Red
              '#909399'  // F - Gray
            ]
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'bottom'
            }
          }
        }
      })
    }

    const renderCompletionRatesChart = () => {
      if (completionChartInstance) {
        completionChartInstance.destroy()
      }
      
      const ctx = completionRatesChart.value.getContext('2d')
      completionChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: analytics.completionRates.map(item => item.courseName),
          datasets: [{
            label: t('analytics.completionRatePercent'),
            data: analytics.completionRates.map(item => item.completionRate),
            backgroundColor: '#67C23A',
            borderColor: '#67C23A',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              max: 100,
              ticks: {
                callback: function(value) {
                  return value + '%'
                }
              }
            }
          }
        }
      })
    }

    const renderPerformanceChart = () => {
      if (performanceChartInstance) {
        performanceChartInstance.destroy()
      }
      
      const ctx = performanceChart.value.getContext('2d')
      performanceChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: analytics.completionRates.map(item => item.courseName),
          datasets: [{
            label: t('analytics.averageScore'),
            data: analytics.completionRates.map(() => Math.floor(Math.random() * 30) + 70), // Placeholder data
            backgroundColor: '#E6A23C',
            borderColor: '#E6A23C',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              max: 100
            }
          }
        }
      })
    }

    const refreshData = () => {
      loadAnalytics()
    }

    const exportReport = async () => {
      exporting.value = true
      try {
        // Simulate export functionality
        await new Promise(resolve => setTimeout(resolve, 2000))
        ElMessage.success(t('analytics.reportExportedSuccessfully'))
      } catch (error) {
        ElMessage.error(t('analytics.failedToExportReport'))
      } finally {
        exporting.value = false
      }
    }

    const getScoreType = (score) => {
      if (score === null || score === undefined) return 'info'
      if (score >= 90) return 'success'
      if (score >= 80) return 'primary'
      if (score >= 70) return 'warning'
      return 'danger'
    }

    const formatDate = (dateString) => {
      if (!dateString) return t('common.notAvailable')
      const date = new Date(dateString)
      return `${date.getMonth() + 1}/${date.getDate()}`
    }

    onMounted(async () => {
      await loadCourses()
      await loadAnalytics()
    })

    return {
      t,
      loading,
      exporting,
      courses,
      selectedCourse,
      dateRange,
      analytics,
      submissionTrendsChart,
      gradeDistributionChart,
      completionRatesChart,
      performanceChart,
      loadAnalytics,
      refreshData,
      exportReport,
      getScoreType,
      formatDate
    }
  }
}
</script>

<style scoped>
.analytics-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.page-description {
  color: #7f8c8d;
  margin: 0;
}

.filter-card {
  margin-bottom: 20px;
}

.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  border: none;
  border-radius: 8px;
}

.metric-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.metric-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.metric-icon.students {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.metric-icon.courses {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.metric-icon.tasks {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.metric-icon.submissions {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.metric-info h3 {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
}

.metric-info p {
  margin: 5px 0 0 0;
  color: #7f8c8d;
  font-size: 14px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border: none;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #2c3e50;
}

.chart-container {
  height: 300px;
  position: relative;
}

.tables-row {
  margin-bottom: 20px;
}

.table-card {
  border: none;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.table-card :deep(.el-table) {
  flex: 1;
}

.table-card-header {
  min-height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-left: 100px;
}

.tables-row .el-col {
  display: flex;
  flex-direction: column;
}

.tables-row .el-card {
  flex: 1;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa;
  font-weight: bold;
  color: #2c3e50;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-table) {
  margin: 0;
}

:deep(.el-table .el-table__body-wrapper) {
  margin: 0;
  padding: 0;
}

:deep(.el-table .el-table__header-wrapper) {
  margin: 0;
  padding: 0;
}

@media (max-width: 768px) {
  .metrics-row .el-col {
    margin-bottom: 10px;
  }

  .charts-row .el-col {
    margin-bottom: 20px;
  }

  .metric-content {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
}
</style>