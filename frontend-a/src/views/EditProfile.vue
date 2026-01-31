<template>
  <div class="edit-profile">
    <!-- 顶部导航 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="page-title">修改信息</h1>
      <div class="header-placeholder"></div>
    </div>

    <div class="form-container" v-loading="loading">
      <!-- 头像 -->
      <div class="avatar-section">
        <div class="avatar" :style="{ backgroundImage: form.avatar ? `url(${form.avatar})` : '' }">
          <span v-if="!form.avatar" class="avatar-placeholder">{{ form.username?.charAt(0) || '?' }}</span>
        </div>
        <el-button link type="primary" @click="handleChangeAvatar">更换头像</el-button>
      </div>

      <!-- 表单 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="profile-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled placeholder="用户名不可修改" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" style="width: 100%">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 修改密码 -->
      <div class="password-section">
        <h3 class="section-title">修改密码</h3>
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleChangePassword" :loading="changingPwd" style="width: 100%">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi, type User } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const changingPwd = ref(false)

const form = reactive<Partial<User>>({
  username: '',
  phone: '',
  avatar: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadUserInfo = async () => {
  loading.value = true
  try {
    const user = await userApi.getUserInfo()
    if (user) {
      form.username = user.username
      form.phone = user.phone || ''
      form.avatar = user.avatar || ''
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handleChangeAvatar = () => {
  ElMessage.info('头像上传功能开发中')
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await userApi.updateUserInfo({
      phone: form.phone
    })
    ElMessage.success('修改成功')
    // 更新localStorage中的用户信息
    const updatedUser = await userApi.getUserInfo()
    if (updatedUser) {
      localStorage.setItem('userInfo', JSON.stringify(updatedUser))
    }
    // 延迟返回，确保用户看到成功提示
    setTimeout(() => {
      router.back()
    }, 500)
  } catch (e: any) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    submitting.value = false
  }
}

const handleChangePassword = async () => {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return

  changingPwd.value = true
  try {
    await userApi.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    // 清除表单
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value?.resetFields()
    // 清除token
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    // 延迟跳转
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (e: any) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    changingPwd.value = false
  }
}

onMounted(loadUserInfo)
</script>

<style scoped>
.edit-profile {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background-color: white;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #e0e0e0;
}

.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: none;
  border: none;
  cursor: pointer;
  color: #333;
  border-radius: 50%;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.header-placeholder {
  width: 40px;
}

.form-container {
  padding: 20px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background-color: white;
  border-radius: 12px;
  margin-bottom: 16px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #409eff;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder {
  font-size: 32px;
  color: white;
  font-weight: bold;
}

.profile-form {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}

.password-section {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px;
}
</style>
