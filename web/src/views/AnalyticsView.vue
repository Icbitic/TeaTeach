<template>
  <div class="analytics-container">
    <div class="section-header">
      <h2>Analytics & Reports</h2>
      <div class="section-actions">
        <el-button type="primary">
          <el-icon>
            <el-icon-data-analysis/>
          </el-icon>
          Generate Report
        </el-button>
      </div>
    </div>

    <el-card shadow="hover" class="placeholder-card">
      <el-empty description="Analytics Dashboard is under development">
        <template #image>
          <el-icon size="60" color="#409EFF">
            <el-icon-data-analysis/>
          </el-icon>
        </template>
        <template #description>
          <p>This section will include:</p>
          <ul class="feature-list">
            <li>Student performance analytics</li>
            <li>Course completion rates</li>
            <li>Assignment submission trends</li>
            <li>Grade distribution charts</li>
            <li>Attendance tracking</li>
            <li>Custom report generation</li>
            <li>Data export capabilities</li>
          </ul>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AnalyticsView'
}
</script>

<style scoped>
.analytics-container {
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

.placeholder-card {
  border: none;
  border-radius: 8px;
  text-align: center;
  padding: 40px 20px;
}

.feature-list {
  text-align: left;
  display: inline-block;
  margin: 20px 0;
  color: #606266;
}

.feature-list li {
  margin: 8px 0;
  padding-left: 10px;
  position: relative;
}

.feature-list li::before {
  content: '•';
  color: #409EFF;
  font-weight: bold;
  position: absolute;
  left: 0;
}
</style>