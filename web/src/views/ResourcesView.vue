<template>
  <div class="resources-container">
    <div class="section-header">
      <h2>Resource Management</h2>
      <div class="section-actions">
        <el-button type="primary" @click="showUploadDialog = true">
          <el-icon><Upload /></el-icon>
          Upload Resource
        </el-button>
      </div>
    </div>

    <!-- Resources Grid -->
    <div v-loading="loading" class="resources-grid">
      <el-card
        v-for="resource in resources"
        :key="resource.id"
        shadow="hover"
        class="resource-card"
      >
        <template #header>
          <div class="resource-header">
            <div class="resource-title">
              <el-icon class="resource-icon">
                <Document v-if="resource.fileType === 'pdf'"/>
                <VideoPlay v-else-if="resource.fileType === 'mp4'"/>
                <Picture v-else-if="['jpg', 'jpeg', 'png'].includes(resource.fileType)"/>
                <Files v-else/>
              </el-icon>
              <span>{{ resource.resourceName }}</span>
            </div>
            <div class="resource-actions">
              <el-button
                type="primary"
                size="small"
                @click="downloadResource(resource)"
              >
                <el-icon><Download /></el-icon>
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="confirmDelete(resource)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="resource-content">
          <div class="resource-info">
            <p class="resource-description">{{ resource.description || 'No description provided' }}</p>
            <div class="resource-meta">
              <div class="file-info">
                <el-icon><Files /></el-icon>
                <span>{{ formatFileSize(resource.fileSize) }}</span>
                <el-tag size="small" type="info">{{ resource.fileType.toUpperCase() }}</el-tag>
              </div>
              <div class="upload-info">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDate(resource.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && resources.length === 0" class="empty-state">
      <el-empty description="No resources uploaded yet">
        <el-button type="primary" @click="showUploadDialog = true">
          Upload Your First Resource
        </el-button>
      </el-empty>
    </div>

    <!-- Upload Dialog -->
    <el-dialog
      v-model="showUploadDialog"
      title="Upload Resource"
      width="500px"
      :before-close="handleUploadDialogClose"
    >
      <el-form
        ref="uploadFormRef"
        :model="uploadForm"
        :rules="uploadRules"
        label-width="120px"
      >
        <el-form-item label="Resource Name" prop="resourceName">
          <el-input
            v-model="uploadForm.resourceName"
            placeholder="Enter resource name"
          />
        </el-form-item>
        
        <el-form-item label="Description" prop="description">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="3"
            placeholder="Enter resource description (optional)"
          />
        </el-form-item>
        
        <el-form-item label="File" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              Drop file here or <em>click to upload</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                Supported formats: PDF, DOCX, PPTX, MP4, JPG, PNG, TXT
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleUploadDialogClose">Cancel</el-button>
          <el-button
            type="primary"
            :loading="uploading"
            @click="handleUpload"
          >
            Upload
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload,
  Download,
  Delete,
  Document,
  VideoPlay,
  Picture,
  Files,
  Clock,
  UploadFilled
} from '@element-plus/icons-vue'
import { resourceService } from '@/services/resourceService'

export default {
  name: 'ResourcesView',
  components: {
    Upload,
    Download,
    Delete,
    Document,
    VideoPlay,
    Picture,
    Files,
    Clock,
    UploadFilled
  },
  setup() {
    const loading = ref(false)
    const uploading = ref(false)
    const resources = ref([])
    const showUploadDialog = ref(false)
    const uploadFormRef = ref(null)
    const uploadRef = ref(null)
    
    const uploadForm = reactive({
      resourceName: '',
      description: '',
      file: null
    })
    
    const uploadRules = {
      resourceName: [
        { required: true, message: 'Please enter resource name', trigger: 'blur' }
      ],
      file: [
        { required: true, message: 'Please select a file', trigger: 'change' }
      ]
    }
    
    const loadResources = async () => {
      loading.value = true
      try {
        const response = await resourceService.getAllResources()
        resources.value = response.data
      } catch (error) {
        console.error('Error loading resources:', error)
        ElMessage.error('Failed to load resources')
      } finally {
        loading.value = false
      }
    }
    
    const handleFileChange = (file) => {
      uploadForm.file = file.raw
      if (!uploadForm.resourceName) {
        uploadForm.resourceName = file.name.split('.')[0]
      }
    }
    
    const handleFileRemove = () => {
      uploadForm.file = null
    }
    
    const handleUpload = async () => {
      if (!uploadFormRef.value) return
      
      try {
        await uploadFormRef.value.validate()
        
        uploading.value = true
        await resourceService.uploadResource(
          uploadForm.file,
          uploadForm.resourceName,
          uploadForm.description
        )
        
        ElMessage.success('Resource uploaded successfully')
        showUploadDialog.value = false
        resetUploadForm()
        await loadResources()
      } catch (error) {
        console.error('Error uploading resource:', error)
        ElMessage.error('Failed to upload resource')
      } finally {
        uploading.value = false
      }
    }
    
    const handleUploadDialogClose = () => {
      showUploadDialog.value = false
      resetUploadForm()
    }
    
    const resetUploadForm = () => {
      uploadForm.resourceName = ''
      uploadForm.description = ''
      uploadForm.file = null
      if (uploadRef.value) {
        uploadRef.value.clearFiles()
      }
      if (uploadFormRef.value) {
        uploadFormRef.value.resetFields()
      }
    }
    
    const downloadResource = async (resource) => {
      try {
        const response = await resourceService.downloadResource(resource.id)
        
        // Create blob and download
        const blob = new Blob([response.data])
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = resource.resourceName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('Download started')
      } catch (error) {
        console.error('Error downloading resource:', error)
        ElMessage.error('Failed to download resource')
      }
    }
    
    const confirmDelete = async (resource) => {
      try {
        await ElMessageBox.confirm(
          `Are you sure you want to delete "${resource.resourceName}"?`,
          'Confirm Delete',
          {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )
        
        await resourceService.deleteResource(resource.id)
        ElMessage.success('Resource deleted successfully')
        await loadResources()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Error deleting resource:', error)
          ElMessage.error('Failed to delete resource')
        }
      }
    }
    
    const formatFileSize = (bytes) => {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'Unknown'
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    onMounted(() => {
      loadResources()
    })
    
    return {
      loading,
      uploading,
      resources,
      showUploadDialog,
      uploadForm,
      uploadRules,
      uploadFormRef,
      uploadRef,
      handleFileChange,
      handleFileRemove,
      handleUpload,
      handleUploadDialogClose,
      downloadResource,
      confirmDelete,
      formatFileSize,
      formatDate
    }
  }
}
</script>

<style scoped>
.resources-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-header h2 {
  margin: 0;
  color: #2c3e50;
  font-weight: 600;
}

.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.resource-card {
  transition: transform 0.2s;
}

.resource-card:hover {
  transform: translateY(-2px);
}

.resource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resource-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.resource-icon {
  font-size: 18px;
  color: #3498db;
}

.resource-actions {
  display: flex;
  gap: 8px;
}

.resource-content {
  padding: 16px 0;
}

.resource-description {
  color: #7f8c8d;
  margin-bottom: 16px;
  line-height: 1.5;
}

.resource-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-info,
.upload-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #95a5a6;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.el-upload__tip {
  font-size: 12px;
  color: #95a5a6;
  margin-top: 8px;
}
</style>