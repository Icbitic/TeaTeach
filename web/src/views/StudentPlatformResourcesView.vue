<template>
  <div class="student-platform-resources-container">
    <div class="section-header">
      <h2>Platform Resources</h2>
      <div class="section-actions">
        <el-input
          v-model="searchQuery"
          :placeholder="$t('studentResources.searchPlaceholder')"
          style="width: 300px; margin-right: 10px"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="selectedFileType"
          :placeholder="$t('studentResources.filterByType')"
          clearable
          style="width: 150px"
          @change="handleTypeFilter"
        >
          <el-option
            v-for="type in fileTypes"
            :key="type.value"
            :label="type.label"
            :value="type.value"
          />
        </el-select>
      </div>
    </div>

    <!-- Resources Grid -->
    <div v-loading="loading" class="resources-grid">
      <el-card
        v-for="resource in filteredResources"
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
            <el-tag :type="getFileTypeColor(resource.fileType)" size="small">
              {{ resource.fileType.toUpperCase() }}
            </el-tag>
          </div>
        </template>
        
        <div class="resource-content">
          <div class="resource-info">
            <p class="resource-description">{{ resource.description || 'No description provided' }}</p>
            <div class="resource-meta">
              <div class="file-info">
                <el-icon><Files /></el-icon>
                <span>{{ formatFileSize(resource.fileSize) }}</span>
              </div>
              <div class="upload-info">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDate(resource.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <template #footer>
          <div class="resource-actions">
            <el-button
              type="primary"
              @click="viewResource(resource)"
              v-if="isViewableFile(resource.fileType)"
            >
              <el-icon><View /></el-icon>
              View
            </el-button>
            <el-button
              type="success"
              @click="downloadResource(resource)"
            >
              <el-icon><Download /></el-icon>
              Download
            </el-button>
          </div>
        </template>
      </el-card>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && filteredResources.length === 0" class="empty-state">
      <el-empty description="No resources found">
        <el-button type="primary" @click="clearFilters">Clear Filters</el-button>
      </el-empty>
    </div>

    <!-- Media Viewer Dialog -->
    <el-dialog
      v-model="mediaViewerVisible"
      :title="`Viewing: ${selectedResource?.resourceName || ''}`"
      width="90%"
      top="5vh"
      @close="closeMediaViewer"
    >
      <div class="media-viewer-container">
        <video
          v-if="selectedResource && selectedResource.fileType === 'mp4'"
          controls
          style="width: 100%; max-height: 70vh;"
        >
          <source :src="getResourceUrl(selectedResource.id)" type="video/mp4">
          Your browser does not support the video tag.
        </video>
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
  Picture,
  Files,
  Clock,
  View,
  Download,
  Search
} from '@element-plus/icons-vue'
import { resourceService } from '@/services/resourceService'

export default {
  name: 'StudentPlatformResourcesView',
  components: {
    Document,
    VideoPlay,
    Picture,
    Files,
    Clock,
    View,
    Download,
    Search
  },
  setup() {
    const loading = ref(false)
    const searchQuery = ref('')
    const selectedFileType = ref(null)
    const resources = ref([])
    const mediaViewerVisible = ref(false)
    const selectedResource = ref(null)
    
    const fileTypes = [
      { value: 'pdf', label: 'PDF' },
      { value: 'docx', label: 'Word Document' },
      { value: 'pptx', label: 'PowerPoint' },
      { value: 'mp4', label: 'Video' },
      { value: 'jpg', label: 'Image (JPG)' },
      { value: 'jpeg', label: 'Image (JPEG)' },
      { value: 'png', label: 'Image (PNG)' },
      { value: 'txt', label: 'Text File' }
    ]
    
    const filteredResources = computed(() => {
      let filtered = resources.value
      
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(resource => 
          resource.resourceName.toLowerCase().includes(query) ||
          (resource.description && resource.description.toLowerCase().includes(query))
        )
      }
      
      if (selectedFileType.value) {
        filtered = filtered.filter(resource => resource.fileType === selectedFileType.value)
      }
      
      return filtered
    })
    
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
    
    const handleSearch = () => {
      // Reactive computed property will handle the filtering
    }
    
    const handleTypeFilter = () => {
      // Reactive computed property will handle the filtering
    }
    
    const clearFilters = () => {
      searchQuery.value = ''
      selectedFileType.value = null
    }
    
    const viewResource = (resource) => {
      if (resource.fileType === 'mp4') {
        selectedResource.value = resource
        mediaViewerVisible.value = true
      } else if (['pdf', 'jpg', 'jpeg', 'png'].includes(resource.fileType)) {
        // For viewable files, open in new tab
        downloadResource(resource, true)
      } else {
        ElMessage.info('This file type cannot be previewed. Please download to view.')
      }
    }
    
    const closeMediaViewer = () => {
      mediaViewerVisible.value = false
      selectedResource.value = null
    }
    
    const getResourceUrl = (resourceId) => {
      return `${process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'}/api/resources/${resourceId}/download`
    }
    
    const downloadResource = async (resource, openInNewTab = false) => {
      try {
        const response = await resourceService.downloadResource(resource.id)
        
        // Create blob and download
        const blob = new Blob([response.data])
        const url = window.URL.createObjectURL(blob)
        
        if (openInNewTab && ['pdf', 'jpg', 'jpeg', 'png'].includes(resource.fileType)) {
          // Open in new tab for viewable files
          window.open(url, '_blank')
        } else {
          // Download the file
          const link = document.createElement('a')
          link.href = url
          link.download = resource.resourceName
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
        }
        
        window.URL.revokeObjectURL(url)
        
        if (!openInNewTab) {
          ElMessage.success('Download started')
        }
      } catch (error) {
        console.error('Error downloading resource:', error)
        ElMessage.error('Failed to download resource')
      }
    }
    
    const isViewableFile = (fileType) => {
      return ['pdf', 'mp4', 'jpg', 'jpeg', 'png'].includes(fileType)
    }
    
    const getFileTypeColor = (fileType) => {
      const colorMap = {
        pdf: 'danger',
        docx: 'primary',
        pptx: 'warning',
        mp4: 'success',
        jpg: 'info',
        jpeg: 'info',
        png: 'info',
        txt: ''
      }
      return colorMap[fileType] || ''
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
      searchQuery,
      selectedFileType,
      resources,
      filteredResources,
      fileTypes,
      mediaViewerVisible,
      selectedResource,
      handleSearch,
      handleTypeFilter,
      clearFilters,
      viewResource,
      closeMediaViewer,
      downloadResource,
      isViewableFile,
      getFileTypeColor,
      getResourceUrl,
      formatFileSize,
      formatDate
    }
  }
}
</script>

<style scoped>
.student-platform-resources-container {
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

.section-actions {
  display: flex;
  align-items: center;
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
  flex: 1;
  min-width: 0;
}

.resource-title span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-icon {
  font-size: 18px;
  color: #3498db;
  flex-shrink: 0;
}

.resource-content {
  padding: 16px 0;
}

.resource-description {
  color: #7f8c8d;
  margin-bottom: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

.resource-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.media-viewer-container {
  width: 100%;
  height: 70vh;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>