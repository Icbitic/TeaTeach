<template>
  <div class="test-paper-container">
    <div class="header">
      <h1>Test Paper Management</h1>
      <div class="header-actions">
        <button @click="showGenerateModal = true" class="btn btn-primary">
          <i class="fas fa-magic"></i> Generate Test Paper
        </button>
        <button @click="showCreateModal = true" class="btn btn-outline">
          <i class="fas fa-plus"></i> Create Manually
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters">
      <div class="filter-group">
        <label>Course:</label>
        <select v-model="filters.courseId" @change="loadTestPapers">
          <option value="">All Courses</option>
          <option v-for="course in courses" :key="course.id" :value="course.id">
            {{ course.courseName }}
          </option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>Generation Method:</label>
        <select v-model="filters.generationMethod" @change="loadTestPapers">
          <option value="">All Methods</option>
          <option value="MANUAL">Manual</option>
          <option value="RANDOM">Random</option>
          <option value="BY_KNOWLEDGE_POINT">By Knowledge Point</option>
          <option value="BY_DIFFICULTY">By Difficulty</option>
          <option value="BALANCED">Balanced</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>Search:</label>
        <input 
          type="text" 
          v-model="searchQuery" 
          @input="debounceSearch"
          placeholder="Search test papers..."
          class="search-input"
        />
      </div>
    </div>

    <!-- Test Papers List -->
    <div class="test-papers-list">
      <div v-if="loading" class="loading">Loading test papers...</div>
      
      <div v-else-if="testPapers.length === 0" class="no-papers">
        No test papers found. Create your first test paper!
      </div>
      
      <div v-else class="papers-grid">
        <div 
          v-for="paper in testPapers" 
          :key="paper.id" 
          class="paper-card"
        >
          <div class="paper-header">
            <h3 class="paper-title">{{ paper.paperName }}</h3>
            <span class="generation-method" :class="paper.generationMethod ? paper.generationMethod.toLowerCase() : ''">
              {{ formatGenerationMethod(paper.generationMethod) }}
            </span>
          </div>
          
          <div class="paper-info">
            <div class="info-item">
              <i class="fas fa-book"></i>
              <span>Course: {{ getCourseNameById(paper.courseId) }}</span>
            </div>
            <div class="info-item">
              <i class="fas fa-question-circle"></i>
              <span>{{ paper.questionIds.length }} Questions</span>
            </div>
            <div class="info-item">
              <i class="fas fa-star"></i>
              <span>{{ paper.totalScore }} Points</span>
            </div>
            <div class="info-item">
              <i class="fas fa-clock"></i>
              <span>{{ paper.durationMinutes }} Minutes</span>
            </div>
            <div class="info-item">
              <i class="fas fa-calendar"></i>
              <span>{{ formatDate(paper.createdAt) }}</span>
            </div>
          </div>
          
          <div class="paper-actions">
            <button @click="previewPaper(paper)" class="btn btn-sm btn-outline">
              <i class="fas fa-eye"></i> Preview
            </button>
            <button @click="deletePaper(paper.id)" class="btn btn-sm btn-danger">
              <i class="fas fa-trash"></i> Delete
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        @click="currentPage--" 
        :disabled="currentPage === 1"
        class="btn btn-outline"
      >
        Previous
      </button>
      
      <span class="page-info">
        Page {{ currentPage }} of {{ totalPages }}
      </span>
      
      <button 
        @click="currentPage++" 
        :disabled="currentPage === totalPages"
        class="btn btn-outline"
      >
        Next
      </button>
    </div>

    <!-- Generate Test Paper Modal -->
    <div v-if="showGenerateModal" class="modal-overlay" @click="closeGenerateModal">
      <div class="modal large" @click.stop>
        <div class="modal-header">
          <h2>Generate Test Paper</h2>
          <button @click="closeGenerateModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="generateTestPaper">
            <div class="form-row">
              <div class="form-group">
                <label>Paper Name *</label>
                <input 
                  type="text" 
                  v-model="generateForm.paperName" 
                  required 
                  placeholder="Enter test paper name"
                />
              </div>
              
              <div class="form-group">
                <label>Course *</label>
                <select v-model="generateForm.courseId" required>
                  <option value="">Select Course</option>
                  <option v-for="course in courses" :key="course.id" :value="course.id">
                    {{ course.courseName }}
                  </option>
                </select>
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>Generation Method *</label>
                <select v-model="generateForm.generationMethod" required @change="onGenerationMethodChange">
                  <option value="">Select Method</option>
                  <option value="RANDOM">Random Selection</option>
                  <option value="BY_KNOWLEDGE_POINT">By Knowledge Points</option>
                  <option value="BY_DIFFICULTY">By Difficulty</option>
                  <option value="BALANCED">Balanced Distribution</option>
                </select>
              </div>
              
              <div class="form-group">
                <label>Total Questions *</label>
                <input 
                  type="number" 
                  v-model.number="generateForm.totalQuestions" 
                  required 
                  min="1" 
                  max="100"
                  placeholder="20"
                />
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>Duration (minutes)</label>
                <input 
                  type="number" 
                  v-model.number="generateForm.durationMinutes" 
                  min="5" 
                  max="300"
                  placeholder="60"
                />
              </div>
              
              <div class="form-group">
                <label>Total Score</label>
                <input 
                  type="number" 
                  v-model.number="generateForm.totalScore" 
                  min="1" 
                  step="0.5"
                  placeholder="Auto-calculate"
                />
              </div>
            </div>
            
            <!-- Question Types Selection -->
            <div class="form-group">
              <label>Question Types</label>
              <div class="checkbox-group">
                <label class="checkbox-label" v-for="type in questionTypes" :key="type">
                  <input 
                    type="checkbox" 
                    :value="type" 
                    v-model="generateForm.questionTypes"
                  />
                  {{ formatQuestionType(type) }}
                </label>
              </div>
            </div>
            
            <!-- Knowledge Points Selection (for BY_KNOWLEDGE_POINT method) -->
            <div v-if="generateForm.generationMethod === 'BY_KNOWLEDGE_POINT'" class="form-group">
              <label>Knowledge Points *</label>
              <div class="knowledge-points-section">
                <div class="selected-knowledge-points" v-if="selectedKnowledgePoints.length > 0">
                  <h4>Selected Knowledge Points ({{ selectedKnowledgePoints.length }})</h4>
                  <div class="selected-kp-list">
                    <div 
                      v-for="kp in selectedKnowledgePoints" 
                      :key="kp.id" 
                      class="selected-kp-item"
                    >
                      <span class="kp-name">{{ kp.name }}</span>
                      <span class="kp-difficulty" :class="kp.difficultyLevel ? kp.difficultyLevel.toLowerCase() : ''">
                        {{ kp.difficultyLevel || 'Unknown' }}
                      </span>
                      <button 
                        type="button" 
                        @click="removeKnowledgePoint(kp.id)" 
                        class="remove-btn"
                        title="Remove knowledge point"
                      >
                        ×
                      </button>
                    </div>
                  </div>
                </div>
                
                <button 
                  type="button" 
                  @click="showKnowledgeGraphModal = true" 
                  class="knowledge-graph-btn"
                >
                  <i class="fas fa-project-diagram"></i>
                  Select from Knowledge Graph
                </button>
              </div>
            </div>
            
            <!-- Difficulty Distribution (for BY_DIFFICULTY method) -->
            <div v-if="generateForm.generationMethod === 'BY_DIFFICULTY'" class="form-group">
              <label>Difficulty Distribution</label>
              <div class="difficulty-distribution">
                <div class="difficulty-item">
                  <label>Easy Questions:</label>
                  <input 
                    type="number" 
                    v-model.number="generateForm.difficultyQuestionCounts.EASY" 
                    min="0"
                    placeholder="0"
                  />
                </div>
                <div class="difficulty-item">
                  <label>Medium Questions:</label>
                  <input 
                    type="number" 
                    v-model.number="generateForm.difficultyQuestionCounts.MEDIUM" 
                    min="0"
                    placeholder="0"
                  />
                </div>
                <div class="difficulty-item">
                  <label>Hard Questions:</label>
                  <input 
                    type="number" 
                    v-model.number="generateForm.difficultyQuestionCounts.HARD" 
                    min="0"
                    placeholder="0"
                  />
                </div>
              </div>
            </div>
            
            <!-- Balanced Weights (for BALANCED method) -->
            <div v-if="generateForm.generationMethod === 'BALANCED'" class="form-group">
              <label>Difficulty Weights</label>
              <div class="difficulty-weights">
                <div class="weight-item">
                  <label>Easy Weight:</label>
                  <input 
                    type="number" 
                    v-model.number="generateForm.difficultyWeights.EASY" 
                    min="0" 
                    max="1" 
                    step="0.1"
                    placeholder="0.3"
                  />
                </div>
                <div class="weight-item">
                  <label>Medium Weight:</label>
                  <input 
                    type="number" 
                    v-model.number="generateForm.difficultyWeights.MEDIUM" 
                    min="0" 
                    max="1" 
                    step="0.1"
                    placeholder="0.5"
                  />
                </div>
                <div class="weight-item">
                  <label>Hard Weight:</label>
                  <input 
                    type="number" 
                    v-model.number="generateForm.difficultyWeights.HARD" 
                    min="0" 
                    max="1" 
                    step="0.1"
                    placeholder="0.2"
                  />
                </div>
              </div>
            </div>
            
            <!-- Preview Questions Button -->
            <div class="form-group">
              <button 
                type="button" 
                @click="previewGeneratedQuestions" 
                class="btn btn-outline"
                :disabled="!canPreview"
              >
                <i class="fas fa-eye"></i> Preview Questions
              </button>
            </div>
            
            <!-- Preview Results -->
            <div v-if="previewResults.length > 0" class="preview-section">
              <h4>Preview Questions ({{ previewResults.length }} found)</h4>
              <div class="preview-questions">
                <div 
                  v-for="(question, index) in previewResults.slice(0, 5)" 
                  :key="question.id" 
                  class="preview-question"
                >
                  <div class="question-header">
                    <span class="question-number">{{ index + 1 }}.</span>
                    <span class="question-type">{{ formatQuestionType(question.questionType) }}</span>
                    <span class="difficulty">{{ question.difficulty }}</span>
                    <span class="points">{{ question.points }} pts</span>
                  </div>
                  <p class="question-text">{{ truncateText(question.questionText, 100) }}</p>
                </div>
                <div v-if="previewResults.length > 5" class="more-questions">
                  ... and {{ previewResults.length - 5 }} more questions
                </div>
              </div>
            </div>
            
            <div class="modal-actions">
              <button type="button" @click="closeGenerateModal" class="btn btn-outline">
                Cancel
              </button>
              <button type="submit" class="btn btn-primary" :disabled="generating">
                {{ generating ? 'Generating...' : 'Generate Test Paper' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Create Manual Test Paper Modal -->
    <div v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>Create Test Paper Manually</h2>
          <button @click="closeCreateModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="createTestPaper">
            <div class="form-group">
              <label>Paper Name *</label>
              <input 
                type="text" 
                v-model="createForm.paperName" 
                required 
                placeholder="Enter test paper name"
              />
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>Course *</label>
                <select v-model="createForm.courseId" required>
                  <option value="">Select Course</option>
                  <option v-for="course in courses" :key="course.id" :value="course.id">
                    {{ course.courseName }}
                  </option>
                </select>
              </div>
              
              <div class="form-group">
                <label>Duration (minutes)</label>
                <input 
                  type="number" 
                  v-model.number="createForm.durationMinutes" 
                  min="5" 
                  max="300"
                  placeholder="60"
                />
              </div>
            </div>
            
            <div class="form-group">
              <label>Questions *</label>
              <div class="question-selection">
                <div class="search-questions">
                  <input 
                    type="text" 
                    v-model="questionSearch" 
                    placeholder="Search questions..."
                    @input="searchQuestions"
                  />
                </div>
                <div class="questions-list">
                  <div 
                    v-for="question in filteredQuestions" 
                    :key="question.id" 
                    class="question-item"
                    :class="{ selected: createForm.questionIds.includes(question.id) }"
                    @click="toggleQuestion(question.id)"
                  >
                    <div class="question-info">
                      <span class="question-type">{{ formatQuestionType(question.questionType) }}</span>
                      <span class="difficulty">{{ question.difficulty }}</span>
                      <span class="points">{{ question.points }} pts</span>
                    </div>
                    <p class="question-text">{{ truncateText(question.questionText, 80) }}</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="selected-questions">
              <h4>Selected Questions ({{ createForm.questionIds.length }})</h4>
              <div class="selected-list">
                <div 
                  v-for="(questionId, index) in createForm.questionIds" 
                  :key="questionId" 
                  class="selected-question"
                >
                  <span>{{ index + 1 }}. {{ getQuestionById(questionId)?.questionText.substring(0, 50) }}...</span>
                  <button 
                    type="button" 
                    @click="removeQuestion(questionId)" 
                    class="btn btn-sm btn-danger"
                  >
                    <i class="fas fa-times"></i>
                  </button>
                </div>
              </div>
            </div>
            
            <div class="modal-actions">
              <button type="button" @click="closeCreateModal" class="btn btn-outline">
                Cancel
              </button>
              <button type="submit" class="btn btn-primary" :disabled="creating || createForm.questionIds.length === 0">
                {{ creating ? 'Creating...' : 'Create Test Paper' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Preview Paper Modal -->
    <div v-if="showPreviewModal" class="modal-overlay" @click="closePreviewModal">
      <div class="modal large" @click.stop>
        <div class="modal-header">
          <h2>{{ previewPaperData.paperName }}</h2>
          <button @click="closePreviewModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <div class="paper-details">
            <div class="detail-item">
              <strong>Course:</strong> {{ getCourseNameById(previewPaperData.courseId) }}
            </div>
            <div class="detail-item">
              <strong>Questions:</strong> {{ previewPaperData.questionIds.length }}
            </div>
            <div class="detail-item">
              <strong>Total Score:</strong> {{ previewPaperData.totalScore }} points
            </div>
            <div class="detail-item">
              <strong>Duration:</strong> {{ previewPaperData.durationMinutes }} minutes
            </div>
            <div class="detail-item">
              <strong>Generation Method:</strong> {{ formatGenerationMethod(previewPaperData.generationMethod) }}
            </div>
          </div>
          
          <div class="questions-preview">
            <h4>Questions</h4>
            <div 
              v-for="(question, index) in previewQuestions" 
              :key="question.id" 
              class="preview-question-detail"
            >
              <div class="question-header">
                <span class="question-number">{{ index + 1 }}.</span>
                <span class="question-type">{{ formatQuestionType(question.questionType) }}</span>
                <span class="difficulty">{{ question.difficulty }}</span>
                <span class="points">{{ question.points }} pts</span>
              </div>
              <p class="question-text">{{ question.questionText }}</p>
              
              <div v-if="question.options && question.options.length > 0" class="options">
                <div 
                  v-for="(option, optionIndex) in question.options" 
                  :key="optionIndex" 
                  class="option"
                >
                  {{ String.fromCharCode(65 + optionIndex) }}. {{ option }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Knowledge Graph Selection Modal -->
    <div v-if="showKnowledgeGraphModal" class="modal-overlay" @click="closeKnowledgeGraphModal">
      <div class="modal knowledge-graph-modal" @click.stop>
        <div class="modal-header">
          <h2>Select Knowledge Points</h2>
          <button @click="closeKnowledgeGraphModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <div class="knowledge-graph-container">
            <KnowledgeGraphVisualization
              :knowledge-points="allKnowledgePoints"
              :selected-knowledge-point-ids="tempSelectedKnowledgePoints.map(kp => kp.id)"
              :allow-dragging-with-physics-timeout="true"
              @node-click="toggleKnowledgePointSelection"
              ref="knowledgeGraph"
            />
          </div>
          
          <div class="selection-info">
            <p><strong>Instructions:</strong> Click on knowledge points in the graph to select/deselect them for test generation.</p>
            <p><strong>Selected:</strong> {{ tempSelectedKnowledgePoints.length }} knowledge point(s)</p>
          </div>
        </div>
        
        <div class="modal-actions">
          <button type="button" @click="closeKnowledgeGraphModal" class="btn btn-outline">
            Cancel
          </button>
          <button type="button" @click="confirmKnowledgePointSelection" class="btn btn-primary">
            Confirm Selection ({{ tempSelectedKnowledgePoints.length }})
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { testPaperService } from '@/services/testPaperService'
import { questionService } from '@/services/questionService'
import { courseService } from '@/services/courseService'
import { knowledgePointService } from '@/services/knowledgePointService'
import KnowledgeGraphVisualization from '@/components/KnowledgeGraphVisualization.vue'

export default {
  name: 'TestPaperView',
  components: {
    KnowledgeGraphVisualization
  },
  data() {
    return {
      testPapers: [],
      courses: [],
      questions: [],
      knowledgePoints: [],
      loading: false,
      generating: false,
      creating: false,
      showGenerateModal: false,
      showCreateModal: false,
      showPreviewModal: false,
      showKnowledgeGraphModal: false,
      allKnowledgePoints: [],
      selectedKnowledgePoints: [],
      tempSelectedKnowledgePoints: [],
      searchQuery: '',
      questionSearch: '',
      knowledgePointSearch: '',
      currentPage: 1,
      totalPages: 1,
      pageSize: 12,
      filters: {
        courseId: '',
        generationMethod: ''
      },
      generateForm: {
        paperName: '',
        courseId: '',
        generationMethod: '',
        totalQuestions: 20,
        durationMinutes: 60,
        totalScore: null,
        questionTypes: [],
        knowledgePointIds: [],
        knowledgePointQuestionCounts: {},
        difficultyQuestionCounts: {
          EASY: 0,
          MEDIUM: 0,
          HARD: 0
        },
        difficultyWeights: {
          EASY: 0.3,
          MEDIUM: 0.5,
          HARD: 0.2
        }
      },
      createForm: {
        paperName: '',
        courseId: '',
        durationMinutes: 60,
        questionIds: [],
        generationMethod: 'MANUAL'
      },
      previewPaperData: {},
      previewQuestions: [],
      previewResults: [],
      questionTypes: ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'FILL_IN_THE_BLANK', 'SHORT_ANSWER', 'PROGRAMMING'],
      searchTimeout: null
    }
  },
  computed: {
    filteredQuestions() {
      if (!this.questionSearch) return this.questions
      return this.questions.filter(q => 
        q.questionText.toLowerCase().includes(this.questionSearch.toLowerCase())
      )
    },
    
    filteredKnowledgePoints() {
      if (!this.knowledgePointSearch) return this.knowledgePoints
      return this.knowledgePoints.filter(kp => 
        kp.name.toLowerCase().includes(this.knowledgePointSearch.toLowerCase())
      )
    },
    
    canPreview() {
      return this.generateForm.generationMethod && 
             this.generateForm.totalQuestions > 0 &&
             this.generateForm.courseId
    }
  },
  mounted() {
    this.loadTestPapers()
    this.loadCourses()
    this.loadQuestions()
    this.loadKnowledgePoints()
  },
  methods: {
    async loadTestPapers() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize,
          search: this.searchQuery
        }
        
        if (this.filters.courseId) params.courseId = this.filters.courseId
        if (this.filters.generationMethod) params.generationMethod = this.filters.generationMethod
        
        const response = await testPaperService.getTestPapers(params)
        this.testPapers = response.content || response
        this.totalPages = response.totalPages || 1
      } catch (error) {
        console.error('Error loading test papers:', error)
        ElMessage.error('Failed to load test papers')
      } finally {
        this.loading = false
      }
    },
    
    async loadCourses() {
      try {
        const response = await courseService.getAllCourses()
        this.courses = response.data
      } catch (error) {
        console.error('Error loading courses:', error)
        ElMessage.error('Failed to load courses')
      }
    },
    
    async loadQuestions() {
      try {
        const response = await questionService.getQuestions({ size: 1000 })
        this.questions = response.content || response.data || response
      } catch (error) {
        console.error('Error loading questions:', error)
        ElMessage.error('Failed to load questions')
      }
    },
    
    async loadKnowledgePoints() {
      try {
        const response = await knowledgePointService.getAllKnowledgePoints()
        this.allKnowledgePoints = response.data || response
        this.knowledgePoints = this.allKnowledgePoints // For backward compatibility
      } catch (error) {
        console.error('Error loading knowledge points:', error)
        ElMessage.error('Failed to load knowledge points')
      }
    },
    
    debounceSearch() {
      clearTimeout(this.searchTimeout)
      this.searchTimeout = setTimeout(() => {
        this.currentPage = 1
        this.loadTestPapers()
      }, 500)
    },
    
    formatGenerationMethod(method) {
      if (!method) return 'Manual'
      const methods = {
        'RANDOM': 'Random',
        'BY_KNOWLEDGE_POINT': 'By Knowledge Point',
        'BY_DIFFICULTY': 'By Difficulty',
        'BALANCED': 'Balanced'
      }
      return methods[method] || method
    },
    
    formatQuestionType(type) {
      return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
    },
    
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString()
    },
    
    truncateText(text, maxLength) {
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    },
    
    getCourseNameById(courseId) {
      const course = this.courses.find(c => c.id === courseId)
      return course ? course.courseName : 'Unknown Course'
    },
    
    getQuestionById(questionId) {
      return this.questions.find(q => q.id === questionId)
    },
    
    onGenerationMethodChange() {
      // Reset method-specific fields
      this.generateForm.knowledgePointIds = []
      this.generateForm.difficultyQuestionCounts = { EASY: 0, MEDIUM: 0, HARD: 0 }
      this.generateForm.difficultyWeights = { EASY: 0.3, MEDIUM: 0.5, HARD: 0.2 }
      this.previewResults = []
    },
    
    async previewGeneratedQuestions() {
      try {
        this.previewResults = await testPaperService.previewQuestions(this.generateForm)
      } catch (error) {
        console.error('Error previewing questions:', error)
        ElMessage.error('Failed to preview questions')
      }
    },
    
    async generateTestPaper() {
      this.generating = true
      try {
        await testPaperService.generateTestPaper(this.generateForm)
        ElMessage.success('Test paper generated successfully')
        this.closeGenerateModal()
        this.loadTestPapers()
      } catch (error) {
        console.error('Error generating test paper:', error)
        ElMessage.error('Failed to generate test paper')
      } finally {
        this.generating = false
      }
    },
    
    async createTestPaper() {
      this.creating = true
      try {
        const paperData = {
          ...this.createForm,
          totalScore: this.calculateTotalScore(this.createForm.questionIds)
        }
        await testPaperService.createTestPaper(paperData)
        ElMessage.success('Test paper created successfully')
        this.closeCreateModal()
        this.loadTestPapers()
      } catch (error) {
        console.error('Error creating test paper:', error)
        ElMessage.error('Failed to create test paper')
      } finally {
        this.creating = false
      }
    },
    
    calculateTotalScore(questionIds) {
      return questionIds.reduce((total, id) => {
        const question = this.getQuestionById(id)
        return total + (question ? question.points : 0)
      }, 0)
    },
    
    toggleQuestion(questionId) {
      const index = this.createForm.questionIds.indexOf(questionId)
      if (index > -1) {
        this.createForm.questionIds.splice(index, 1)
      } else {
        this.createForm.questionIds.push(questionId)
      }
    },
    
    removeQuestion(questionId) {
      const index = this.createForm.questionIds.indexOf(questionId)
      if (index > -1) {
        this.createForm.questionIds.splice(index, 1)
      }
    },
    
    async previewPaper(paper) {
      try {
        this.previewPaperData = paper
        this.previewQuestions = await Promise.all(
          paper.questionIds.map(id => questionService.getQuestionById(id))
        )
        this.showPreviewModal = true
      } catch (error) {
        console.error('Error loading paper preview:', error)
        ElMessage.error('Failed to load paper preview')
      }
    },
    

    
    async deletePaper(paperId) {
      if (!confirm('Are you sure you want to delete this test paper?')) return
      
      try {
        await testPaperService.deleteTestPaper(paperId)
        ElMessage.success('Test paper deleted successfully')
        this.loadTestPapers()
      } catch (error) {
        console.error('Error deleting test paper:', error)
        ElMessage.error('Failed to delete test paper')
      }
    },
    
    searchQuestions() {
      // Debounced search is handled by the computed property
    },
    
    searchKnowledgePoints() {
      // Debounced search is handled by the computed property
    },
    
    closeGenerateModal() {
      this.showGenerateModal = false
      this.generateForm = {
        paperName: '',
        courseId: '',
        generationMethod: '',
        totalQuestions: 20,
        durationMinutes: 60,
        totalScore: null,
        questionTypes: [],
        knowledgePointIds: [],
        knowledgePointQuestionCounts: {},
        difficultyQuestionCounts: { EASY: 0, MEDIUM: 0, HARD: 0 },
        difficultyWeights: { EASY: 0.3, MEDIUM: 0.5, HARD: 0.2 }
      }
      this.previewResults = []
    },
    
    closeCreateModal() {
      this.showCreateModal = false
      this.createForm = {
        paperName: '',
        courseId: '',
        durationMinutes: 60,
        questionIds: [],
        generationMethod: 'MANUAL'
      }
      this.questionSearch = ''
    },
    
    closePreviewModal() {
      this.showPreviewModal = false
      this.previewPaperData = {}
      this.previewQuestions = []
    },
    
    // Knowledge Graph Methods
    openKnowledgeGraphModal() {
      this.tempSelectedKnowledgePoints = [...this.selectedKnowledgePoints]
      this.showKnowledgeGraphModal = true
    },
    
    closeKnowledgeGraphModal() {
      this.showKnowledgeGraphModal = false
      this.tempSelectedKnowledgePoints = []
    },
    
    confirmKnowledgePointSelection() {
      this.selectedKnowledgePoints = [...this.tempSelectedKnowledgePoints]
      this.generateForm.knowledgePointIds = this.selectedKnowledgePoints.map(kp => kp.id)
      
      // Initialize knowledgePointQuestionCounts with default value of 1 for each selected knowledge point
      this.generateForm.knowledgePointQuestionCounts = {}
      this.selectedKnowledgePoints.forEach(kp => {
        this.generateForm.knowledgePointQuestionCounts[kp.id] = 1
      })
      
      this.closeKnowledgeGraphModal()
      ElMessage.success(`Selected ${this.selectedKnowledgePoints.length} knowledge points`)
    },
    
    toggleKnowledgePointSelection(knowledgePoint) {
      const index = this.tempSelectedKnowledgePoints.findIndex(kp => kp.id === knowledgePoint.id)
      if (index > -1) {
        // Remove if already selected
        this.tempSelectedKnowledgePoints.splice(index, 1)
      } else {
        // Add if not selected
        this.tempSelectedKnowledgePoints.push(knowledgePoint)
      }
    },
    
    onKnowledgePointSelectionChange(selectedIds) {
      this.tempSelectedKnowledgePoints = this.allKnowledgePoints.filter(kp => 
        selectedIds.includes(kp.id)
      )
    },
    
    removeKnowledgePoint(index) {
      const removedKp = this.selectedKnowledgePoints[index]
      this.selectedKnowledgePoints.splice(index, 1)
      this.generateForm.knowledgePointIds = this.selectedKnowledgePoints.map(kp => kp.id)
      
      // Remove from knowledgePointQuestionCounts
      if (removedKp && this.generateForm.knowledgePointQuestionCounts) {
        delete this.generateForm.knowledgePointQuestionCounts[removedKp.id]
      }
    }
  }
}
</script>

<style scoped>
/* Base styles similar to QuestionBankView but adapted for test papers */
.test-paper-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
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
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.filter-group label {
  font-weight: 600;
  color: #495057;
  font-size: 14px;
}

.filter-group select,
.search-input {
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
}

.search-input {
  min-width: 250px;
}

.papers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.paper-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.paper-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.paper-title {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
  flex: 1;
}

.generation-method {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  margin-left: 10px;
}

.generation-method.random { background: #e3f2fd; color: #1976d2; }
.generation-method.by_knowledge_point { background: #f3e5f5; color: #7b1fa2; }
.generation-method.by_difficulty { background: #e8f5e8; color: #388e3c; }
.generation-method.balanced { background: #fff3e0; color: #f57c00; }

.paper-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #495057;
}

.info-item i {
  width: 16px;
  color: #6c757d;
}

.paper-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 30px;
}

.page-info {
  font-weight: 600;
  color: #495057;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal.large {
  max-width: 900px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h2 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 600;
  color: #495057;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.knowledge-points-selection,
.question-selection {
  border: 1px solid #ced4da;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.search-knowledge-points,
.search-questions {
  padding: 10px;
  border-bottom: 1px solid #e9ecef;
}

.search-knowledge-points input,
.search-questions input {
  width: 100%;
  border: none;
  outline: none;
}

.knowledge-points-list,
.questions-list {
  padding: 10px;
  max-height: 150px;
  overflow-y: auto;
}

.question-item {
  padding: 10px;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.question-item:hover {
  background: #f8f9fa;
}

.question-item.selected {
  background: #e3f2fd;
  border-color: #1976d2;
}

.question-info {
  display: flex;
  gap: 10px;
  margin-bottom: 5px;
  font-size: 12px;
}

.question-type,
.difficulty,
.points {
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 600;
}

.question-type { background: #e3f2fd; color: #1976d2; }
.difficulty { background: #fff3e0; color: #ef6c00; }
.points { background: #f5f5f5; color: #666; }

.question-text {
  margin: 0;
  font-size: 14px;
  color: #495057;
}

.difficulty-distribution,
.difficulty-weights {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.difficulty-item,
.weight-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.difficulty-item label,
.weight-item label {
  font-size: 14px;
  font-weight: 600;
}

.preview-section {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.preview-questions {
  margin-top: 10px;
}

.preview-question {
  padding: 10px;
  background: white;
  border-radius: 4px;
  margin-bottom: 8px;
}

.question-header {
  display: flex;
  gap: 10px;
  margin-bottom: 5px;
  font-size: 12px;
}

.question-number {
  font-weight: 600;
  color: #2c3e50;
}

.more-questions {
  text-align: center;
  color: #6c757d;
  font-style: italic;
  margin-top: 10px;
}

.selected-questions {
  margin-top: 20px;
}

.selected-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 10px;
}

.selected-question {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 5px;
}

.paper-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.detail-item {
  font-size: 14px;
}

.questions-preview {
  margin-top: 20px;
}

.preview-question-detail {
  padding: 15px;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  margin-bottom: 15px;
}

.options {
  margin-top: 10px;
  padding-left: 20px;
}

.option {
  margin-bottom: 5px;
  color: #495057;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-outline {
  background: white;
  color: #6c757d;
  border: 1px solid #ced4da;
}

.btn-outline:hover {
  background: #f8f9fa;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover {
  background: #c82333;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading,
.no-papers {
  text-align: center;
  padding: 40px;
  color: #6c757d;
  font-size: 16px;
}

@media (max-width: 768px) {
  .papers-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .filters {
    flex-direction: column;
  }
  
  .search-input {
    min-width: auto;
  }
  
  .difficulty-distribution,
  .difficulty-weights {
    grid-template-columns: 1fr;
  }
  
  .paper-details {
    grid-template-columns: 1fr;
  }
}

/* Knowledge Graph Modal Styles */
.knowledge-graph-modal {
  max-width: 95vw;
  width: 1200px;
  height: 90vh;
  max-height: 900px;
}

.knowledge-graph-container {
  height: 600px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 15px 0;
}

.selected-knowledge-points {
  margin: 15px 0;
}

.selected-knowledge-points h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 14px;
}

.knowledge-point-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
  padding: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background: #f9f9f9;
}

.knowledge-point-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 8px;
  background: #e3f2fd;
  color: #1976d2;
  border-radius: 12px;
  font-size: 12px;
  border: 1px solid #bbdefb;
}

.knowledge-point-tag .remove-btn {
  background: none;
  border: none;
  color: #1976d2;
  cursor: pointer;
  padding: 0;
  margin-left: 4px;
  font-size: 14px;
  line-height: 1;
}

.knowledge-point-tag .remove-btn:hover {
  color: #d32f2f;
}

.selection-info {
  margin: 15px 0;
  padding: 10px;
  background: #f0f8ff;
  border-left: 4px solid #2196f3;
  border-radius: 4px;
}

.selection-info p {
  margin: 0;
  color: #1976d2;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 0 0 0;
  border-top: 1px solid #eee;
  margin-top: 15px;
}
</style>