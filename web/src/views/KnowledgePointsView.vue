<template>
  <div class="knowledge-points-view">
    <div class="header">
      <h1>{{ $t('knowledgePoints.management') }}</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          {{ $t('knowledgePoints.add') }}
        </el-button>
        <el-button type="success" @click="showGenerateDialog = true">
          <el-icon><MagicStick /></el-icon>
          {{ $t('knowledgePoints.generateFromContent') }}
        </el-button>
      </div>
    </div>

    <!-- Course Filter -->
    <div class="filters">
      <el-select
        v-model="selectedCourseId"
        :placeholder="$t('knowledgePoints.filterByCourse')"
        clearable
        @change="loadKnowledgePoints"
        style="width: 300px"
      >
        <el-option
          v-for="course in courses"
          :key="course.id"
          :label="course.courseName"
          :value="course.id"
        />
      </el-select>
    </div>

    <!-- Tabs for different views -->
    <el-tabs v-model="activeTab" class="knowledge-tabs">
      <el-tab-pane :label="$t('knowledgePoints.tableView')" name="table">
        <!-- Knowledge Points Table -->
    <el-table
      :data="knowledgePoints"
      v-loading="loading"
      style="width: 100%"
      class="knowledge-points-table"
    >
      <el-table-column prop="id" :label="$t('knowledgePoints.id')" width="80" />
      <el-table-column prop="name" :label="$t('knowledgePoints.name')" width="200" />
      <el-table-column :label="$t('knowledgePoints.course')" width="150">
        <template #default="scope">
          {{ getCourseName(scope.row.courseId) }}
        </template>
      </el-table-column>
      <el-table-column prop="briefDescription" :label="$t('knowledgePoints.briefDescription')" width="300" />
      <el-table-column prop="difficultyLevel" :label="$t('knowledgePoints.difficulty')" width="120">
        <template #default="scope">
          <el-tag
            :type="getDifficultyTagType(scope.row.difficultyLevel)"
            size="small"
          >
            {{ scope.row.difficultyLevel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('knowledgePoints.prerequisites')" width="150">
        <template #default="scope">
          <el-tag
            v-for="prereqId in scope.row.prerequisiteKnowledgePointIds || []"
            :key="prereqId"
            size="small"
            style="margin: 2px"
          >
            {{ getKnowledgePointName(prereqId) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('knowledgePoints.related')" width="150">
        <template #default="scope">
          <el-tag
            v-for="relatedId in scope.row.relatedKnowledgePointIds || []"
            :key="relatedId"
            size="small"
            type="info"
            style="margin: 2px"
          >
            {{ getKnowledgePointName(relatedId) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('knowledgePoints.actions')" width="200" fixed="right">
        <template #default="scope">
          <el-button
            size="small"
            @click="editKnowledgePoint(scope.row)"
          >
            {{ $t('knowledgePoints.edit') }}
          </el-button>
          <el-button
            size="small"
            type="danger"
            @click="deleteKnowledgePoint(scope.row.id)"
          >
            {{ $t('knowledgePoints.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
      </el-tab-pane>
      
      <el-tab-pane :label="$t('knowledgePoints.graphView')" name="graph">
        <KnowledgeGraphVisualization
          :knowledge-points="knowledgePoints"
          @node-click="editKnowledgePoint"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- Create/Edit Dialog -->
    <el-dialog
      :title="editingKnowledgePoint ? $t('knowledgePoints.editKnowledgePoint') : $t('knowledgePoints.createKnowledgePoint')"
      v-model="showCreateDialog"
      width="600px"
    >
      <el-form
        :model="knowledgePointForm"
        :rules="formRules"
        ref="knowledgePointFormRef"
        label-width="150px"
      >
        <el-form-item :label="$t('knowledgePoints.name')" prop="name">
          <el-input v-model="knowledgePointForm.name" />
        </el-form-item>
        
        <el-form-item :label="$t('knowledgePoints.course')" prop="courseId">
          <el-select v-model="knowledgePointForm.courseId" :placeholder="$t('knowledgePointsManagement.selectCourse')">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item :label="$t('knowledgePoints.briefDescription')" prop="briefDescription">
          <el-input
            v-model="knowledgePointForm.briefDescription"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        
        <el-form-item :label="$t('knowledgePoints.detailedContent')">
          <el-input
            v-model="knowledgePointForm.detailedContent"
            type="textarea"
            :rows="5"
          />
        </el-form-item>
        
        <el-form-item :label="$t('knowledgePoints.difficulty')" prop="difficultyLevel">
          <el-select v-model="knowledgePointForm.difficultyLevel" :placeholder="$t('knowledgePointsManagement.selectDifficulty')">
            <el-option :label="$t('knowledgePoints.beginner')" value="BEGINNER" />
            <el-option :label="$t('knowledgePoints.intermediate')" value="INTERMEDIATE" />
            <el-option :label="$t('knowledgePoints.advanced')" value="ADVANCED" />
          </el-select>
        </el-form-item>
        
        <el-form-item :label="$t('knowledgePoints.prerequisites')">
          <el-select
            v-model="knowledgePointForm.prerequisiteKnowledgePointIds"
            multiple
            :placeholder="$t('knowledgePointsManagement.selectPrerequisites')"
          >
            <el-option
              v-for="kp in availableKnowledgePoints"
              :key="kp.id"
              :label="kp.name"
              :value="kp.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item :label="$t('knowledgePoints.related')">
          <el-select
            v-model="knowledgePointForm.relatedKnowledgePointIds"
            multiple
            :placeholder="$t('knowledgePointsManagement.selectRelatedPoints')"
          >
            <el-option
              v-for="kp in availableKnowledgePoints"
              :key="kp.id"
              :label="kp.name"
              :value="kp.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="saveKnowledgePoint">
            {{ editingKnowledgePoint ? $t('knowledgePoints.update') : $t('knowledgePoints.create') }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Generate from Content Dialog -->
    <el-dialog
      :title="$t('knowledgePoints.generateFromContentTitle')"
      v-model="showGenerateDialog"
      width="700px"
    >
      <el-form :model="generateForm" label-width="120px">
        <el-form-item :label="$t('knowledgePoints.course')" required>
          <el-select v-model="generateForm.courseId" :placeholder="$t('knowledgePointsManagement.selectCourse')">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item :label="$t('knowledgePoints.courseContent')" required>
          <el-input
            v-model="generateForm.courseContent"
            type="textarea"
            :rows="10"
            :placeholder="$t('knowledgePointsManagement.courseContentPlaceholder')"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showGenerateDialog = false">{{ $t('common.cancel') }}</el-button>
          <el-button
            type="primary"
            @click="generateKnowledgeGraph"
            :loading="generating"
          >
            {{ $t('knowledgePoints.generateFromContent') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import knowledgePointService from '../services/knowledgePointService'
import courseService from '../services/courseService'
import KnowledgeGraphVisualization from '../components/KnowledgeGraphVisualization.vue'

export default {
  name: 'KnowledgePointsView',
  components: {
    Plus,
    MagicStick,
    KnowledgeGraphVisualization
  },
  setup() {
    const { t } = useI18n()
    const loading = ref(false)
    const generating = ref(false)
    const knowledgePoints = ref([])
    const courses = ref([])
    const selectedCourseId = ref(null)
    const showCreateDialog = ref(false)
    const showGenerateDialog = ref(false)
    const editingKnowledgePoint = ref(null)
    const knowledgePointFormRef = ref(null)
    const activeTab = ref('table')

    const knowledgePointForm = reactive({
      name: '',
      briefDescription: '',
      detailedContent: '',
      difficultyLevel: '',
      courseId: null,
      prerequisiteKnowledgePointIds: [],
      relatedKnowledgePointIds: []
    })

    const generateForm = reactive({
      courseId: null,
      courseContent: ''
    })

    const formRules = {
      name: [{ required: true, message: 'Please enter knowledge point name', trigger: 'blur' }],
      briefDescription: [{ required: true, message: 'Please enter brief description', trigger: 'blur' }],
      difficultyLevel: [{ required: true, message: 'Please select difficulty level', trigger: 'change' }],
      courseId: [{ required: true, message: 'Please select a course', trigger: 'change' }]
    }

    const availableKnowledgePoints = computed(() => {
      return knowledgePoints.value.filter(kp => 
        kp.id !== editingKnowledgePoint.value?.id
      )
    })

    const loadKnowledgePoints = async () => {
      loading.value = true
      try {
        let response
        if (selectedCourseId.value) {
          response = await knowledgePointService.getKnowledgePointsByCourseId(selectedCourseId.value)
        } else {
          response = await knowledgePointService.getAllKnowledgePoints()
        }
        knowledgePoints.value = response.data
      } catch (error) {
        ElMessage.error('Failed to load knowledge points')
        console.error('Error loading knowledge points:', error)
      } finally {
        loading.value = false
      }
    }

    const loadCourses = async () => {
      try {
        const response = await courseService.getAllCourses()
        courses.value = response.data
      } catch (error) {
        ElMessage.error('Failed to load courses')
        console.error('Error loading courses:', error)
      }
    }

    const resetForm = () => {
      Object.assign(knowledgePointForm, {
        name: '',
        briefDescription: '',
        detailedContent: '',
        difficultyLevel: '',
        courseId: null,
        prerequisiteKnowledgePointIds: [],
        relatedKnowledgePointIds: []
      })
      editingKnowledgePoint.value = null
    }

    const editKnowledgePoint = (knowledgePoint) => {
      editingKnowledgePoint.value = knowledgePoint
      Object.assign(knowledgePointForm, {
        ...knowledgePoint,
        prerequisiteKnowledgePointIds: knowledgePoint.prerequisiteKnowledgePointIds || [],
        relatedKnowledgePointIds: knowledgePoint.relatedKnowledgePointIds || []
      })
      showCreateDialog.value = true
    }

    const saveKnowledgePoint = async () => {
      if (!knowledgePointFormRef.value) return
      
      try {
        await knowledgePointFormRef.value.validate()
        
        if (editingKnowledgePoint.value) {
          await knowledgePointService.updateKnowledgePoint(
            editingKnowledgePoint.value.id,
            knowledgePointForm
          )
          ElMessage.success('Knowledge point updated successfully')
        } else {
          await knowledgePointService.createKnowledgePoint(knowledgePointForm)
          ElMessage.success('Knowledge point created successfully')
        }
        
        showCreateDialog.value = false
        resetForm()
        loadKnowledgePoints()
      } catch (error) {
        if (error.errors) {
          // Validation errors
          return
        }
        ElMessage.error('Failed to save knowledge point')
        console.error('Error saving knowledge point:', error)
      }
    }

    const deleteKnowledgePoint = async (id) => {
      try {
        await ElMessageBox.confirm(
          'This will permanently delete the knowledge point. Continue?',
          'Warning',
          {
            confirmButtonText: 'OK',
            cancelButtonText: 'Cancel',
            type: 'warning'
          }
        )
        
        await knowledgePointService.deleteKnowledgePoint(id)
        ElMessage.success('Knowledge point deleted successfully')
        loadKnowledgePoints()
      } catch (error) {
        if (error === 'cancel') {
          return
        }
        ElMessage.error('Failed to delete knowledge point')
        console.error('Error deleting knowledge point:', error)
      }
    }

    const generateKnowledgeGraph = async () => {
      if (!generateForm.courseId || !generateForm.courseContent.trim()) {
        ElMessage.warning('Please select a course and provide content')
        return
      }
      
      generating.value = true
      try {
        const response = await knowledgePointService.generateKnowledgeGraphFromContent(
          generateForm.courseId,
          generateForm.courseContent
        )
        
        ElMessage.success(`Generated ${response.data.length} knowledge points successfully`)
        showGenerateDialog.value = false
        generateForm.courseId = null
        generateForm.courseContent = ''
        loadKnowledgePoints()
      } catch (error) {
        ElMessage.error('Failed to generate knowledge points')
        console.error('Error generating knowledge points:', error)
      } finally {
        generating.value = false
      }
    }

    const getDifficultyTagType = (difficulty) => {
      switch (difficulty) {
        case 'BEGINNER': return 'success'
        case 'INTERMEDIATE': return 'warning'
        case 'ADVANCED': return 'danger'
        default: return 'info'
      }
    }

    const getKnowledgePointName = (id) => {
      const kp = knowledgePoints.value.find(k => k.id === id)
      return kp ? kp.name : `KP-${id}`
    }

    const getCourseName = (courseId) => {
      const course = courses.value.find(c => c.id === courseId)
      return course ? course.courseName : `Course-${courseId}`
    }

    onMounted(() => {
      loadKnowledgePoints()
      loadCourses()
    })

    return {
      t,
      loading,
      generating,
      knowledgePoints,
      courses,
      selectedCourseId,
      showCreateDialog,
      showGenerateDialog,
      editingKnowledgePoint,
      knowledgePointForm,
      generateForm,
      formRules,
      knowledgePointFormRef,
      availableKnowledgePoints,
      activeTab,
      loadKnowledgePoints,
      editKnowledgePoint,
      saveKnowledgePoint,
      deleteKnowledgePoint,
      generateKnowledgeGraph,
      getDifficultyTagType,
      getKnowledgePointName,
      getCourseName,
      resetForm
    }
  }
}
</script>

<style scoped>
.knowledge-points-view {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filters {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.knowledge-points-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-table th) {
  background-color: #f8f9fa;
  color: #2c3e50;
  font-weight: 600;
}

:deep(.el-tag) {
  margin: 2px;
}

.knowledge-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}

:deep(.el-tabs__item.is-active) {
  color: #3498db;
}
</style>