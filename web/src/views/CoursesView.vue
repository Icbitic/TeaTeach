<template>
  <div class="courses-container">
    <div class="section-header">
      <h2>Course Management</h2>
      <div class="section-actions">
        <el-button type="primary" @click="showAddCourseDialog">
          <el-icon>
            <el-icon-plus/>
          </el-icon>
          Add Course
        </el-button>
        <el-button type="info" @click="exportCourses">
          <el-icon>
            <el-icon-download/>
          </el-icon>
          Export
        </el-button>
      </div>
    </div>

    <!-- Search and Filter -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="courseSearchForm" class="search-form">
        <el-form-item label="Search">
          <el-input
              v-model="courseSearchForm.keyword"
              placeholder="Course name, code or instructor"
              clearable
              @keyup.enter="searchCourses"
          >
            <template #append>
              <el-button @click="searchCourses">
                <el-icon>
                  <el-icon-search/>
                </el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCourses">Filter</el-button>
          <el-button @click="resetCourseSearch">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Courses Table -->
    <el-card shadow="hover" class="data-card">
      <el-table
          :data="courses"
          style="width: 100%"
          v-loading="courseLoading"
          border
          stripe
          height="400"
          @selection-change="handleCourseSelectionChange"
          table-layout="fixed"
      >
        <el-table-column type="selection" width="55" fixed="left"/>
        <el-table-column prop="courseCode" label="Course Code" width="120" sortable/>
        <el-table-column prop="courseName" label="Course Name" min-width="200" sortable/>
        <el-table-column prop="instructor" label="Instructor" min-width="150" show-overflow-tooltip/>
        <el-table-column prop="credits" label="Credits" width="80" sortable/>
        <el-table-column prop="hours" label="Hours" width="80" sortable/>
        <el-table-column prop="description" label="Description" min-width="200" show-overflow-tooltip/>

        <el-table-column label="Actions" fixed="right" width="120">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="editCourse(scope.row)"
                circle
                plain>
              <el-icon>
                <el-icon-edit/>
              </el-icon>
            </el-button>
            <el-button
                type="danger"
                size="small"
                @click="confirmDeleteCourse(scope.row)"
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
            :total="totalCourses"
            :current-page="courseCurrentPage"
            :page-size="coursePageSize"
            :page-sizes="[5, 10, 20, 50]"
            @size-change="handleCourseSizeChange"
            @current-change="handleCourseCurrentChange"
        />
      </div>
    </el-card>

    <!-- Add/Edit Course Dialog -->
    <el-dialog
        v-model="courseDialog.visible"
        :title="courseDialog.isEdit ? 'Edit Course' : 'Add New Course'"
        width="500px">
      <el-form
          :model="courseDialog.form"
          :rules="courseDialog.rules"
          ref="courseFormRef"
          label-width="120px">
        <el-form-item label="Course Code" prop="courseCode">
          <el-input v-model="courseDialog.form.courseCode" placeholder="Enter course code (e.g., CS101)"/>
        </el-form-item>
        <el-form-item label="Course Name" prop="courseName">
          <el-input v-model="courseDialog.form.courseName" placeholder="Enter course name"/>
        </el-form-item>
        <el-form-item label="Instructor" prop="instructor">
          <el-input v-model="courseDialog.form.instructor" placeholder="Enter instructor name"/>
        </el-form-item>
        <el-form-item label="Credits" prop="credits">
          <el-input-number v-model="courseDialog.form.credits" :min="1" :max="10" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="Hours" prop="hours">
          <el-input-number v-model="courseDialog.form.hours" :min="1" :max="200" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="Description" prop="description">
          <el-input v-model="courseDialog.form.description" type="textarea" :rows="3" placeholder="Enter course description"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="courseDialog.visible = false">Cancel</el-button>
          <el-button type="primary" @click="submitCourseForm" :loading="courseDialog.loading">
            {{ courseDialog.isEdit ? 'Update' : 'Create' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import courseService from '@/services/courseService'

export default {
  name: 'CoursesView',
  setup() {
    const courseFormRef = ref(null)
    const courseLoading = ref(false)
    const courseCurrentPage = ref(1)
    const coursePageSize = ref(10)
    const totalCourses = ref(0)
    const courses = ref([])
    const selectedCourses = ref([])

    // Course search form
    const courseSearchForm = reactive({
      keyword: ''
    })

    // Course dialog
    const courseDialog = reactive({
      visible: false,
      isEdit: false,
      loading: false,
      form: {
        id: null,
        courseCode: '',
        courseName: '',
        instructor: '',
        credits: 3,
        hours: 48,
        description: ''
      },
      rules: {
        courseCode: [
          { required: true, message: 'Please enter course code', trigger: 'blur' },
          { min: 2, max: 10, message: 'Length should be 2 to 10 characters', trigger: 'blur' }
        ],
        courseName: [
          { required: true, message: 'Please enter course name', trigger: 'blur' }
        ],
        instructor: [
          { required: true, message: 'Please enter instructor name', trigger: 'blur' }
        ],
        credits: [
          { required: true, message: 'Please enter credits', trigger: 'blur' }
        ],
        hours: [
          { required: true, message: 'Please enter hours', trigger: 'blur' }
        ]
      }
    })

    // Methods
    const fetchCourses = () => {
      courseLoading.value = true

      courseService.getAllCourses()
        .then(response => {
          let courseData = response.data
          console.log('Course data from API:', courseData)

          // Apply client-side filtering if search parameters are provided
          if (courseSearchForm.keyword) {
            const keyword = courseSearchForm.keyword.toLowerCase()
            courseData = courseData.filter(course =>
              course.courseName.toLowerCase().includes(keyword) ||
              course.courseCode.toLowerCase().includes(keyword) ||
              course.instructor.toLowerCase().includes(keyword)
            )
          }

          // Apply pagination
          totalCourses.value = courseData.length
          const startIndex = (courseCurrentPage.value - 1) * coursePageSize.value
          const endIndex = startIndex + coursePageSize.value
          courses.value = courseData.slice(startIndex, endIndex)

          courseLoading.value = false
        })
        .catch(error => {
          console.error('Error fetching courses:', error)
          ElMessage.error('Failed to load courses')
          courseLoading.value = false
        })
    }

    const searchCourses = () => {
      courseCurrentPage.value = 1
      fetchCourses()
    }

    const resetCourseSearch = () => {
      courseSearchForm.keyword = ''
      searchCourses()
    }

    const handleCourseSizeChange = (size) => {
      coursePageSize.value = size
      fetchCourses()
    }

    const handleCourseCurrentChange = (page) => {
      courseCurrentPage.value = page
      fetchCourses()
    }

    const handleCourseSelectionChange = (selection) => {
      selectedCourses.value = selection
    }

    const showAddCourseDialog = () => {
      courseDialog.isEdit = false
      courseDialog.form = {
        id: null,
        courseCode: '',
        courseName: '',
        instructor: '',
        credits: 3,
        hours: 48,
        description: ''
      }
      courseDialog.visible = true
    }

    const editCourse = (course) => {
      courseDialog.isEdit = true
      courseDialog.form = {
        id: course.id,
        courseCode: course.courseCode,
        courseName: course.courseName,
        instructor: course.instructor,
        credits: course.credits,
        hours: course.hours,
        description: course.description
      }
      courseDialog.visible = true
    }

    const submitCourseForm = () => {
      courseFormRef.value.validate((valid) => {
        if (valid) {
          courseDialog.loading = true

          const courseData = {
            courseCode: courseDialog.form.courseCode,
            courseName: courseDialog.form.courseName,
            instructor: courseDialog.form.instructor,
            credits: courseDialog.form.credits,
            hours: courseDialog.form.hours,
            description: courseDialog.form.description
          }

          if (courseDialog.isEdit) {
            courseData.id = courseDialog.form.id
          }

          const apiCall = courseDialog.isEdit
            ? courseService.updateCourse(courseDialog.form.id, courseData)
            : courseService.createCourse(courseData)

          apiCall
            .then(() => {
              ElMessage({
                message: `Course ${courseDialog.isEdit ? 'updated' : 'created'} successfully`,
                type: 'success'
              })
              courseDialog.visible = false
              fetchCourses()
            })
            .catch(error => {
              console.error(`Error ${courseDialog.isEdit ? 'updating' : 'creating'} course:`, error)
              const errorMessage = error.response?.data?.message || `Failed to ${courseDialog.isEdit ? 'update' : 'create'} course`
              ElMessage.error(errorMessage)
            })
            .finally(() => {
              courseDialog.loading = false
            })
        }
      })
    }

    const confirmDeleteCourse = (course) => {
      ElMessageBox.confirm(
        `Are you sure you want to delete course "${course.courseName}"?`,
        'Confirm Delete',
        {
          confirmButtonText: 'Delete',
          cancelButtonText: 'Cancel',
          type: 'warning',
          confirmButtonClass: 'el-button--danger'
        }
      )
        .then(() => {
          courseService.deleteCourse(course.id)
            .then(() => {
              ElMessage({
                type: 'success',
                message: 'Course deleted successfully'
              })
              fetchCourses()
            })
            .catch(error => {
              console.error('Error deleting course:', error)
              const errorMessage = error.response?.data?.message || 'Failed to delete course'
              ElMessage.error(errorMessage)
            })
        })
        .catch(() => {
          // User cancelled
        })
    }

    const exportCourses = () => {
      courseService.getAllCourses()
        .then(response => {
          const courseData = response.data
          const csvContent = [
            ['Course Code', 'Course Name', 'Instructor', 'Credits', 'Hours', 'Description'],
            ...courseData.map(course => [
              course.courseCode,
              course.courseName,
              course.instructor,
              course.credits,
              course.hours,
              course.description
            ])
          ]
            .map(row => row.map(field => `"${field}"`).join(','))
            .join('\n')

          const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `courses_${new Date().toISOString().split('T')[0]}.csv`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          ElMessage({
            message: 'Courses exported successfully',
            type: 'success'
          })
        })
        .catch(error => {
          console.error('Error exporting courses:', error)
          const errorMessage = error.response?.data?.message || 'Failed to export courses'
          ElMessage.error(errorMessage)
        })
    }

    onMounted(() => {
      fetchCourses()
    })

    return {
      courseFormRef,
      courseLoading,
      courseCurrentPage,
      coursePageSize,
      totalCourses,
      courses,
      selectedCourses,
      courseSearchForm,
      courseDialog,
      fetchCourses,
      searchCourses,
      resetCourseSearch,
      handleCourseSizeChange,
      handleCourseCurrentChange,
      handleCourseSelectionChange,
      showAddCourseDialog,
      editCourse,
      submitCourseForm,
      confirmDeleteCourse,
      exportCourses
    }
  }
}
</script>

<style scoped>
.courses-container {
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
</style>