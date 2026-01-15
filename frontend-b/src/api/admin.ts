import request from '@/utils/request'

export const adminApi = {
  // 登录
  login(data: { username: string; password: string }) {
    return request.post('/api/v1/admin/auth/login', data)
  },

  // 获取用户信息
  getUserInfo() {
    return request.get('/api/v1/admin/auth/info')
  },

  // 退出登录
  logout() {
    return request.post('/api/v1/admin/auth/logout')
  }
}

