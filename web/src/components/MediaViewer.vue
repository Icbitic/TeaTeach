<template>
  <div class="media-viewer">
    <!-- Video Player -->
    <div v-if="isVideo" class="video-container">
      <video
        ref="videoPlayer"
        :src="mediaUrl"
        controls
        preload="metadata"
        @loadedmetadata="onVideoLoaded"
        @timeupdate="onTimeUpdate"
        @play="onPlay"
        @pause="onPause"
        @ended="onEnded"
        @seeking="onSeeking"
        @seeked="onSeeked"
        class="video-player"
      >
        Your browser does not support the video tag.
      </video>
      
      <!-- Video Progress Info -->
      <div v-if="showProgress" class="video-progress-info">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="Watch Progress" :value="watchPercentage" suffix="%" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="Total Watch Time" :value="totalWatchTime" suffix="s" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="Current Time" :value="currentTime" suffix="s" />
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- Image Viewer -->
    <div v-else-if="isImage" class="image-container">
      <el-image
        :src="mediaUrl"
        :preview-src-list="[mediaUrl]"
        fit="contain"
        class="image-viewer"
      >
        <template #error>
          <div class="image-error">
            <el-icon><Picture /></el-icon>
            <span>Failed to load image</span>
          </div>
        </template>
      </el-image>
    </div>

    <!-- PDF Viewer -->
    <div v-else-if="isPdf" class="pdf-container">
      <iframe
        :src="pdfViewerUrl"
        class="pdf-viewer"
        frameborder="0"
      >
        Your browser does not support PDF viewing.
      </iframe>
      <div class="pdf-fallback">
        <el-button type="primary" @click="downloadFile">
          <el-icon><Download /></el-icon>
          Download PDF
        </el-button>
      </div>
    </div>

    <!-- Audio Player -->
    <div v-else-if="isAudio" class="audio-container">
      <audio
        ref="audioPlayer"
        :src="mediaUrl"
        controls
        preload="metadata"
        @loadedmetadata="onAudioLoaded"
        @timeupdate="onTimeUpdate"
        @play="onPlay"
        @pause="onPause"
        @ended="onEnded"
        class="audio-player"
      >
        Your browser does not support the audio tag.
      </audio>
    </div>

    <!-- Document Preview -->
    <div v-else-if="isDocument" class="document-container">
      <div class="document-preview">
        <el-icon class="document-icon"><Document /></el-icon>
        <div class="document-info">
          <h4>{{ resource.resourceName }}</h4>
          <p>{{ getFileTypeDescription() }}</p>
          <p class="file-size">{{ formatFileSize(resource.fileSize) }}</p>
        </div>
      </div>
      <div class="document-actions">
        <el-button type="primary" @click="downloadFile">
          <el-icon><Download /></el-icon>
          Download
        </el-button>
        <el-button v-if="canPreview" @click="previewFile">
          <el-icon><View /></el-icon>
          Preview
        </el-button>
      </div>
    </div>

    <!-- Unsupported File Type -->
    <div v-else class="unsupported-container">
      <div class="unsupported-content">
        <el-icon class="unsupported-icon"><Warning /></el-icon>
        <h4>Unsupported File Type</h4>
        <p>{{ resource.resourceName }}</p>
        <el-button type="primary" @click="downloadFile">
          <el-icon><Download /></el-icon>
          Download File
        </el-button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <el-loading-spinner />
      <p>Loading media...</p>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Download, Document, View, Warning } from '@element-plus/icons-vue'
import { resourceService } from '@/services/resourceService'
import { playbackService } from '@/services/playbackService'

export default {
  name: 'MediaViewer',
  components: {
    Picture,
    Download,
    Document,
    View,
    Warning
  },
  props: {
    resource: {
      type: Object,
      required: true
    },
    studentId: {
      type: [String, Number],
      default: null
    },
    showProgress: {
      type: Boolean,
      default: false
    },
    autoTrack: {
      type: Boolean,
      default: true
    }
  },
  emits: ['progress-update', 'playback-complete'],
  setup(props, { emit }) {
    const videoPlayer = ref(null)
    const audioPlayer = ref(null)
    const loading = ref(false)
    const mediaUrl = ref('')
    
    // Video tracking state
    const videoDuration = ref(0)
    const currentTime = ref(0)
    const watchPercentage = ref(0)
    const totalWatchTime = ref(0)
    const playbackVector = ref([])
    const isPlaying = ref(false)
    const lastRecordedTime = ref(0)
    
    // Tracking intervals
    let trackingInterval = null
    let recordingInterval = null
    
    // File type detection
    const fileExtension = computed(() => {
      if (!props.resource.resourceName) return ''
      return props.resource.resourceName.split('.').pop().toLowerCase()
    })
    
    const isVideo = computed(() => {
      const videoExtensions = ['mp4', 'webm', 'ogg', 'avi', 'mov', 'wmv', 'flv', 'm4v']
      return videoExtensions.includes(fileExtension.value)
    })
    
    const isAudio = computed(() => {
      const audioExtensions = ['mp3', 'wav', 'ogg', 'aac', 'flac', 'm4a', 'wma']
      return audioExtensions.includes(fileExtension.value)
    })
    
    const isImage = computed(() => {
      const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp']
      return imageExtensions.includes(fileExtension.value)
    })
    
    const isPdf = computed(() => {
      return fileExtension.value === 'pdf'
    })
    
    const isDocument = computed(() => {
      const docExtensions = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'rtf']
      return docExtensions.includes(fileExtension.value)
    })
    
    const canPreview = computed(() => {
      const previewableExtensions = ['txt', 'pdf', 'doc', 'docx']
      return previewableExtensions.includes(fileExtension.value)
    })
    
    const pdfViewerUrl = computed(() => {
      if (!isPdf.value) return ''
      return `${mediaUrl.value}#toolbar=1&navpanes=1&scrollbar=1`
    })
    
    // Load media URL
    const loadMediaUrl = async () => {
      try {
        loading.value = true
        
        if (isImage.value || isVideo.value || isAudio.value || isPdf.value) {
          // For media files that can be displayed directly
          const response = await resourceService.downloadResource(props.resource.id)
          const blob = new Blob([response.data])
          mediaUrl.value = URL.createObjectURL(blob)
        }
      } catch (error) {
        console.error('Error loading media:', error)
        ElMessage.error('Failed to load media file')
      } finally {
        loading.value = false
      }
    }
    
    // Video event handlers
    const onVideoLoaded = () => {
      if (videoPlayer.value) {
        videoDuration.value = Math.floor(videoPlayer.value.duration)
        playbackVector.value = new Array(videoDuration.value).fill(0)
        loadExistingProgress()
      }
    }
    
    const onAudioLoaded = () => {
      if (audioPlayer.value) {
        videoDuration.value = Math.floor(audioPlayer.value.duration)
        playbackVector.value = new Array(videoDuration.value).fill(0)
      }
    }
    
    const onTimeUpdate = (event) => {
      const player = event.target
      currentTime.value = Math.floor(player.currentTime)
      // Note: trackPlayback() is handled by setInterval in startTracking()
    }
    
    const onPlay = () => {
      isPlaying.value = true
      startTracking()
    }
    
    const onPause = () => {
      isPlaying.value = false
      stopTracking()
      recordPlayback()
    }
    
    const onEnded = () => {
      isPlaying.value = false
      stopTracking()
      recordPlayback()
      emit('playback-complete', {
        resourceId: props.resource.id,
        watchPercentage: watchPercentage.value,
        totalWatchTime: totalWatchTime.value
      })
    }
    
    const onSeeking = () => {
      // Record current position before seeking
      if (isPlaying.value) {
        recordPlayback()
      }
    }
    
    const onSeeked = () => {
      // Update tracking after seeking
      lastRecordedTime.value = currentTime.value
    }
    
    // Playback tracking
    const startTracking = () => {
      if (!props.autoTrack || !props.studentId) return
      
      trackingInterval = setInterval(() => {
        trackPlayback()
      }, 1000) // Track every second
      
      recordingInterval = setInterval(() => {
        recordPlayback()
      }, 10000) // Record every 10 seconds
    }
    
    const stopTracking = () => {
      if (trackingInterval) {
        clearInterval(trackingInterval)
        trackingInterval = null
      }
      if (recordingInterval) {
        clearInterval(recordingInterval)
        recordingInterval = null
      }
    }
    
    const trackPlayback = () => {
      const currentSecond = Math.floor(currentTime.value)
      
      // Only track if we're in a new second to prevent duplicate counting
      if (currentSecond >= 0 && currentSecond < playbackVector.value.length && 
          currentSecond !== lastRecordedTime.value) {
        playbackVector.value[currentSecond]++
        totalWatchTime.value++
        lastRecordedTime.value = currentSecond
        
        // Calculate watch percentage
        const watchedSeconds = playbackVector.value.filter(count => count > 0).length
        watchPercentage.value = Math.round((watchedSeconds / videoDuration.value) * 100)
        
        emit('progress-update', {
          resourceId: props.resource.id,
          currentTime: currentTime.value,
          watchPercentage: watchPercentage.value,
          totalWatchTime: totalWatchTime.value
        })
      }
    }
    
    const recordPlayback = async () => {
      if (!props.studentId || !props.autoTrack) return
      
      try {
        await playbackService.recordPlaybackCounts(
          props.studentId,
          props.resource.id,
          playbackVector.value
        )
        // Clear playbackVector after successful upload to prevent duplicates
        playbackVector.value = new Array(videoDuration.value).fill(0)
      } catch (error) {
        console.error('Error recording playback:', error)
      }
    }
    
    const loadExistingProgress = async () => {
      if (!props.studentId) return
      
      try {
        const response = await playbackService.getWatchProgress(
          props.studentId,
          props.resource.id
        )
        watchPercentage.value = Math.round(response.data.watchPercentage)
      } catch (error) {
        console.error('Error loading existing progress:', error)
      }
    }
    
    // File operations
    const downloadFile = async () => {
      try {
        loading.value = true
        const response = await resourceService.downloadResource(props.resource.id)
        
        // Create download link
        const blob = new Blob([response.data])
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = props.resource.resourceName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        
        ElMessage.success('File downloaded successfully')
      } catch (error) {
        console.error('Download failed:', error)
        ElMessage.error('Failed to download file')
      } finally {
        loading.value = false
      }
    }
    
    const previewFile = () => {
      // Open file in new tab for preview
      if (mediaUrl.value) {
        window.open(mediaUrl.value, '_blank')
      } else {
        ElMessage.info('Preview not available for this file type')
      }
    }
    
    // Utility functions
    const getFileTypeDescription = () => {
      const descriptions = {
        'pdf': 'PDF Document',
        'doc': 'Word Document',
        'docx': 'Word Document',
        'xls': 'Excel Spreadsheet',
        'xlsx': 'Excel Spreadsheet',
        'ppt': 'PowerPoint Presentation',
        'pptx': 'PowerPoint Presentation',
        'txt': 'Text Document',
        'rtf': 'Rich Text Document'
      }
      return descriptions[fileExtension.value] || 'Document'
    }
    
    const formatFileSize = (bytes) => {
      if (!bytes) return 'Unknown size'
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(1024))
      return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
    }
    
    // Lifecycle
    onMounted(() => {
      loadMediaUrl()
    })
    
    onUnmounted(() => {
      stopTracking()
      if (mediaUrl.value) {
        URL.revokeObjectURL(mediaUrl.value)
      }
    })
    
    // Watch for resource changes
    watch(() => props.resource, () => {
      if (mediaUrl.value) {
        URL.revokeObjectURL(mediaUrl.value)
      }
      loadMediaUrl()
    })
    
    return {
      videoPlayer,
      audioPlayer,
      loading,
      mediaUrl,
      videoDuration,
      currentTime,
      watchPercentage,
      totalWatchTime,
      isVideo,
      isAudio,
      isImage,
      isPdf,
      isDocument,
      canPreview,
      pdfViewerUrl,
      onVideoLoaded,
      onAudioLoaded,
      onTimeUpdate,
      onPlay,
      onPause,
      onEnded,
      onSeeking,
      onSeeked,
      downloadFile,
      previewFile,
      getFileTypeDescription,
      formatFileSize
    }
  }
}
</script>

<style scoped>
.media-viewer {
  width: 100%;
  max-width: 100%;
}

.video-container {
  position: relative;
  width: 100%;
}

.video-player {
  width: 100%;
  height: auto;
  max-height: 500px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.video-progress-info {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.image-container {
  text-align: center;
}

.image-viewer {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.pdf-container {
  position: relative;
}

.pdf-viewer {
  width: 100%;
  height: 600px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.pdf-fallback {
  text-align: center;
  margin-top: 20px;
}

.audio-container {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.audio-player {
  width: 100%;
  max-width: 400px;
}

.document-container {
  padding: 30px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.document-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.document-icon {
  font-size: 48px;
  color: #409eff;
  margin-right: 20px;
}

.document-info h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.document-info p {
  margin: 5px 0;
  color: #606266;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.document-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.unsupported-container {
  padding: 40px;
  text-align: center;
  background: #f8f9fa;
  border-radius: 8px;
}

.unsupported-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.unsupported-icon {
  font-size: 48px;
  color: #e6a23c;
  margin-bottom: 20px;
}

.unsupported-content h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.unsupported-content p {
  margin: 0 0 20px 0;
  color: #606266;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #606266;
}

.loading-container p {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .video-player {
    max-height: 300px;
  }
  
  .pdf-viewer {
    height: 400px;
  }
  
  .document-preview {
    flex-direction: column;
  }
  
  .document-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>