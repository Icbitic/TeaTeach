<template>
  <div class="students-container">
    <div class="section-header">
      <h2>{{ $t('students.management') }}</h2>
      <div class="section-actions">
        <el-button type="primary" @click="showAddStudentDialog">
          <el-icon>
            <el-icon-plus/>
          </el-icon>
          {{ $t('students.addStudent') }}
        </el-button>
        <el-button type="success" @click="showImportDialog">
          <el-icon>
            <el-icon-upload/>
          </el-icon>
          {{ $t('students.import') }}
        </el-button>
        <el-button type="info" @click="exportStudents">
          <el-icon>
            <el-icon-download/>
          </el-icon>
          {{ $t('students.export') }}
        </el-button>
      </div>
    </div>

    <!-- Search and Filter -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('students.search')">
          <el-input
              v-model="searchForm.keyword"
              :placeholder="$t('students.searchPlaceholder')"
              clearable
              @keyup.enter="searchStudents"
          >
            <template #append>
              <el-button @click="searchStudents">
                <el-icon>
                  <el-icon-search/>
                </el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('students.major')">
          <el-select v-model="searchForm.major" :placeholder="$t('students.allMajors')" clearable style="width: 200px">
            <el-option :label="$t('majors.computerScience')" value="Computer Science"/>
            <el-option :label="$t('majors.engineering')" value="Engineering"/>
            <el-option :label="$t('majors.mathematics')" value="Mathematics"/>
            <el-option :label="$t('majors.physics')" value="Physics"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchStudents">{{ $t('students.filter') }}</el-button>
          <el-button @click="resetSearch">{{ $t('students.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Students Table -->
    <el-card shadow="hover" class="data-card">
      <el-table
          :data="students"
          style="width: 100%"
          v-loading="loading"
          border
          stripe
          height="400"
          @selection-change="handleSelectionChange"
          table-layout="fixed"
      >
        <el-table-column type="selection" width="55" fixed="left"/>
        <el-table-column prop="studentId" :label="$t('students.studentId')" width="120" sortable/>
        <el-table-column prop="name" :label="$t('students.name')" min-width="150" sortable/>
        <el-table-column prop="email" :label="$t('students.email')" min-width="200" show-overflow-tooltip/>
        <el-table-column prop="major" :label="$t('students.major')" min-width="150" show-overflow-tooltip/>
        <el-table-column prop="dateOfBirth" :label="$t('students.dateOfBirth')" width="120" :formatter="formatDateColumn"/>

        <el-table-column :label="$t('students.actions')" fixed="right" width="120">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="editStudent(scope.row)"
                circle
                plain>
              <el-icon>
                <el-icon-edit/>
              </el-icon>
            </el-button>
            <el-button
                type="danger"
                size="small"
                @click="deleteStudent(scope.row)"
                circle
                plain>
              <el-icon>
                <el-icon-delete/>
              </el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalStudents"
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Add/Edit Student Dialog -->
    <el-dialog
        v-model="studentDialog.visible"
        :title="studentDialog.isEdit ? $t('students.editStudent') : $t('students.addNewStudent')"
        width="500px">
      <el-form
          :model="studentDialog.form"
          :rules="studentDialog.rules"
          ref="studentFormRef"
          label-width="120px">
        <el-form-item :label="$t('students.studentId')" prop="studentId">
          <el-input v-model="studentDialog.form.studentId" :placeholder="$t('studentManagement.studentIdPlaceholder')"/>
        </el-form-item>
        <el-form-item :label="$t('common.name')" prop="name">
          <el-input v-model="studentDialog.form.name" :placeholder="$t('studentManagement.fullNamePlaceholder')"/>
        </el-form-item>
        <el-form-item :label="$t('common.email')" prop="email">
          <el-input v-model="studentDialog.form.email" :placeholder="$t('studentManagement.emailPlaceholder')"/>
        </el-form-item>
        <el-form-item :label="$t('common.major')" prop="major">
          <el-select v-model="studentDialog.form.major" :placeholder="$t('studentManagement.selectMajor')" style="width: 100%">
            <el-option :label="$t('majors.computerScience')" value="Computer Science"/>
            <el-option :label="$t('majors.engineering')" value="Engineering"/>
            <el-option :label="$t('majors.mathematics')" value="Mathematics"/>
            <el-option :label="$t('majors.physics')" value="Physics"/>
            <el-option :label="$t('majors.chemistry')" value="Chemistry"/>
            <el-option :label="$t('majors.biology')" value="Biology"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('students.dateOfBirth')" prop="dateOfBirth">
          <el-date-picker
              v-model="studentDialog.form.dateOfBirth"
              type="date"
              :placeholder="$t('studentManagement.selectDate')"
              style="width: 100%"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="studentDialog.visible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitStudentForm" :loading="studentDialog.loading">
            {{ studentDialog.isEdit ? $t('common.update') : $t('common.create') }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Import Dialog -->
    <el-dialog
        v-model="importDialog.visible"
        :title="$t('students.importStudents')"
        width="500px">
      <el-upload
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :file-list="importDialog.fileList">
        <el-icon class="el-icon--upload">
          <el-icon-upload-filled/>
        </el-icon>
        <div class="el-upload__text">
          Drop file here or <em>click to upload</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            Supported formats: .xlsx, .xls, .csv
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialog.visible = false">Cancel</el-button>
          <el-button type="primary" @click="importStudents" :loading="importDialog.loading">
            Import
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import studentService from '@/services/studentService'

export default {
  name: 'StudentsView',
  setup() {
    const { t } = useI18n()
    const studentFormRef = ref(null)
    const loading = ref(false)

    // Pagination
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalStudents = ref(0)

    // Student data
    const students = ref([])
    const selectedStudents = ref([])

    // Search form
    const searchForm = reactive({
      keyword: '',
      major: ''
    })

    // Student dialog
    const studentDialog = reactive({
      visible: false,
      isEdit: false,
      loading: false,
      form: {
        id: null,
        studentId: '',
        name: '',
        email: '',
        major: '',
        dateOfBirth: ''
      },
      rules: {
        studentId: [
          { required: true, message: 'Please enter student ID', trigger: 'blur' },
          { min: 3, max: 20, message: 'Length should be 3 to 20 characters', trigger: 'blur' }
        ],
        name: [
          { required: true, message: 'Please enter name', trigger: 'blur' }
        ],
        email: [
          { required: true, message: 'Please enter email', trigger: 'blur' },
          { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
        ],
        major: [
          { required: true, message: 'Please select major', trigger: 'change' }
        ]
      }
    })

    // Import dialog
    const importDialog = reactive({
      visible: false,
      loading: false,
      fileList: []
    })

    // Methods
    const fetchStudents = () => {
      loading.value = true

      studentService.getAllStudents()
        .then(response => {
          let studentData = response.data
          console.log('Student data from API:', studentData)

          // Apply client-side filtering if search parameters are provided
          if (searchForm.keyword) {
            const keyword = searchForm.keyword.toLowerCase()
            studentData = studentData.filter(student =>
              student.name.toLowerCase().includes(keyword) ||
              student.studentId.toLowerCase().includes(keyword) ||
              student.email.toLowerCase().includes(keyword)
            )
          }

          if (searchForm.major) {
            studentData = studentData.filter(student =>
              student.major === searchForm.major
            )
          }

          // Apply pagination
          totalStudents.value = studentData.length
          const startIndex = (currentPage.value - 1) * pageSize.value
          const endIndex = startIndex + pageSize.value
          students.value = studentData.slice(startIndex, endIndex)

          loading.value = false
        })
        .catch(error => {
          console.error('Error fetching students:', error)
          ElMessage.error('Failed to load students')
          loading.value = false
        })
    }

    const searchStudents = () => {
      currentPage.value = 1
      fetchStudents()
    }

    const resetSearch = () => {
      searchForm.keyword = ''
      searchForm.major = ''
      searchStudents()
    }

    const handleSizeChange = (size) => {
      pageSize.value = size
      fetchStudents()
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchStudents()
    }

    const handleSelectionChange = (selection) => {
      selectedStudents.value = selection
    }

    const resetStudentForm = () => {
      studentDialog.form.id = null
      studentDialog.form.studentId = ''
      studentDialog.form.name = ''
      studentDialog.form.email = ''
      studentDialog.form.major = ''
      studentDialog.form.dateOfBirth = ''

      if (studentFormRef.value) {
        studentFormRef.value.resetFields()
      }
    }

    const showAddStudentDialog = () => {
      resetStudentForm()
      studentDialog.isEdit = false
      studentDialog.visible = true
    }

    const editStudent = (student) => {
      resetStudentForm()
      studentDialog.isEdit = true
      studentDialog.form.id = student.id
      studentDialog.form.studentId = student.studentId
      studentDialog.form.name = student.name
      studentDialog.form.email = student.email
      studentDialog.form.major = student.major
      studentDialog.form.dateOfBirth = student.dateOfBirth ?
        (typeof student.dateOfBirth === 'string' ? new Date(student.dateOfBirth) : student.dateOfBirth) : null
      studentDialog.visible = true
    }

    const submitStudentForm = () => {
      if (!studentFormRef.value) return

      studentFormRef.value.validate((valid) => {
        if (valid) {
          studentDialog.loading = true

          const studentData = {
            studentId: studentDialog.form.studentId,
            name: studentDialog.form.name,
            email: studentDialog.form.email,
            major: studentDialog.form.major,
            dateOfBirth: studentDialog.form.dateOfBirth ?
              (studentDialog.form.dateOfBirth instanceof Date ?
                studentDialog.form.dateOfBirth.toISOString().split('T')[0] :
                studentDialog.form.dateOfBirth) :
              null
          }

          if (studentDialog.isEdit) {
            studentData.id = studentDialog.form.id
          }

          const apiCall = studentDialog.isEdit
            ? studentService.updateStudent(studentDialog.form.id, studentData)
            : studentService.createStudent(studentData)

          apiCall
            .then(() => {
              ElMessage({
                message: `Student ${studentDialog.isEdit ? 'updated' : 'created'} successfully`,
                type: 'success'
              })
              studentDialog.visible = false
              fetchStudents()
            })
            .catch(error => {
              console.error(`Error ${studentDialog.isEdit ? 'updating' : 'creating'} student:`, error)
              const errorMessage = error.response?.data?.message || `Failed to ${studentDialog.isEdit ? 'update' : 'create'} student`
              ElMessage.error(errorMessage)
            })
            .finally(() => {
              studentDialog.loading = false
            })
        }
      })
    }

    const deleteStudent = (student) => {
      ElMessageBox.confirm(
        `Are you sure you want to delete student "${student.name}"?`,
        'Confirm Delete',
        {
          confirmButtonText: 'Delete',
          cancelButtonText: 'Cancel',
          type: 'warning',
          confirmButtonClass: 'el-button--danger'
        }
      )
        .then(() => {
          studentService.deleteStudent(student.id)
            .then(() => {
              ElMessage({
                type: 'success',
                message: 'Student deleted successfully'
              })
              fetchStudents()
            })
            .catch(error => {
              console.error('Error deleting student:', error)
              const errorMessage = error.response?.data?.message || 'Failed to delete student'
              ElMessage.error(errorMessage)
            })
        })
        .catch(() => {
          // User cancelled
        })
    }

    const showImportDialog = () => {
      importDialog.fileList = []
      importDialog.visible = true
    }

    const handleFileChange = (file) => {
      console.log('File selected:', file)
      importDialog.fileList = [file]
    }

    const importStudents = () => {
      if (importDialog.fileList.length === 0) {
        ElMessage.warning('Please select a file to import')
        return
      }

      importDialog.loading = true

      const file = importDialog.fileList[0].raw
      const formData = new FormData()
      formData.append('file', file)

      studentService.importStudents(formData)
        .then(() => {
          ElMessage({
            message: 'Students imported successfully',
            type: 'success'
          })
          importDialog.visible = false
          fetchStudents()
        })
        .catch(error => {
          console.error('Error importing students:', error)
          const errorMessage = error.response?.data?.message || 'Failed to import students'
          ElMessage.error(errorMessage)
        })
        .finally(() => {
          importDialog.loading = false
        })
    }

    const exportStudents = () => {
      studentService.exportStudents()
        .then(response => {
          const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
          })

          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `students_${new Date().toISOString().split('T')[0]}.xlsx`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          ElMessage({
            message: 'Students exported successfully',
            type: 'success'
          })
        })
        .catch(error => {
          console.error('Error exporting students:', error)
          const errorMessage = error.response?.data?.message || 'Failed to export students'
          ElMessage.error(errorMessage)
        })
    }

    const formatDateColumn = (row, column, cellValue) => {
      if (!cellValue) return ''
      try {
        let date
        if (typeof cellValue === 'string') {
          date = new Date(cellValue)
        } else if (cellValue instanceof Date) {
          date = cellValue
        } else if (Array.isArray(cellValue) && cellValue.length >= 3) {
          date = new Date(cellValue[0], cellValue[1] - 1, cellValue[2])
        } else {
          return ''
        }

        if (isNaN(date.getTime())) {
          return ''
        }

        return date.toLocaleDateString()
      } catch (error) {
        console.error('Error formatting date:', error, cellValue)
        return ''
      }
    }

    onMounted(() => {
      fetchStudents()
    })

    return {
      t,
      studentFormRef,
      loading,
      currentPage,
      pageSize,
      totalStudents,
      students,
      selectedStudents,
      searchForm,
      studentDialog,
      importDialog,
      fetchStudents,
      searchStudents,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      showAddStudentDialog,
      editStudent,
      submitStudentForm,
      deleteStudent,
      showImportDialog,
      handleFileChange,
      importStudents,
      exportStudents,
      formatDateColumn
    }
  }
}
</script>

<style scoped>
.students-container {
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
  gap: 10px;
}

.filter-card {
  margin-bottom: 20px;
  border: none;
  border-radius: 8px;
}

.search-form {
  margin: 0;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
}

.search-form .el-form-item {
  margin-bottom: 0;
  margin-right: 20px;
}

.data-card {
  border: none;
  border-radius: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.upload-demo {
  width: 100%;
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 7px;
}
</style>