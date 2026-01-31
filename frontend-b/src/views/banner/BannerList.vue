<template>
  <div class="banner-management">
    <div class="header">
      <h2 class="page-title">轮播图管理</h2>
      <el-button type="primary" @click="handleAdd">新增轮播图</el-button>
    </div>
    
    <el-table :data="banners" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="展览封面" width="120">
        <template #default="{ row }">
          <el-image 
            v-if="row.coverImage"
            :src="row.coverImage" 
            fit="cover" 
            style="width: 80px; height: 60px; border-radius: 4px;"
          />
          <span v-else style="color: #999;">无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="exhibitionId" label="展览ID" width="100" />
      <el-table-column prop="exhibitionName" label="展览名称" min-width="200" />
      <el-table-column prop="title" label="轮播图标题" min-width="150" />
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

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      width="600px"
    >
      <el-form :model="form" label-width="120px">
        <el-form-item label="展览ID" required>
          <el-input-number 
            v-model="form.exhibitionId" 
            :min="1" 
            placeholder="请输入展览ID" 
            style="width: 100%;" 
            @change="handleExhibitionIdChange"
          />
          <div style="color: #999; font-size: 12px; margin-top: 4px;">
            请输入要关联的展览ID，轮播图将使用该展览的封面图
          </div>
        </el-form-item>
        <el-form-item label="轮播图标题" required>
          <el-input v-model="form.title" placeholder="请输入轮播图标题" maxlength="100" show-word-limit />
          <div style="color: #999; font-size: 12px; margin-top: 4px;">
            标题会根据展览ID自动填充，您可以修改
          </div>
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
import request from '@/utils/request'

interface Banner {
  id?: number
  exhibitionId: number
  exhibitionName?: string
  coverImage?: string
  title: string
  sortOrder: number
  status: number
  createTime?: string
  updateTime?: string
}

const banners = ref<Banner[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = ref<Banner>({
  exhibitionId: 1,
  title: '',
  sortOrder: 0,
  status: 1
})

const fetchData = async () => {
  try {
    const res = await request.get('/admin/banner/list', {
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

// 根据展览ID获取展览信息并自动填充标题
const handleExhibitionIdChange = async (exhibitionId: number | null) => {
  if (!exhibitionId || exhibitionId < 1) return
  
  try {
    const res = await request.get(`/admin/exhibition/${exhibitionId}`)
    if (res && res.name) {
      // 自动填充标题为展览名称，用户可以修改
      form.value.title = res.name
    }
  } catch (e: any) {
    ElMessage.warning('未找到该展览，请检查展览ID是否正确')
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    exhibitionId: 1,
    title: '',
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
    if (!form.value.exhibitionId) {
      ElMessage.warning('请输入展览ID')
      return
    }
    if (!form.value.title) {
      ElMessage.warning('请输入轮播图标题')
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
</style>
