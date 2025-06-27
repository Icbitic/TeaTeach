<template>
  <div class="video-heatmap-container">
    <div class="page-header">
      <h2><TypewriterText :text="'Video Heatmap Visualization'" :show="true" :speed="70" /></h2>
      <p class="page-description">Analyze student video viewing patterns and engagement</p>
    </div>

    <!-- Filter Controls -->
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="selectedResource" placeholder="Select Video Resource" @change="loadHeatmapData" clearable>
            <el-option
              v-for="resource in resources"
              :key="resource.id"
              :label="resource.resourceName"
              :value="resource.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedStudent" placeholder="Select Student (Optional)" @change="loadStudentProgress" clearable>
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="student.name"
              :value="student.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            Refresh Data
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button @click="exportHeatmapData" :loading="exporting">
            <el-icon><Download /></el-icon>
            Export Data
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- Resource Info Card -->
    <el-card v-if="selectedResourceInfo" class="resource-info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>Resource Information</span>
          <el-tag type="primary">{{ selectedResourceInfo.fileType?.toUpperCase() || 'Video' }}</el-tag>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">Title:</span>
            <span class="info-value">{{ selectedResourceInfo.resourceName }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">Total Views:</span>
            <span class="info-value">{{ totalViews }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- Heatmap Visualization -->
    <el-card v-if="heatmapData.length > 0" class="heatmap-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>Video Engagement Heatmap</span>
          <el-tag type="success">Interactive</el-tag>
        </div>
      </template>
      <div class="heatmap-container">
        <div class="heatmap-legend">
          <span class="legend-label">Engagement Level:</span>
          <div class="legend-gradient">
            <span class="legend-text">Low</span>
            <div class="gradient-bar"></div>
            <span class="legend-text">High</span>
          </div>
        </div>
        <div class="heatmap-chart">
          <canvas ref="heatmapCanvas" @mousemove="onHeatmapHover" @click="onHeatmapClick"></canvas>
        </div>
        <div class="time-axis">
          <div v-for="(mark, index) in timeMarks" :key="index" class="time-mark" :style="{ left: mark.position + '%' }">
            {{ mark.time }}
          </div>
        </div>
      </div>
    </el-card>

    <!-- Student Progress Card -->
    <el-card v-if="selectedStudent && studentProgress" class="progress-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>Individual Student Progress</span>
          <el-tag type="info">{{ getStudentName(selectedStudent) }}</el-tag>
        </div>
      </template>
      <div class="progress-content">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="progress-metric">
              <div class="metric-value">{{ studentProgress.watchPercentage }}%</div>
              <div class="metric-label">Watch Percentage</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="progress-metric">
              <div class="metric-value">{{ studentProgress.totalPlayCount }}</div>
              <div class="metric-label">Total Play Count</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="progress-metric">
              <div class="metric-value">{{ studentProgress.maxPlayCount || 'N/A' }}</div>
              <div class="metric-label">Max Replays</div>
            </div>
          </el-col>
        </el-row>
        <div class="progress-bar-container">
          <el-progress 
            :percentage="studentProgress.watchPercentage" 
            :color="getProgressColor(studentProgress.watchPercentage)"
            :stroke-width="20"
            :show-text="false"
          />
        </div>
      </div>
    </el-card>

    <!-- Statistics Cards -->
    <el-row :gutter="20" class="stats-row" v-if="heatmapData.length > 0">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon engagement">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ averageEngagement }}%</h3>
              <p>Avg Engagement</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon hotspot">
              <el-icon><Location /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ formatTime(peakEngagementTime) }}</h3>
              <p>Peak Engagement</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon dropoff">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ formatTime(dropoffPoint) }}</h3>
              <p>Drop-off Point</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon completion">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ completionRate }}%</h3>
              <p>Completion Rate</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Empty State -->
    <el-card v-if="!loading && heatmapData.length === 0" class="empty-card" shadow="never">
      <el-empty description="No heatmap data available">
        <template #image>
          <el-icon size="60" color="#409EFF">
            <VideoCamera />
          </el-icon>
        </template>
        <template #description>
          <p>Select a video resource to view engagement heatmap</p>
        </template>
      </el-empty>
    </el-card>

    <!-- Tooltip -->
    <div v-if="tooltip.visible" class="heatmap-tooltip" :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }">
      <div class="tooltip-content">
        <div class="tooltip-time">{{ tooltip.time }}</div>
        <div class="tooltip-engagement">Engagement: {{ tooltip.engagement }}%</div>
        <div class="tooltip-views">Views: {{ tooltip.views }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download, TrendCharts, Location, Warning, CircleCheck, VideoCamera } from '@element-plus/icons-vue'
import TypewriterText from '@/components/TypewriterText.vue'
import { playbackService } from '@/services/playbackService'
import { resourceService } from '@/services/resourceService'
import { studentService } from '@/services/studentService'

export default {
  name: 'VideoHeatmapView',
  components: {
    TypewriterText,
    Refresh,
    Download,
    TrendCharts,
    Location,
    Warning,
    CircleCheck,
    VideoCamera
  },
  setup() {
    const loading = ref(false)
    const exporting = ref(false)
    const resources = ref([])
    const students = ref([])
    const selectedResource = ref('')
    const selectedStudent = ref('')
    const heatmapData = ref([])
    const totalViews = ref(0)
    const studentProgress = ref(null)
    const heatmapCanvas = ref(null)
    
    const selectedResourceInfo = computed(() => {
      return resources.value.find(r => r.id === selectedResource.value)
    })

    const tooltip = reactive({
      visible: false,
      x: 0,
      y: 0,
      time: '',
      engagement: 0,
      views: 0
    })

    // Computed property to get effective heatmap data (trimmed to last non-zero value)
    const effectiveHeatmapData = computed(() => {
      if (heatmapData.value.length === 0) return []
      
      // Find the last non-zero index
      let lastNonZeroIndex = heatmapData.value.length - 1
      for (let i = heatmapData.value.length - 1; i >= 0; i--) {
        if (heatmapData.value[i] > 0) {
          lastNonZeroIndex = i
          break
        }
      }
      
      // Return data up to the last non-zero value (inclusive)
      return heatmapData.value.slice(0, lastNonZeroIndex + 1)
    })

    const timeMarks = computed(() => {
      if (effectiveHeatmapData.value.length === 0) return []
      const duration = effectiveHeatmapData.value.length
      const marks = []
      const intervals = Math.min(10, Math.max(5, Math.floor(duration / 60))) // Show 5-10 marks
      
      for (let i = 0; i <= intervals; i++) {
        const position = (i / intervals) * 100
        const timeInSeconds = Math.floor((i / intervals) * duration)
        marks.push({
          position,
          time: formatTime(timeInSeconds)
        })
      }
      return marks
    })

    const averageEngagement = computed(() => {
      if (effectiveHeatmapData.value.length === 0) return 0
      const sum = effectiveHeatmapData.value.reduce((acc, val) => acc + val, 0)
      const max = Math.max(...effectiveHeatmapData.value)
      return max > 0 ? Math.round((sum / effectiveHeatmapData.value.length / max) * 100) : 0
    })

    const peakEngagementTime = computed(() => {
      if (effectiveHeatmapData.value.length === 0) return 0
      const maxValue = Math.max(...effectiveHeatmapData.value)
      const peakIndex = effectiveHeatmapData.value.indexOf(maxValue)
      return peakIndex
    })

    const dropoffPoint = computed(() => {
      if (effectiveHeatmapData.value.length === 0) return 0
      // Find the point where engagement drops significantly
      let maxDrop = 0
      let dropIndex = 0
      for (let i = 1; i < effectiveHeatmapData.value.length; i++) {
        const drop = effectiveHeatmapData.value[i - 1] - effectiveHeatmapData.value[i]
        if (drop > maxDrop) {
          maxDrop = drop
          dropIndex = i
        }
      }
      return dropIndex
    })

    const completionRate = computed(() => {
      if (effectiveHeatmapData.value.length === 0 || totalViews.value === 0) return 0
      const lastQuarter = Math.floor(effectiveHeatmapData.value.length * 0.75)
      const completions = effectiveHeatmapData.value.slice(lastQuarter).reduce((acc, val) => acc + val, 0)
      const avgCompletions = completions / (effectiveHeatmapData.value.length - lastQuarter)
      return Math.max(100, Math.round((avgCompletions / totalViews.value) * 100))
    })

    const loadResources = async () => {
      try {
        const response = await resourceService.getAllResources()
        // Filter for video resources based on fileType
        resources.value = response.data.filter(r => 
          r.fileType === 'mp4' || 
          r.fileType === 'avi' || 
          r.fileType === 'mov' || 
          r.fileType === 'wmv' || 
          r.fileType === 'webm'
        )
        console.log('Loaded video resources:', resources.value)
      } catch (error) {
        console.error('Failed to load resources:', error)
        ElMessage.error('Failed to load video resources')
      }
    }

    const loadStudents = async () => {
      try {
        const response = await studentService.getAllStudents()
        students.value = response.data.map(student => ({
          id: student.id,
          name: student.user?.username || student.name || `Student ${student.id}`
        }))
      } catch (error) {
        console.error('Failed to load students:', error)
        ElMessage.error('Failed to load students')
      }
    }

    const loadHeatmapData = async () => {
      if (!selectedResource.value) {
        heatmapData.value = []
        totalViews.value = 0
        return
      }

      loading.value = true
      try {
        const response = await playbackService.getHeatmap(selectedResource.value)
        heatmapData.value = response.data.heatmapData || []
        totalViews.value = response.data.totalViews || 0
        
        await nextTick()
        renderHeatmap()
      } catch (error) {
        console.error('Failed to load heatmap data:', error)
        ElMessage.error('Failed to load heatmap data')
        heatmapData.value = []
        totalViews.value = 0
      } finally {
        loading.value = false
      }
    }

    const loadStudentProgress = async () => {
      if (!selectedStudent.value || !selectedResource.value) {
        studentProgress.value = null
        return
      }

      try {
        const response = await playbackService.getWatchProgress(selectedStudent.value, selectedResource.value)
        studentProgress.value = response.data
      } catch (error) {
        console.error('Failed to load student progress:', error)
        studentProgress.value = null
      }
    }

    const renderHeatmap = () => {
      if (!heatmapCanvas.value || effectiveHeatmapData.value.length === 0) return

      const canvas = heatmapCanvas.value
      const ctx = canvas.getContext('2d')
      const container = canvas.parentElement
      
      // Set canvas size
      canvas.width = container.clientWidth
      canvas.height = 100
      
      const width = canvas.width
      const height = canvas.height
      const barWidth = width / effectiveHeatmapData.value.length
      const maxValue = Math.max(...effectiveHeatmapData.value)
      
      // Clear canvas
      ctx.clearRect(0, 0, width, height)
      
      // Draw heatmap bars
      effectiveHeatmapData.value.forEach((value, index) => {
        const intensity = maxValue > 0 ? value / maxValue : 0
        const barHeight = height * 0.8 // Leave some space for padding
        const x = index * barWidth
        const y = height - barHeight
        
        // Create gradient color based on intensity
        const hue = intensity * 120 // From red (0) to green (120)
        const saturation = 70
        const lightness = 40 + (intensity * 20)
        
        ctx.fillStyle = `hsl(${hue}, ${saturation}%, ${lightness}%)`
        ctx.fillRect(x, y, barWidth, barHeight)
      })
    }

    const onHeatmapHover = (event) => {
      if (effectiveHeatmapData.value.length === 0) return
      
      const canvas = heatmapCanvas.value
      const rect = canvas.getBoundingClientRect()
      const x = event.clientX - rect.left
      const barWidth = canvas.width / effectiveHeatmapData.value.length
      const index = Math.floor(x / barWidth)
      
      if (index >= 0 && index < effectiveHeatmapData.value.length) {
        const maxValue = Math.max(...effectiveHeatmapData.value)
        const engagement = maxValue > 0 ? Math.round((effectiveHeatmapData.value[index] / maxValue) * 100) : 0
        
        tooltip.visible = true
        tooltip.x = event.clientX + 10
        tooltip.y = event.clientY - 10
        tooltip.time = formatTime(index)
        tooltip.engagement = engagement
        tooltip.views = effectiveHeatmapData.value[index]
      }
    }

    const onHeatmapClick = (event) => {
      // Future: Could implement seeking to specific time
      console.log('Clicked on heatmap at:', event)
    }

    const refreshData = async () => {
      await Promise.all([
        loadResources(),
        loadStudents(),
        loadHeatmapData()
      ])
    }

    const exportHeatmapData = async () => {
      if (effectiveHeatmapData.value.length === 0) {
        ElMessage.warning('No data to export')
        return
      }

      exporting.value = true
      try {
        // Create CSV data
        const csvData = effectiveHeatmapData.value.map((value, index) => {
          return `${formatTime(index)},${value}`
        }).join('\n')
        
        const header = 'Time,Engagement Count\n'
        const blob = new Blob([header + csvData], { type: 'text/csv' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `heatmap_${selectedResource.value}_${new Date().toISOString().split('T')[0]}.csv`
        link.click()
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('Heatmap data exported successfully!')
      } catch (error) {
        ElMessage.error('Failed to export data')
      } finally {
        exporting.value = false
      }
    }

    const formatTime = (seconds) => {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins}:${secs.toString().padStart(2, '0')}`
    }

    const formatDuration = (seconds) => {
      const hours = Math.floor(seconds / 3600)
      const mins = Math.floor((seconds % 3600) / 60)
      const secs = seconds % 60
      
      if (hours > 0) {
        return `${hours}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
      }
      return `${mins}:${secs.toString().padStart(2, '0')}`
    }

    const getStudentName = (studentId) => {
      const student = students.value.find(s => s.id === studentId)
      return student ? student.name : `Student ${studentId}`
    }

    const getProgressColor = (percentage) => {
      if (percentage >= 80) return '#67C23A'
      if (percentage >= 60) return '#E6A23C'
      if (percentage >= 40) return '#F56C6C'
      return '#909399'
    }

    // Hide tooltip when mouse leaves the component
    const hideTooltip = () => {
      tooltip.visible = false
    }

    onMounted(async () => {
      await Promise.all([
        loadResources(),
        loadStudents()
      ])
      
      // Add event listener to hide tooltip
      document.addEventListener('mouseleave', hideTooltip)
    })

    return {
      loading,
      exporting,
      resources,
      students,
      selectedResource,
      selectedStudent,
      selectedResourceInfo,
      heatmapData,
      totalViews,
      studentProgress,
      heatmapCanvas,
      tooltip,
      timeMarks,
      averageEngagement,
      peakEngagementTime,
      dropoffPoint,
      completionRate,
      loadHeatmapData,
      loadStudentProgress,
      refreshData,
      exportHeatmapData,
      onHeatmapHover,
      onHeatmapClick,
      formatTime,
      formatDuration,
      getStudentName,
      getProgressColor
    }
  }
}
</script>

<style scoped>
.video-heatmap-container {
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

.resource-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #2c3e50;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-label {
  font-weight: 600;
  color: #7f8c8d;
  font-size: 14px;
}

.info-value {
  color: #2c3e50;
  font-size: 16px;
}

.heatmap-card {
  margin-bottom: 20px;
}

.heatmap-container {
  position: relative;
}

.heatmap-legend {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.legend-label {
  font-weight: 600;
  color: #2c3e50;
}

.legend-gradient {
  display: flex;
  align-items: center;
  gap: 10px;
}

.gradient-bar {
  width: 100px;
  height: 20px;
  background: linear-gradient(to right, hsl(0, 70%, 50%), hsl(60, 70%, 50%), hsl(120, 70%, 50%));
  border-radius: 10px;
  border: 1px solid #ddd;
}

.legend-text {
  font-size: 12px;
  color: #7f8c8d;
}

.heatmap-chart {
  position: relative;
  height: 100px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.heatmap-chart canvas {
  width: 100%;
  height: 100%;
  cursor: crosshair;
}

.time-axis {
  position: relative;
  height: 30px;
  margin-top: 10px;
}

.time-mark {
  position: absolute;
  font-size: 12px;
  color: #7f8c8d;
  transform: translateX(-50%);
}

.progress-card {
  margin-bottom: 20px;
}

.progress-content {
  text-align: center;
}

.progress-metric {
  text-align: center;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 5px;
}

.metric-label {
  color: #7f8c8d;
  font-size: 14px;
}

.progress-bar-container {
  margin-top: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.engagement {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.hotspot {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.dropoff {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.stat-icon.completion {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info h3 {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-info p {
  margin: 5px 0 0 0;
  color: #7f8c8d;
  font-size: 14px;
}

.empty-card {
  text-align: center;
  padding: 40px 20px;
}

.heatmap-tooltip {
  position: fixed;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  pointer-events: none;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.tooltip-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tooltip-time {
  font-weight: bold;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}

@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 10px;
  }
  
  .stat-content {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
  
  .legend-gradient {
    flex-direction: column;
    gap: 5px;
  }
}
</style>