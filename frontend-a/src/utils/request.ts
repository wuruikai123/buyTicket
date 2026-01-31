import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api/v1', // 基础路径，需要配置 vite 代理
    timeout: 5000 // 超时时间
})

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        // 添加 token 到请求头
        const token = localStorage.getItem('token')
        if (token) {
            // JWT拦截器期望的格式是 "Bearer <token>"
            config.headers.Authorization = `Bearer ${token}`
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
            // code -2 表示未登录或 Token 过期 (由 JwtInterceptor 返回)
            console.error('Auth Error:', res.msg)
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            return Promise.reject(new Error(res.msg || 'Token无效或已过期'))
        } else {
            // 可以在这里统一处理错误提示，例如 ElMessage.error(res.msg)
            console.error('API Error:', res.msg)
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    (error) => {
        console.error('Request Error:', error)
        // 处理 HTTP 状态码错误
        if (error.response) {
            if (error.response.status === 401) {
                // 401 未授权 - 尝试解析响应体
                const data = error.response.data
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                if (data && data.code === -2) {
                    // JWT拦截器返回的401响应
                    return Promise.reject(new Error(data.msg || 'Token无效或已过期'))
                }
                return Promise.reject(new Error('未授权，请重新登录'))
            }
        }
        return Promise.reject(error)
    }
)

export default request
