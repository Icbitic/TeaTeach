<template>
  <div class="tasks-container">
    <div class="section-header">
      <h2>Task Management</h2>
      <div class="section-actions">
        <el-select
          v-model="selectedCourse"
          placeholder="Filter by Course"
          clearable
          style="width: 200px; margin-right: 10px"
          @change="handleCourseFilter"
        >
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="course.courseName"
            :value="course.id"
          />
        </el-select>
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          Add Task
        </el-button>
      </div>
    </div>

    <!-- Tasks Table -->
    <el-card shadow="hover" class="tasks-card">
      <el-table
        :data="filteredTasks"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="No tasks found"
      >
        <el-table-column prop="taskName" label="Task Name" min-width="150">
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
        
        <el-table-column prop="courseName" label="Course" width="150" />
        
        <el-table-column prop="taskType" label="Type" width="140">
          <template #default="{ row }">
            <el-tag :type="getTaskTypeColor(row.taskType)" size="small">
              {{ getTaskTypeLabel(row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="deadline" label="Deadline" width="160">
          <template #default="{ row }">
            <div class="deadline-cell">
              <el-icon><Clock /></el-icon>
              <span :class="getDeadlineClass(row.deadline)">
                {{ formatDeadline(row.deadline) }}
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="submissionMethod" label="Submission" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">
              {{ row.submissionMethod }}
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
        
        <el-table-column label="Resources" width="200" align="center">
          <template #default="{ row }">
            <div class="resource-cell">
              <el-button
                type="primary"
                size="small"
                @click="showResourcesDialog(row)"
                :loading="loadingTaskResources[row.id]"
              >
                <el-icon><Files /></el-icon>
                Manage Files ({{ getResourceCount(row.id) }})
              </el-button>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="Actions" width="220" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="default"
              @click="editTask(row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              type="danger"
              size="default"
              @click="deleteTask(row)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Resources Management Dialog -->
    <el-dialog
      v-model="resourcesDialogVisible"
      :title="`Manage Files - ${selectedTask?.title || ''}`"
      width="800px"
      @close="closeResourcesDialog"
    >
      <div class="resources-dialog-content">
        <!-- Upload Section -->
        <div class="upload-section">
          <h4>Upload New Files</h4>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileSelect"
            :file-list="uploadFileList"
            multiple
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              Drop files here or <em>click to upload</em>
            </div>
          </el-upload>
          <div class="upload-actions" v-if="uploadFileList.length > 0">
            <el-button type="primary" @click="uploadFiles" :loading="uploading">
              Upload Selected Files
            </el-button>
            <el-button @click="clearUploadList">Clear</el-button>
          </div>
        </div>

        <!-- Existing Files Section -->
        <div class="existing-files-section">
          <h4>Existing Files</h4>
          <div v-if="taskResources.length === 0" class="no-files">
            <el-empty description="No files uploaded yet" />
          </div>
          <div v-else class="files-list">
            <div
              v-for="resource in taskResources"
              :key="resource.id"
              class="file-item"
            >
              <div class="file-info">
                <el-icon><Document /></el-icon>
                <span class="file-name">{{ resource.resourceName }}</span>
                <span class="file-size">{{ formatFileSize(resource.fileSize) }}</span>
              </div>
              <div class="file-actions">
                <el-button
                  type="success"
                  size="small"
                  @click="downloadTaskResource(resource.id)"
                  :loading="downloadingResources[resource.id]"
                >
                  <el-icon><Download /></el-icon>
                  Download
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="confirmDeleteResource(resource)"
                  :loading="deletingResources[resource.id]"
                >
                  <el-icon><Delete /></el-icon>
                  Delete
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Create/Edit Task Dialog -->
    <el-dialog
      :title="dialogMode === 'create' ? 'Create New Task' : 'Edit Task'"
      v-model="dialogVisible"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskFormRules"
        label-width="120px"
        label-position="left"
      >
        <el-form-item label="Course" prop="courseId">
          <el-select
            v-model="taskForm.courseId"
            placeholder="Select a course"
            style="width: 100%"
          >
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="Task Name" prop="taskName">
          <el-input
            v-model="taskForm.taskName"
            placeholder="Enter task name"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="Task Type" prop="taskType">
          <el-select
            v-model="taskForm.taskType"
            placeholder="Select task type"
            style="width: 100%"
          >
            <el-option
              v-for="type in taskTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            >
              <div class="task-type-option">
                <el-icon class="option-icon">
                  <component :is="type.icon" />
                </el-icon>
                <span>{{ type.label }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="Description" prop="taskDescription">
          <el-input
            v-model="taskForm.taskDescription"
            type="textarea"
            :rows="4"
            placeholder="Enter task description"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="Deadline" prop="deadline">
          <el-date-picker
            v-model="taskForm.deadline"
            type="datetime"
            placeholder="Select deadline"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        
        <el-form-item label="Submission" prop="submissionMethod">
          <el-select
            v-model="taskForm.submissionMethod"
            placeholder="Select submission method"
            style="width: 100%"
          >
            <el-option label="File Upload" value="upload" />
            <el-option label="Text Submission" value="text" />
            <el-option label="URL Submission" value="url" />
            <el-option label="No Submission" value="none" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose">Cancel</el-button>
          <el-button
            type="primary"
            @click="submitTask"
            :loading="submitting"
          >
            {{ dialogMode === 'create' ? 'Create Task' : 'Update Task' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Document,
  VideoPlay,
  Files,
  Trophy,
  Tools,
  Clock,
  UploadFilled
} from '@element-plus/icons-vue'
import taskService from '@/services/taskService'
import courseService from '@/services/courseService'
import { resourceService } from '@/services/resourceService'

export default {
  name: 'TasksView',
  components: {
    Plus,
    Edit,
    Delete,
    Document,
    VideoPlay,
    Files,
    Trophy,
    Tools,
    UploadFilled,
    Clock
  },
  setup() {
    // Reactive data
    const loading = ref(false)
    const submitting = ref(false)
    const dialogVisible = ref(false)
    const dialogMode = ref('create')
    const selectedCourse = ref(null)
    const taskFormRef = ref(null)
    const downloadingResources = ref({})
    const deletingResources = ref({})
    const loadingTaskResources = ref({})
    const taskResourcesMap = ref({})
    
    // Resources Dialog
    const resourcesDialogVisible = ref(false)
    const selectedTask = ref(null)
    const taskResources = ref([])
    const uploadFileList = ref([])
    const uploading = ref(false)
    const uploadRef = ref(null)
    
    const tasks = ref([])
    const courses = ref([])
    
    // Task form data
    const taskForm = reactive({
      id: null,
      courseId: null,
      taskName: '',
      taskType: '',
      taskDescription: '',
      deadline: null,
      submissionMethod: ''
    })
    

    
    // Task types configuration
    const taskTypes = [
      { value: 'CHAPTER_HOMEWORK', label: 'Chapter Homework', icon: 'Edit' },
      { value: 'EXAM_QUIZ', label: 'Exam/Quiz', icon: 'Trophy' },
      { value: 'VIDEO_WATCH', label: 'Video Watch', icon: 'VideoPlay' },
      { value: 'READING_MATERIAL', label: 'Reading Material', icon: 'Document' },
      { value: 'REPORT_UPLOAD', label: 'Report Upload', icon: 'Files' },
      { value: 'PRACTICE_PROJECT', label: 'Practice Project', icon: 'Tools' }
    ]
    
    // Form validation rules
    const taskFormRules = {
      courseId: [
        { required: true, message: 'Please select a course', trigger: 'change' }
      ],
      taskName: [
        { required: true, message: 'Please enter task name', trigger: 'blur' },
        { min: 2, max: 100, message: 'Length should be 2 to 100 characters', trigger: 'blur' }
      ],
      taskType: [
        { required: true, message: 'Please select task type', trigger: 'change' }
      ],
      taskDescription: [
        { required: true, message: 'Please enter task description', trigger: 'blur' },
        { max: 500, message: 'Description cannot exceed 500 characters', trigger: 'blur' }
      ],
      deadline: [
        { required: true, message: 'Please select deadline', trigger: 'change' }
      ],
      submissionMethod: [
        { required: true, message: 'Please select submission method', trigger: 'change' }
      ]
    };
    
    // Computed properties
    const filteredTasks = computed(() => {
      if (!selectedCourse.value) {
        return tasks.value
      }
      return tasks.value.filter(task => task.courseId === selectedCourse.value)
    })
    

    
    // Methods
    const loadTasks = async () => {
      try {
        loading.value = true
        const response = await taskService.getAllTasks()
        tasks.value = response.data
        
        // Enrich tasks with course names
        for (const task of tasks.value) {
          const course = courses.value.find(c => c.id === task.courseId)
          task.courseName = course ? course.courseName : 'Unknown Course'
        }
      } catch (error) {
        console.error('Error loading tasks:', error)
        ElMessage.error('Failed to load tasks')
      } finally {
        loading.value = false
      }
    }
    
    const loadCourses = async () => {
      try {
        const response = await courseService.getAllCourses()
        courses.value = response.data
      } catch (error) {
        console.error('Error loading courses:', error)
        ElMessage.error('Failed to load courses')
      }
    }
    
    const showCreateDialog = () => {
      dialogMode.value = 'create'
      resetForm()
      dialogVisible.value = true
    }
    
    const editTask = (task) => {
      dialogMode.value = 'edit'
      Object.assign(taskForm, {
        ...task,
        deadline: task.deadline ? new Date(task.deadline) : null
      })
      dialogVisible.value = true
    }
    
    const deleteTask = async (task) => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete the task "${task.taskName}"?`,
          'Confirm Delete',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )
        
        await taskService.deleteTask(task.id)
        ElMessage.success('Task deleted successfully')
        await loadTasks()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error deleting task:', error)
          ElMessage.error('Failed to delete task')
        }
      }
    }
    
    const submitTask = async () => {
      try {
        const valid = await taskFormRef.value.validate()
        if (!valid) return
        
        submitting.value = true
        
        const taskData = {
          courseId: taskForm.courseId,
          taskName: taskForm.taskName,
          taskType: taskForm.taskType,
          taskDescription: taskForm.taskDescription,
          submissionMethod: taskForm.submissionMethod,
          deadline: taskForm.deadline ? taskForm.deadline.toISOString() : null
        }
        
        // Include id only for updates
        if (dialogMode.value === 'edit') {
          taskData.id = taskForm.id
        }
        
        if (dialogMode.value === 'create') {
          await taskService.createTask(taskData)
          ElMessage.success('Task created successfully')
        } else {
          await taskService.updateTask(taskForm.id, taskData)
          ElMessage.success('Task updated successfully')
        }
        
        dialogVisible.value = false
        await loadTasks()
      } catch (error) {
        console.error('Error submitting task:', error)
        ElMessage.error(`Failed to ${dialogMode.value} task`)
      } finally {
        submitting.value = false
      }
    }
    
    const handleDialogClose = () => {
      dialogVisible.value = false
      resetForm()
    }
    
    const handleCourseFilter = () => {
      // Filtering is handled by computed property
    }
    
    const resetForm = () => {
      Object.assign(taskForm, {
        id: null,
        courseId: null,
        taskName: '',
        taskType: '',
        taskDescription: '',
        deadline: null,
        submissionMethod: ''
      })

      if (taskFormRef.value) {
        taskFormRef.value.clearValidate()
      }
    }
    
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
    
    const truncateText = (text, maxLength) => {
      if (!text) return ''
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
    }
    
    // Resource handling methods

    
    const disabledDate = (time) => {
      return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
    }
    
    // Resource count helper
    const getResourceCount = (taskId) => {
      return taskResourcesMap.value[taskId]?.length || 0
    }

    // Load task resources
    const loadTaskResources = async (taskId) => {
      try {
        loadingTaskResources.value[taskId] = true
        const response = await resourceService.getTaskResources(taskId)
        taskResourcesMap.value[taskId] = response.data
        return response.data
      } catch (error) {
        console.error('Failed to load task resources:', error)
        ElMessage.error('Failed to load task resources')
        return []
      } finally {
        loadingTaskResources.value[taskId] = false
      }
    }

    // Show resources dialog
    const showResourcesDialog = async (task) => {
      selectedTask.value = task
      resourcesDialogVisible.value = true
      taskResources.value = await loadTaskResources(task.id)
    }

    // Close resources dialog
    const closeResourcesDialog = () => {
      resourcesDialogVisible.value = false
      selectedTask.value = null
      taskResources.value = []
      uploadFileList.value = []
    }

    // Handle file selection for upload
    const handleFileSelect = (file, fileList) => {
      uploadFileList.value = fileList
    }

    // Clear upload list
    const clearUploadList = () => {
      uploadFileList.value = []
      uploadRef.value?.clearFiles()
    }

    // Upload files
    const uploadFiles = async () => {
      if (!selectedTask.value || uploadFileList.value.length === 0) return
      
      try {
        uploading.value = true
        
        for (const fileItem of uploadFileList.value) {
          // Upload the file first
          const uploadResponse = await resourceService.uploadResource(
            fileItem.raw,
            fileItem.name,
            `File for task: ${selectedTask.value.title}`
          )
          
          // Associate the uploaded resource with the task
          await resourceService.addResourceToTask(
            selectedTask.value.id,
            uploadResponse.data.id
          )
        }
        
        ElMessage.success(`${uploadFileList.value.length} file(s) uploaded successfully`)
        
        // Refresh task resources
        taskResources.value = await loadTaskResources(selectedTask.value.id)
        clearUploadList()
        
      } catch (error) {
        console.error('Upload failed:', error)
        ElMessage.error('Failed to upload files')
      } finally {
        uploading.value = false
      }
    }

    // Download task resource
    const downloadTaskResource = async (resourceId) => {
      try {
        downloadingResources.value[resourceId] = true
        const response = await resourceService.downloadResource(resourceId)
        
        // Create blob and download
        const blob = new Blob([response.data])
        const url = window.URL.createObjectURL(blob)
        
        // Extract filename from Content-Disposition header (case-insensitive)
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
      } finally {
        downloadingResources.value[resourceId] = false
      }
    }

    // Confirm delete resource
    const confirmDeleteResource = (resource) => {
      ElMessageBox.confirm(
        `Are you sure you want to delete "${resource.resourceName}"?`,
        'Delete File',
        {
          confirmButtonText: 'Delete',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }
      ).then(() => {
        deleteTaskResource(resource)
      })
    }

    // Delete task resource
    const deleteTaskResource = async (resource) => {
      try {
        deletingResources.value[resource.id] = true
        
        // Remove resource from task
        await resourceService.removeResourceFromTask(
          selectedTask.value.id,
          resource.id
        )
        
        // Delete the resource itself
        await resourceService.deleteResource(resource.id)
        
        ElMessage.success('File deleted successfully')
        
        // Refresh task resources
        taskResources.value = await loadTaskResources(selectedTask.value.id)
        
      } catch (error) {
        console.error('Delete failed:', error)
        ElMessage.error('Failed to delete file')
      } finally {
        deletingResources.value[resource.id] = false
      }
    }

    // Format file size
    const formatFileSize = (bytes) => {
      if (!bytes) return 'Unknown size'
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(1024))
      return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
    }
    
    // Lifecycle
    onMounted(async () => {
      await loadCourses()
      await loadTasks()
      
      // Load resource counts for all tasks
      for (const task of tasks.value) {
        await loadTaskResources(task.id)
      }
    })
    
    return {
      // Reactive data
      loading,
      submitting,
      dialogVisible,
      dialogMode,
      selectedCourse,
      taskFormRef,
      tasks,
      courses,
      taskForm,
      taskTypes,
      taskFormRules,

      downloadingResources,
      
      // Computed
      filteredTasks,
      
      // Methods
      showCreateDialog,
      editTask,
      deleteTask,
      submitTask,
      handleDialogClose,
      handleCourseFilter,
      getTaskTypeLabel,
      getTaskTypeColor,
      formatDeadline,
      getDeadlineClass,
      truncateText,
      disabledDate,
      getResourceCount,
      showResourcesDialog,
      closeResourcesDialog,
      handleFileSelect,
      clearUploadList,
      uploadFiles,
      downloadTaskResource,
      confirmDeleteResource,
      formatFileSize,
      
      // Resources dialog data
      resourcesDialogVisible,
      selectedTask,
      taskResources,
      uploadFileList,
      uploading,
      deletingResources,
      loadingTaskResources,
      taskResourcesMap
    }
  }
}
</script>

<style scoped>
.tasks-container {
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

.tasks-card {
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* Task table styles */
.task-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-icon {
  color: #409EFF;
  font-size: 16px;
}

.deadline-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.deadline-normal {
  color: #606266;
}

.deadline-soon {
  color: #E6A23C;
  font-weight: 500;
}

.deadline-urgent {
  color: #F56C6C;
  font-weight: 600;
}

.deadline-overdue {
  color: #F56C6C;
  font-weight: 600;
  text-decoration: line-through;
}

.description-cell {
  color: #606266;
  line-height: 1.4;
}

/* Dialog styles */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.task-type-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-icon {
  color: #409EFF;
  font-size: 14px;
}

/* Form styles */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-textarea__inner) {
  border-radius: 6px;
}

:deep(.el-date-editor) {
  border-radius: 6px;
}

/* Table action buttons */
:deep(.el-table .el-button + .el-button) {
  margin-left: 8px;
}

/* Responsive design */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .section-actions {
    justify-content: space-between;
  }
  
  :deep(.el-table .el-table__cell) {
    padding: 8px 4px;
  }
}

/* Loading and empty states */
:deep(.el-loading-mask) {
  border-radius: 8px;
}

:deep(.el-table__empty-text) {
  color: #909399;
  font-size: 14px;
}

/* Tag styles */
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
}

/* Button styles */
:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409EFF 0%, #66B3FF 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #337ECC 0%, #5AA3FF 100%);
}

/* Card hover effects */
.tasks-card:hover {
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
  transition: box-shadow 0.3s ease;
}

/* Dialog styles */
:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 20px 24px;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

/* Resource upload section styles */
.resource-upload-section {
  margin-top: 10px;
}

.resource-upload-section .el-upload {
  width: 100%;
}

.resource-upload-section .el-upload-dragger {
  width: 100%;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.resource-upload-section .el-upload-dragger:hover {
  border-color: #409eff;
}

.resource-upload-section .el-icon--upload {
  font-size: 28px;
  color: #c0c4cc;
  margin: 20px 0 16px;
  line-height: 50px;
}

.resource-upload-section .el-upload__text {
  color: #606266;
  font-size: 14px;
  text-align: center;
}

.resource-upload-section .el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 7px;
  text-align: center;
}

/* Resource column styles */
.resource-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.no-resource {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 32px;
}

.no-resource-text {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

.resource-cell .el-button {
  font-size: 12px;
  padding: 4px 8px;
}

.resource-cell .el-button .el-icon {
  margin-right: 4px;
}

/* Animation for task rows */
:deep(.el-table__row) {
  transition: background-color 0.2s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f8f9fa;
}

/* Resources Dialog Styles */
.resources-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.upload-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

.upload-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-weight: 600;
}

.upload-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.existing-files-section {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.existing-files-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-weight: 600;
}

.no-files {
  text-align: center;
  padding: 40px 20px;
}

.files-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background-color: #ffffff;
  transition: all 0.3s ease;
}

.file-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.file-info .el-icon {
  color: #409eff;
  font-size: 18px;
}

.file-name {
  font-weight: 500;
  color: #303133;
  margin-right: 10px;
}

.file-size {
  color: #909399;
  font-size: 12px;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.file-actions .el-button {
  padding: 5px 10px;
}
</style>