<template>
  <div class="banner-management">
    <h2 class="page-title">首页轮播图</h2>
    
    <div class="banner-cards">
      <!-- 已有轮播图 -->
      <div
        v-for="banner in banners"
        :key="banner.id"
        class="banner-card"
      >
        <div class="card-image" :style="{ backgroundImage: `url(${getImageUrl(banner.imageUrl)})` }">
          <button class="delete-btn" @click.stop="handleDelete(banner)">
            <span class="delete-icon">×</span>
          </button>
        </div>
        <div class="card-info">
          <div class="info-row">
            <span class="label">当前图片</span>
          </div>
          <div class="info-row url-row">
            <span class="value">{{ banner.imageUrl || 'xxxxxxxxxxxxxxxxxxxxxxx' }}</span>
          </div>
          <div class="info-row">
            <span class="label">主标题</span>
            <span class="value">{{ banner.title || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">次标题</span>
            <span class="value">{{ banner.subtitle || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">跳转页面</span>
            <span class="value">{{ getLinkTypeText(banner) }}</span>
          </div>
          <div class="info-row">
            <button class="view-detail-btn" @click.stop="showDetail(banner)">
              查看详细信息
            </button>
          </div>
        </div>
      </div>

      <!-- 新增按钮卡片 -->
      <div v-if="banners.length < 3" class="banner-card add-card" @click="handleAdd">
        <div class="card-image add-image">
          <span class="add-icon">+</span>
        </div>
        <div class="card-info">
          <div class="info-row">
            <span class="label">主标题</span>
            <span class="placeholder">10字以内</span>
          </div>
          <div class="info-row">
            <span class="label">主标题</span>
            <span class="placeholder">10字以内</span>
          </div>
          <div class="info-row">
            <span class="label">次标题</span>
            <span class="placeholder">10字以内</span>
          </div>
          <div class="info-row">
            <span class="label">跳转页面</span>
            <span class="placeholder">文字以内</span>
          </div>
          <div class="info-row">
            <span class="label">跳转页面</span>
            <span class="placeholder">文字以内</span>
          </div>
          <div class="info-row">
            <button class="view-detail-btn disabled">查看详细信息</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :show-close="false"
      width="900px"
      class="detail-dialog"
    >
      <div class="detail-content">
        <div class="detail-left">
          <h3 class="detail-title">关于展厅 <span class="detail-limit">500字以内</span></h3>
          <div class="detail-text">
            <p><strong>主标题：</strong>{{ currentBanner?.title || '未设置' }}</p>
            <p><strong>次标题：</strong>{{ currentBanner?.subtitle || '未设置' }}</p>
            <p><strong>图片URL：</strong>{{ currentBanner?.imageUrl }}</p>
            <p><strong>链接类型：</strong>{{ getLinkTypeText(currentBanner) }}</p>
            <p><strong>链接地址：</strong>{{ getLinkText(currentBanner) }}</p>
            <p><strong>排序：</strong>{{ currentBanner?.sortOrder }}</p>
            <p><strong>状态：</strong>{{ currentBanner?.status === 1 ? '启用' : '禁用' }}</p>
            <p><strong>创建时间：</strong>{{ formatDateTime(currentBanner?.createTime) }}</p>
          </div>
          <div class="detail-actions">
            <button class="action-btn edit-btn" @click="handleEdit(currentBanner)">编辑</button>
            <button class="action-btn confirm-btn" @click="detailVisible = false">关闭</button>
          </div>
        </div>
        <div class="detail-right">
          <button class="close-btn" @click="detailVisible = false">
            <span class="close-icon">×</span>
          </button>
          <div class="preview-section">
            <div class="preview-label">背景图预览</div>
            <div class="preview-image" :style="{ backgroundImage: `url(${getImageUrl(currentBanner?.imageUrl || '')})` }"></div>
          </div>
          <div class="preview-section">
            <div class="preview-label">页面图预览</div>
            <div class="preview-image" :style="{ backgroundImage: `url(${getImageUrl(currentBanner?.imageUrl || '')})` }"></div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="主标题">
          <el-input v-model="form.title" placeholder="请输入主标题（10字以内）" maxlength="10" />
        </el-form-item>
        <el-form-item label="次标题">
          <el-input v-model="form.subtitle" placeholder="请输入次标题（10字以内）" maxlength="10" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
          <el-image 
            v-if="form.imageUrl" 
            :src="getImageUrl(form.imageUrl)" 
            fit="cover" 
            style="width: 100%; height: 200px; margin-top: 10px; border-radius: 4px;"
          />
        </el-form-item>
        <el-form-item label="链接类型">
          <el-select v-model="form.linkType" placeholder="请选择">
            <el-option label="无链接" :value="0" />
            <el-option label="展览详情" :value="1" />
            <el-option label="外部链接" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.linkType === 1" label="展览ID">
          <el-input-number v-model="form.linkId" :min="1" />
        </el-form-item>
        <el-form-item v-if="form.linkType === 2" label="外部链接">
          <el-input v-model="form.linkUrl" placeholder="请输入链接URL" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="对外展示">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

interface Banner {
  id?: number
  title: string
  subtitle?: string
  imageUrl: string
  linkType: number
  linkId?: number
  linkUrl?: string
  sortOrder: number
  status: number
  createTime?: string
}

const banners = ref<Banner[]>([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const currentBanner = ref<Banner | null>(null)

const form = ref<Banner>({
  title: '',
  imageUrl: '',
  linkType: 0,
  sortOrder: 0,
  status: 1
})

const fetchData = async () => {
  try {
    const res = await request.get('/banner/list', {
      params: {
        page: 1,
        size: 100
      }
    })
    banners.value = res?.records || []
  } catch (e: any) {
    ElMessage.error(e.message || '获取数据失败')
  }
}

const getImageUrl = (url: string) => {
  if (!url) return ''
  // 如果是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果是相对路径，拼接后端地址
  return `http://localhost:8080${url}`
}

const getLinkTypeText = (banner: Banner | null) => {
  if (!banner) return ''
  if (banner.linkType === 0) return '无链接'
  if (banner.linkType === 1) return '展览详情'
  if (banner.linkType === 2) return '外部链接'
  return '未知'
}

const getLinkText = (banner: Banner | null) => {
  if (!banner) return ''
  if (banner.linkType === 0) return '无'
  if (banner.linkType === 1) return `展览ID: ${banner.linkId}`
  if (banner.linkType === 2) return banner.linkUrl || '-'
  return '-'
}

const formatDateTime = (dateTime: string | undefined) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    title: '',
    subtitle: '',
    imageUrl: '',
    linkType: 0,
    sortOrder: banners.value.length,
    status: 1
  }
  dialogVisible.value = true
}

const handleEdit = (banner: Banner | null) => {
  if (!banner) return
  isEdit.value = true
  form.value = { ...banner }
  detailVisible.value = false
  dialogVisible.value = true
}

const showDetail = (banner: Banner) => {
  currentBanner.value = banner
  detailVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value && form.value.id) {
      await request.post('/banner/update', form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/banner/create', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleDelete = async (banner: Banner) => {
  try {
    await ElMessageBox.confirm('确定要删除这个轮播图吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/banner/${banner.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.banner-management {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px 0;
}

.banner-cards {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.banner-card {
  width: 180px;
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s;
}

.banner-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-image {
  width: 100%;
  height: 120px;
  background-color: #e0e0e0;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-image {
  background-color: #f0f0f0;
}

.add-icon {
  font-size: 48px;
  color: #ccc;
  font-weight: 300;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.5);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.delete-btn:hover {
  background-color: rgba(0, 0, 0, 0.7);
}

.delete-icon {
  color: white;
  font-size: 20px;
  line-height: 1;
}

.card-info {
  padding: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
}

.info-row.url-row {
  display: block;
  margin-bottom: 12px;
}

.info-row .label {
  color: #666;
}

.info-row .value {
  color: #333;
  font-weight: 500;
  max-width: 100px;
}

.info-row .value.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-row.url-row .value {
  max-width: 100%;
  display: block;
  font-size: 11px;
  color: #999;
  word-break: break-all;
}

.info-row .placeholder {
  color: #ccc;
  font-size: 11px;
}

.view-detail-btn {
  width: 100%;
  padding: 6px;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.view-detail-btn:hover:not(.disabled) {
  background-color: #e8e8e8;
  border-color: #d0d0d0;
}

.view-detail-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.add-card .card-info {
  opacity: 0.6;
}

/* 详情弹窗样式 */
.detail-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.detail-content {
  display: flex;
  height: 500px;
}

.detail-left {
  flex: 1;
  padding: 32px;
  display: flex;
  flex-direction: column;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.detail-limit {
  font-size: 12px;
  font-weight: 400;
  color: #999;
  margin-left: 8px;
}

.detail-text {
  flex: 1;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.8;
  color: #666;
  overflow-y: auto;
}

.detail-text p {
  margin: 0 0 12px 0;
}

.detail-actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.action-btn {
  padding: 10px 32px;
  border-radius: 4px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn {
  background-color: #409eff;
  color: white;
}

.edit-btn:hover {
  background-color: #66b1ff;
}

.confirm-btn {
  background-color: #e0e0e0;
  color: #666;
}

.confirm-btn:hover {
  background-color: #d0d0d0;
}

.detail-right {
  width: 300px;
  background-color: #f5f5f5;
  padding: 32px 24px;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.1);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: rgba(0, 0, 0, 0.2);
}

.close-icon {
  font-size: 24px;
  color: #666;
  line-height: 1;
}

.preview-section {
  margin-bottom: 24px;
}

.preview-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.preview-image {
  width: 100%;
  height: 150px;
  background-color: #e0e0e0;
  background-size: cover;
  background-position: center;
  border-radius: 4px;
}
</style>
