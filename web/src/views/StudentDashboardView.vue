<template>
  <div class="student-dashboard-container">
    <div class="dashboard-header">
      <h2><TypewriterText :text="$t('studentDashboard.title')" :show="true" :speed="70" @typing-complete="onDashboardTitleComplete" /></h2>
      <p class="welcome-message"><TypewriterText :text="$t('studentDashboard.welcomeMessage', { username: user?.username || $t('studentDashboard.defaultUser') })" :show="showWelcomeMessage" :speed="50" /></p>
    </div>

    <!-- Stats Cards -->
    <el-row :gutter="20" class="dashboard-stats">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon courses-icon">
            <el-icon>
              <el-icon-reading/>
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ enrolledCourses.length }}</div>
            <div class="stat-label">{{ $t('studentDashboard.enrolledCourses') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon tasks-icon">
            <el-icon>
              <el-icon-document/>
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalTasks }}</div>
            <div class="stat-label">{{ $t('studentDashboard.totalTasks') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon pending-icon">
            <el-icon>
              <el-icon-clock/>
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingTasks }}</div>
            <div class="stat-label">{{ $t('studentDashboard.pendingTasks') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon completed-icon">
            <el-icon>
              <el-icon-check/>
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ completedTasks }}</div>
            <div class="stat-label">{{ $t('studentDashboard.completedTasks') }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick Actions -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="action-card abilities-card" @click="goToAbilities">
          <div class="action-content">
            <div class="action-icon abilities-icon">
              <el-icon>
                <el-icon-cpu/>
              </el-icon>
            </div>
            <div class="action-info">
              <h4>{{ $t('studentDashboard.myLearningAbilities') }}</h4>
              <p>{{ $t('studentDashboard.abilitiesDescription') }}</p>
            </div>

          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="action-card grades-card" @click="goToGrades">
          <div class="action-content">
            <div class="action-icon grades-icon">
              <el-icon>
                <el-icon-data-analysis/>
              </el-icon>
            </div>
            <div class="action-info">
              <h4>{{ $t('studentDashboard.myGrades') }}</h4>
              <p>{{ $t('studentDashboard.gradesDescription') }}</p>
            </div>

          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="action-card resources-card" @click="goToResources">
          <div class="action-content">
            <div class="action-icon resources-icon">
              <el-icon>
                <el-icon-folder/>
              </el-icon>
            </div>
            <div class="action-info">
              <h4>{{ $t('studentDashboard.learningResources') }}</h4>
              <p>{{ $t('studentDashboard.resourcesDescription') }}</p>
            </div>

          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Course Filter -->
    <el-row :gutter="20" class="filter-section">
      <el-col :span="24">
        <el-card shadow="hover">
          <div class="filter-header">
            <h3>{{ $t('studentDashboard.myLearningTasks') }}</h3>
            <el-select
              v-model="selectedCourseId"
              :placeholder="$t('studentDashboard.filterByCourse')"
              clearable
              style="width: 250px"
              @change="handleCourseFilter"
            >
              <el-option
                v-for="course in enrolledCourses"
                :key="course.id"
                :label="course.courseName"
                :value="course.id"
              />
            </el-select>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tasks List -->
    <el-row :gutter="20" class="tasks-section">
      <el-col :span="24">
        <el-card shadow="hover" class="tasks-card">
          <el-table
            :data="filteredTasks"
            v-loading="loading"
            stripe
            style="width: 100%"
            :empty-text="$t('studentDashboard.noTasksFound')"
          >
            <el-table-column prop="taskName" :label="$t('studentDashboard.taskName')" min-width="180">
              <template #default="{ row }">
                <div class="task-name">
                  <el-icon class="task-icon">
                    <Document v-if="row.taskType === 'READING_MATERIAL'"/>
                    <VideoPlay v-else-if="row.taskType === 'VIDEO_WATCH'"/>
                    <Edit v-else-if="row.taskType === 'CHAPTER_HOMEWORK'"/>
                    <Files v-else-if="row.taskType === 'REPORT_UPLOAD'"/>
                    <Trophy v-else-if="row.taskType === 'EXAM_QUIZ'"/>
                    <Tools v-else-if="row.taskType === 'PRACTICE_PROJECT'"/>
                    <Document v-else/>
                  </el-icon>
                  {{ row.taskName }}
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="courseName" :label="$t('studentDashboard.course')" width="150" />
            
            <el-table-column prop="taskType" :label="$t('studentDashboard.type')" width="140">
              <template #default="{ row }">
                <el-tag :type="getTaskTypeColor(row.taskType)" size="small">
                  {{ getTaskTypeLabel(row.taskType) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="deadline" :label="$t('studentDashboard.deadline')" width="160">
              <template #default="{ row }">
                <div class="deadline-cell">
                  <el-icon><Clock /></el-icon>
                  <span :class="getDeadlineClass(row.deadline)">
                    {{ formatDeadline(row.deadline) }}
                  </span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="Status" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusColor(getTaskStatus(row))" size="small">
                  {{ getTaskStatus(row) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="taskDescription" label="Description" min-width="200">
              <template #default="{ row }">
                <el-tooltip :content="row.taskDescription" placement="top">
                  <div class="description-cell">
                    {{ truncateText(row.taskDescription, 50) }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            
            <el-table-column label="Actions" width="200" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button
                    type="primary"
                    size="small"
                    @click="viewTask(row)"
                  >
                    View
                  </el-button>
                  <el-button
                    v-if="canSubmit(row)"
                    type="success"
                    size="small"
                    @click="showSubmissionDialog(row)"
                  >
                    Submit
                  </el-button>
                  <el-button
                    v-else-if="hasSubmission(row)"
                    type="info"
                    size="small"
                    @click="viewSubmission(row)"
                  >
                    View Submission
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- Task Detail Dialog -->
    <el-dialog
      v-model="taskDetailVisible"
      :title="selectedTask?.taskName"
      width="60%"
      :before-close="handleTaskDetailClose"
    >
      <div v-if="selectedTask" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="Course">
            {{ selectedTask.courseName }}
          </el-descriptions-item>
          <el-descriptions-item label="Type">
            <el-tag :type="getTaskTypeColor(selectedTask.taskType)" size="small">
              {{ getTaskTypeLabel(selectedTask.taskType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Deadline">
            <span :class="getDeadlineClass(selectedTask.deadline)">
              {{ formatDeadline(selectedTask.deadline) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="Submission Method">
            {{ selectedTask.submissionMethod }}
          </el-descriptions-item>
          <el-descriptions-item label="Description" :span="2">
            {{ selectedTask.taskDescription }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="taskResources.length > 0" class="task-resources">
          <h4>Resources</h4>
          <div class="resources-grid">
            <div v-for="resource in taskResources" :key="resource.id" class="resource-item">
              <div class="resource-info">
                <div class="resource-icon">
                  <el-icon v-if="isVideoFile(resource.fileName)"><VideoPlay /></el-icon>
                  <el-icon v-else-if="isImageFile(resource.fileName)"><Picture /></el-icon>
                  <el-icon v-else-if="isPdfFile(resource.fileName)"><Document /></el-icon>
                  <el-icon v-else-if="isAudioFile(resource.fileName)"><Headphone /></el-icon>
                  <el-icon v-else><Document /></el-icon>
                </div>
                <div class="resource-details">
                  <h5>{{ resource.fileName || resource.resourceName }}</h5>
                  <p v-if="resource.fileSize">{{ formatFileSize(resource.fileSize) }}</p>
                </div>
              </div>
              <div class="resource-actions">
                <el-button type="primary" size="small" @click="viewResource(resource)">
                  <el-icon><View /></el-icon>
                  View
                </el-button>
                <el-button type="default" size="small" @click="downloadResource(resource)">
                  <el-icon><Download /></el-icon>
                  Download
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Media Viewer Dialog -->
    <el-dialog
      v-model="mediaViewerVisible"
      :title="selectedResource?.fileName || selectedResource?.resourceName"
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

    <!-- Submission Dialog -->
    <el-dialog
      v-model="submissionDialogVisible"
      :title="`Submit: ${selectedTask?.taskName}`"
      width="50%"
      :before-close="handleSubmissionDialogClose"
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
            :placeholder="$t('studentDashboard.submissionPlaceholder')"
          />
        </el-form-item>
        
        <el-form-item 
          v-else-if="selectedTask?.submissionMethod === 'url'"
          label="URL" 
          prop="submissionContent"
        >
          <el-input
            v-model="submissionForm.submissionContent"
            :placeholder="$t('studentDashboard.submissionUrlPlaceholder')"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submissionDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitTask" :loading="submitting">
            Submit
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Submission View Dialog -->
    <el-dialog
      v-model="submissionViewVisible"
      :title="`Submission: ${selectedTask?.taskName}`"
      width="50%"
    >
      <div v-if="currentSubmission" class="submission-view">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="Submission Time">
            {{ formatDateTime(currentSubmission.submissionTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="Status">
            <el-tag :type="getSubmissionStatusColor(currentSubmission.completionStatus)">
              {{ getSubmissionStatusText(currentSubmission.completionStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentSubmission.score !== null" label="Score">
            {{ currentSubmission.score }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentSubmission.feedback" label="Feedback">
            {{ currentSubmission.feedback }}
          </el-descriptions-item>
          <el-descriptions-item label="Content">
            <div class="submission-content">
              {{ currentSubmission.submissionContent }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document,
  VideoPlay,
  Edit,
  Files,
  Trophy,
  Tools,
  Clock,
  Picture,
  Headphone,
  View,
  Download
} from '@element-plus/icons-vue'
import TypewriterText from '../components/TypewriterText.vue'
import MediaViewer from '../components/MediaViewer.vue'
import { taskService } from '../services/taskService'
import { submissionService } from '../services/submissionService'
import { courseEnrollmentService } from '../services/courseEnrollmentService'
import { resourceService } from '../services/resourceService'

export default {
  name: 'StudentDashboardView',
  components: {
    TypewriterText,
    MediaViewer,
    Document,
    VideoPlay,
    Edit,
    Files,
    Trophy,
    Tools,
    Clock,
    Picture,
    Headphone,
    View,
    Download
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const user = computed(() => store.getters.user)
    
    // Reactive data
    const showWelcomeMessage = ref(false)
    const loading = ref(false)
    const submitting = ref(false)
    const enrolledCourses = ref([])
    const allTasks = ref([])
    const submissions = ref([])
    const selectedCourseId = ref(null)
    const taskDetailVisible = ref(false)
    const submissionDialogVisible = ref(false)
    const submissionViewVisible = ref(false)
    const selectedTask = ref(null)
    const currentSubmission = ref(null)
    const taskResources = ref([])
    const fileList = ref([])
    
    // MediaViewer data
    const mediaViewerVisible = ref(false)
    const selectedResource = ref(null)
    const currentStudentId = computed(() => user.value?.id)
    
    const submissionForm = reactive({
      taskId: null,
      studentId: null,
      submissionContent: '',
      completionStatus: 2 // submitted
    })
    
    const submissionRules = {
      submissionContent: [
        { required: true, message: 'Please provide submission content', trigger: 'blur' }
      ]
    }
    
    // Computed properties
    const filteredTasks = computed(() => {
      if (!selectedCourseId.value) {
        return allTasks.value
      }
      return allTasks.value.filter(task => task.courseId === selectedCourseId.value)
    })
    
    const totalTasks = computed(() => allTasks.value.length)
    
    const pendingTasks = computed(() => {
      return allTasks.value.filter(task => {
        const submission = submissions.value.find(s => s.taskId === task.id)
        return !submission || submission.completionStatus < 2
      }).length
    })
    
    const completedTasks = computed(() => {
      return allTasks.value.filter(task => {
        const submission = submissions.value.find(s => s.taskId === task.id)
        return submission && submission.completionStatus >= 2
      }).length
    })
    
    // Methods
    const onDashboardTitleComplete = () => {
      showWelcomeMessage.value = true
    }
    
    const loadEnrolledCourses = async () => {
      try {
        const response = await courseEnrollmentService.getCoursesByStudent(user.value.referenceId)
        enrolledCourses.value = response.data
      } catch (error) {
        console.error('Error loading enrolled courses:', error)
        ElMessage.error('Failed to load enrolled courses')
      }
    }
    
    const loadTasksForCourses = async () => {
      try {
        loading.value = true
        const taskPromises = enrolledCourses.value.map(async (course) => {
          const response = await taskService.getTasksForCourse(course.id)
          return response.data.map(task => ({
            ...task,
            courseName: course.courseName
          }))
        })
        
        const taskArrays = await Promise.all(taskPromises)
        allTasks.value = taskArrays.flat()
      } catch (error) {
        console.error('Error loading tasks:', error)
        ElMessage.error('Failed to load tasks')
      } finally {
        loading.value = false
      }
    }
    
    const loadSubmissions = async () => {
      try {
        const response = await submissionService.getSubmissionsByStudent(user.value.referenceId)
        submissions.value = response.data
      } catch (error) {
        console.error('Error loading submissions:', error)
        ElMessage.error('Failed to load submissions')
      }
    }
    
    const handleCourseFilter = () => {
      // Filter is handled by computed property
    }
    
    const viewTask = async (task) => {
      selectedTask.value = task
      taskDetailVisible.value = true
      
      // Load task resources if needed
      try {
        const response = await taskService.getTaskResources?.(task.id)
        if (response) {
          taskResources.value = response.data
        }
      } catch (error) {
        console.error('Error loading task resources:', error)
      }
    }
    
    const showSubmissionDialog = (task) => {
      selectedTask.value = task
      submissionForm.taskId = task.id
      submissionForm.studentId = user.value.referenceId
      submissionForm.submissionContent = ''
      fileList.value = []
      submissionDialogVisible.value = true
    }
    
    const viewSubmission = (task) => {
      selectedTask.value = task
      currentSubmission.value = submissions.value.find(s => s.taskId === task.id)
      submissionViewVisible.value = true
    }
    
    const canSubmit = (task) => {
      const submission = submissions.value.find(s => s.taskId === task.id)
      const now = new Date()
      const deadline = new Date(task.deadline)
      
      return (!submission || submission.completionStatus < 2) && now <= deadline
    }
    
    const hasSubmission = (task) => {
      const submission = submissions.value.find(s => s.taskId === task.id)
      return submission && submission.completionStatus >= 2
    }
    
    const getTaskStatus = (task) => {
      const submission = submissions.value.find(s => s.taskId === task.id)
      const now = new Date()
      const deadline = new Date(task.deadline)
      
      if (!submission) {
        return now > deadline ? 'Overdue' : 'Not Started'
      }
      
      switch (submission.completionStatus) {
        case 0: return 'Not Started'
        case 1: return 'In Progress'
        case 2: return 'Submitted'
        case 3: return 'Graded'
        default: return 'Unknown'
      }
    }
    
    const handleFileChange = (file) => {
      submissionForm.submissionContent = file.name
    }
    
    const submitTask = async () => {
      try {
        submitting.value = true
        
        const submissionData = {
          taskId: submissionForm.taskId,
          studentId: submissionForm.studentId,
          submissionContent: submissionForm.submissionContent,
          submissionTime: new Date().toISOString(),
          completionStatus: submissionForm.completionStatus
        }
        
        await submissionService.createSubmission(submissionData)
        
        ElMessage.success('Task submitted successfully!')
        submissionDialogVisible.value = false
        
        // Reload submissions
        await loadSubmissions()
      } catch (error) {
        console.error('Error submitting task:', error)
        ElMessage.error('Failed to submit task')
      } finally {
        submitting.value = false
      }
    }
    
    const handleTaskDetailClose = () => {
      taskDetailVisible.value = false
      selectedTask.value = null
      taskResources.value = []
    }
    
    const handleSubmissionDialogClose = () => {
      submissionDialogVisible.value = false
      selectedTask.value = null
      submissionForm.submissionContent = ''
      fileList.value = []
    }
    
    // Utility methods
    const getTaskTypeLabel = (type) => {
      const labels = {
        'CHAPTER_HOMEWORK': 'Homework',
        'EXAM_QUIZ': 'Quiz',
        'VIDEO_WATCH': 'Video',
        'READING_MATERIAL': 'Reading',
        'REPORT_UPLOAD': 'Report',
        'PRACTICE_PROJECT': 'Project'
      }
      return labels[type] || type
    }
    
    const getTaskTypeColor = (type) => {
      const colors = {
        'CHAPTER_HOMEWORK': 'primary',
        'EXAM_QUIZ': 'danger',
        'VIDEO_WATCH': 'success',
        'READING_MATERIAL': 'info',
        'REPORT_UPLOAD': 'warning',
        'PRACTICE_PROJECT': 'primary'
      }
      return colors[type] || 'info'
    }
    
    const getStatusColor = (status) => {
      const colors = {
        'Not Started': 'info',
        'In Progress': 'warning',
        'Submitted': 'success',
        'Graded': 'primary',
        'Overdue': 'danger'
      }
      return colors[status] || 'info'
    }
    
    const getSubmissionStatusColor = (status) => {
      const colors = {
        0: 'info',
        1: 'warning',
        2: 'success',
        3: 'primary'
      }
      return colors[status] || 'info'
    }
    
    const getSubmissionStatusText = (status) => {
      const texts = {
        0: 'Not Started',
        1: 'In Progress',
        2: 'Submitted',
        3: 'Graded'
      }
      return texts[status] || 'Unknown'
    }
    
    const formatDeadline = (deadline) => {
      return new Date(deadline).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const formatDateTime = (dateTime) => {
      return new Date(dateTime).toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const getDeadlineClass = (deadline) => {
      const now = new Date()
      const deadlineDate = new Date(deadline)
      const timeDiff = deadlineDate - now
      const daysDiff = timeDiff / (1000 * 60 * 60 * 24)
      
      if (daysDiff < 0) return 'overdue'
      if (daysDiff < 1) return 'urgent'
      if (daysDiff < 3) return 'warning'
      return 'normal'
    }
    
    const truncateText = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }
    
    // Resource handling methods
    const viewResource = (resource) => {
      selectedResource.value = resource
      mediaViewerVisible.value = true
    }
    
    const closeMediaViewer = () => {
      mediaViewerVisible.value = false
      selectedResource.value = null
    }
    
    const downloadResource = async (resource) => {
      try {
        const response = await resourceService.downloadResource(resource.id)
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        // Add file extension if not already present
        let filename = resource.fileName || resource.resourceName
        if (!filename.toLowerCase().endsWith('.' + resource.fileType.toLowerCase())) {
          filename += '.' + resource.fileType.toLowerCase()
        }
        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
        ElMessage.success('Resource downloaded successfully')
      } catch (error) {
        console.error('Error downloading resource:', error)
        ElMessage.error('Failed to download resource')
      }
    }
    
    const handleProgressUpdate = (progress) => {
      console.log('Progress updated:', progress)
    }
    
    const handlePlaybackComplete = () => {
      ElMessage.success('Playback completed!')
    }
    
    // File type checking methods
    const isVideoFile = (fileName) => {
      if (!fileName) return false
      const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm', '.mkv']
      return videoExtensions.some(ext => fileName.toLowerCase().endsWith(ext))
    }
    
    const isImageFile = (fileName) => {
      if (!fileName) return false
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.svg', '.webp']
      return imageExtensions.some(ext => fileName.toLowerCase().endsWith(ext))
    }
    
    const isPdfFile = (fileName) => {
      if (!fileName) return false
      return fileName.toLowerCase().endsWith('.pdf')
    }
    
    const isAudioFile = (fileName) => {
      if (!fileName) return false
      const audioExtensions = ['.mp3', '.wav', '.ogg', '.aac', '.flac', '.m4a']
      return audioExtensions.some(ext => fileName.toLowerCase().endsWith(ext))
    }
    
    const formatFileSize = (bytes) => {
      if (!bytes) return ''
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      if (bytes === 0) return '0 Bytes'
      const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)))
      return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
    }
    
    // Navigation methods for quick action cards
    const navigateToAbilities = () => {
      router.push('/my-abilities')
    }

    const navigateToGrades = () => {
      // TODO: Implement grades view when available
      ElMessage.info('Grades view coming soon!')
    }

    const navigateToResources = () => {
      router.push('/resources')
    }

    // Initialize data
    onMounted(async () => {
      await loadEnrolledCourses()
      await loadTasksForCourses()
      await loadSubmissions()
    })

    return {
      user,
      showWelcomeMessage,
      loading,
      submitting,
      enrolledCourses,
      allTasks,
      submissions,
      selectedCourseId,
      filteredTasks,
      totalTasks,
      pendingTasks,
      completedTasks,
      taskDetailVisible,
      submissionDialogVisible,
      submissionViewVisible,
      selectedTask,
      currentSubmission,
      taskResources,
      fileList,
      submissionForm,
      submissionRules,
      mediaViewerVisible,
      selectedResource,
      currentStudentId,
      onDashboardTitleComplete,
      handleCourseFilter,
      viewTask,
      showSubmissionDialog,
      viewSubmission,
      canSubmit,
      hasSubmission,
      getTaskStatus,
      handleFileChange,
      submitTask,
      handleTaskDetailClose,
      handleSubmissionDialogClose,
      getTaskTypeLabel,
      getTaskTypeColor,
      getStatusColor,
      getSubmissionStatusColor,
      getSubmissionStatusText,
      formatDeadline,
      formatDateTime,
      getDeadlineClass,
      truncateText,
      viewResource,
      closeMediaViewer,
      downloadResource,
      handleProgressUpdate,
      handlePlaybackComplete,
      isVideoFile,
      isImageFile,
      isPdfFile,
      isAudioFile,
      formatFileSize,
      navigateToAbilities,
      navigateToGrades,
      navigateToResources
    }
  }
}
</script>

<style scoped>
.student-dashboard-container {
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
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.courses-icon {
  background: linear-gradient(45deg, #667eea, #764ba2);
}

.tasks-icon {
  background: linear-gradient(45deg, #f093fb, #f5576c);
}

.pending-icon {
  background: linear-gradient(45deg, #ffecd2, #fcb69f);
}

.completed-icon {
  background: linear-gradient(45deg, #a8edea, #fed6e3);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-header h3 {
  margin: 0;
  color: #333;
}

.tasks-section {
  margin-bottom: 20px;
}

.tasks-card {
  border: none;
  border-radius: 8px;
  overflow: hidden;
}

.task-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-icon {
  color: #667eea;
}

.deadline-cell {
  display: flex;
  align-items: center;
  gap: 5px;
}

.deadline-cell .normal {
  color: #67c23a;
}

.deadline-cell .warning {
  color: #e6a23c;
}

.deadline-cell .urgent {
  color: #f56c6c;
}

.deadline-cell .overdue {
  color: #f56c6c;
  font-weight: bold;
}

.description-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-buttons {
  display: flex;
  gap: 5px;
  justify-content: center;
}

.task-detail {
  padding: 20px 0;
}

.task-resources {
  margin-top: 20px;
}

.task-resources h4 {
  margin-bottom: 10px;
  color: #333;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 5px;
  margin-bottom: 5px;
}

.submission-view {
  padding: 20px 0;
}

.submission-content {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 5px;
  max-height: 200px;
  overflow-y: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* Responsive design */
@media (max-width: 768px) {
  .dashboard-header h2 {
    font-size: 2rem;
  }
  
  .welcome-message {
    font-size: 1rem;
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
  
  .filter-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}

/* Resource grid styles */
.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.resource-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  transition: all 0.3s ease;
}

.resource-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.resource-info {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.resource-icon {
  margin-right: 12px;
  font-size: 24px;
  color: #409eff;
}

.resource-details h5 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  word-break: break-word;
}

.resource-details p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.resource-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.media-viewer-container {
  min-height: 400px;
}

@media (max-width: 768px) {
  .resources-grid {
    grid-template-columns: 1fr;
  }
  
  .resource-actions {
    justify-content: center;
  }
}
</style>