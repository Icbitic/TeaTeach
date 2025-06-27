<template>
  <el-dropdown @command="changeLanguage" trigger="click">
    <el-button type="text" class="language-switcher">
      <el-icon><el-icon-translate /></el-icon>
      {{ currentLanguageLabel }}
      <el-icon class="el-icon--right"><el-icon-arrow-down /></el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="en" :disabled="currentLocale === 'en'">
          {{ $t('language.english') }}
        </el-dropdown-item>
        <el-dropdown-item command="zh" :disabled="currentLocale === 'zh'">
          {{ $t('language.chinese') }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'LanguageSwitcher',
  setup() {
    const { locale } = useI18n()
    
    const currentLocale = computed(() => locale.value)
    
    const currentLanguageLabel = computed(() => {
      return locale.value === 'zh' ? '中文' : 'English'
    })
    
    const changeLanguage = (lang) => {
      locale.value = lang
      localStorage.setItem('language', lang)
    }
    
    return {
      currentLocale,
      currentLanguageLabel,
      changeLanguage
    }
  }
}
</script>

<style scoped>
.language-switcher {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
}

.language-switcher:hover {
  color: #409eff;
}
</style>