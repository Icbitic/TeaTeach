<template>
  <div class="submissions-container">
    <div class="page-header">
      <h2><TypewriterText :text="'Student Submissions'" :show="true" :speed="70" /></h2>
      <p class="page-description">Review, score, and provide feedback on student task submissions</p>
    </div>

    <!-- Filters -->
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="selectedCourse" placeholder="Select Course" @change="loadTasks" clearable>
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedTask" placeholder="Select Task" @change="loadSubmissions" clearable>
            <el-option
              v-for="task in tasks"
              :key="task.id"
              :label="task.taskName"
              :value="task.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="Filter by Status" @change="filterSubmissions" clearable>
            <el-option label="All" value="" />
            <el-option label="Submitted" value="2" />
            <el-option label="Graded" value="3" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="loadSubmissions" :loading="loading">
            <el-icon><Refresh /></el-icon>
            Refresh
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- Submissions Table -->
    <el-card class="submissions-table-card" shadow="never">
      <el-table
        :data="filteredSubmissions"
        v-loading="loading"
        stripe
        style="width: 100%"
        @row-click="viewSubmission"
        :header-cell-style="{ backgroundColor: '#f5f7fa', fontWeight: 'bold' }"
        border
      >
        <el-table-column prop="studentName" label="Student" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div class="student-cell">
              <el-icon class="student-icon"><User /></el-icon>
              {{ scope.row.studentName }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="taskName" label="Task" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <div class="task-cell">
              <el-icon class="task-icon"><Document /></el-icon>
              {{ scope.row.taskName }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="submissionTime" label="Submitted At" width="180" align="center">
          <template #default="scope">
            <div class="time-cell">
              <el-icon class="time-icon"><Clock /></el-icon>
              {{ formatDateTime(scope.row.submissionTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="Score" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.score !== null" type="success" size="large">
              {{ scope.row.score }}/100
            </el-tag>
            <el-tag v-else type="warning" size="large">Pending</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="completionStatus" label="Status" width="130" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.completionStatus)" size="large">
              {{ getStatusText(scope.row.completionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="220" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                type="primary"
                size="small"
                @click.stop="gradeSubmission(scope.row)"
              >
                {{ scope.row.score !== null ? 'Edit Grade' : 'Grade' }}
              </el-button>
              <el-button
                type="info"
                size="small"
                @click.stop="viewSubmissionFiles(scope.row)"
              >
                Files
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Grading Dialog -->
    <el-dialog
      v-model="gradingDialogVisible"
      title="Grade Submission"
      width="600px"
      @close="resetGradingForm"
    >
      <div v-if="currentSubmission">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="Student">{{ currentSubmission.studentName }}</el-descriptions-item>
          <el-descriptions-item label="Task">{{ currentSubmission.taskName }}</el-descriptions-item>
          <el-descriptions-item label="Submitted At">{{ formatDateTime(currentSubmission.submissionTime) }}</el-descriptions-item>
          <el-descriptions-item label="Current Score">{{ currentSubmission.score || 'Not graded' }}</el-descriptions-item>
        </el-descriptions>

        <div class="submission-content" v-if="currentSubmission.submissionContent">
          <h4>Submission Content:</h4>
          <el-card class="content-card">
            <p>{{ currentSubmission.submissionContent }}</p>
          </el-card>
        </div>

        <el-form :model="gradingForm" :rules="gradingRules" ref="gradingFormRef" label-width="100px" class="grading-form">
          <el-form-item label="Score" prop="score">
            <el-input-number
              v-model="gradingForm.score"
              :min="0"
              :max="100"
              :precision="1"
              placeholder="Enter score (0-100)"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="Feedback" prop="feedback">
            <el-input
              v-model="gradingForm.feedback"
              type="textarea"
              :rows="4"
              placeholder="Provide feedback for the student..."
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="gradingDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitGrade" :loading="grading">
            Submit Grade
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Files Dialog -->
    <el-dialog
      v-model="filesDialogVisible"
      title="Submission Files"
      width="500px"
    >
      <div v-if="submissionFiles.length > 0">
        <el-list>
          <el-list-item v-for="file in submissionFiles" :key="file.id">
            <div class="file-item">
              <el-icon><Document /></el-icon>
              <span class="file-name">{{ file.fileName }}</span>
              <el-button
                type="primary"
                size="small"
                @click="downloadFile(file)"
              >
                Download
              </el-button>
            </div>
          </el-list-item>
        </el-list>
      </div>
      <div v-else class="no-files">
        <el-empty description="No files uploaded for this submission" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Document, Clock, Refresh } from '@element-plus/icons-vue'
import TypewriterText from '@/components/TypewriterText.vue'
import submissionService from '@/services/submissionService'
import courseService from '@/services/courseService'
import taskService from '@/services/taskService'
import studentService from '@/services/studentService'

export default {
  name: 'SubmissionsView',
  components: {
    TypewriterText,
    User,
    Document,
    Clock,
    Refresh
  },
  setup() {
    const loading = ref(false)
    const grading = ref(false)
    const courses = ref([])
    const tasks = ref([])
    const submissions = ref([])
    const submissionFiles = ref([])
    const selectedCourse = ref('')
    const selectedTask = ref('')
    const statusFilter = ref('')
    const gradingDialogVisible = ref(false)
    const filesDialogVisible = ref(false)
    const currentSubmission = ref(null)
    const gradingFormRef = ref(null)

    const gradingForm = reactive({
      score: null,
      feedback: ''
    })

    const gradingRules = {
      score: [
        { required: true, message: 'Please enter a score', trigger: 'blur' },
        { type: 'number', min: 0, max: 100, message: 'Score must be between 0 and 100', trigger: 'blur' }
      ]
    }

    const filteredSubmissions = computed(() => {
      if (!statusFilter.value) return submissions.value
      return submissions.value.filter(submission => 
        submission.completionStatus.toString() === statusFilter.value
      )
    })

    const loadCourses = async () => {
      try {
        const response = await courseService.getAllCourses()
        courses.value = response.data
      } catch (error) {
        ElMessage.error('Failed to load courses')
      }
    }

    const loadTasks = async () => {
      if (!selectedCourse.value) {
        tasks.value = []
        return
      }
      try {
        const response = await taskService.getTasksForCourse(selectedCourse.value)
        tasks.value = response.data
      } catch (error) {
        ElMessage.error('Failed to load tasks')
      }
    }

    const loadSubmissions = async () => {
      if (!selectedTask.value) {
        submissions.value = []
        return
      }
      
      loading.value = true
      try {
        const response = await submissionService.getSubmissionsByTask(selectedTask.value)
        const submissionsData = response.data
        
        // Get all students first to avoid multiple API calls
        let studentsMap = new Map()
        try {
          const studentsResponse = await studentService.getAllStudents()
          studentsResponse.data.forEach(student => {
            studentsMap.set(student.id, student)
          })
        } catch (error) {
          console.error('Failed to load students:', error)
        }
        
        // Enrich submissions with student and task names
        for (let submission of submissionsData) {
          // Get student name from the map
          const student = studentsMap.get(submission.studentId)
          if (student) {
            submission.studentName = student.user?.username || student.name || `Student ${submission.studentId}`
          } else {
            submission.studentName = `Student ${submission.studentId}`
          }
          
          // Get task name
          const task = tasks.value.find(t => t.id === submission.taskId)
          submission.taskName = task?.taskName || `Task ${submission.taskId}`
        }
        
        submissions.value = submissionsData
      } catch (error) {
        console.error('Error loading submissions:', error)
        ElMessage.error('Failed to load submissions')
      } finally {
        loading.value = false
      }
    }

    const filterSubmissions = () => {
      // Filtering is handled by computed property
    }

    const viewSubmission = () => {
      // Could expand to show more details
    }

    const gradeSubmission = (submission) => {
      currentSubmission.value = submission
      gradingForm.score = submission.score
      gradingForm.feedback = submission.feedback || ''
      gradingDialogVisible.value = true
    }

    const submitGrade = async () => {
      if (!gradingFormRef.value) return
      
      try {
        await gradingFormRef.value.validate()
        grading.value = true
        
        await submissionService.gradeSubmission(
          currentSubmission.value.id,
          gradingForm.score,
          gradingForm.feedback
        )
        
        ElMessage.success('Grade submitted successfully')
        gradingDialogVisible.value = false
        loadSubmissions() // Refresh the list
      } catch (error) {
        if (error.response) {
          ElMessage.error('Failed to submit grade')
        }
      } finally {
        grading.value = false
      }
    }

    const resetGradingForm = () => {
      gradingForm.score = null
      gradingForm.feedback = ''
      currentSubmission.value = null
      if (gradingFormRef.value) {
        gradingFormRef.value.clearValidate()
      }
    }

    const viewSubmissionFiles = async (submission) => {
      try {
        const response = await submissionService.getSubmissionFiles(submission.id)
        submissionFiles.value = response.data
        filesDialogVisible.value = true
      } catch (error) {
        ElMessage.error('Failed to load submission files')
      }
    }

    const downloadFile = async (file) => {
      try {
        const response = await submissionService.downloadSubmissionFile(file.id)
        
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', file.fileName)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
      } catch (error) {
        ElMessage.error('Failed to download file')
      }
    }

    const formatDateTime = (dateTime) => {
      if (!dateTime) return 'N/A'
      return new Date(dateTime).toLocaleString()
    }

    const getStatusType = (status) => {
      switch (status) {
        case 0: return 'info'
        case 1: return 'warning'
        case 2: return 'primary'
        case 3: return 'success'
        default: return 'info'
      }
    }

    const getStatusText = (status) => {
      switch (status) {
        case 0: return 'Not Started'
        case 1: return 'In Progress'
        case 2: return 'Submitted'
        case 3: return 'Graded'
        default: return 'Unknown'
      }
    }

    onMounted(() => {
      loadCourses()
    })

    return {
      loading,
      grading,
      courses,
      tasks,
      submissions,
      submissionFiles,
      selectedCourse,
      selectedTask,
      statusFilter,
      gradingDialogVisible,
      filesDialogVisible,
      currentSubmission,
      gradingForm,
      gradingRules,
      gradingFormRef,
      filteredSubmissions,
      loadTasks,
      loadSubmissions,
      filterSubmissions,
      viewSubmission,
      gradeSubmission,
      submitGrade,
      resetGradingForm,
      viewSubmissionFiles,
      downloadFile,
      formatDateTime,
      getStatusType,
      getStatusText
    }
  }
}
</script>

<style scoped>
.submissions-container {
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

.submissions-table-card {
  margin-bottom: 20px;
}

.grading-form {
  margin-top: 20px;
}

.submission-content {
  margin: 20px 0;
}

.submission-content h4 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.content-card {
  background-color: #f8f9fa;
}

.content-card p {
  margin: 0;
  white-space: pre-wrap;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-files {
  text-align: center;
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* Table cell styling */
.student-cell,
.task-cell,
.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.student-icon,
.task-icon,
.time-icon {
  color: #409eff;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

/* Improve table header styling */
:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa !important;
  font-weight: bold;
  color: #2c3e50;
  border-bottom: 2px solid #e4e7ed;
}

/* Improve table cell padding */
:deep(.el-table td.el-table__cell) {
  padding: 12px 8px;
}

/* Better tag styling */
:deep(.el-tag) {
  font-weight: 500;
  border-radius: 6px;
}

/* Responsive table */
@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .student-cell,
  .task-cell,
  .time-cell {
    font-size: 12px;
  }
}
</style>