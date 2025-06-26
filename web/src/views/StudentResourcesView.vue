<template>
  <div class="student-resources-container">
    <div class="section-header">
      <h2>Course Materials</h2>
      <div class="section-actions">
        <el-select
          v-model="selectedCourse"
          placeholder="Filter by Course"
          clearable
          style="width: 200px; margin-right: 10px"
          @change="handleCourseFilter"
        >
          <el-option
            v-for="course in enrolledCourses"
            :key="course.id"
            :label="course.courseName"
            :value="course.id"
          />
        </el-select>
        <el-select
          v-model="selectedTaskType"
          placeholder="Filter by Type"
          clearable
          style="width: 150px"
          @change="handleTypeFilter"
        >
          <el-option
            v-for="type in taskTypes"
            :key="type.value"
            :label="type.label"
            :value="type.value"
          />
        </el-select>
      </div>
    </div>

    <!-- Course Tasks Grid -->
    <div v-loading="loading" class="tasks-grid">
      <el-card
        v-for="task in filteredTasks"
        :key="task.id"
        shadow="hover"
        class="task-card"
      >
        <template #header>
          <div class="task-header">
            <div class="task-title">
              <el-icon class="task-icon">
                <Document v-if="task.taskType === 'READING_MATERIAL'"/>
                <VideoPlay v-else-if="task.taskType === 'VIDEO_WATCH'"/>
                <Edit v-else-if="task.taskType === 'CHAPTER_HOMEWORK'"/>
                <Files v-else-if="task.taskType === 'REPORT_UPLOAD'"/>
                <Trophy v-else-if="task.taskType === 'EXAM_QUIZ'"/>
                <Tools v-else-if="task.taskType === 'PRACTICE_PROJECT'"/>
                <Document v-else/>
              </el-icon>
              <span>{{ task.taskName }}</span>
            </div>
            <el-tag :type="getTaskTypeColor(task.taskType)" size="small">
              {{ getTaskTypeLabel(task.taskType) }}
            </el-tag>
          </div>
        </template>
        
        <div class="task-content">
          <div class="task-info">
            <p class="course-name">
              <el-icon><School /></el-icon>
              {{ task.courseName }}
            </p>
            <p class="task-description">{{ task.taskDescription }}</p>
            <div class="task-meta">
              <div class="deadline">
                <el-icon><Clock /></el-icon>
                <span :class="getDeadlineClass(task.deadline)">
                  {{ formatDeadline(task.deadline) }}
                </span>
              </div>
              <div class="resources-count">
                <el-icon><Files /></el-icon>
                <span>{{ getResourceCount(task.id) }} files</span>
              </div>
            </div>
          </div>
          
          <!-- Progress Bar for Video Tasks -->
          <div v-if="task.taskType === 'VIDEO_WATCH' && taskProgress[task.id]" class="progress-section">
            <div class="progress-info">
              <span class="progress-label">Watch Progress</span>
              <span class="progress-value">{{ taskProgress[task.id].watchPercentage || 0 }}%</span>
            </div>
            <el-progress
              :percentage="taskProgress[task.id].watchPercentage || 0"
              :color="getProgressColor(taskProgress[task.id].watchPercentage || 0)"
              :stroke-width="8"
            />
          </div>
        </div>
        
        <template #footer>
          <div class="task-actions">
            <el-button
              type="primary"
              @click="viewTaskResources(task)"
              :loading="loadingTaskResources[task.id]"
            >
              <el-icon><View /></el-icon>
              View Materials
            </el-button>
            <el-button
              v-if="task.taskType === 'VIDEO_WATCH'"
              type="success"
              @click="continueWatching(task)"
              :disabled="!hasVideoResources(task.id)"
            >
              <el-icon><VideoPlay /></el-icon>
              {{ taskProgress[task.id]?.watchPercentage > 0 ? 'Continue' : 'Start' }} Watching
            </el-button>
          </div>
        </template>
      </el-card>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && filteredTasks.length === 0" class="empty-state">
      <el-empty description="No course materials found">
        <el-button type="primary" @click="clearFilters">Clear Filters</el-button>
      </el-empty>
    </div>

    <!-- Task Resources Dialog -->
    <el-dialog
      v-model="resourcesDialogVisible"
      :title="`${selectedTask?.taskName || ''} - Course Materials`"
      width="1000px"
      @close="closeResourcesDialog"
    >
      <div class="resources-dialog-content">
        <div v-if="taskResources.length === 0" class="no-resources">
          <el-empty description="No materials available for this task" />
        </div>
        <div v-else class="resources-grid">
          <div
            v-for="resource in taskResources"
            :key="resource.id"
            class="resource-item"
            @click="viewResource(resource)"
          >
            <div class="resource-preview">
              <el-icon class="resource-icon">
                <VideoPlay v-if="isVideoFile(resource.resourceName)"/>
                <Picture v-else-if="isImageFile(resource.resourceName)"/>
                <Document v-else-if="isPdfFile(resource.resourceName)"/>
                <Files v-else/>
              </el-icon>
            </div>
            <div class="resource-info">
              <h4 class="resource-name">{{ resource.resourceName }}</h4>
              <p class="resource-size">{{ formatFileSize(resource.fileSize) }}</p>
              <div class="resource-type">{{ getFileTypeDescription(resource.resourceName) }}</div>
            </div>
            <div class="resource-actions">
              <el-button type="primary" size="small">
                <el-icon><View /></el-icon>
                View
              </el-button>
              <el-button type="success" size="small" @click.stop="downloadResource(resource.id)">
                <el-icon><Download /></el-icon>
                Download
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Media Viewer Dialog -->
    <el-dialog
      v-model="mediaViewerVisible"
      :title="`Viewing: ${selectedResource?.resourceName || ''}`"
      width="90%"
      top="5vh"
      @close="closeMediaViewer"
    >
      <div class="media-viewer-container">
        <MediaViewer
          v-if="selectedResource"
          :resource="selectedResource"
          :student-id="currentStudentId"
          :show-progress="true"
          :auto-track="true"
          @progress-update="handleProgressUpdate"
          @playback-complete="handlePlaybackComplete"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document,
  VideoPlay,
  Edit,
  Files,
  Trophy,
  Tools,
  Clock,
  View,
  Download,
  Picture,
  School
} from '@element-plus/icons-vue'
import taskService from '@/services/taskService'
import { courseEnrollmentService } from '@/services/courseEnrollmentService'
import { resourceService } from '@/services/resourceService'
import { playbackService } from '@/services/playbackService'
import MediaViewer from '@/components/MediaViewer.vue'

export default {
  name: 'StudentResourcesView',
  components: {
    Document,
    VideoPlay,
    Edit,
    Files,
    Trophy,
    Tools,
    Clock,
    View,
    Download,
    Picture,
    School,
    MediaViewer
  },
  setup() {
    // Reactive data
    const loading = ref(false)
    const selectedCourse = ref(null)
    const selectedTaskType = ref(null)
    const loadingTaskResources = ref({})
    const taskResourcesMap = ref({})
    const taskProgress = ref({})
    
    // Dialog states
    const resourcesDialogVisible = ref(false)
    const mediaViewerVisible = ref(false)
    const selectedTask = ref(null)
    const selectedResource = ref(null)
    const taskResources = ref([])
    
    const enrolledCourses = ref([])
    const courseTasks = ref([])
    
    // Get current student ID from localStorage
    const currentStudentId = computed(() => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      return user.referenceId || null
    })
    
    // Task types configuration
    const taskTypes = [
      { value: 'CHAPTER_HOMEWORK', label: 'Chapter Homework' },
      { value: 'EXAM_QUIZ', label: 'Exam/Quiz' },
      { value: 'VIDEO_WATCH', label: 'Video Watch' },
      { value: 'READING_MATERIAL', label: 'Reading Material' },
      { value: 'REPORT_UPLOAD', label: 'Report Upload' },
      { value: 'PRACTICE_PROJECT', label: 'Practice Project' }
    ]
    
    // Computed properties
    const filteredTasks = computed(() => {
      let filtered = courseTasks.value
      
      if (selectedCourse.value) {
        filtered = filtered.filter(task => task.courseId === selectedCourse.value)
      }
      
      if (selectedTaskType.value) {
        filtered = filtered.filter(task => task.taskType === selectedTaskType.value)
      }
      
      return filtered
    })
    
    // Methods
    const loadEnrolledCourses = async () => {
      try {
        if (!currentStudentId.value) {
          ElMessage.error('Student ID not found. Please log in again.')
          return
        }
        
        const response = await courseEnrollmentService.getCoursesByStudent(currentStudentId.value)
        enrolledCourses.value = response.data
      } catch (error) {
        console.error('Error loading enrolled courses:', error)
        ElMessage.error('Failed to load enrolled courses')
      }
    }
    
    const loadCourseTasks = async () => {
      try {
        loading.value = true
        const allTasks = []
        
        for (const course of enrolledCourses.value) {
          try {
            const response = await taskService.getTasksForCourse(course.id)
            const tasksWithCourse = response.data.map(task => ({
              ...task,
              courseName: course.courseName,
              courseId: course.id
            }))
            allTasks.push(...tasksWithCourse)
          } catch (error) {
            console.error(`Error loading tasks for course ${course.id}:`, error)
          }
        }
        
        courseTasks.value = allTasks
        
        // Load resources for all tasks
        for (const task of allTasks) {
          await loadTaskResources(task.id)
          if (task.taskType === 'VIDEO_WATCH') {
            await loadTaskProgress(task.id)
          }
        }
      } catch (error) {
        console.error('Error loading course tasks:', error)
        ElMessage.error('Failed to load course tasks')
      } finally {
        loading.value = false
      }
    }
    
    const loadTaskResources = async (taskId) => {
      try {
        loadingTaskResources.value[taskId] = true
        const response = await resourceService.getTaskResources(taskId)
        taskResourcesMap.value[taskId] = response.data
        return response.data
      } catch (error) {
        console.error('Failed to load task resources:', error)
        return []
      } finally {
        loadingTaskResources.value[taskId] = false
      }
    }
    
    const loadTaskProgress = async (taskId) => {
      try {
        const resources = taskResourcesMap.value[taskId] || []
        const videoResources = resources.filter(r => isVideoFile(r.resourceName))
        
        if (videoResources.length > 0 && currentStudentId.value) {
          // Load progress for the first video resource
          const response = await playbackService.getWatchProgress(
            currentStudentId.value,
            videoResources[0].id
          )
          taskProgress.value[taskId] = {
            watchPercentage: Math.round(response.data.watchPercentage || 0)
          }
        }
      } catch (error) {
        console.error('Failed to load task progress:', error)
      }
    }
    
    const viewTaskResources = async (task) => {
      selectedTask.value = task
      taskResources.value = await loadTaskResources(task.id)
      resourcesDialogVisible.value = true
    }
    
    const closeResourcesDialog = () => {
      resourcesDialogVisible.value = false
      selectedTask.value = null
      taskResources.value = []
    }
    
    const viewResource = (resource) => {
      selectedResource.value = resource
      mediaViewerVisible.value = true
      closeResourcesDialog()
    }
    
    const closeMediaViewer = () => {
      mediaViewerVisible.value = false
      selectedResource.value = null
    }
    
    const continueWatching = async (task) => {
      const resources = taskResourcesMap.value[task.id] || []
      const videoResource = resources.find(r => isVideoFile(r.resourceName))
      
      if (videoResource) {
        viewResource(videoResource)
      } else {
        ElMessage.warning('No video resources found for this task')
      }
    }
    
    const downloadResource = async (resourceId) => {
      try {
        const response = await resourceService.downloadResource(resourceId)
        
        // Create blob and download
        const blob = new Blob([response.data])
        const url = window.URL.createObjectURL(blob)
        
        // Extract filename from Content-Disposition header
        const contentDisposition = response.headers['content-disposition'] || response.headers['Content-Disposition']
        let filename = 'download'
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename="?([^"]+)"?/)
          if (filenameMatch) {
            filename = filenameMatch[1]
          }
        }
        
        // Create download link and trigger download
        const link = document.createElement('a')
        link.href = url
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('File downloaded successfully')
      } catch (error) {
        console.error('Download failed:', error)
        ElMessage.error('Failed to download file')
      }
    }
    
    const handleProgressUpdate = async (progressData) => {
      // Update local progress
      if (selectedTask.value) {
        taskProgress.value[selectedTask.value.id] = {
          watchPercentage: progressData.watchPercentage
        }
      }
    }
    
    const handlePlaybackComplete = (completionData) => {
      ElMessage.success(`Video completed! Watch percentage: ${completionData.watchPercentage}%`)
      
      // Update local progress
      if (selectedTask.value) {
        taskProgress.value[selectedTask.value.id] = {
          watchPercentage: completionData.watchPercentage
        }
      }
    }
    
    const handleCourseFilter = () => {
      // Filtering handled by computed property
    }
    
    const handleTypeFilter = () => {
      // Filtering handled by computed property
    }
    
    const clearFilters = () => {
      selectedCourse.value = null
      selectedTaskType.value = null
    }
    
    // Utility functions
    const getTaskTypeLabel = (taskType) => {
      const type = taskTypes.find(t => t.value === taskType)
      return type ? type.label : taskType
    }
    
    const getTaskTypeColor = (taskType) => {
      const colorMap = {
        'CHAPTER_HOMEWORK': 'primary',
        'EXAM_QUIZ': 'danger',
        'VIDEO_WATCH': 'success',
        'READING_MATERIAL': 'info',
        'REPORT_UPLOAD': 'warning',
        'PRACTICE_PROJECT': 'primary'
      }
      return colorMap[taskType] || 'info'
    }
    
    const formatDeadline = (deadline) => {
      if (!deadline) return 'No deadline'
      const date = new Date(deadline)
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
    
    const getDeadlineClass = (deadline) => {
      if (!deadline) return ''
      const now = new Date()
      const deadlineDate = new Date(deadline)
      const diffHours = (deadlineDate - now) / (1000 * 60 * 60)
      
      if (diffHours < 0) return 'deadline-overdue'
      if (diffHours < 24) return 'deadline-urgent'
      if (diffHours < 72) return 'deadline-soon'
      return 'deadline-normal'
    }
    
    const getResourceCount = (taskId) => {
      return taskResourcesMap.value[taskId]?.length || 0
    }
    
    const hasVideoResources = (taskId) => {
      const resources = taskResourcesMap.value[taskId] || []
      return resources.some(r => isVideoFile(r.resourceName))
    }
    
    const isVideoFile = (filename) => {
      const videoExtensions = ['mp4', 'webm', 'ogg', 'avi', 'mov', 'wmv', 'flv', 'm4v']
      const extension = filename.split('.').pop().toLowerCase()
      return videoExtensions.includes(extension)
    }
    
    const isImageFile = (filename) => {
      const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp']
      const extension = filename.split('.').pop().toLowerCase()
      return imageExtensions.includes(extension)
    }
    
    const isPdfFile = (filename) => {
      return filename.split('.').pop().toLowerCase() === 'pdf'
    }
    
    const getFileTypeDescription = (filename) => {
      const extension = filename.split('.').pop().toLowerCase()
      const descriptions = {
        'mp4': 'MP4 Video',
        'webm': 'WebM Video',
        'ogg': 'OGG Video',
        'avi': 'AVI Video',
        'mov': 'MOV Video',
        'pdf': 'PDF Document',
        'doc': 'Word Document',
        'docx': 'Word Document',
        'xls': 'Excel Spreadsheet',
        'xlsx': 'Excel Spreadsheet',
        'ppt': 'PowerPoint Presentation',
        'pptx': 'PowerPoint Presentation',
        'txt': 'Text Document',
        'jpg': 'JPEG Image',
        'jpeg': 'JPEG Image',
        'png': 'PNG Image',
        'gif': 'GIF Image'
      }
      return descriptions[extension] || 'File'
    }
    
    const formatFileSize = (bytes) => {
      if (!bytes) return 'Unknown size'
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(1024))
      return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
    }
    
    const getProgressColor = (percentage) => {
      if (percentage >= 90) return '#67c23a'
      if (percentage >= 70) return '#e6a23c'
      if (percentage >= 50) return '#f56c6c'
      return '#909399'
    }
    
    // Lifecycle
    onMounted(async () => {
      await loadEnrolledCourses()
      await loadCourseTasks()
    })
    
    return {
      // Reactive data
      loading,
      selectedCourse,
      selectedTaskType,
      loadingTaskResources,
      taskProgress,
      enrolledCourses,
      courseTasks,
      taskTypes,
      
      // Dialog states
      resourcesDialogVisible,
      mediaViewerVisible,
      selectedTask,
      selectedResource,
      taskResources,
      
      // Computed
      filteredTasks,
      currentStudentId,
      
      // Methods
      viewTaskResources,
      closeResourcesDialog,
      viewResource,
      closeMediaViewer,
      continueWatching,
      downloadResource,
      handleProgressUpdate,
      handlePlaybackComplete,
      handleCourseFilter,
      handleTypeFilter,
      clearFilters,
      
      // Utility functions
      getTaskTypeLabel,
      getTaskTypeColor,
      formatDeadline,
      getDeadlineClass,
      getResourceCount,
      hasVideoResources,
      isVideoFile,
      isImageFile,
      isPdfFile,
      getFileTypeDescription,
      formatFileSize,
      getProgressColor
    }
  }
}
</script>

<style scoped>
.student-resources-container {
  padding: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Tasks Grid */
.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.task-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.task-icon {
  color: #409EFF;
  font-size: 18px;
}

.task-content {
  padding: 0;
}

.task-info {
  margin-bottom: 15px;
}

.course-name {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
}

.task-description {
  margin: 0 0 15px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.deadline,
.resources-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

.deadline-normal {
  color: #606266;
}

.deadline-soon {
  color: #e6a23c;
}

.deadline-urgent {
  color: #f56c6c;
}

.deadline-overdue {
  color: #f56c6c;
  font-weight: 600;
}

.progress-section {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 13px;
  color: #606266;
}

.progress-value {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.task-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 40px;
}

/* Resources Dialog */
.resources-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.no-resources {
  text-align: center;
  padding: 40px;
}

.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 15px;
}

.resource-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.resource-item:hover {
  border-color: #409EFF;
  background: #f0f9ff;
}

.resource-preview {
  margin-right: 15px;
}

.resource-icon {
  font-size: 32px;
  color: #409EFF;
}

.resource-info {
  flex: 1;
  min-width: 0;
}

.resource-name {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.resource-size {
  margin: 0 0 5px 0;
  font-size: 12px;
  color: #909399;
}

.resource-type {
  font-size: 12px;
  color: #606266;
}

.resource-actions {
  display: flex;
  gap: 8px;
}

/* Media Viewer */
.media-viewer-container {
  width: 100%;
  min-height: 400px;
}

.media-viewer-container .media-viewer {
  border-radius: 8px;
  overflow: hidden;
}

/* Responsive Design */
@media (max-width: 768px) {
  .tasks-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .section-actions {
    justify-content: center;
  }
  
  .task-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .task-actions {
    flex-direction: column;
  }
  
  .resources-grid {
    grid-template-columns: 1fr;
  }
  
  .resource-item {
    flex-direction: column;
    text-align: center;
  }
  
  .resource-preview {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>