<template>
  <div class="settings-container">
    <div class="section-header">
      <h2>{{ $t('settings.title') }}</h2>
    </div>

    <el-card shadow="hover" class="placeholder-card">
      <el-empty :description="$t('settings.underDevelopment')">
        <template #image>
          <el-icon size="60" color="#409EFF">
            <el-icon-setting/>
          </el-icon>
        </template>
        <template #description>
          <p>{{ $t('settings.willInclude') }}:</p>
          <ul class="feature-list">
            <li>{{ $t('settings.userProfileManagement') }}</li>
            <li>{{ $t('settings.accountPreferences') }}</li>
            <li>{{ $t('settings.notificationSettings') }}</li>
            <li>{{ $t('settings.systemConfiguration') }}</li>
            <li>{{ $t('settings.securitySettings') }}</li>
            <li>{{ $t('settings.themeCustomization') }}</li>
            <li>{{ $t('settings.dataBackupRestore') }}</li>
            <li>{{ $t('settings.integrationSettings') }}</li>
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