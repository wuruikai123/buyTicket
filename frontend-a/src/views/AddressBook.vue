<template>
  <div class="address-book">
    <!-- 顶部导航 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="page-title">地址管理</h1>
      <div class="header-placeholder"></div>
    </div>

    <!-- 地址列表 -->
    <div class="address-list" v-loading="loading">
      <div v-if="addresses.length === 0" class="empty-state">
        <div class="empty-icon">📍</div>
        <p class="empty-text">暂无收货地址</p>
      </div>

      <div v-for="addr in addresses" :key="addr.id" class="address-card">
        <div class="address-info" @click="selectAddress(addr)">
          <div class="address-header">
            <span class="name">{{ addr.name }}</span>
            <span class="phone">{{ addr.phone }}</span>
            <el-tag v-if="addr.isDefault" type="primary" size="small">默认</el-tag>
          </div>
          <div class="address-detail">
            {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}
          </div>
        </div>
        <div class="address-actions">
          <el-button link @click="handleSetDefault(addr)" v-if="!addr.isDefault">
            设为默认
          </el-button>
          <el-button link type="primary" @click="handleEdit(addr)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(addr)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 添加地址按钮 -->
    <div class="add-btn-wrapper">
      <el-button type="primary" size="large" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加新地址
      </el-button>
    </div>

    <!-- 地址编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地址' : '添加地址'" width="90%" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="form.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" prop="province">
          <div class="region-select">
            <el-input v-model="form.province" placeholder="省" style="width: 32%" />
            <el-input v-model="form.city" placeholder="市" style="width: 32%" />
            <el-input v-model="form.district" placeholder="区/县" style="width: 32%" />
          </div>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="街道、楼牌号等" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi, type Address } from '@/api/user'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const addresses = ref<Address[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 是否为选择模式（从结算页跳转过来）
const isSelectMode = route.query.select === 'true'

const form = reactive<Address>({
  id: 0,
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const loadAddresses = async () => {
  loading.value = true
  try {
    const res = await userApi.getAddressList()
    addresses.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: 0,
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: addresses.value.length === 0
  })
  dialogVisible.value = true
}

const handleEdit = (addr: Address) => {
  isEdit.value = true
  Object.assign(form, addr)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await userApi.updateAddress(form)
      ElMessage.success('修改成功')
    } else {
      await userApi.addAddress(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadAddresses()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (addr: Address) => {
  try {
    await ElMessageBox.confirm('确定删除该地址吗？', '提示', { type: 'warning' })
    await userApi.deleteAddress(addr.id)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (e) {
    // 取消删除
  }
}

const handleSetDefault = async (addr: Address) => {
  try {
    await userApi.setDefaultAddress(addr.id)
    ElMessage.success('设置成功')
    loadAddresses()
  } catch (e: any) {
    ElMessage.error(e.message || '设置失败')
  }
}

// 选择地址（选择模式下）
const selectAddress = (addr: Address) => {
  if (isSelectMode) {
    sessionStorage.setItem('selectedAddress', JSON.stringify(addr))
    router.back()
  }
}

onMounted(loadAddresses)
</script>

<style scoped>
.address-book {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 80px;
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

.address-list {
  padding: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  background-color: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
  color: #999;
}

.address-card {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.address-info {
  cursor: pointer;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.phone {
  font-size: 14px;
  color: #666;
}

.address-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.add-btn-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
}

.add-btn-wrapper .el-button {
  width: 100%;
}

.region-select {
  display: flex;
  gap: 8px;
}
</style>
