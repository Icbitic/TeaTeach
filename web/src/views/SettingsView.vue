<template>
  <div class="settings-container">
    <div class="section-header">
      <h2>Settings</h2>
    </div>

    <el-card shadow="hover" class="placeholder-card">
      <el-empty description="Settings panel is under development">
        <template #image>
          <el-icon size="60" color="#409EFF">
            <el-icon-setting/>
          </el-icon>
        </template>
        <template #description>
          <p>This section will include:</p>
          <ul class="feature-list">
            <li>User profile management</li>
            <li>Account preferences</li>
            <li>Notification settings</li>
            <li>System configuration</li>
            <li>Security settings</li>
            <li>Theme customization</li>
            <li>Data backup and restore</li>
            <li>Integration settings</li>
          </ul>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'SettingsView'
}
</script>

<style scoped>
.settings-container {
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