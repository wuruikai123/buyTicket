import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '',  // 不设置 baseURL，因为 API 路径已经是完整的
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = authStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, msg, data } = response.data
    
    if (code === 0 || code === 200) {
      return data
    } else if (code === -2) {
      ElMessage.error('登录已过期，请重新登录')
      const authStore = useAuthStore()
      authStore.logout()
      router.push('/login')
      return Promise.reject(new Error(msg))
    } else {
      ElMessage.error(msg || '请求失败')
      return Promise.reject(new Error(msg))
    }
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request

