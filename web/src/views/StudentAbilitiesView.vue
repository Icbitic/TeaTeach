<template>
  <div class="student-abilities-view">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1><TypewriterText :text="'My Learning Abilities'" :show="true" :speed="70" /></h1>
          <p class="page-description">
            Discover your learning strengths and get personalized recommendations to enhance your educational journey.
          </p>
        </div>
        <div class="header-actions">
          <el-button
            type="primary"
            @click="refreshAnalysis"
            :loading="refreshing"
            icon="el-icon-refresh"
          >
            Refresh Analysis
          </el-button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="abilities-content">
      <StudentAbilityVisualization
        :student-id="currentStudentId"
        ref="abilityVisualization"
      />
    </div>

    <!-- Help Section -->
    <el-card class="help-section" shadow="hover">
      <div class="help-header">
        <h3><i class="el-icon-question"></i> Understanding Your Analysis</h3>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon problem-solving">
              <i class="el-icon-cpu"></i>
            </div>
            <h4>Problem Solving</h4>
            <p>Measures your ability to identify, analyze, and solve complex problems systematically.</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon critical-thinking">
              <i class="el-icon-view"></i>
            </div>
            <h4>Critical Thinking</h4>
            <p>Evaluates your capacity to analyze information objectively and make reasoned judgments.</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon creativity">
              <i class="el-icon-brush"></i>
            </div>
            <h4>Creativity</h4>
            <p>Assesses your ability to generate innovative ideas and think outside conventional boundaries.</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon collaboration">
              <i class="el-icon-user-solid"></i>
            </div>
            <h4>Collaboration</h4>
            <p>Measures your effectiveness in working with others and contributing to team success.</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon communication">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <h4>Communication</h4>
            <p>Evaluates your ability to express ideas clearly and interact effectively with others.</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon recommendations">
              <i class="el-icon-magic-stick"></i>
            </div>
            <h4>Recommendations</h4>
            <p>AI-powered suggestions for resources that match your learning style and improvement areas.</p>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- Tips Section -->
    <el-card class="tips-section" shadow="hover">
      <div class="tips-header">
        <h3><i class="el-icon-lightbulb"></i> Tips for Improvement</h3>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :md="12">
          <div class="tip-category">
            <h4>Enhancing Your Strengths</h4>
            <ul>
              <li>Focus on advanced materials in your strong areas</li>
              <li>Mentor other students in subjects you excel at</li>
              <li>Take on leadership roles in group projects</li>
              <li>Explore specialized courses in your interest areas</li>
            </ul>
          </div>
        </el-col>
        <el-col :xs="24" :md="12">
          <div class="tip-category">
            <h4>Improving Weak Areas</h4>
            <ul>
              <li>Practice regularly with targeted exercises</li>
              <li>Seek help from teachers or tutors</li>
              <li>Form study groups with classmates</li>
              <li>Use the recommended resources for guided learning</li>
            </ul>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import StudentAbilityVisualization from '../components/StudentAbilityVisualization.vue'
import TypewriterText from '../components/TypewriterText.vue'

export default {
  name: 'StudentAbilitiesView',
  components: {
    StudentAbilityVisualization,
    TypewriterText
  },
  data() {
    return {
      refreshing: false
    }
  },
  computed: {
    currentStudentId() {
      // Get student ID from user data stored in localStorage
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      return user.referenceId
    }
  },
  methods: {
    async refreshAnalysis() {
      this.refreshing = true
      try {
        // Refresh the ability visualization component
        if (this.$refs.abilityVisualization) {
          await this.$refs.abilityVisualization.loadAbilityData()
        }
        this.$message.success('Analysis refreshed successfully!')
      } catch (error) {
        console.error('Error refreshing analysis:', error)
        this.$message.error('Failed to refresh analysis')
      } finally {
        this.refreshing = false
      }
    }
  }
}
</script>

<style scoped>
.student-abilities-view {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.page-header {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.header-text h1 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 32px;
  font-weight: 600;
}

.page-description {
  color: #606266;
  font-size: 16px;
  margin: 0;
  line-height: 1.5;
}

.header-actions {
  flex-shrink: 0;
}

.abilities-content {
  background: white;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.help-section,
.tips-section {
  margin-bottom: 30px;
}

.help-header,
.tips-header {
  margin-bottom: 25px;
  text-align: center;
}

.help-header h3,
.tips-header h3 {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.help-item {
  text-align: center;
  padding: 20px;
  height: 100%;
}

.help-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  font-size: 24px;
  color: white;
}

.help-icon.problem-solving {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.help-icon.critical-thinking {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.help-icon.creativity {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.help-icon.collaboration {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.help-icon.communication {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.help-icon.recommendations {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.help-item h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 16px;
}

.help-item p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.tip-category {
  padding: 20px;
}

.tip-category h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 18px;
}

.tip-category ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
}

.tip-category li {
  margin-bottom: 8px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .student-abilities-view {
    padding: 15px;
  }
  
  .page-header {
    padding: 20px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-text h1 {
    font-size: 24px;
  }
  
  .help-item {
    padding: 15px;
    margin-bottom: 20px;
  }
  
  .tip-category {
    padding: 15px;
  }
}
</style>