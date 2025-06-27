<template>
  <div class="knowledge-graph-container">
    <div class="graph-header">
      <h3>Knowledge Graph Visualization</h3>
      <div class="graph-info">
        <el-tag type="info">{{ knowledgePoints.length }} Knowledge Points</el-tag>
        <el-tag type="success">{{ relationshipCount }} Relationships</el-tag>
        <el-tag v-if="isLargeGraph" type="warning">Performance Mode</el-tag>
        <el-tag v-if="isVeryLargeGraph" type="danger">High Performance Mode</el-tag>
      </div>
      <div class="graph-controls">
        <el-button size="small" @click="resetView">Reset View</el-button>
        <el-button size="small" @click="fitToScreen">Fit to Screen</el-button>
        <el-button 
          v-if="!isVeryLargeGraph" 
          size="small" 
          :type="physicsEnabled ? 'primary' : 'default'"
          @click="togglePhysics"
        >
          {{ physicsEnabled ? 'Disable' : 'Enable' }} Physics
        </el-button>
      </div>
    </div>
    
    <!-- Performance Notice -->
    <div v-if="isLargeGraph" class="performance-notice">
      <el-alert
        :title="isVeryLargeGraph ? 'High Performance Mode Active' : 'Performance Mode Active'"
        :description="isVeryLargeGraph ? 
          'Physics simulation disabled for optimal performance with large graphs. Drag nodes freely without lag.' :
          'Reduced physics simulation for better performance. Physics will pause during dragging.'"
        :type="isVeryLargeGraph ? 'warning' : 'info'"
        :closable="false"
        show-icon
      />
    </div>
    
    <!-- Network Graph -->
    <div class="graph-content">
      <div ref="networkContainer" class="network-container"></div>
    </div>
    
    <!-- Legend -->
    <div class="legend">
      <div class="legend-section">
        <h4>Difficulty Levels</h4>
        <div class="legend-items">
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
      <div class="legend-section">
        <h4>Connections</h4>
        <div class="legend-items">
          <div class="legend-item">
            <div class="legend-line prerequisite"></div>
            <span>Prerequisites (→)</span>
          </div>
          <div class="legend-item">
            <div class="legend-line related"></div>
            <span>Related (⇢)</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Network } from 'vis-network/standalone/esm/vis-network'

export default {
  name: 'KnowledgeGraphVisualization',
  props: {
    knowledgePoints: {
      type: Array,
      default: () => []
    },
    selectedKnowledgePointIds: {
      type: Array,
      default: () => []
    },
    disableDragging: {
      type: Boolean,
      default: false
    },
    allowDraggingWithPhysicsTimeout: {
      type: Boolean,
      default: false
    }
  },
  emits: ['node-click'],
  setup(props, { emit }) {
    const networkContainer = ref(null)
    let network = null

    // Count total relationships
    const relationshipCount = computed(() => {
      let count = 0
      props.knowledgePoints.forEach(kp => {
        count += (kp.prerequisiteKnowledgePointIds?.length || 0)
        count += (kp.relatedKnowledgePointIds?.length || 0)
      })
      return count
    })

    // Get difficulty color
    const getDifficultyColor = (difficulty) => {
      switch (difficulty) {
        case 'BEGINNER': return '#4CAF50'
        case 'INTERMEDIATE': return '#FF9800'
        case 'ADVANCED': return '#F44336'
        default: return '#9E9E9E'
      }
    }

    // Get difficulty border color
    const getDifficultyBorderColor = (difficulty) => {
      switch (difficulty) {
        case 'BEGINNER': return '#388E3C'
        case 'INTERMEDIATE': return '#F57C00'
        case 'ADVANCED': return '#D32F2F'
        default: return '#757575'
      }
    }

    // Create nodes data with performance optimizations
    const createNodes = () => {
      return props.knowledgePoints.map(kp => {
        const isSelected = props.selectedKnowledgePointIds.includes(kp.id)
        
        // Base node configuration
        const nodeConfig = {
          id: kp.id,
          label: kp.name,
          title: isVeryLargeGraph.value ? kp.name : 
            `${kp.name}\n\nDifficulty: ${kp.difficultyLevel}\nDescription: ${kp.briefDescription || 'No description'}${isSelected ? '\n\n✓ SELECTED' : ''}`,
          color: {
            background: isSelected ? '#007bff' : getDifficultyColor(kp.difficultyLevel),
            border: isSelected ? '#0056b3' : getDifficultyBorderColor(kp.difficultyLevel)
          },
          font: {
            color: '#333333',
            size: isSelected ? 16 : 14,
            face: 'Arial',
            strokeWidth: isSelected ? 3 : 2,
            strokeColor: '#ffffff'
          },
          shape: 'dot',
          size: isSelected ? 25 : 20,
          borderWidth: isSelected ? 3 : 2,
          data: kp
        }
        
        // Add performance-intensive features only for smaller graphs
         if (!isLargeGraph.value) {
           nodeConfig.color.highlight = {
             background: isSelected ? '#0056b3' : getDifficultyColor(kp.difficultyLevel),
             border: isSelected ? '#004085' : getDifficultyBorderColor(kp.difficultyLevel)
           }
           // Font stroke is already set above for all graph sizes
         }
        
        return nodeConfig
      })
    }

    // Create edges data
    const createEdges = () => {
      const edges = []
      
      props.knowledgePoints.forEach(kp => {
        // Prerequisites (arrows pointing to current node)
        if (kp.prerequisiteKnowledgePointIds?.length) {
          kp.prerequisiteKnowledgePointIds.forEach(prereqId => {
            if (props.knowledgePoints.find(p => p.id === prereqId)) {
              edges.push({
                from: prereqId,
                to: kp.id,
                arrows: 'to',
                color: {
                  color: '#F44336',
                  highlight: '#D32F2F'
                },
                width: 2,
                dashes: false
              })
            }
          })
        }
        
        // Related points (undirected lines)
        if (kp.relatedKnowledgePointIds?.length) {
          kp.relatedKnowledgePointIds.forEach(relatedId => {
            if (props.knowledgePoints.find(p => p.id === relatedId) && 
                !edges.find(e => 
                  (e.from === kp.id && e.to === relatedId) || 
                  (e.from === relatedId && e.to === kp.id)
                )) {
              edges.push({
                from: kp.id,
                to: relatedId,
                color: {
                  color: '#2196F3',
                  highlight: '#1976D2'
                },
                width: 2,
                dashes: [5, 5]
              })
            }
          })
        }
      })
      
      return edges
    }

   // Performance optimization variables
    const isLargeGraph = computed(() => props.knowledgePoints.length > 50)
    const isVeryLargeGraph = computed(() => props.knowledgePoints.length > 200)
    let isDragging = false
    let dragTimeout = null
    let physicsTimeout = null
    const isStabilized = ref(false)
    
    // Initialize network
    const initNetwork = async () => {
      if (!networkContainer.value || props.knowledgePoints.length === 0) return
      
      await nextTick()
      
      const nodes = createNodes()
      const edges = createEdges()
      
      const data = { nodes, edges }
      
      // Adaptive options based on graph size
      const getPhysicsOptions = () => {
        if (isVeryLargeGraph.value) {
          // Minimal physics for very large graphs
          return {
            enabled: false
          }
        } else if (isLargeGraph.value) {
          // Reduced physics for large graphs
          return {
            enabled: true,
            stabilization: {
              enabled: true,
              iterations: 50,
              updateInterval: 50
            },
            barnesHut: {
              gravitationalConstant: -1000,
              centralGravity: 0.1,
              springLength: 100,
              springConstant: 0.02,
              damping: 0.2,
              avoidOverlap: 0.05
            },
            adaptiveTimestep: true,
            maxVelocity: 20,
            minVelocity: 0.5,
            solver: 'barnesHut',
            timestep: 0.3
          }
        } else {
          // Full physics for small graphs
          return {
            enabled: true,
            stabilization: {
              enabled: true,
              iterations: 100,
              updateInterval: 25
            },
            barnesHut: {
              gravitationalConstant: -2000,
              centralGravity: 0.3,
              springLength: 120,
              springConstant: 0.04,
              damping: 0.09,
              avoidOverlap: 0.1
            },
            adaptiveTimestep: true,
            maxVelocity: 30,
            minVelocity: 0.1,
            solver: 'barnesHut',
            timestep: 0.5
          }
        }
      }
      
      const options = {
          nodes: {
            borderWidth: 2,
            shadow: false,
            font: {
              color: '#333333',
              size: 14,
              face: 'Arial',
              strokeWidth: isLargeGraph.value ? 0 : 2, // Reduce stroke for performance
              strokeColor: '#ffffff'
            }
          },
          edges: {
            shadow: false,
            smooth: {
              type: isLargeGraph.value ? 'discrete' : 'continuous', // Use discrete for better performance
              roundness: 0.5
            },
            arrows: {
              to: {
                enabled: true,
                scaleFactor: 1.2
              }
            }
          },
          physics: getPhysicsOptions(),
          interaction: {
            hover: !isVeryLargeGraph.value, // Disable hover for very large graphs
            tooltipDelay: isLargeGraph.value ? 500 : 200,
            hideEdgesOnDrag: isLargeGraph.value, // Hide edges during drag for performance
            hideNodesOnDrag: isVeryLargeGraph.value, // Hide nodes during drag for very large graphs
            dragNodes: props.allowDraggingWithPhysicsTimeout || !props.disableDragging,
            dragView: true, // Always allow camera dragging
            zoomView: true,
            selectConnectedEdges: false
          },
          layout: {
            improvedLayout: !isVeryLargeGraph.value // Disable for very large graphs
          }
        }
      
      // Destroy existing network
      if (network) {
        network.destroy()
      }
      
      // Create new network
      network = new Network(networkContainer.value, data, options)
      
      // Auto-fit and center the view after stabilization
      network.once('stabilizationIterationsDone', () => {
        isStabilized.value = true
        
        network.fit({
          animation: {
            duration: 1000,
            easingFunction: 'easeInOutQuad'
          }
        })
      })
      
      // Reset stabilization state when physics starts again
      network.on('startStabilizing', () => {
        isStabilized.value = false
      })
      
      // Add event listeners with performance optimizations
      network.on('click', (params) => {
        if (params.nodes.length > 0) {
          const nodeId = params.nodes[0]
          const kp = props.knowledgePoints.find(point => point.id === nodeId)
          if (kp) {
            emit('node-click', kp)
          }
        }
      })
      
      // Only add hover events for smaller graphs
      if (!isVeryLargeGraph.value) {
        network.on('hoverNode', () => {
          networkContainer.value.style.cursor = 'pointer'
        })
        
        network.on('blurNode', () => {
          networkContainer.value.style.cursor = 'default'
        })
      }
      
      // Drag event handlers
      if (props.allowDraggingWithPhysicsTimeout || !props.disableDragging) {
        network.on('dragStart', () => {
          isDragging = true
          
          // For allowDraggingWithPhysicsTimeout: disable physics after 1 second
          if (props.allowDraggingWithPhysicsTimeout) {
            if (physicsTimeout) clearTimeout(physicsTimeout)
            physicsTimeout = setTimeout(() => {
              if (network && isDragging) {
                network.setOptions({ physics: { enabled: false } })
              }
            }, 1000) // Disable physics after 1 second of dragging
          }
          // For regular large graphs: disable physics immediately during drag
          else if (isLargeGraph.value && !isVeryLargeGraph.value) {
            network.setOptions({ physics: { enabled: false } })
          }
        })
        
        network.on('dragEnd', () => {
          isDragging = false
          
          // Clear physics timeout if it exists
          if (physicsTimeout) {
            clearTimeout(physicsTimeout)
            physicsTimeout = null
          }
          
          // Re-enable physics after drag with debouncing
          if (props.allowDraggingWithPhysicsTimeout || (isLargeGraph.value && !isVeryLargeGraph.value)) {
            if (dragTimeout) clearTimeout(dragTimeout)
            dragTimeout = setTimeout(() => {
              if (network && !isDragging) {
                network.setOptions({ physics: getPhysicsOptions() })
              }
            }, 500) // Wait 500ms after drag ends
          }
        })
      }
      
      // Throttle stabilization progress for large graphs
       if (isLargeGraph.value) {
         let lastProgressUpdate = 0
         network.on('stabilizationProgress', () => {
           const now = Date.now()
           if (now - lastProgressUpdate > 100) { // Throttle to every 100ms
             lastProgressUpdate = now
             // Optional: emit progress event if needed
           }
         })
       }
    }

    // Control functions
    const resetView = () => {
      if (network) {
        network.fit()
      }
    }

    const fitToScreen = () => {
      if (network) {
        network.fit({ animation: true })
      }
    }

    // Debounced network update for performance
    let updateTimeout = null
    const debouncedNetworkUpdate = () => {
      if (updateTimeout) clearTimeout(updateTimeout)
      updateTimeout = setTimeout(() => {
        initNetwork()
      }, 100) // Debounce network updates
    }
    
    // Optimized update function for selection changes only
    const updateNodeSelection = () => {
      if (!network) return
      
      // Update node colors without redrawing the entire canvas
      const updatedNodes = props.knowledgePoints.map(kp => {
        const isSelected = props.selectedKnowledgePointIds.includes(kp.id)
        return {
          id: kp.id,
          color: {
            background: isSelected ? '#007bff' : getDifficultyColor(kp.difficultyLevel),
            border: isSelected ? '#0056b3' : getDifficultyBorderColor(kp.difficultyLevel)
          },
          font: {
            color: '#333333',
            size: isSelected ? 16 : 14
          },
          size: isSelected ? 25 : 20,
          borderWidth: isSelected ? 3 : 2
        }
      })
      
      // Use update instead of setData to avoid canvas redraw
      network.body.data.nodes.update(updatedNodes)
    }
    
    // Watch for changes with optimized updates
    watch(() => props.knowledgePoints, () => {
      debouncedNetworkUpdate()
    }, { deep: true })
    
    // Separate watcher for selection changes (more efficient)
    watch(() => props.selectedKnowledgePointIds, () => {
      // For selection changes, just update nodes instead of recreating network
      if (network && props.knowledgePoints.length > 0) {
        updateNodeSelection()
      }
    }, { deep: true })

    onMounted(() => {
      initNetwork()
    })

    // Performance toggle for manual control
    const physicsEnabled = ref(true)
    
    const togglePhysics = () => {
      physicsEnabled.value = !physicsEnabled.value
      if (network) {
        network.setOptions({ 
          physics: { 
            enabled: physicsEnabled.value && !isVeryLargeGraph.value 
          } 
        })
      }
    }
    
    onUnmounted(() => {
      // Clean up timeouts
      if (updateTimeout) clearTimeout(updateTimeout)
      if (dragTimeout) clearTimeout(dragTimeout)
      if (physicsTimeout) clearTimeout(physicsTimeout)
      
      if (network) {
        network.destroy()
      }
    })

    return {
      networkContainer,
      relationshipCount,
      resetView,
      fitToScreen,
      togglePhysics,
      physicsEnabled,
      isLargeGraph,
      isVeryLargeGraph
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
  flex-wrap: wrap;
  gap: 10px;
}

.graph-header h3 {
  margin: 0;
  color: #333;
}

.graph-info {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.graph-controls {
  display: flex;
  gap: 8px;
}

.performance-notice {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.graph-content {
  height: 600px;
  background: #fff;
  position: relative;
}

.network-container {
  width: 100%;
  height: 100%;
  background: #fafafa;
  /* Optimize rendering for large graphs */
  will-change: transform;
  transform: translateZ(0);
}

.legend {
  display: flex;
  justify-content: space-around;
  padding: 16px;
  background: #fff;
  border-top: 1px solid #eee;
  flex-wrap: wrap;
  gap: 20px;
}

.legend-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.legend-section h4 {
  margin: 0;
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.legend-items {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
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

.legend-line {
  width: 30px;
  height: 3px;
  border-radius: 2px;
}

.legend-line.prerequisite {
  background-color: #F44336;
  position: relative;
}

.legend-line.prerequisite::after {
  content: '';
  position: absolute;
  right: -3px;
  top: -2px;
  width: 0;
  height: 0;
  border-left: 6px solid #F44336;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
}

.legend-line.related {
  background-color: #2196F3;
  background-image: repeating-linear-gradient(
    90deg,
    #2196F3,
    #2196F3 3px,
    transparent 3px,
    transparent 6px
  );
  position: relative;
}

.legend-line.related::after {
  content: '';
  position: absolute;
  right: -3px;
  top: -2px;
  width: 0;
  height: 0;
  border-left: 6px solid #2196F3;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
}

/* Responsive design */
@media (max-width: 768px) {
  .graph-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .graph-content {
    height: 400px;
  }
  
  .legend {
    flex-direction: column;
    align-items: center;
  }
  
  .legend-items {
    justify-content: center;
  }
}
</style>