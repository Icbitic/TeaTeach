<template>
  <div class="student-abilities-view">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1><TypewriterText :text="$t('abilities.myLearningAbilities')" :show="true" :speed="70" /></h1>
          <p class="page-description">
            {{ $t('abilities.pageDescription') }}
          </p>
        </div>
        <div class="header-actions">
          <el-button
            type="primary"
            @click="refreshAnalysis"
            :loading="refreshing"
            icon="el-icon-refresh"
          >
            {{ $t('abilities.refreshAnalysis') }}
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
        <h3><i class="el-icon-question"></i> {{ $t('abilities.understandingAnalysis') }}</h3>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon problem-solving">
              <i class="el-icon-cpu"></i>
            </div>
            <h4>{{ $t('abilities.problemSolving') }}</h4>
            <p>{{ $t('abilities.problemSolvingDesc') }}</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon critical-thinking">
              <i class="el-icon-view"></i>
            </div>
            <h4>{{ $t('abilities.criticalThinking') }}</h4>
            <p>{{ $t('abilities.criticalThinkingDesc') }}</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon creativity">
              <i class="el-icon-brush"></i>
            </div>
            <h4>{{ $t('abilities.creativity') }}</h4>
            <p>{{ $t('abilities.creativityDesc') }}</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon collaboration">
              <i class="el-icon-user-solid"></i>
            </div>
            <h4>{{ $t('abilities.collaboration') }}</h4>
            <p>{{ $t('abilities.collaborationDesc') }}</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon communication">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <h4>{{ $t('abilities.communication') }}</h4>
            <p>{{ $t('abilities.communicationDesc') }}</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="help-item">
            <div class="help-icon recommendations">
              <i class="el-icon-magic-stick"></i>
            </div>
            <h4>{{ $t('abilities.recommendations') }}</h4>
            <p>{{ $t('abilities.recommendationsDesc') }}</p>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- Tips Section -->
    <el-card class="tips-section" shadow="hover">
      <div class="tips-header">
        <h3><i class="el-icon-lightbulb"></i> {{ $t('abilities.tipsForImprovement') }}</h3>
      </div>
      <el-row :gutter="20">
        <el-col :xs="24" :md="12">
          <div class="tip-category">
            <h4>{{ $t('abilities.enhancingStrengths') }}</h4>
            <ul>
              <li>{{ $t('abilities.tip1') }}</li>
              <li>{{ $t('abilities.tip2') }}</li>
              <li>{{ $t('abilities.tip3') }}</li>
              <li>{{ $t('abilities.tip4') }}</li>
            </ul>
          </div>
        </el-col>
        <el-col :xs="24" :md="12">
          <div class="tip-category">
            <h4>{{ $t('abilities.improvingWeakAreas') }}</h4>
            <ul>
              <li>{{ $t('abilities.tip5') }}</li>
              <li>{{ $t('abilities.tip6') }}</li>
              <li>{{ $t('abilities.tip7') }}</li>
              <li>{{ $t('abilities.tip8') }}</li>
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
        this.$message.success(this.$t('studentAbility.analysisRefreshedSuccessfully'))
      } catch (error) {
        console.error(this.$t('errors.errorRefreshingAnalysis'), error)
        this.$message.error(this.$t('studentAbility.failedToRefreshAnalysis'))
      } finally {
        this.refreshing = false
      }
    }
  }
}
</script>

<style scoped>
.student-abilities-view {
  padding: 32px 0 0 0;
  min-height: 100vh;
  background: linear-gradient(135deg, #e0e7ff 0%, #f3e8ff 100%);
}

.page-header {
  background: rgba(255,255,255,0.98);
  border-radius: 18px;
  padding: 36px 32px;
  margin-bottom: 36px;
  box-shadow: 0 8px 32px rgba(76, 110, 245, 0.10);
  transition: box-shadow 0.2s, transform 0.2s;
}
.page-header:hover {
  box-shadow: 0 16px 48px rgba(76, 110, 245, 0.16);
  transform: translateY(-2px) scale(1.01);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.header-text h1 {
  margin: 0 0 10px 0;
  background: linear-gradient(90deg, #4c6ef5 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 1px;
}

.page-description {
  color: #606266;
  font-size: 17px;
  margin: 0;
  line-height: 1.6;
  opacity: 0.88;
}

.header-actions {
  flex-shrink: 0;
}

.abilities-content {
  background: rgba(255,255,255,0.98);
  border-radius: 18px;
  margin-bottom: 36px;
  box-shadow: 0 8px 32px rgba(76, 110, 245, 0.10);
  transition: box-shadow 0.2s, transform 0.2s;
}
.abilities-content:hover {
  box-shadow: 0 16px 48px rgba(76, 110, 245, 0.16);
  transform: translateY(-2px) scale(1.01);
}

.help-section,
.tips-section {
  margin-bottom: 36px;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(76, 110, 245, 0.10);
  background: rgba(255,255,255,0.98);
  transition: box-shadow 0.2s;
}
.help-section:hover,
.tips-section:hover {
  box-shadow: 0 10px 32px rgba(76, 110, 245, 0.16);
}

.help-header,
.tips-header {
  margin-bottom: 25px;
  text-align: center;
}

.help-header h3,
.tips-header h3 {
  margin: 0;
  background: linear-gradient(90deg, #4c6ef5 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 22px;
  font-weight: 700;
}

.help-item {
  text-align: center;
  padding: 24px 12px;
  height: 100%;
  border-radius: 12px;
  transition: box-shadow 0.2s, transform 0.2s;
}
.help-item:hover {
  box-shadow: 0 4px 16px rgba(76, 110, 245, 0.10);
  transform: translateY(-2px) scale(1.01);
}

.help-icon {
  width: 62px;
  height: 62px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  font-size: 28px;
  color: white;
  box-shadow: 0 2px 8px rgba(76, 110, 245, 0.10);
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
  color: #4c6ef5;
  font-size: 17px;
  font-weight: 700;
}

.help-item p {
  margin: 0;
  color: #606266;
  font-size: 15px;
  line-height: 1.6;
}

.tip-category {
  padding: 24px 12px;
  border-radius: 12px;
  background: #f8f9fa;
  box-shadow: 0 1px 6px rgba(76, 110, 245, 0.07);
  margin-bottom: 10px;
}

.tip-category h4 {
  margin: 0 0 15px 0;
  color: #764ba2;
  font-size: 19px;
  font-weight: 700;
}

.tip-category ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
}

.tip-category li {
  margin-bottom: 8px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .student-abilities-view {
    padding: 15px;
  }
  .page-header {
    padding: 18px 8px;
  }
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .header-text h1 {
    font-size: 22px;
  }
  .help-item {
    padding: 12px 4px;
    margin-bottom: 16px;
  }
  .tip-category {
    padding: 12px 4px;
  }
}
</style>