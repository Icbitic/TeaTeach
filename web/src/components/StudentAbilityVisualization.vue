<template>
  <div class="student-ability-visualization">
    <!-- Header Section -->
    <div class="visualization-header">
      <h3><i class="el-icon-data-analysis"></i> {{ $t('studentAbility.myLearningAbilities') }}</h3>
      <p class="subtitle">{{ $t('studentAbility.aiAnalysisSubtitle') }}</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- Error State -->
    <el-alert
      v-else-if="error"
      :title="error"
      type="error"
      show-icon
      :closable="false"
      class="error-alert"
    />

    <!-- Main Content -->
    <div v-else-if="abilityData" class="ability-content">
      <!-- Overall Score Card -->
      <el-card class="overall-score-card" shadow="hover">
        <div class="score-header">
          <h4>{{ $t('studentAbility.overallLearningScore') }}</h4>
          <div class="score-circle">
            <el-progress
              type="circle"
              :percentage="overallScore"
              :width="120"
              :stroke-width="8"
              :color="getScoreColor(overallScore)"
            >
              <template #default="{ percentage }">
                <span class="score-text">{{ percentage }}%</span>
              </template>
            </el-progress>
          </div>
        </div>
        <div class="score-description">
          <p>{{ getScoreDescription(overallScore) }}</p>
        </div>
      </el-card>

      <!-- Ability Dimensions -->
      <el-row :gutter="20" class="ability-dimensions">
        <el-col :xs="24" :sm="12" :lg="8" v-for="(ability, key) in abilityData.abilities" :key="key">
          <el-card class="ability-card" shadow="hover">
            <div class="ability-header">
              <div class="ability-icon" :class="getAbilityIconClass(key)">
                <i :class="getAbilityIcon(key)"></i>
              </div>
              <h5>{{ getAbilityTitle(key) }}</h5>
            </div>
            <div class="ability-progress">
              <el-progress
                :percentage="ability.score"
                :stroke-width="6"
                :color="getAbilityColor(ability.level)"
              />
              <div class="ability-level">
                <el-tag :type="getAbilityTagType(ability.level)" size="small">
                  {{ ability.level }}
                </el-tag>
              </div>
            </div>
            <div class="ability-description">
              <p>{{ ability.description }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Interested Fields -->
      <el-card class="interests-card" shadow="hover">
        <div class="card-header">
          <h4><i class="el-icon-star-on"></i> {{ $t('studentAbility.yourLearningInterests') }}</h4>
        </div>
        <div class="interests-content">
          <el-tag
            v-for="field in abilityData.interestedFields"
            :key="field"
            type="primary"
            size="medium"
            class="interest-tag"
          >
            {{ field }}
          </el-tag>
        </div>
      </el-card>

      <!-- Recommendations Section -->
      <el-card class="recommendations-card" shadow="hover">
        <div class="card-header">
          <h4><i class="el-icon-magic-stick"></i> {{ $t('studentAbility.personalizedRecommendations') }}</h4>
          <el-button
            type="primary"
            size="small"
            @click="loadRecommendations"
            :loading="recommendationsLoading"
          >
            {{ $t('studentAbility.getRagRecommendations') }}
          </el-button>
          <el-button
            type="success"
            size="small"
            @click="testRagDirect"
            :loading="recommendationsLoading"
            style="margin-left: 10px"
          >
            {{ $t('studentAbility.testRagDirect') }}
          </el-button>
        </div>
        
        <div v-if="recommendations" class="recommendations-content">
          <el-tabs v-model="activeRecommendationTab" type="card">
            <el-tab-pane :label="$t('studentAbility.allRecommendations')" name="all">
              <div class="resource-list">
                <div
                  v-for="(resource, index) in allUniqueRecommendations"
                  :key="index"
                  class="resource-item"
                >
                  <div class="resource-header">
                    <h6>{{ resource.title }}</h6>
                    <div class="resource-tags">
                      <el-tag size="mini" :type="getResourceDifficultyType(resource.difficulty)">
                        {{ resource.difficulty }}
                      </el-tag>
                      <el-tag size="mini" type="info" style="margin-left: 5px">
                        {{ resource.sourceType }}
                      </el-tag>
                    </div>
                  </div>
                  <p class="resource-description">{{ resource.description }}</p>
                  <div class="resource-meta">
                    <span class="resource-reason">{{ resource.reason }}</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-card>

      <!-- Analysis Timestamp -->
      <div class="analysis-footer">
        <el-text type="info" size="small">
          <i class="el-icon-time"></i>
          {{ $t('studentAbility.analysisGeneratedOn', { date: formatDate(abilityData.analysisDate) }) }}
        </el-text>
      </div>
    </div>
  </div>
</template>

<script>
import studentAbilityService from '../services/studentAbilityService.js'

export default {
  name: 'StudentAbilityVisualization',
  props: {
    studentId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      loading: false,
      error: null,
      abilityData: null,
      recommendations: null,
      recommendationsLoading: false,
      activeRecommendationTab: 'all'
    }
  },
  computed: {
    overallScore() {
      if (!this.abilityData?.abilities) return 0
      const abilities = this.abilityData.abilities
      const scores = Object.values(abilities).map(ability => ability.score)
      return Math.round(scores.reduce((sum, score) => sum + score, 0) / scores.length)
    },
    allUniqueRecommendations() {
      if (!this.recommendations?.recommendations) return []
      
      const allResources = []
      const seenTitles = new Set()
      
      // Combine all recommendations from different types
      Object.entries(this.recommendations.recommendations).forEach(([type, resources]) => {
        if (Array.isArray(resources)) {
          resources.forEach(resource => {
            // Use title as unique identifier to remove duplicates
            const titleKey = resource.title?.toLowerCase().trim()
            if (titleKey && !seenTitles.has(titleKey)) {
              seenTitles.add(titleKey)
              allResources.push({
                ...resource,
                sourceType: this.getResourceTypeLabel(type)
              })
            }
          })
        }
      })
      
      return allResources
    }
  },
  mounted() {
    this.loadAbilityData()
  },
  methods: {
    async loadAbilityData() {
      this.loading = true
      this.error = null
      
      try {
        const response = await studentAbilityService.analyzeStudentAbilities(this.studentId)
        this.abilityData = response.data
      } catch (error) {
        console.error('Error loading ability data:', error)
        this.error = this.$t('studentAbility.failedToLoadAnalysis')
      } finally {
        this.loading = false
      }
    },

    async loadRecommendations() {
      this.recommendationsLoading = true
      
      try {
        const response = await studentAbilityService.getComprehensiveRecommendations(this.studentId)
        console.log('RAG Recommendations Response:', response.data)
        
        // Transform the new RAG response format to match the expected frontend structure
        const ragData = response.data
        const transformedRecommendations = {
          recommendations: {}
        }
        
        // Process each resource type from RAG response
        const resourceTypes = ['course', 'book', 'video', 'practice']
        resourceTypes.forEach(type => {
          const typeKey = type + 'Recommendations'
          if (ragData[typeKey] && ragData[typeKey].resources) {
            transformedRecommendations.recommendations[type + 's'] = ragData[typeKey].resources
          }
        })
        
        // If no specific type recommendations, use overall recommendations
        if (ragData.overallRecommendations && ragData.overallRecommendations.resources) {
          transformedRecommendations.recommendations.overall = ragData.overallRecommendations.resources
        }
        
        this.recommendations = transformedRecommendations
        
      } catch (error) {
        console.error('Error loading recommendations:', error)
        this.$message.error(this.$t('studentAbility.failedToLoadRecommendations'))
      } finally {
        this.recommendationsLoading = false
      }
    },

    async testRagDirect() {
      this.recommendationsLoading = true
      
      try {
        const response = await studentAbilityService.testRagRecommendations(this.studentId, 'all')
        console.log('Direct RAG Test Response:', response.data)
        
        // Display the raw response in a simple format
        const directRecommendations = {
          recommendations: {
            direct: response.data.resources || []
          }
        }
        
        this.recommendations = directRecommendations
        this.$message.success(this.$t('studentAbility.ragTestCompleted'))
        
      } catch (error) {
        console.error('Error testing RAG direct:', error)
        this.$message.error(this.$t('studentAbility.ragTestFailed') + ': ' + error.message)
      } finally {
        this.recommendationsLoading = false
      }
    },

    getScoreColor(score) {
      if (score >= 80) return '#67c23a'
      if (score >= 60) return '#e6a23c'
      return '#f56c6c'
    },

    getScoreDescription(score) {
      if (score >= 80) return this.$t('studentAbility.scoreExcellent')
      if (score >= 60) return this.$t('studentAbility.scoreGood')
      return this.$t('studentAbility.scoreNeedsWork')
    },

    getAbilityTitle(key) {
      const titles = {
        problemSolving: this.$t('abilities.problemSolving'),
        criticalThinking: this.$t('abilities.criticalThinking'),
        creativity: this.$t('abilities.creativity'),
        collaboration: this.$t('abilities.collaboration'),
        communication: this.$t('abilities.communication')
      }
      return titles[key] || key
    },

    getAbilityIcon(key) {
      const icons = {
        problemSolving: 'el-icon-cpu',
        criticalThinking: 'el-icon-view',
        creativity: 'el-icon-brush',
        collaboration: 'el-icon-user-solid',
        communication: 'el-icon-chat-dot-round'
      }
      return icons[key] || 'el-icon-star-on'
    },

    getAbilityIconClass(key) {
      return `ability-icon-${key}`
    },

    getAbilityColor(level) {
      const colors = {
        [this.$t('studentAbility.levelExcellent')]: '#67c23a',
        [this.$t('studentAbility.levelGood')]: '#409eff',
        [this.$t('studentAbility.levelAverage')]: '#e6a23c',
        [this.$t('studentAbility.levelNeedsImprovement')]: '#f56c6c'
      }
      return colors[level] || '#909399'
    },

    getAbilityTagType(level) {
      const types = {
        [this.$t('studentAbility.levelExcellent')]: 'success',
        [this.$t('studentAbility.levelGood')]: 'primary',
        [this.$t('studentAbility.levelAverage')]: 'warning',
        [this.$t('studentAbility.levelNeedsImprovement')]: 'danger'
      }
      return types[level] || 'info'
    },

    getResourceTypeLabel(type) {
      const labels = {
        videos: this.$t('studentAbility.resourceTypes.videos'),
        articles: this.$t('studentAbility.resourceTypes.articles'),
        exercises: this.$t('studentAbility.resourceTypes.exercises'),
        projects: this.$t('studentAbility.resourceTypes.projects'),
        courses: this.$t('studentAbility.resourceTypes.courses'),
        books: this.$t('studentAbility.resourceTypes.books'),
        practices: this.$t('studentAbility.resourceTypes.practices'),
        overall: this.$t('studentAbility.resourceTypes.overall'),
        direct: this.$t('studentAbility.resourceTypes.direct')
      }
      return labels[type] || type.charAt(0).toUpperCase() + type.slice(1)
    },

    getResourceDifficultyType(difficulty) {
      const types = {
        [this.$t('studentAbility.difficultyBeginner')]: 'success',
        [this.$t('studentAbility.difficultyIntermediate')]: 'warning',
        [this.$t('studentAbility.difficultyAdvanced')]: 'danger'
      }
      return types[difficulty] || 'info'
    },

    formatDate(dateString) {
      if (!dateString) return this.$t('studentAbility.unknownDate')
      return new Date(dateString).toLocaleDateString(this.$i18n.locale === 'zh' ? 'zh-CN' : 'en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.student-ability-visualization {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.visualization-header {
  text-align: center;
  margin-bottom: 30px;
}

.visualization-header h3 {
  color: #303133;
  margin-bottom: 8px;
  font-size: 24px;
}

.subtitle {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.loading-container {
  padding: 40px;
}

.error-alert {
  margin-bottom: 20px;
}

.overall-score-card {
  margin-bottom: 30px;
  text-align: center;
}

.score-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.score-header h4 {
  margin: 0;
  color: #303133;
}

.score-text {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.score-description {
  margin-top: 15px;
  color: #606266;
}

.ability-dimensions {
  margin-bottom: 30px;
}

.ability-card {
  height: 100%;
  transition: transform 0.3s ease;
}

.ability-card:hover {
  transform: translateY(-5px);
}

.ability-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.ability-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
}

.ability-icon-problemSolving {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.ability-icon-criticalThinking {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.ability-icon-creativity {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.ability-icon-collaboration {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.ability-icon-communication {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.ability-header h5 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.ability-progress {
  margin-bottom: 15px;
}

.ability-level {
  margin-top: 8px;
  text-align: right;
}

.ability-description {
  color: #606266;
  font-size: 13px;
  line-height: 1.4;
}

.ability-description p {
  margin: 0;
}

.interests-card,
.recommendations-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h4 {
  margin: 0;
  color: #303133;
}

.interests-content {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.interest-tag {
  font-size: 13px;
  padding: 8px 16px;
}

.recommendations-content {
  margin-top: 20px;
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.resource-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.resource-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.resource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.resource-header h6 {
  margin: 0;
  color: #303133;
  font-size: 14px;
}

.resource-tags {
  display: flex;
  align-items: center;
  gap: 5px;
}

.resource-description {
  color: #606266;
  font-size: 13px;
  margin: 8px 0;
  line-height: 1.4;
}

.resource-meta {
  margin-top: 10px;
}

.resource-reason {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

.analysis-footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .student-ability-visualization {
    padding: 15px;
  }
  
  .score-header {
    gap: 15px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>