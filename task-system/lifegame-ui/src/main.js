import { createApp } from 'vue'
//import './style.css'
import App from './App.vue'

// 1. 引入 Element Plus 及其样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 2. 引入路由
import router from './router'
const app = createApp(App)

// 3. 使用插件
app.use(ElementPlus)
app.use(router)

app.mount('#app')
