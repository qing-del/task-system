<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>🚀 LifeGame 启动</h2>
        </div>
      </template>

      <el-form :model="form" label-width="0px">
        <el-form-item>
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-full" @click="handleLogin" :loading="loading" size="large">登 录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button class="btn-full" @click="handleRegister" size="large">注 册 新 账 号</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request' // 引入我们刚才写的请求工具
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

// 响应式表单数据
const form = reactive({
  username: '',
  password: ''
})

// 进入页面时做一点检测
onMounted(() => {
  const token = localStorage.getItem('token')
  
  // 如果有 Token 并且 Token 有效 就跳转到游戏页面（后续开发）
  // if(token) {
  //   router.push('/game')
  // } 

  // 清除无效 Token
  localStorage.removeItem('token')
  
})

// 登录逻辑
const handleLogin = async () => {
  if(!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    // 发送请求给后端 /auth/login
    const res = await request.post('/auth/login', form)

    // 如果 Axios 没报错，说明成功了 (拦截器已经过滤了 data)
    // 1. 存 Token
    localStorage.setItem('token', res.token)
    ElMessage.success('登录成功，正在进入系统...')

    // 2. 跳转到游戏主页
    router.push('/game')

  } catch (error) {
    if (error.response && (error.response.status === 403 || error.response.status === 401)) {
      ElMessage.error('登陆失败！请检查账号和密码')
    } else {
      console.error(error) // 拦截器已弹出错误提示
      ElMessage.error(error.response.data || '无法连接到服务器')
    }
  } finally {
    loading.value = false
  }
}

// 注册逻辑 (简单复用登录接口逻辑，你需要确保后端注册接口路径正确)
const handleRegister = async () => {
  if(!form.username || !form.password) return ElMessage.warning('请输入账号密码')
  try {
    const res = await request.post('/auth/register', form)
    // 后端返回 String "注册成功" 或其他
    ElMessage.success('注册成功，请点击登录')
  } catch (e) {
    // 错误处理
    ElMessage.error(e.response.data || '注册失败，请稍后重试')
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center;  /* 垂直居中 */
  background: linear-gradient(135deg, #1f1c2c, #928dab);
}
.login-card {
  width: 400px;
  border-radius: 10px;
}
.card-header {
  text-align: center;
}
.btn-full {
  width: 100%;
}
</style>