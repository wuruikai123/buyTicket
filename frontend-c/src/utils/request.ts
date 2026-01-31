import axios from 'axios'

const request = axios.create({
  baseURL: '/api/v1',  // 修改为 /api/v1，不要包含 /admin
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('seller_token')
    if (token) {
      // 直接使用token，不添加Bearer前缀
      // 后端拦截器直接使用token解析JWT
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
    const { code, msg, data } = response.data
    
    if (code === 0 || code === 200) {
      return data
    } else {
      const error = new Error(msg || '请求失败')
      return Promise.reject(error)
    }
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

export default request
