<template>
  <div class="my-tasks-view">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1>My Tasks</h1>
          <p>Manage and track your assignments</p>
        </div>
        <div class="header-stats">
          <div class="stat-card">
            <div class="stat-number">{{ tasks.length }}</div>
            <div class="stat-label">Total Tasks</div>
          </div>
          <div class="stat-card overdue">
            <div class="stat-number">{{ overdueTasks.length }}</div>
            <div class="stat-label">Overdue</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-container">
      <div class="filters">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-select
              v-model="selectedCourse"
              placeholder="All Courses"
              clearable
              class="filter-select"
            >
              <el-option label="All Courses" value="" />
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="selectedType"
              placeholder="All Types"
              clearable
              class="filter-select"
            >
              <el-option label="All Types" value="" />
              <el-option label="Assignment" value="assignment" />
              <el-option label="Quiz" value="quiz" />
              <el-option label="Project" value="project" />
              <el-option label="Reading" value="reading" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="selectedStatus"
              placeholder="All Status"
              clearable
              class="filter-select"
            >
              <el-option label="All Status" value="" />
              <el-option label="Pending" value="pending" />
              <el-option label="Completed" value="completed" />
              <el-option label="Overdue" value="overdue" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchQuery"
              placeholder="Search tasks..."
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- Tasks Grid -->
    <div class="tasks-container">
      <div class="tasks-grid" v-loading="loading">
        <div
          v-for="task in filteredTasks"
          :key="task.id"
          class="task-card"
          :class="getStatusClass(task)"
        >
          <!-- Task Status Badge -->
          <div class="task-status-badge" :class="getStatusClass(task)">
            <el-icon v-if="task.status === 'completed'"><Check /></el-icon>
            <el-icon v-else-if="isOverdue(task.deadline)"><Warning /></el-icon>
            <el-icon v-else><Clock /></el-icon>
          </div>

          <div class="task-header">
            <div class="task-title-section">
                <h3 class="task-title" @click="viewTaskDetails(task)">{{ task.taskName }}</h3>
                <div class="task-tags">
                  <span class="task-type">
                    {{ getTaskTypeLabel(task.taskType) }}
                  </span>
                  <el-tag 
                    v-if="task.priority" 
                    :type="getPriorityColor(task.priority)" 
                    size="small" 
                    class="priority-tag"
                  >
                    {{ task.priority }}
                  </el-tag>
                </div>
              </div>
            <!-- Students can only view task details, not edit/delete -->
            <el-button type="text" class="task-menu-btn" @click="viewTaskDetails(task)">
              <el-icon><View /></el-icon>
            </el-button>
          </div>
          
          <div class="task-content">
            <div class="course-info">
              <el-icon class="course-icon"><Reading /></el-icon>
              <span class="course-name">{{ getCourseNameById(task.courseId) }}</span>
            </div>
            
            <p class="task-description">{{ task.taskDescription }}</p>
            
            <div class="task-meta">
              <div class="deadline-info">
                <el-icon class="meta-icon"><Calendar /></el-icon>
                <div class="deadline-content">
                  <span class="deadline-label">Due Date</span>
                  <span class="deadline-date" :class="{ 'overdue-text': isOverdue(task.deadline) }">
                    {{ formatDate(task.deadline) }}
                  </span>
                </div>
              </div>
              
              <div class="resources-info" v-if="task.resources && task.resources.length > 0">
                <el-icon class="meta-icon"><Folder /></el-icon>
                <div class="resources-content">
                  <span class="resources-label">Resources</span>
                  <span class="resources-count">{{ task.resources.length }} file(s)</span>
                </div>
              </div>
            </div>
            
            <!-- Video Progress (Hidden - Recording Silently) -->
            <div v-if="false" class="progress-section">
              <!-- Progress tracking is happening silently in the background -->
            </div>

            <!-- Submission Status -->
            <div v-if="task.submissionStatus" class="submission-section">
              <div class="submission-header">
                <el-icon class="submission-icon"><Document /></el-icon>
                <span class="submission-text">Submission: {{ task.submissionStatus }}</span>
              </div>
              <div v-if="task.submissionDate" class="submission-date">
                Submitted on {{ formatDate(task.submissionDate) }}
              </div>
            </div>
          </div>
          
          <div class="task-actions">
            <el-button
              v-if="task.resources && task.resources.length > 0"
              type="primary"
              size="default"
              @click="viewTaskResources(task)"
              class="action-btn primary-btn"
            >
              <el-icon><View /></el-icon>
              <span>View Materials</span>
            </el-button>
            
            <el-button
              v-if="hasVideoResources(task) && task.watchProgress < 100"
              type="success"
              size="default"
              @click="continueWatching(task)"
              class="action-btn success-btn"
            >
              <el-icon><VideoPlay /></el-icon>
              <span>{{ task.watchProgress > 0 ? 'Continue' : 'Start' }} Video</span>
            </el-button>

            <el-button
              v-if="canSubmitTask(task)"
              type="warning"
              size="default"
              @click="submitTask(task)"
              class="action-btn warning-btn"
            >
              <el-icon><Upload /></el-icon>
              <span>Submit Work</span>
            </el-button>
          </div>
        </div>
        
        <div v-if="filteredTasks.length === 0 && !loading" class="no-tasks">
          <div class="empty-state">
            <div class="empty-icon">📝</div>
            <h3>No tasks found</h3>
            <p>You don't have any tasks matching the current filters.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Task Resources Dialog -->
    <el-dialog
      v-model="resourcesDialogVisible"
      :title="`Resources - ${selectedTask?.name}`"
      width="80%"
      top="5vh"
    >
      <div class="resources-grid">
        <div
          v-for="resource in selectedTask?.resources"
          :key="resource.id"
          class="resource-item"
        >
          <div class="resource-info">
            <div class="resource-icon">
              <el-icon v-if="isVideoFile(resource.resourceName)"><VideoPlay /></el-icon>
              <el-icon v-else-if="isImageFile(resource.resourceName)"><Picture /></el-icon>
              <el-icon v-else-if="isPdfFile(resource.resourceName)"><Document /></el-icon>
              <el-icon v-else-if="isAudioFile(resource.resourceName)"><Headphone /></el-icon>
              <el-icon v-else><Document /></el-icon>
            </div>
            <div class="resource-details">
              <h4>{{ resource.resourceName }}</h4>
              <p>{{ formatFileSize(resource.fileSize) }}</p>
              <p v-if="resource.uploadDate">Uploaded: {{ formatDate(resource.uploadDate) }}</p>
            </div>
          </div>
          
          <div class="resource-actions">
            <el-button
              type="primary"
              size="small"
              @click="viewResource(resource)"
            >
              <el-icon><View /></el-icon>
              View
            </el-button>
            <el-button
              type="default"
              size="small"
              @click="downloadResource(resource)"
            >
              <el-icon><Download /></el-icon>
              Download
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Media Viewer Dialog -->
    <el-dialog
      v-model="mediaViewerVisible"
      :title="selectedResource?.fileName"
      width="90%"
      top="5vh"
      @close="closeMediaViewer"
    >
      <div class="media-viewer-container">
        <MediaViewer
          v-if="selectedResource"
          :resource="selectedResource"
          :student-id="currentStudentId"
          :show-progress="false"
          :auto-track="true"
          @progress-update="handleProgressUpdate"
          @playback-complete="handlePlaybackComplete"
        />
      </div>
    </el-dialog>

    <!-- Task Submission Dialog -->
    <el-dialog
      v-model="submissionDialogVisible"
      :title="`Submit: ${selectedTask?.taskName}`"
      width="50%"
      @close="closeSubmissionDialog"
    >
      <el-form
        ref="submissionFormRef"
        :model="submissionForm"
        :rules="submissionRules"
        label-width="120px"
      >
        <el-form-item label="Task" prop="taskName">
          <el-input :value="selectedTask?.taskName || ''" disabled />
        </el-form-item>
        
        <el-form-item 
          v-if="selectedTask?.submissionMethod === 'upload'"
          label="File Upload" 
          prop="submissionContent"
        >
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            :limit="1"
          >
            <el-button type="primary">Select File</el-button>
            <template #tip>
              <div class="el-upload__tip">
                Please select the file you want to submit
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item 
          v-else-if="selectedTask?.submissionMethod === 'text'"
          label="Text Content" 
          prop="submissionContent"
        >
          <el-input
            v-model="submissionForm.submissionContent"
            type="textarea"
            :rows="6"
            placeholder="Enter your submission content here..."
          />
        </el-form-item>
        
        <el-form-item 
          v-else-if="selectedTask?.submissionMethod === 'url'"
          label="URL" 
          prop="submissionContent"
        >
          <el-input
            v-model="submissionForm.submissionContent"
            placeholder="Enter the URL of your submission"
          />
        </el-form-item>
        
        <el-form-item 
          v-else
          label="Submission Content" 
          prop="submissionContent"
        >
          <el-input
            v-model="submissionForm.submissionContent"
            type="textarea"
            :rows="6"
            placeholder="Enter your submission text or notes..."
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeSubmissionDialog">Cancel</el-button>
          <el-button type="primary" @click="confirmSubmission" :loading="submitting">
            Submit
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Students can only submit tasks, not edit them -->
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Reading,
  Calendar,
  Folder,
  VideoPlay,
  View,
  Picture,
  Document,
  Headphone,
  Download,
  Check,
  Warning,
  Clock,
  Upload,
  Search
} from '@element-plus/icons-vue'
import MediaViewer from '../components/MediaViewer.vue'
import { taskService } from '../services/taskService'
import { courseEnrollmentService } from '../services/courseEnrollmentService'
import { resourceService } from '../services/resourceService'
import { playbackService } from '../services/playbackService'
import { submissionService } from '../services/submissionService'

export default {
  name: 'MyTasksView',
  components: {
    MediaViewer,
    Reading,
    Calendar,
    Folder,
    VideoPlay,
    View,
    Picture,
    Document,
    Headphone,
    Download,
    Check,
    Warning,
    Clock,
    Upload,
    Search
  },
  setup() {
    // Reactive data
    const loading = ref(false)
    const tasks = ref([])
    const courses = ref([])
    const selectedCourse = ref(null)
    const selectedType = ref(null)
    const selectedStatus = ref(null)
    const searchQuery = ref('')
    const resourcesDialogVisible = ref(false)
    const mediaViewerVisible = ref(false)
    const submissionDialogVisible = ref(false)
    const selectedTask = ref(null)
    const selectedResource = ref(null)
    const submitting = ref(false)
    const submissionFormRef = ref(null)
    const uploadRef = ref(null)
    const fileList = ref([])
    
    // Form data
    const submissionForm = ref({
      taskId: null,
      studentId: null,
      submissionContent: '',
      completionStatus: 2
    })
    
    // Form validation rules
    const submissionRules = ref({
      submissionContent: [
        { required: true, message: 'Please provide submission content', trigger: 'blur' }
      ]
    })

    // Task types
    const taskTypes = [
      { value: 'ASSIGNMENT', label: 'Assignment' },
      { value: 'QUIZ', label: 'Quiz' },
      { value: 'PROJECT', label: 'Project' },
      { value: 'READING_MATERIAL', label: 'Reading Material' },
      { value: 'VIDEO_WATCH', label: 'Video Watch' },
      { value: 'DISCUSSION', label: 'Discussion' }
    ]

    // Computed properties
    const currentStudentId = computed(() => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      return user.referenceId || user.id
    })

    const overdueTasks = computed(() => {
      return tasks.value.filter(task => isOverdue(task.deadline) && task.status !== 'completed')
    })

    const filteredTasks = computed(() => {
      let filtered = tasks.value

      // Search filter
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(task => 
          task.taskName.toLowerCase().includes(query) ||
          task.taskDescription.toLowerCase().includes(query) ||
          getCourseNameById(task.courseId).toLowerCase().includes(query)
        )
      }

      // Course filter
      if (selectedCourse.value) {
        filtered = filtered.filter(task => task.courseId === selectedCourse.value)
      }

      // Type filter
      if (selectedType.value) {
        filtered = filtered.filter(task => task.taskType === selectedType.value)
      }

      // Status filter
      if (selectedStatus.value) {
        filtered = filtered.filter(task => {
          if (selectedStatus.value === 'overdue') return isOverdue(task.deadline) && task.status !== 'completed'
          if (selectedStatus.value === 'pending') return !isOverdue(task.deadline) && task.status !== 'completed'
          if (selectedStatus.value === 'completed') return task.status === 'completed'
          return true
        })
      }

      return filtered
    })

    // Methods
    const loadCourses = async () => {
      try {
        const response = await courseEnrollmentService.getCoursesByStudent(currentStudentId.value)
        courses.value = response.data || []
      } catch (error) {
        console.error('Error loading courses:', error)
        ElMessage.error('Failed to load courses')
      }
    }

    const loadTasks = async () => {
      loading.value = true
      try {
        // Get enrolled courses first
        const coursesResponse = await courseEnrollmentService.getCoursesByStudent(currentStudentId.value)
        const enrolledCourses = coursesResponse.data || []
        
        const allTasks = []
        
        // Get tasks for each enrolled course
        for (const course of enrolledCourses) {
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
        
        tasks.value = allTasks
        
        // Load resources, progress, and submission status for each task
        for (const task of tasks.value) {
          await loadTaskResources(task)
          await loadSubmissionStatus(task)
          if (hasVideoResources(task)) {
            await loadWatchProgress(task)
          }
        }
      } catch (error) {
        console.error('Error loading tasks:', error)
        ElMessage.error('Failed to load tasks')
      } finally {
        loading.value = false
      }
    }

    const loadTaskResources = async (task) => {
      try {
        const response = await resourceService.getTaskResources(task.id)
        task.resources = response.data || []
      } catch (error) {
        console.error('Error loading task resources:', error)
        task.resources = []
      }
    }

    const loadSubmissionStatus = async (task) => {
      try {
        // Check if student has already submitted this task
        const response = await taskService.getSubmissionByStudentAndTask(currentStudentId.value, task.id)
        if (response.data) {
          task.submissionStatus = 'submitted'
          task.submissionDate = response.data.submissionTime
          task.status = 'completed'
        }
      } catch (error) {
        // No submission found or error - task is not submitted
        task.submissionStatus = null
        task.submissionDate = null
      }
    }

    const loadWatchProgress = async (task) => {
      try {
        const videoResources = task.resources.filter(r => isVideoFile(r.resourceName))
        if (videoResources.length > 0) {
          // Get progress for the first video resource
          const response = await playbackService.getWatchProgress(
            currentStudentId.value,
            videoResources[0].id
          )
          task.watchProgress = response.data.watchPercentage || 0
        }
      } catch (error) {
        console.error('Error loading watch progress:', error)
        task.watchProgress = 0
      }
    }

    const getCourseNameById = (courseId) => {
      const course = courses.value.find(c => c.id === courseId)
      return course ? course.name : 'Unknown Course'
    }

    const getTaskTypeLabel = (type) => {
      const taskType = taskTypes.find(t => t.value === type)
      return taskType ? taskType.label : type
    }

    const getTaskTypeColor = (type) => {
      const colorMap = {
        'ASSIGNMENT': 'primary',
        'QUIZ': 'warning',
        'PROJECT': 'success',
        'READING_MATERIAL': 'info',
        'VIDEO_WATCH': 'danger',
        'DISCUSSION': 'default'
      }
      return colorMap[type] || 'default'
    }

    const isOverdue = (deadline) => {
      return new Date(deadline) < new Date()
    }

    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    const hasVideoResources = (task) => {
      return task.resources && task.resources.some(r => isVideoFile(r.resourceName))
    }

    const isVideoFile = (fileName) => {
      const videoExtensions = ['.mp4', '.webm', '.avi', '.mov', '.wmv', '.flv', '.mkv']
      return videoExtensions.some(ext => fileName.toLowerCase().endsWith(ext))
    }

    const isImageFile = (fileName) => {
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.svg', '.webp']
      return imageExtensions.some(ext => fileName.toLowerCase().endsWith(ext))
    }

    const isPdfFile = (fileName) => {
      return fileName.toLowerCase().endsWith('.pdf')
    }

    const isAudioFile = (fileName) => {
      const audioExtensions = ['.mp3', '.wav', '.ogg', '.aac', '.flac']
      return audioExtensions.some(ext => fileName.toLowerCase().endsWith(ext))
    }

    const getProgressColor = (percentage) => {
      if (percentage < 30) return '#f56c6c'
      if (percentage < 70) return '#e6a23c'
      return '#67c23a'
    }

    const getStatusClass = (task) => {
      if (task.status === 'completed') return 'completed'
      if (isOverdue(task.deadline)) return 'overdue'
      return 'pending'
    }

    const getPriorityColor = (priority) => {
      const colorMap = {
        'low': 'info',
        'medium': 'warning',
        'high': 'danger'
      }
      return colorMap[priority] || 'info'
    }

    const canSubmitTask = (task) => {
      // Allow submissions for task types that require student work
      const submittableTypes = ['CHAPTER_HOMEWORK', 'VIDEO_WATCH', 'EXAM_QUIZ', 'READING_MATERIAL', 'REPORT_UPLOAD', 'PRACTICE_PROJECT']
      const canSubmit = submittableTypes.includes(task.taskType) && task.status !== 'completed' && !task.submissionStatus
      
      // Debug logging
      console.log('canSubmitTask debug:', {
        taskName: task.taskName,
        taskType: task.taskType,
        status: task.status,
        submissionStatus: task.submissionStatus,
        typeAllowed: submittableTypes.includes(task.taskType),
        statusOk: task.status !== 'completed',
        noSubmission: !task.submissionStatus,
        canSubmit
      })
      
      return canSubmit
    }

    const viewTaskDetails = (task) => {
      // For students, viewing task details means accessing task resources
      viewTaskResources(task)
    }

    const viewTaskResources = (task) => {
      selectedTask.value = task
      resourcesDialogVisible.value = true
    }

    const continueWatching = (task) => {
      const videoResource = task.resources.find(r => isVideoFile(r.resourceName))
      if (videoResource) {
        viewResource(videoResource)
      }
    }



    const viewResource = (resource) => {
      selectedResource.value = resource
      mediaViewerVisible.value = true
      resourcesDialogVisible.value = false
    }

    const downloadResource = async (resource) => {
      try {
        const response = await resourceService.downloadResource(resource.id)
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', resource.fileName)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('Error downloading resource:', error)
        ElMessage.error('Failed to download resource')
      }
    }

    const closeMediaViewer = () => {
      mediaViewerVisible.value = false
      selectedResource.value = null
    }

    const handleProgressUpdate = (data) => {
      console.log('Progress update:', data)
      // Update the task's watch progress
      if (selectedTask.value) {
        selectedTask.value.watchProgress = data.watchPercentage
      }
    }

    const handlePlaybackComplete = (data) => {
      console.log('Playback complete:', data)
      ElMessage.success(`Video completed! Watch percentage: ${Math.round(data.watchPercentage)}%`)
      
      // Update the task's watch progress
      if (selectedTask.value) {
        selectedTask.value.watchProgress = data.watchPercentage
      }
    }

    // Task submission method for students

    const submitTask = (task) => {
      selectedTask.value = task
      submissionForm.value.taskId = task.id
      submissionForm.value.studentId = currentStudentId.value
      submissionForm.value.submissionContent = ''
      submissionForm.value.completionStatus = 2
      fileList.value = []
      submissionDialogVisible.value = true
    }
    
    const handleFileChange = (file, uploadFileList) => {
      submissionForm.value.submissionContent = file.name
      // Update the fileList to ensure we have the actual file objects for upload
      fileList.value = uploadFileList
    }

    // Students can only submit tasks, not complete or delete them

    const confirmSubmission = async () => {
      if (!submissionForm.value.submissionContent) {
        ElMessage.warning('Please provide submission content')
        return
      }
      
      submitting.value = true
      try {
        const submissionData = {
          taskId: submissionForm.value.taskId,
          studentId: submissionForm.value.studentId,
          submissionContent: submissionForm.value.submissionContent,
          submissionTime: new Date().toISOString(),
          completionStatus: submissionForm.value.completionStatus
        }
        
        // Create the submission first
        const submissionResponse = await submissionService.createSubmission(submissionData)
        const submissionId = submissionResponse.data.id
        
        // If submission method is upload and there are files, upload them
        if (selectedTask.value?.submissionMethod === 'upload' && fileList.value.length > 0) {
          for (const fileItem of fileList.value) {
            if (fileItem.raw) {
              await submissionService.uploadSubmissionFile(submissionId, fileItem.raw)
            }
          }
        }
        
        // Update task status
        if (selectedTask.value) {
          selectedTask.value.submissionStatus = 'submitted'
          selectedTask.value.submissionDate = new Date().toISOString()
          selectedTask.value.status = 'completed'
        }
        
        ElMessage.success('Task submitted successfully!')
        closeSubmissionDialog()
        
        // Reload task data to reflect changes
        if (selectedTask.value) {
          await loadSubmissionStatus(selectedTask.value)
        }
      } catch (error) {
        console.error('Error submitting task:', error)
        ElMessage.error('Failed to submit task')
      } finally {
        submitting.value = false
      }
    }

    const closeSubmissionDialog = () => {
      submissionDialogVisible.value = false
      selectedTask.value = null
      submissionForm.value = {
        taskId: null,
        studentId: null,
        submissionContent: '',
        completionStatus: 2
      }
      fileList.value = []
    }

    // Lifecycle
    onMounted(async () => {
      await loadCourses()
      await loadTasks()
    })

    return {
      // Reactive data
      loading,
      tasks,
      courses,
      selectedCourse,
      selectedType,
      selectedStatus,
      searchQuery,
      resourcesDialogVisible,
      mediaViewerVisible,
      submissionDialogVisible,
      selectedTask,
      selectedResource,
      submitting,
      submissionForm,
      submissionFormRef,
      submissionRules,
      uploadRef,
      fileList,
      taskTypes,
      
      // Computed
      currentStudentId,
      filteredTasks,
      overdueTasks,
      
      // Methods
      loadTasks,
      getCourseNameById,
      getTaskTypeLabel,
      getTaskTypeColor,
      getStatusClass,
      getPriorityColor,
      canSubmitTask,
      isOverdue,
      formatDate,
      formatFileSize,
      hasVideoResources,
      isVideoFile,
      isImageFile,
      isPdfFile,
      isAudioFile,
      getProgressColor,
      viewTaskDetails,
      viewTaskResources,
      continueWatching,
      viewResource,
      downloadResource,
      closeMediaViewer,
      handleProgressUpdate,
      handlePlaybackComplete,
      submitTask,
      handleFileChange,
      confirmSubmission,
      closeSubmissionDialog
    }
  }
}
</script>

<style scoped>
.my-tasks-view {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.page-header {
  background: white;
  padding: 2rem;
  margin-bottom: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.title-section h1 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 2.5rem;
  font-weight: 600;
}

.title-section p {
  margin: 0;
  color: #64748b;
  font-size: 1.1rem;
  font-weight: 500;
}

.header-stats {
  display: flex;
  gap: 1rem;
}

.stat-card {
  background: #409eff;
  color: white;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  text-align: center;
  min-width: 80px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-card.overdue {
  background: #f56c6c;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 0.8rem;
  opacity: 0.9;
  margin-top: 4px;
}

.filters-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.filters {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.filter-select,
.search-input {
  width: 100%;
}

.filter-select :deep(.el-input__wrapper) {
  border-radius: 4px;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 4px;
  transition: all 0.3s ease;
}

.tasks-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem 2rem;
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
  gap: 1.5rem;
}

.task-card {
  position: relative;
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
  overflow: hidden;
}

.task-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #409eff;
  transition: height 0.3s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.task-card.overdue::before {
  background: #f56c6c;
}

.task-card.completed::before {
  background: #67c23a;
}

.task-card.in-progress::before {
  background: #e6a23c;
}

.task-status-badge {
  position: absolute;
  top: 1rem;
  right: 4rem;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: white;
  z-index: 2;
}

.task-status-badge.completed {
  background: #67c23a;
}

.task-status-badge.overdue {
  background: #f56c6c;
}

.task-status-badge.pending {
  background: #409eff;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  position: relative;
  z-index: 1;
}

.task-title-section {
  flex: 1;
  margin-right: 1rem;
}

.task-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 0.5rem 0;
  line-height: 1.3;
  cursor: pointer;
  transition: color 0.2s ease;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-title:hover {
  color: #667eea;
}

.task-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.task-type {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 0.25rem 0.5rem;
  background: #409eff;
  color: white;
  border-radius: 4px;
  display: inline-block;
}

.task-type-tag {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.priority-tag {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.task-menu-btn {
  padding: 0.5rem;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.task-menu-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.task-content {
  margin-bottom: 1.25rem;
}

.course-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0 0 0.75rem 0;
  padding: 0.5rem 0.75rem;
  background: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #d1ecf1;
}

.course-icon {
  color: #409eff;
  font-size: 1rem;
}

.course-name {
  color: #409eff;
  font-weight: 600;
  font-size: 0.9rem;
}

.task-description {
  margin: 0 0 1rem 0;
  color: #475569;
  line-height: 1.6;
  font-size: 0.95rem;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-meta {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.deadline-info,
.resources-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.deadline-info:hover,
.resources-info:hover {
  background: rgba(102, 126, 234, 0.05);
  border-color: rgba(102, 126, 234, 0.2);
}

.meta-icon {
  color: #667eea;
  font-size: 1.1rem;
  min-width: 20px;
}

.deadline-content,
.resources-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.deadline-label,
.resources-label {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.deadline-date,
.resources-count {
  font-size: 0.9rem;
  color: #374151;
  font-weight: 600;
}

.overdue-text {
  color: #dc2626;
  font-weight: 700;
}

.progress-section {
  margin: 1rem 0;
  padding: 1rem;
  background: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #d1ecf1;
}

.progress-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.progress-icon {
  color: #0284c7;
  font-size: 1.1rem;
}

.progress-text {
  font-size: 0.9rem;
  color: #0369a1;
  font-weight: 600;
}

.custom-progress {
  margin-top: 0.5rem;
}

.custom-progress :deep(.el-progress-bar__outer) {
  background-color: rgba(2, 132, 199, 0.1);
  border-radius: 8px;
  height: 8px;
}

.custom-progress :deep(.el-progress-bar__inner) {
  border-radius: 8px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.submission-section {
  margin: 1rem 0;
  padding: 1rem;
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border-radius: 16px;
  border: 1px solid #bbf7d0;
}

.submission-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.submission-icon {
  color: #059669;
  font-size: 1.1rem;
}

.submission-text {
  font-size: 0.9rem;
  color: #065f46;
  font-weight: 600;
}

.submission-date {
  font-size: 0.8rem;
  color: #047857;
  font-style: italic;
}

.task-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
  text-decoration: none;
  min-height: 40px;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.primary-btn {
  background: #409eff;
  color: white;
}

.primary-btn:hover {
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}

.success-btn {
  background: #67c23a;
  color: white;
}

.success-btn:hover {
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.4);
}

.warning-btn {
  background: #e6a23c;
  color: white;
}

.warning-btn:hover {
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.4);
}

.no-tasks {
  grid-column: 1 / -1;
  text-align: center;
  padding: 4rem 2rem;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  max-width: 400px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  color: #cbd5e1;
  margin-bottom: 0.5rem;
}

.empty-state h3 {
  margin: 0;
  color: #475569;
  font-size: 1.5rem;
  font-weight: 600;
}

.empty-state p {
  margin: 0;
  color: #64748b;
  font-size: 1rem;
  line-height: 1.6;
  text-align: center;
}

.resources-grid {
  display: grid;
  gap: 1rem;
  max-height: 60vh;
  overflow-y: auto;
  padding: 0.5rem;
}

.resource-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.resource-item:hover {
  background: #409eff;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.resource-item:hover .resource-icon {
  color: white;
}

.resource-item:hover .resource-details h4,
.resource-item:hover .resource-details p {
  color: white;
}

.resource-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.resource-icon {
  font-size: 1.75rem;
  color: #667eea;
  transition: color 0.3s ease;
  min-width: 28px;
}

.resource-details h4 {
  margin: 0 0 0.25rem 0;
  color: #1a202c;
  font-size: 1rem;
  font-weight: 600;
  transition: color 0.3s ease;
}

.resource-details p {
  margin: 0;
  color: #64748b;
  font-size: 0.85rem;
  transition: color 0.3s ease;
}

.resource-actions {
  display: flex;
  gap: 0.5rem;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.resource-item:hover .resource-actions {
  opacity: 1;
}

.resource-actions .el-button {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.resource-actions .el-button:hover {
  transform: translateY(-1px);
}

.media-viewer-container {
  width: 100%;
  min-height: 500px;
  border-radius: 16px;
  overflow: hidden;
  background: #000;
  position: relative;
}

.media-viewer-container .media-viewer {
  width: 100%;
  height: 100%;
  border-radius: 16px;
}

.submission-form {
  padding: 1rem 0;
}

.submission-form h3 {
  margin: 0 0 0.5rem 0;
  color: #1a202c;
  font-size: 1.25rem;
  font-weight: 700;
}

.submission-description {
  margin: 0 0 1.5rem 0;
  color: #64748b;
  line-height: 1.6;
}

.submission-upload {
  width: 100%;
}

.submission-upload :deep(.el-upload-dragger) {
  border-radius: 16px;
  border: 2px dashed #cbd5e1;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  transition: all 0.3s ease;
}

.submission-upload :deep(.el-upload-dragger:hover) {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
}

.edit-form {
  padding: 1rem 0;
}

.edit-form .el-form-item {
  margin-bottom: 1.5rem;
}

.edit-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.edit-form :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
}

.edit-form :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.edit-form :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.edit-form :deep(.el-textarea__inner:hover) {
  border-color: #667eea;
}

.edit-form :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding-top: 1rem;
}

.dialog-footer .el-button {
  border-radius: 12px;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.dialog-footer .el-button:hover {
  transform: translateY(-1px);
}

/* Dialog customizations */
:deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: #409eff;
  color: white;
  padding: 1.5rem 2rem;
  margin: 0;
}

:deep(.el-dialog__title) {
  color: white;
  font-weight: 700;
  font-size: 1.25rem;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 1.25rem;
}

:deep(.el-dialog__body) {
  padding: 2rem;
}

:deep(.el-dialog__footer) {
  padding: 1rem 2rem 2rem;
  background: #f8fafc;
}

/* Responsive design */
@media (max-width: 768px) {
  .tasks-grid {
    grid-template-columns: 1fr;
  }
  
  .header-content {
    flex-direction: column;
    gap: 1.5rem;
    text-align: center;
  }
  
  .header-stats {
    justify-content: center;
  }
  
  .task-actions {
    flex-direction: column;
  }
  
  .action-btn {
    justify-content: center;
  }
}
</style>