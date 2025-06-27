<template>
  <div class="question-bank-container">
    <div class="header">
      <h1>Question Bank Management</h1>
      <button @click="showCreateModal = true" class="btn btn-primary">
        <i class="fas fa-plus"></i> Add Question
      </button>
    </div>

    <!-- Filters -->
    <div class="filters">
      <div class="filter-group">
        <label>Question Type:</label>
        <select v-model="filters.type" @change="loadQuestions">
          <option value="">All Types</option>
          <option value="SINGLE_CHOICE">Single Choice</option>
          <option value="MULTIPLE_CHOICE">Multiple Choice</option>
          <option value="FILL_IN_THE_BLANK">Fill in the Blank</option>
          <option value="SHORT_ANSWER">Short Answer</option>
          <option value="PROGRAMMING">Programming</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>Difficulty:</label>
        <select v-model="filters.difficulty" @change="loadQuestions">
          <option value="">All Difficulties</option>
          <option value="EASY">Easy</option>
          <option value="MEDIUM">Medium</option>
          <option value="HARD">Hard</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>Search:</label>
        <input 
          type="text" 
          v-model="searchQuery" 
          @input="debounceSearch"
          placeholder="Search questions..."
          class="search-input"
        />
      </div>
    </div>

    <!-- Questions List -->
    <div class="questions-list">
      <div v-if="loading" class="loading">Loading questions...</div>
      
      <div v-else-if="questions.length === 0" class="no-questions">
        No questions found. Create your first question!
      </div>
      
      <div v-else class="questions-grid">
        <div 
          v-for="question in questions" 
          :key="question.id" 
          class="question-card"
        >
          <div class="question-header">
            <span class="question-type" :class="question.questionType.toLowerCase()">
              {{ formatQuestionType(question.questionType) }}
            </span>
            <span class="difficulty" :class="question.difficulty.toLowerCase()">
              {{ question.difficulty }}
            </span>
            <span class="points">{{ question.points }} pts</span>
          </div>
          
          <div class="question-content">
            <p class="question-text">{{ truncateText(question.questionText, 150) }}</p>
            
            <div v-if="question.options && question.options.length > 0" class="options-preview">
              <div class="option" v-for="(option, index) in question.options.slice(0, 2)" :key="index">
                {{ String.fromCharCode(65 + index) }}. {{ option }}
              </div>
              <div v-if="question.options.length > 2" class="more-options">
                ... and {{ question.options.length - 2 }} more options
              </div>
            </div>
          </div>
          
          <div class="question-footer">
            <div class="stats">
              <span class="usage-count">
                <i class="fas fa-chart-line"></i> Used {{ question.usageCount }} times
              </span>
              <span class="avg-score">
                <i class="fas fa-star"></i> Avg: {{ question.averageScore ? question.averageScore.toFixed(1) : 'N/A' }}%
              </span>
            </div>
            
            <div class="actions">
              <button @click="editQuestion(question)" class="btn btn-sm btn-outline">
                <i class="fas fa-edit"></i> Edit
              </button>
              <button @click="deleteQuestion(question.id)" class="btn btn-sm btn-danger">
                <i class="fas fa-trash"></i> Delete
              </button>
            </div>
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

    <!-- Create/Edit Question Modal -->
    <div v-if="showCreateModal" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>{{ editingQuestion ? 'Edit Question' : 'Create New Question' }}</h2>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveQuestion">
            <div class="form-group">
              <label>Question Text *</label>
              <textarea 
                v-model="questionForm.questionText" 
                required 
                rows="4"
                placeholder="Enter your question here..."
              ></textarea>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>Question Type *</label>
                <select v-model="questionForm.questionType" required @change="onQuestionTypeChange">
                  <option value="">Select Type</option>
                  <option value="SINGLE_CHOICE">Single Choice</option>
                  <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                  <option value="FILL_IN_THE_BLANK">Fill in the Blank</option>
                  <option value="SHORT_ANSWER">Short Answer</option>
                  <option value="PROGRAMMING">Programming</option>
                </select>
              </div>
              
              <div class="form-group">
                <label>Difficulty *</label>
                <select v-model="questionForm.difficulty" required>
                  <option value="EASY">Easy</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HARD">Hard</option>
                </select>
              </div>
              
              <div class="form-group">
                <label>Points</label>
                <input 
                  type="number" 
                  v-model.number="questionForm.points" 
                  min="0.5" 
                  step="0.5"
                  placeholder="1.0"
                />
              </div>
            </div>
            
            <!-- Options for Multiple Choice Questions -->
            <div v-if="isChoiceQuestion" class="form-group">
              <label>Answer Options *</label>
              <div class="options-container">
                <div 
                  v-for="(option, index) in questionForm.options" 
                  :key="index" 
                  class="option-input"
                >
                  <input 
                    type="text" 
                    v-model="questionForm.options[index]" 
                    :placeholder="`Option ${String.fromCharCode(65 + index)}`"
                    required
                  />
                  <button 
                    type="button" 
                    @click="removeOption(index)" 
                    class="btn btn-sm btn-danger"
                    :disabled="questionForm.options.length <= 2"
                  >
                    <i class="fas fa-times"></i>
                  </button>
                </div>
                
                <button 
                  type="button" 
                  @click="addOption" 
                  class="btn btn-sm btn-outline"
                  :disabled="questionForm.options.length >= 6"
                >
                  <i class="fas fa-plus"></i> Add Option
                </button>
              </div>
            </div>
            
            <!-- Correct Answer -->
            <div class="form-group">
              <label>Correct Answer *</label>
              <div v-if="questionForm.questionType === 'SINGLE_CHOICE'">
                <select v-model="questionForm.correctAnswer" required>
                  <option value="">Select correct option</option>
                  <option 
                    v-for="(option, index) in questionForm.options" 
                    :key="index" 
                    :value="option"
                  >
                    {{ String.fromCharCode(65 + index) }}. {{ option }}
                  </option>
                </select>
              </div>
              
              <div v-else-if="questionForm.questionType === 'MULTIPLE_CHOICE'">
                <div class="checkbox-group">
                  <label 
                    v-for="(option, index) in questionForm.options" 
                    :key="index" 
                    class="checkbox-label"
                  >
                    <input 
                      type="checkbox" 
                      :value="option" 
                      v-model="questionForm.correctAnswer"
                    />
                    {{ String.fromCharCode(65 + index) }}. {{ option }}
                  </label>
                </div>
              </div>
              
              <div v-else>
                <textarea 
                  v-model="questionForm.correctAnswer" 
                  required 
                  rows="3"
                  placeholder="Enter the correct answer..."
                ></textarea>
              </div>
            </div>
            
            <!-- Programming Language (for programming questions) -->
            <div v-if="questionForm.questionType === 'PROGRAMMING'" class="form-group">
              <label>Programming Language</label>
              <select v-model="questionForm.programmingLanguage">
                <option value="">Select Language</option>
                <option value="java">Java</option>
                <option value="python">Python</option>
                <option value="javascript">JavaScript</option>
                <option value="cpp">C++</option>
                <option value="c">C</option>
                <option value="csharp">C#</option>
              </select>
            </div>
            
            <!-- Template Code (for programming questions) -->
            <div v-if="questionForm.questionType === 'PROGRAMMING'" class="form-group">
              <label>Template Code</label>
              <textarea 
                v-model="questionForm.templateCode" 
                rows="6"
                placeholder="Enter template code here..."
                class="code-textarea"
              ></textarea>
            </div>
            
            <div class="form-group">
              <label>Explanation</label>
              <textarea 
                v-model="questionForm.explanation" 
                rows="3"
                placeholder="Explain the answer or provide additional context..."
              ></textarea>
            </div>
            
            <!-- Knowledge Points Selection -->
            <div class="form-group">
              <label>Knowledge Points</label>
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
                      <span class="kp-difficulty" :class="kp.difficultyLevel.toLowerCase()">{{ kp.difficultyLevel }}</span>
                      <button 
                        type="button" 
                        @click="removeKnowledgePoint(kp.id)" 
                        class="btn btn-sm btn-danger"
                      >
                        <i class="fas fa-times"></i>
                      </button>
                    </div>
                  </div>
                </div>
                
                <button 
                  type="button" 
                  @click="showKnowledgeGraphModal = true" 
                  class="btn btn-outline knowledge-graph-btn"
                >
                  <i class="fas fa-project-diagram"></i> 
                  {{ selectedKnowledgePoints.length > 0 ? 'Modify Knowledge Points' : 'Select Knowledge Points' }}
                </button>
              </div>
            </div>
            
            <div class="form-group">
              <label>Tags (comma-separated)</label>
              <input 
                type="text" 
                v-model="tagsInput" 
                placeholder="e.g., algebra, geometry, calculus"
              />
            </div>
            
            <div class="modal-actions">
              <button type="button" @click="closeModal" class="btn btn-outline">
                Cancel
              </button>
              <button type="submit" class="btn btn-primary" :disabled="saving">
                {{ saving ? 'Saving...' : (editingQuestion ? 'Update' : 'Create') }}
              </button>
            </div>
          </form>
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
              :disable-dragging="true"
              @node-click="toggleKnowledgePointSelection"
              ref="knowledgeGraph"
            />
          </div>
          
          <div class="selection-info">
            <p><strong>Instructions:</strong> Click on nodes in the graph to select/deselect knowledge points for this question.</p>
            <p><strong>Selected:</strong> {{ selectedKnowledgePoints.length }} knowledge point(s)</p>
          </div>
        </div>
        
        <div class="modal-actions">
          <button type="button" @click="closeKnowledgeGraphModal" class="btn btn-outline">
            Cancel
          </button>
          <button type="button" @click="confirmKnowledgePointSelection" class="btn btn-primary">
            Confirm Selection
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { questionService } from '@/services/questionService'
import knowledgePointService from '@/services/knowledgePointService'
import KnowledgeGraphVisualization from '@/components/KnowledgeGraphVisualization.vue'

export default {
  name: 'QuestionBankView',
  components: {
    KnowledgeGraphVisualization
  },
  data() {
    return {
      questions: [],
      loading: false,
      saving: false,
      showCreateModal: false,
      editingQuestion: null,
      searchQuery: '',
      currentPage: 1,
      totalPages: 1,
      pageSize: 12,
      filters: {
        type: '',
        difficulty: ''
      },
      questionForm: {
        questionText: '',
        questionType: '',
        options: ['', ''],
        correctAnswer: '',
        explanation: '',
        difficulty: 'MEDIUM',
        points: 1.0,
        programmingLanguage: '',
        templateCode: '',
        tags: [],
        knowledgePointIds: []
      },
      tagsInput: '',
      searchTimeout: null,
      // Knowledge Graph related
      showKnowledgeGraphModal: false,
      allKnowledgePoints: [],
      selectedKnowledgePoints: [],
      tempSelectedKnowledgePoints: []
    }
  },
  computed: {
    isChoiceQuestion() {
      return ['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(this.questionForm.questionType)
    }
  },
  mounted() {
    this.loadQuestions()
    this.loadKnowledgePoints()
  },
  methods: {
    async loadQuestions() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize,
          search: this.searchQuery
        }
        
        if (this.filters.type) params.type = this.filters.type
        if (this.filters.difficulty) params.difficulty = this.filters.difficulty
        
        const response = await questionService.getQuestions(params)
        this.questions = response.content || response
        this.totalPages = response.totalPages || 1
      } catch (error) {
        console.error('Error loading questions:', error)
        ElMessage.error('Failed to load questions')
      } finally {
        this.loading = false
      }
    },
    
    debounceSearch() {
      clearTimeout(this.searchTimeout)
      this.searchTimeout = setTimeout(() => {
        this.currentPage = 1
        this.loadQuestions()
      }, 500)
    },
    
    formatQuestionType(type) {
      return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
    },
    
    truncateText(text, maxLength) {
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    },
    
    editQuestion(question) {
      this.editingQuestion = question
      // Handle correctAnswer based on question type
      let correctAnswer = question.correctAnswer
      if (question.questionType === 'MULTIPLE_CHOICE') {
        // Convert comma-separated string to array for multiple choice
        if (typeof question.correctAnswer === 'string' && question.correctAnswer) {
          correctAnswer = question.correctAnswer.split(',').map(answer => answer.trim())
        } else {
          correctAnswer = Array.isArray(question.correctAnswer) ? question.correctAnswer : []
        }
      }
      
      this.questionForm = {
        ...question,
        options: question.options ? [...question.options] : ['', ''],
        correctAnswer: correctAnswer,
        knowledgePointIds: question.knowledgePointIds || [],
        tags: question.tags || []
      }
      this.tagsInput = (question.tags || []).join(', ')
      
      // Load selected knowledge points for editing
      this.selectedKnowledgePoints = this.allKnowledgePoints.filter(kp => 
        (question.knowledgePointIds || []).includes(kp.id)
      )
      this.tempSelectedKnowledgePoints = [...this.selectedKnowledgePoints]
      
      this.showCreateModal = true
    },
    
    async deleteQuestion(questionId) {
      if (!confirm('Are you sure you want to delete this question?')) return
      
      try {
        await questionService.deleteQuestion(questionId)
        ElMessage.success('Question deleted successfully')
        this.loadQuestions()
      } catch (error) {
        console.error('Error deleting question:', error)
        ElMessage.error('Failed to delete question')
      }
    },
    
    onQuestionTypeChange() {
      if (this.isChoiceQuestion) {
        if (this.questionForm.options.length < 2) {
          this.questionForm.options = ['', '']
        }
        // Set correctAnswer based on question type
        if (this.questionForm.questionType === 'MULTIPLE_CHOICE') {
          this.questionForm.correctAnswer = []
        } else {
          this.questionForm.correctAnswer = ''
        }
      } else {
        this.questionForm.options = []
        this.questionForm.correctAnswer = ''
      }
    },
    
    addOption() {
      if (this.questionForm.options.length < 6) {
        this.questionForm.options.push('')
      }
    },
    
    removeOption(index) {
      if (this.questionForm.options.length > 2) {
        this.questionForm.options.splice(index, 1)
      }
    },
    
    async saveQuestion() {
      this.saving = true
      try {
        // Process tags
        this.questionForm.tags = this.tagsInput
          .split(',')
          .map(tag => tag.trim())
          .filter(tag => tag.length > 0)
        
        // Clean up options for non-choice questions
        if (!this.isChoiceQuestion) {
          this.questionForm.options = null
        }
        
        // Prepare form data for submission
        const formData = { ...this.questionForm }
        
        // Convert correctAnswer array to string for multiple choice questions
        if (this.questionForm.questionType === 'MULTIPLE_CHOICE' && Array.isArray(this.questionForm.correctAnswer)) {
          formData.correctAnswer = this.questionForm.correctAnswer.join(',')
        }
        
        if (this.editingQuestion) {
          await questionService.updateQuestion(this.editingQuestion.id, formData)
          ElMessage.success('Question updated successfully')
        } else {
          await questionService.createQuestion(formData)
          ElMessage.success('Question created successfully')
        }
        
        this.closeModal()
        this.loadQuestions()
      } catch (error) {
        console.error('Error saving question:', error)
        ElMessage.error('Failed to save question')
      } finally {
        this.saving = false
      }
    },
    
    closeModal() {
      this.showCreateModal = false
      this.editingQuestion = null
      this.questionForm = {
        questionText: '',
        questionType: '',
        options: ['', ''],
        correctAnswer: '',
        explanation: '',
        difficulty: 'MEDIUM',
        knowledgePointIds: [],
        points: 1.0,
        programmingLanguage: '',
        templateCode: '',
        tags: []
      }
      this.tagsInput = ''
      this.selectedKnowledgePoints = []
    },
    
    // Knowledge Graph related methods
    async loadKnowledgePoints() {
      try {
        const response = await knowledgePointService.getAllKnowledgePoints()
        this.allKnowledgePoints = response.data || []
      } catch (error) {
        console.error('Error loading knowledge points:', error)
        ElMessage.error('Failed to load knowledge points')
      }
    },
    
    toggleKnowledgePointSelection(knowledgePoint) {
      const index = this.tempSelectedKnowledgePoints.findIndex(kp => kp.id === knowledgePoint.id)
      if (index > -1) {
        this.tempSelectedKnowledgePoints.splice(index, 1)
      } else {
        this.tempSelectedKnowledgePoints.push(knowledgePoint)
      }
    },
    
    removeKnowledgePoint(knowledgePointId) {
      this.selectedKnowledgePoints = this.selectedKnowledgePoints.filter(kp => kp.id !== knowledgePointId)
      this.questionForm.knowledgePointIds = this.selectedKnowledgePoints.map(kp => kp.id)
    },
    
    closeKnowledgeGraphModal() {
      this.showKnowledgeGraphModal = false
      this.tempSelectedKnowledgePoints = [...this.selectedKnowledgePoints]
    },
    
    confirmKnowledgePointSelection() {
      this.selectedKnowledgePoints = [...this.tempSelectedKnowledgePoints]
      this.questionForm.knowledgePointIds = this.selectedKnowledgePoints.map(kp => kp.id)
      this.showKnowledgeGraphModal = false
    }
  }
}
</script>

<style scoped>
.question-bank-container {
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

.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.question-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.question-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.question-header {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.question-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.question-type.single_choice { background: #e3f2fd; color: #1976d2; }
.question-type.multiple_choice { background: #f3e5f5; color: #7b1fa2; }
.question-type.fill_in_the_blank { background: #e8f5e8; color: #388e3c; }
.question-type.short_answer { background: #fff3e0; color: #f57c00; }
.question-type.programming { background: #fce4ec; color: #c2185b; }

.difficulty {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.difficulty.easy { background: #e8f5e8; color: #2e7d32; }
.difficulty.medium { background: #fff3e0; color: #ef6c00; }
.difficulty.hard { background: #ffebee; color: #c62828; }

.points {
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #666;
}

.question-content {
  margin-bottom: 15px;
}

.question-text {
  font-size: 16px;
  line-height: 1.5;
  margin-bottom: 10px;
  color: #2c3e50;
}

.options-preview {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
}

.option {
  margin-bottom: 5px;
  color: #495057;
}

.more-options {
  color: #6c757d;
  font-style: italic;
  font-size: 13px;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #e9ecef;
}

.stats {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #6c757d;
}

.actions {
  display: flex;
  gap: 8px;
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
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
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
  grid-template-columns: 2fr 1fr 1fr;
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

.form-group textarea {
  resize: vertical;
  font-family: inherit;
}

.code-textarea {
  font-family: 'Courier New', monospace;
  background: #f8f9fa;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-input {
  display: flex;
  gap: 10px;
  align-items: center;
}

.option-input input {
  flex: 1;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
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
.no-questions {
  text-align: center;
  padding: 40px;
  color: #6c757d;
  font-size: 16px;
}

/* Knowledge Graph Modal Styles */
.knowledge-graph-modal {
  width: 90vw;
  max-width: 1200px;
  height: 90vh;
  max-height: 900px;
}

.knowledge-graph-container {
  height: 600px;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
}

.selection-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.selection-info p {
  margin: 5px 0;
  color: #666;
}

/* Knowledge Points Selection Styles */
.knowledge-points-section {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  background: #fafafa;
}

.selected-knowledge-points {
  margin-bottom: 15px;
}

.selected-knowledge-points h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 14px;
}

.selected-kp-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-kp-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 13px;
}

.kp-name {
  font-weight: 500;
  color: #2c3e50;
}

.kp-difficulty {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
}

.kp-difficulty.beginner {
  background: #e8f5e8;
  color: #388e3c;
}

.kp-difficulty.intermediate {
  background: #fff3e0;
  color: #f57c00;
}

.kp-difficulty.advanced {
  background: #fce4ec;
  color: #c2185b;
}

.knowledge-graph-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 2px dashed #007bff;
  background: white;
  color: #007bff;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.knowledge-graph-btn:hover {
  background: #007bff;
  color: white;
  border-style: solid;
}

@media (max-width: 768px) {
  .questions-grid {
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
}
</style>