import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api/v1', // 基础路径，需要配置 vite 代理
    timeout: 5000 // 超时时间
})

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        // 如果有 token，可以在这里添加
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = token
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
        const res = response.data
        // 假设后端返回格式 { code: 200, data: ..., msg: ... }
        if (res.code === 0 || res.code === 200) {
            return res.data
        } else {
            // 可以在这里统一处理错误提示，例如 ElMessage.error(res.msg)
            console.error('API Error:', res.msg)
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    (error) => {
        console.error('Request Error:', error)
        return Promise.reject(error)
    }
)

export default request
