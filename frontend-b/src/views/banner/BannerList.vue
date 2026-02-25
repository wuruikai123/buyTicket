<template>
  <div class="banner-management">
    <div class="header">
      <h2 class="page-title">轮播图管理</h2>
      <el-button type="primary" @click="handleAdd">新增轮播图</el-button>
    </div>
    
    <el-table :data="banners" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="轮播图图片" width="200">
        <template #default="{ row }">
          <el-image 
            v-if="row.imageUrl"
            :src="row.imageUrl" 
            fit="cover" 
            style="width: 160px; height: 80px; border-radius: 4px;"
          />
          <span v-else style="color: #999;">无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="轮播图标题" min-width="150" />
      <el-table-column prop="linkUrl" label="跳转链接" min-width="200" />
      <el-table-column prop="sortOrder" label="排序号" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="上传图片" required>
          <el-upload
            class="banner-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="banner-image" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传轮播图</div>
            </div>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="轮播图标题">
          <el-input 
            v-model="form.title" 
            placeholder="请输入轮播图标题（选填）" 
            maxlength="25" 
            show-word-limit 
          />
        </el-form-item>
        
        <el-form-item label="跳转链接">
          <el-input 
            v-model="form.linkUrl" 
            placeholder="请输入跳转的链接（选填）" 
          />
        </el-form-item>
        
        <el-form-item label="排序号" required>
          <el-input-number v-model="form.sortOrder" :min="0" placeholder="数字越小越靠前" style="width: 100%;" />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Banner {
  id?: number
  imageUrl: string
  title: string
  linkUrl: string
  sortOrder: number
  status: number
  createTime?: string
  updateTime?: string
}

const banners = ref<Banner[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const form = ref<Banner>({
  imageUrl: '',
  title: '',
  linkUrl: '',
  sortOrder: 0,
  status: 1
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/banner/list', {
      params: {
        page: pagination.value.page,
        size: pagination.value.size
      }
    })
    banners.value = res?.records || []
    pagination.value.total = res?.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  fetchData()
}

const handlePageChange = () => {
  fetchData()
}

const beforeImageUpload = async (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }

  const formData = new FormData()
  formData.append('file', file)

  try {
    const result: any = await request.post('/admin/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (result && result.url) {
      form.value.imageUrl = result.url
      ElMessage.success('图片上传成功')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  }

  return false
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    imageUrl: '',
    title: '',
    linkUrl: '',
    sortOrder: banners.value.length,
    status: 1
  }
  dialogVisible.value = true
}

const handleEdit = (banner: Banner) => {
  isEdit.value = true
  form.value = { ...banner }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (!form.value.imageUrl) {
      ElMessage.warning('请上传轮播图图片')
      return
    }
    
    if (isEdit.value && form.value.id) {
      await request.post('/admin/banner/update', form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/banner/create', form.value)
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
    await request.delete(`/admin/banner/${banner.id}`)
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
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.banner-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    display: block;
    width: 400px;
    height: 200px;

    &:hover {
      border-color: #409eff;
    }
  }

  .banner-image {
    width: 400px;
    height: 200px;
    object-fit: cover;
    display: block;
  }

  .upload-placeholder {
    width: 400px;
    height: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #fafafa;

    .upload-icon {
      font-size: 48px;
      color: #8c939d;
      margin-bottom: 8px;
    }

    .upload-text {
      font-size: 14px;
      color: #8c939d;
    }
  }
}
</style>
