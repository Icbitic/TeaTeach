import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import i18n from './i18n'

// Import Element Plus Icons
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// Register all icons globally
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(`ElIcon${key}`, component)
}

app.use(store)
app.use(router)
app.use(ElementPlus)
app.use(i18n)

app.mount('#app')