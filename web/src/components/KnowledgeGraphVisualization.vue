<template>
  <div class="knowledge-graph-container">
    <div class="graph-header">
      <h3>Knowledge Graph Visualization</h3>
      <div class="graph-info">
        <el-tag type="info">{{ knowledgePoints.length }} Knowledge Points</el-tag>
        <el-tag type="success">{{ relationshipCount }} Relationships</el-tag>
      </div>
    </div>
    
    <!-- Simple Tree/List View -->
    <div class="graph-content">
      <div class="knowledge-tree">
        <div 
          v-for="kp in organizedKnowledgePoints" 
          :key="kp.id" 
          class="knowledge-node"
          :class="`difficulty-${kp.difficultyLevel?.toLowerCase()}`"
          @click="$emit('node-click', kp)"
        >
          <div class="node-header">
            <h4>{{ kp.name }}</h4>
            <el-tag 
              :type="getDifficultyTagType(kp.difficultyLevel)"
              size="small"
            >
              {{ kp.difficultyLevel || 'Unknown' }}
            </el-tag>
          </div>
          
          <p class="node-description">{{ kp.briefDescription }}</p>
          
          <div class="node-relationships" v-if="kp.prerequisiteKnowledgePointIds?.length || kp.relatedKnowledgePointIds?.length">
            <div v-if="kp.prerequisiteKnowledgePointIds?.length" class="prerequisites">
              <strong>Prerequisites:</strong>
              <el-tag 
                v-for="prereqId in kp.prerequisiteKnowledgePointIds" 
                :key="prereqId"
                size="small"
                type="danger"
                style="margin: 2px"
                @click.stop="selectKnowledgePoint(prereqId)"
              >
                {{ getKnowledgePointName(prereqId) }}
              </el-tag>
            </div>
            
            <div v-if="kp.relatedKnowledgePointIds?.length" class="related">
              <strong>Related:</strong>
              <el-tag 
                v-for="relatedId in kp.relatedKnowledgePointIds" 
                :key="relatedId"
                size="small"
                type="info"
                style="margin: 2px"
                @click.stop="selectKnowledgePoint(relatedId)"
              >
                {{ getKnowledgePointName(relatedId) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Legend -->
    <div class="legend">
      <div class="legend-item">
        <div class="legend-color beginner"></div>
        <span>Beginner</span>
      </div>
      <div class="legend-item">
        <div class="legend-color intermediate"></div>
        <span>Intermediate</span>
      </div>
      <div class="legend-item">
        <div class="legend-color advanced"></div>
        <span>Advanced</span>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'KnowledgeGraphVisualization',
  props: {
    knowledgePoints: {
      type: Array,
      default: () => []
    }
  },
  emits: ['node-click'],
  setup(props, { emit }) {
    // Organize knowledge points by difficulty level
    const organizedKnowledgePoints = computed(() => {
      const sorted = [...props.knowledgePoints].sort((a, b) => {
        const difficultyOrder = { 'BEGINNER': 1, 'INTERMEDIATE': 2, 'ADVANCED': 3 }
        const aOrder = difficultyOrder[a.difficultyLevel] || 0
        const bOrder = difficultyOrder[b.difficultyLevel] || 0
        return aOrder - bOrder
      })
      return sorted
    })

    // Count total relationships
    const relationshipCount = computed(() => {
      let count = 0
      props.knowledgePoints.forEach(kp => {
        count += (kp.prerequisiteKnowledgePointIds?.length || 0)
        count += (kp.relatedKnowledgePointIds?.length || 0)
      })
      return count
    })

    const getDifficultyTagType = (difficulty) => {
      switch (difficulty) {
        case 'BEGINNER': return 'success'
        case 'INTERMEDIATE': return 'warning'
        case 'ADVANCED': return 'danger'
        default: return 'info'
      }
    }

    const getKnowledgePointName = (id) => {
      const kp = props.knowledgePoints.find(point => point.id === id)
      return kp ? kp.name : `Unknown (${id})`
    }

    const selectKnowledgePoint = (id) => {
      const kp = props.knowledgePoints.find(point => point.id === id)
      if (kp) {
        emit('node-click', kp)
      }
    }

    return {
      organizedKnowledgePoints,
      relationshipCount,
      getDifficultyTagType,
      getKnowledgePointName,
      selectKnowledgePoint
    }
  }
}
</script>

<style scoped>
.knowledge-graph-container {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  background: #f9f9f9;
}

.graph-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.graph-header h3 {
  margin: 0;
  color: #333;
}

.graph-info {
  display: flex;
  gap: 8px;
}

.graph-content {
  max-height: 600px;
  overflow-y: auto;
  background: #fff;
}

.knowledge-tree {
  padding: 16px;
}

.knowledge-node {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.knowledge-node:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.knowledge-node.difficulty-beginner {
  border-left: 4px solid #4CAF50;
}

.knowledge-node.difficulty-intermediate {
  border-left: 4px solid #FF9800;
}

.knowledge-node.difficulty-advanced {
  border-left: 4px solid #F44336;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.node-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.node-description {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

.node-relationships {
  margin-top: 12px;
}

.prerequisites, .related {
  margin-bottom: 8px;
}

.prerequisites strong, .related strong {
  display: inline-block;
  margin-right: 8px;
  font-size: 12px;
  color: #333;
}

.legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 12px;
  background: #fff;
  border-top: 1px solid #eee;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-color.beginner {
  background-color: #4CAF50;
}

.legend-color.intermediate {
  background-color: #FF9800;
}

.legend-color.advanced {
  background-color: #F44336;
}
</style>