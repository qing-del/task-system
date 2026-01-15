import axios from 'axios'
import { useRouter } from 'vue-router'
import {ElMessage} from 'element-plus'

const router = useRouter()

// 1. 创建 Axios 实例
const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

// 2. 请求拦截器：自动戴上”工牌”（Token）
request.interceptors.request.use(config => {
    // 从 LocalStorage 获取 Token
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
}, error => {
    return Promise.reject(error)
})

// 3. 响应拦截器：统一处理报错
request.interceptors.response.use(
    response => {
        return response.data  // 只返回后端的部分数据，丢掉 axios 的壳
    },
    error => {
        // 登录或则注册的报错给登录界面自己处理
        if(error.config.url.includes('/login') || error.config.url.includes('/register')) {
            return Promise.reject(error)
        }

        // 如果后端返回 403/401， 说明 Token 过期或者错误
        if(error.response && (error.response.status === 403 || error.response.status === 401)) {
            ElMessage.error('登录已过期，请重新登录！')
            localStorage.removeItem('token')
            // 这里可以加入跳转登录页的逻辑
            router.push('/')
        } else {
            ElMessage.error(error.message || '网络异常')
        }
        return Promise.reject(error)
    }
)

export default request