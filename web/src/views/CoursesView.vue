<template>
  <div class="courses-container">
    <div class="section-header">
      <h2>{{ $t('courses.management') }}</h2>
      <div class="section-actions">
        <el-button type="primary" @click="showAddCourseDialog">
          <el-icon>
            <el-icon-plus/>
          </el-icon>
          {{ $t('courses.addCourse') }}
        </el-button>
        <el-button type="info" @click="exportCourses">
          <el-icon>
            <el-icon-download/>
          </el-icon>
          {{ $t('courses.export') }}
        </el-button>
      </div>
    </div>

    <!-- Search and Filter -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="courseSearchForm" class="search-form">
        <el-form-item :label="$t('common.search')">
          <el-input
              v-model="courseSearchForm.keyword"
              :placeholder="$t('courses.searchPlaceholder')"
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
          <el-button type="primary" @click="searchCourses">{{ $t('courses.filter') }}</el-button>
          <el-button @click="resetCourseSearch">{{ $t('courses.reset') }}</el-button>
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
        <el-table-column prop="courseCode" :label="$t('courses.courseCode')" width="120" sortable/>
        <el-table-column prop="courseName" :label="$t('courses.courseName')" min-width="200" sortable/>
        <el-table-column prop="instructor" :label="$t('courses.instructor')" min-width="150" show-overflow-tooltip/>
        <el-table-column prop="credits" :label="$t('courses.credits')" width="80" sortable/>
        <el-table-column prop="hours" :label="$t('courses.hours')" width="80" sortable/>
        <el-table-column prop="description" :label="$t('courses.description')" min-width="200" show-overflow-tooltip/>

        <el-table-column :label="$t('courses.actions')" fixed="right" width="220">
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
                type="success"
                size="small"
                @click="manageStudents(scope.row)"
                circle
                plain
                :title="$t('courses.manageStudents')">
              <el-icon>
                <el-icon-user/>
              </el-icon>
            </el-button>
            <el-button
                type="warning"
                size="small"
                @click="generateKnowledgePoints(scope.row)"
                circle
                plain
                :title="$t('courses.generateKnowledgePoints')">
              <el-icon>
                <el-icon-magic-stick/>
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
        :title="courseDialog.isEdit ? $t('courses.editCourse') : $t('courses.addNewCourse')"
        width="500px">
      <el-form
          :model="courseDialog.form"
          :rules="courseDialog.rules"
          ref="courseFormRef"
          label-width="120px">
        <el-form-item :label="$t('courses.courseCode')" prop="courseCode">
          <el-input v-model="courseDialog.form.courseCode" :placeholder="$t('courseManagement.courseCodePlaceholder')"/>
        </el-form-item>
        <el-form-item :label="$t('courses.courseName')" prop="courseName">
          <el-input v-model="courseDialog.form.courseName" :placeholder="$t('courseManagement.courseNamePlaceholder')"/>
        </el-form-item>
        <el-form-item :label="$t('courses.instructor')" prop="instructor">
          <el-input v-model="courseDialog.form.instructor" :placeholder="$t('courseManagement.instructorPlaceholder')"/>
        </el-form-item>
        <el-form-item :label="$t('courses.credits')" prop="credits">
          <el-input-number v-model="courseDialog.form.credits" :min="1" :max="10" style="width: 100%"/>
        </el-form-item>
        <el-form-item :label="$t('courses.hours')" prop="hours">
          <el-input-number v-model="courseDialog.form.hours" :min="1" :max="200" style="width: 100%"/>
        </el-form-item>
        <el-form-item :label="$t('courses.description')" prop="description">
          <el-input v-model="courseDialog.form.description" type="textarea" :rows="3" :placeholder="$t('courseManagement.descriptionPlaceholder')"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="courseDialog.visible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitCourseForm" :loading="courseDialog.loading">
            {{ courseDialog.isEdit ? $t('common.update') : $t('common.create') }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Student Enrollment Dialog -->
    <el-dialog
        v-model="enrollmentDialog.visible"
:title="$t('courses.manageCourseStudents')"
        width="800px"
        :close-on-click-modal="false">
      <div class="enrollment-content">
        <div class="course-info">
          <h3>{{ enrollmentDialog.course.courseName }} ({{ enrollmentDialog.course.courseCode }})</h3>
          <p>{{ $t('courses.instructor') }}: {{ enrollmentDialog.course.instructor }}</p>
        </div>
        
        <el-tabs v-model="enrollmentDialog.activeTab">
          <!-- Enrolled Students Tab -->
          <el-tab-pane :label="$t('courses.enrolledStudents')" name="enrolled">
            <div class="tab-header">
              <div class="tab-header-left">
                <span>{{ $t('courses.totalEnrolled') }}: {{ enrolledStudents.length }}</span>
              </div>
              <div class="tab-header-right">
                <el-button type="danger" size="small" @click="unenrollSelectedStudents" :disabled="selectedEnrolledStudents.length === 0">
                  {{ $t('courses.removeSelected') }}
                </el-button>
              </div>
            </div>
            <el-table
                :data="enrolledStudents"
                v-loading="enrollmentDialog.loading"
                @selection-change="handleEnrolledStudentsSelectionChange"
                height="300">
              <el-table-column type="selection" width="55"/>
              <el-table-column prop="studentId" :label="$t('common.studentId')" width="120"/>
              <el-table-column prop="name" :label="$t('common.name')" min-width="150"/>
              <el-table-column prop="email" :label="$t('common.email')" min-width="200"/>
              <el-table-column prop="major" :label="$t('common.major')" min-width="150"/>
              <el-table-column :label="$t('common.actions')" width="100">
                <template #default="scope">
                  <el-button
                      type="danger"
                      size="small"
                      @click="unenrollStudent(scope.row)"
                      plain>
                    {{ $t('courses.remove') }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <!-- Add Students Tab -->
          <el-tab-pane :label="$t('courses.addStudents')" name="add">
            <div class="tab-header">
              <div class="tab-header-left">
                <el-input
                    v-model="studentSearchKeyword"
                    :placeholder="$t('courseManagement.searchStudentsPlaceholder')"
                    style="width: 300px"
                    clearable>
                  <template #append>
                    <el-button @click="searchAvailableStudents">
                      <el-icon><el-icon-search/></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </div>
              <div class="tab-header-right">
                <el-button type="primary" @click="enrollSelectedStudents" :disabled="selectedAvailableStudents.length === 0">
                  {{ $t('courses.enrollSelected') }} ({{ selectedAvailableStudents.length }})
                </el-button>
              </div>
            </div>
            <el-table
                :data="availableStudents"
                v-loading="enrollmentDialog.loading"
                @selection-change="handleAvailableStudentsSelectionChange"
                height="300">
              <el-table-column type="selection" width="55"/>
              <el-table-column prop="studentId" :label="$t('common.studentId')" width="120"/>
              <el-table-column prop="name" :label="$t('common.name')" min-width="150"/>
              <el-table-column prop="email" :label="$t('common.email')" min-width="200"/>
              <el-table-column prop="major" :label="$t('common.major')" min-width="150"/>
              <el-table-column :label="$t('common.actions')" width="100">
                <template #default="scope">
                  <el-button
                      type="primary"
                      size="small"
                      @click="enrollStudent(scope.row)"
                      plain>
                    {{ $t('courses.enroll') }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="enrollmentDialog.visible = false">{{ $t('common.close') }}</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Knowledge Point Generation Dialog -->
    <el-dialog
        v-model="knowledgePointDialog.visible"
        :title="$t('courses.generateKnowledgePoints')"
        width="600px"
        :close-on-click-modal="false">
      <div class="knowledge-point-content">
        <div class="course-info">
          <h3>{{ knowledgePointDialog.course.courseName }} ({{ knowledgePointDialog.course.courseCode }})</h3>
          <p>{{ $t('courses.instructor') }}: {{ knowledgePointDialog.course.instructor }}</p>
        </div>
        
        <el-form :model="knowledgePointDialog.form" label-width="120px">
          <el-form-item :label="$t('courses.courseContent')" required>
            <el-input
                v-model="knowledgePointDialog.form.courseContent"
                type="textarea"
                :rows="8"
                :placeholder="$t('courseManagement.contentPlaceholder')"
                maxlength="10000"
                show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-alert
                :title="$t('courses.howItWorks')"
                type="info"
                :closable="false"
                show-icon>
              <template #default>
                <p>{{ $t('courses.aiAnalysisDescription') }}</p>
                <ul>
                  <li>{{ $t('courses.extractKnowledgePoints') }}</li>
                  <li>{{ $t('courses.identifyPrerequisites') }}</li>
                  <li>{{ $t('courses.createKnowledgeGraph') }}</li>
                </ul>
              </template>
            </el-alert>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="knowledgePointDialog.visible = false">{{ $t('common.cancel') }}</el-button>
          <el-button 
              type="primary" 
              @click="submitKnowledgePointGeneration" 
              :loading="knowledgePointDialog.loading"
              :disabled="!knowledgePointDialog.form.courseContent.trim()">
            {{ $t('courses.generateKnowledgePoints') }}
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
import courseService from '@/services/courseService'
import courseEnrollmentService from '@/services/courseEnrollmentService'
import knowledgePointService from '@/services/knowledgePointService'

export default {
  name: 'CoursesView',
  setup() {
    const { t } = useI18n()
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
          { required: true, message: t('validation.pleaseEnterCourseCode'), trigger: 'blur' },
          { min: 2, max: 10, message: t('validation.lengthShouldBe2To10Characters'), trigger: 'blur' }
        ],
        courseName: [
          { required: true, message: t('validation.pleaseEnterCourseName'), trigger: 'blur' }
        ],
        instructor: [
          { required: true, message: t('validation.pleaseEnterInstructorName'), trigger: 'blur' }
        ],
        credits: [
          { required: true, message: t('validation.pleaseEnterCredits'), trigger: 'blur' }
        ],
        hours: [
          { required: true, message: t('validation.pleaseEnterHours'), trigger: 'blur' }
        ]
      }
    })

    // Enrollment dialog and data
    const enrollmentDialog = reactive({
      visible: false,
      loading: false,
      activeTab: 'enrolled',
      course: {}
    })
    
    const enrolledStudents = ref([])
    const availableStudents = ref([])
    const selectedEnrolledStudents = ref([])
    const selectedAvailableStudents = ref([])
    const studentSearchKeyword = ref('')

    // Knowledge Point Generation dialog and data
    const knowledgePointDialog = reactive({
      visible: false,
      loading: false,
      course: {},
      form: {
        courseContent: ''
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
          console.error(t('errors.errorFetchingCourses'), error)
          ElMessage.error(t('errors.failedToLoadCourses'))
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
                message: courseDialog.isEdit ? t('courses.courseUpdatedSuccessfully') : t('courses.courseCreatedSuccessfully'),
                type: 'success'
              })
              courseDialog.visible = false
              fetchCourses()
            })
            .catch(error => {
              console.error(courseDialog.isEdit ? t('errors.errorUpdatingCourse') : t('errors.errorCreatingCourse'), error)
              const errorMessage = error.response?.data?.message || (courseDialog.isEdit ? t('courses.failedToUpdateCourse') : t('courses.failedToCreateCourse'))
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
        t('courses.confirmDeleteCourse', { courseName: course.courseName }),
        t('common.confirmDelete'),
        {
          confirmButtonText: t('common.delete'),
          cancelButtonText: t('common.cancel'),
          type: 'warning',
          confirmButtonClass: 'el-button--danger'
        }
      )
        .then(() => {
          courseService.deleteCourse(course.id)
            .then(() => {
              ElMessage({
                type: 'success',
                message: t('courses.courseDeletedSuccessfully')
              })
              fetchCourses()
            })
            .catch(error => {
              console.error(t('errors.errorDeletingCourse'), error)
              const errorMessage = error.response?.data?.message || t('courses.failedToDeleteCourse')
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
            [t('courses.courseCode'), t('courses.courseName'), t('courses.instructor'), t('courses.credits'), t('courses.hours'), t('courses.description')],
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
            message: t('courses.coursesExportedSuccessfully'),
            type: 'success'
          })
        })
        .catch(error => {
          console.error(t('errors.errorExportingCourses'), error)
          const errorMessage = error.response?.data?.message || t('courses.failedToExportCourses')
          ElMessage.error(errorMessage)
        })
    }

    // Enrollment methods
    const manageStudents = (course) => {
      enrollmentDialog.course = course
      enrollmentDialog.visible = true
      enrollmentDialog.activeTab = 'enrolled'
      fetchEnrolledStudents(course.id)
      fetchAvailableStudents(course.id)
    }

    const fetchEnrolledStudents = async (courseId) => {
      try {
        const response = await courseEnrollmentService.getStudentsByCourse(courseId)
        enrolledStudents.value = response.data
      } catch (error) {
        console.error(t('errors.errorFetchingEnrolledStudents'), error)
        ElMessage.error(t('courses.failedToFetchEnrolledStudents'))
      }
    }

    const fetchAvailableStudents = async (courseId) => {
      try {
        const response = await courseEnrollmentService.getAvailableStudents(courseId)
        availableStudents.value = response.data
      } catch (error) {
        console.error(t('errors.errorFetchingAvailableStudents'), error)
        ElMessage.error(t('courses.failedToFetchAvailableStudents'))
      }
    }

    const removeSelectedStudents = async () => {
      if (selectedEnrolledStudents.value.length === 0) {
        ElMessage.warning(t('courses.pleaseSelectStudentsToRemove'))
        return
      }

      try {
        enrollmentDialog.loading = true
        for (const student of selectedEnrolledStudents.value) {
          await courseEnrollmentService.unenrollStudent(enrollmentDialog.course.id, student.id)
        }
        ElMessage.success(t('courses.studentsRemovedSuccessfully'))
        selectedEnrolledStudents.value = []
        await fetchEnrolledStudents(enrollmentDialog.course.id)
        await fetchAvailableStudents(enrollmentDialog.course.id)
      } catch (error) {
        console.error(t('errors.errorRemovingStudents'), error)
        ElMessage.error(t('courses.failedToRemoveStudents'))
      } finally {
        enrollmentDialog.loading = false
      }
    }

    const unenrollStudent = async (student) => {
      try {
        enrollmentDialog.loading = true
        await courseEnrollmentService.unenrollStudent(enrollmentDialog.course.id, student.id)
        ElMessage.success(t('courses.studentRemovedSuccessfully', { name: student.name }))
        await fetchEnrolledStudents(enrollmentDialog.course.id)
        await fetchAvailableStudents(enrollmentDialog.course.id)
      } catch (error) {
        console.error(t('errors.errorRemovingStudent'), error)
        ElMessage.error(t('courses.failedToRemoveStudent'))
      } finally {
        enrollmentDialog.loading = false
      }
    }

    const enrollStudent = async (student) => {
      try {
        enrollmentDialog.loading = true
        await courseEnrollmentService.enrollStudent(enrollmentDialog.course.id, student.id)
        ElMessage.success(t('courses.studentEnrolledSuccessfully', { name: student.name }))
        await fetchEnrolledStudents(enrollmentDialog.course.id)
        await fetchAvailableStudents(enrollmentDialog.course.id)
      } catch (error) {
        console.error(t('errors.errorEnrollingStudent'), error)
        ElMessage.error(t('courses.failedToEnrollStudent'))
      } finally {
        enrollmentDialog.loading = false
      }
    }

    const enrollSelectedStudents = async () => {
      if (selectedAvailableStudents.value.length === 0) {
        ElMessage.warning(t('courses.pleaseSelectStudentsToEnroll'))
        return
      }

      try {
        enrollmentDialog.loading = true
        const studentIds = selectedAvailableStudents.value.map(student => student.id)
        await courseEnrollmentService.enrollMultipleStudents(enrollmentDialog.course.id, studentIds)
        ElMessage.success(t('courses.studentsEnrolledSuccessfully'))
        selectedAvailableStudents.value = []
        await fetchEnrolledStudents(enrollmentDialog.course.id)
        await fetchAvailableStudents(enrollmentDialog.course.id)
      } catch (error) {
        console.error(t('errors.errorEnrollingStudents'), error)
        ElMessage.error(t('courses.failedToEnrollStudents'))
      } finally {
        enrollmentDialog.loading = false
      }
    }

    const handleEnrolledStudentsSelectionChange = (selection) => {
      selectedEnrolledStudents.value = selection
    }

    const handleAvailableStudentsSelectionChange = (selection) => {
      selectedAvailableStudents.value = selection
    }

    const unenrollSelectedStudents = async () => {
      if (selectedEnrolledStudents.value.length === 0) {
        ElMessage.warning(t('courses.pleaseSelectStudentsToUnenroll'))
        return
      }

      try {
        enrollmentDialog.loading = true
        for (const student of selectedEnrolledStudents.value) {
          await courseEnrollmentService.unenrollStudent(enrollmentDialog.course.id, student.id)
        }
        ElMessage.success(t('courses.studentsUnenrolledSuccessfully', { count: selectedEnrolledStudents.value.length }))
        selectedEnrolledStudents.value = []
        await fetchEnrolledStudents(enrollmentDialog.course.id)
        await fetchAvailableStudents(enrollmentDialog.course.id)
      } catch (error) {
        console.error(t('errors.errorUnenrollingStudents'), error)
        ElMessage.error(t('courses.failedToUnenrollStudents'))
      } finally {
        enrollmentDialog.loading = false
      }
    }

    // Knowledge Point Generation methods
    const generateKnowledgePoints = (course) => {
      knowledgePointDialog.course = { ...course }
      knowledgePointDialog.form.courseContent = ''
      knowledgePointDialog.visible = true
    }

    const submitKnowledgePointGeneration = async () => {
      if (!knowledgePointDialog.form.courseContent.trim()) {
        ElMessage.warning(t('courses.pleaseEnterCourseContent'))
        return
      }

      try {
        knowledgePointDialog.loading = true
        const response = await knowledgePointService.generateKnowledgeGraphFromContent(
          knowledgePointDialog.course.id,
          knowledgePointDialog.form.courseContent
        )
        
        const generatedCount = response.data.length
        ElMessage.success(t('courses.knowledgePointsGeneratedSuccessfully', { count: generatedCount, courseName: knowledgePointDialog.course.courseName }))
        
        // Reset form and close dialog
        knowledgePointDialog.form.courseContent = ''
        knowledgePointDialog.visible = false
        
      } catch (error) {
        console.error(t('errors.errorGeneratingKnowledgePoints'), error)
        ElMessage.error(t('courses.failedToGenerateKnowledgePoints'))
      } finally {
        knowledgePointDialog.loading = false
      }
    }

    const filteredAvailableStudents = computed(() => {
      if (!studentSearchKeyword.value) {
        return availableStudents.value
      }
      const keyword = studentSearchKeyword.value.toLowerCase()
      return availableStudents.value.filter(student => 
        student.name.toLowerCase().includes(keyword) ||
        student.email.toLowerCase().includes(keyword) ||
        student.studentId.toLowerCase().includes(keyword)
      )
    })

    onMounted(() => {
      fetchCourses()
    })

    return {
      t,
      courseFormRef,
      courseLoading,
      courseCurrentPage,
      coursePageSize,
      totalCourses,
      courses,
      selectedCourses,
      courseSearchForm,
      courseDialog,
      enrollmentDialog,
      knowledgePointDialog,
      enrolledStudents,
      availableStudents,
      selectedEnrolledStudents,
      selectedAvailableStudents,
      studentSearchKeyword,
      filteredAvailableStudents,
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
      exportCourses,
      manageStudents,
      fetchEnrolledStudents,
      fetchAvailableStudents,
      removeSelectedStudents,
      unenrollStudent,
      enrollStudent,
      enrollSelectedStudents,
      unenrollSelectedStudents,
      handleEnrolledStudentsSelectionChange,
      handleAvailableStudentsSelectionChange,
      generateKnowledgePoints,
      submitKnowledgePointGeneration
    }
  }
}
</script>

<style scoped>
.courses-container {
  padding: 32px 0 0 0;
  min-height: 100vh;
  background: linear-gradient(135deg, #e0e7ff 0%, #f3e8ff 100%);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 8px;
}

.section-header h2 {
  margin: 0;
  color: #4c6ef5;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 1px;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 22px;
  border: none;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(76, 110, 245, 0.07);
  background: rgba(255,255,255,0.98);
  transition: box-shadow 0.2s;
}
.filter-card:hover {
  box-shadow: 0 6px 24px rgba(76, 110, 245, 0.13);
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
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(76, 110, 245, 0.10);
  background: rgba(255,255,255,0.98);
  transition: box-shadow 0.2s;
}
.data-card:hover {
  box-shadow: 0 10px 32px rgba(76, 110, 245, 0.16);
}

.el-table {
  border-radius: 12px;
  overflow: hidden;
  font-size: 15px;
}
.el-table th, .el-table td {
  background: transparent;
}
.el-table__body tr:hover > td {
  background: #f0f5ff !important;
  transition: background 0.2s;
}
.el-table__body tr.current-row > td {
  background: #e0e7ff !important;
}

.el-button {
  border-radius: 8px !important;
  transition: box-shadow 0.2s, background 0.2s, color 0.2s;
}
.el-button--primary {
  background: linear-gradient(90deg, #4c6ef5 0%, #764ba2 100%);
  border: none;
}
.el-button--primary:hover {
  background: linear-gradient(90deg, #5f8cff 0%, #a084e8 100%);
}
.el-button--danger {
  background: linear-gradient(90deg, #ff6b6b 0%, #f06595 100%);
  border: none;
}
.el-button--danger:hover {
  background: linear-gradient(90deg, #ff8787 0%, #f783ac 100%);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.knowledge-point-content .course-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 1px 6px rgba(76, 110, 245, 0.07);
}

.knowledge-point-content .course-info h3 {
  margin: 0 0 5px 0;
  color: #4c6ef5;
  font-size: 19px;
  font-weight: 600;
}

.knowledge-point-content .course-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.knowledge-point-content .el-textarea {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.knowledge-point-content .el-alert {
  margin-top: 10px;
}

.knowledge-point-content .el-alert ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.knowledge-point-content .el-alert li {
  margin-bottom: 4px;
}
</style>