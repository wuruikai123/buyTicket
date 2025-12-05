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
        // 假设后端返回格式 { code: 0, data: ..., msg: ... }
        // code 0 或 200 表示成功
        if (res.code === 0 || res.code === 200) {
            return res.data
        } else if (res.code === -2) {
            // code -2 表示未登录或 Token 过期 (由 LoginInterceptor 返回)
            console.error('Auth Error:', res.msg)
            localStorage.removeItem('token')
            // 强制跳转登录页
            window.location.href = '/login'
            return Promise.reject(new Error(res.msg || '请先登录'))
        } else {
            // 可以在这里统一处理错误提示，例如 ElMessage.error(res.msg)
            console.error('API Error:', res.msg)
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    (error) => {
        console.error('Request Error:', error)
        // 处理 HTTP 状态码错误
        if (error.response && error.response.status === 401) {
             localStorage.removeItem('token')
             window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default request
