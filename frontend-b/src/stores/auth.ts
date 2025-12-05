import { defineStore } from 'pinia'
import { ref } from 'vue'
import { storage } from '@/utils/storage'
import { adminApi } from '@/api/admin'

interface UserInfo {
  id: number
  username: string
  realName: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(storage.get('admin_token') || '')
  const userInfo = ref<UserInfo | null>(storage.get('admin_userInfo') || null)

  const login = async (username: string, password: string) => {
    try {
      const data: any = await adminApi.login({ username, password })
      token.value = data.token
      // 适配后端返回结构
      const adminData = data.admin || data.userInfo
      if (adminData) {
          userInfo.value = {
              ...adminData,
              role: adminData.role || '超级管理员' // 后端未返回 role，给个默认值
          }
          storage.set('admin_userInfo', userInfo.value)
      }
      storage.set('admin_token', data.token)
      return data
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    storage.remove('admin_token')
    storage.remove('admin_userInfo')
  }

  const getUserInfo = async () => {
    if (!token.value) return
    try {
      const data = await adminApi.getUserInfo()
      userInfo.value = data
      storage.set('admin_userInfo', data)
      return data
    } catch (error) {
      throw error
    }
  }

  return {
    token,
    userInfo,
    login,
    logout,
    getUserInfo
  }
})

