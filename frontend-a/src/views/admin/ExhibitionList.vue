<template>
  <div class="exhibition-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>展览列表</span>
          <el-button type="primary" @click="$router.push('/admin/exhibition/create')">
            <el-icon><Plus /></el-icon>
            创建展览
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="展览名称">
          <el-input v-model="searchForm.name" placeholder="请输入展览名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              fit="cover"
              style="width: 80px; height: 60px; border-radius: 4px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="展览名称" min-width="180" />
        <el-table-column prop="shortDesc" label="简介" min-width="200" show-overflow-tooltip />
        <el-table-column label="展览时间" width="200">
          <template #default="{ row }">
            {{ row.startDate }} ~ {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="票价" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">
              {{ statusMap[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleInventory(row)">库存</el-button>
            <el-popconfirm title="确定删除该展览吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminExhibitionApi, type AdminExhibition } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const tableData = ref<AdminExhibition[]>([])

const searchForm = reactive({
  name: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const statusMap: Record<number, { label: string; type: 'info' | 'success' | 'warning' }> = {
  0: { label: '待开始', type: 'info' },
  1: { label: '进行中', type: 'success' },
  2: { label: '已结束', type: 'warning' }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminExhibitionApi.getList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name || undefined,
      status: searchForm.status
    })
    tableData.value = res.records
    pagination.total = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = undefined
  handleSearch()
}

const handleEdit = (row: AdminExhibition) => {
  router.push(`/admin/exhibition/edit/${row.id}`)
}

const handleInventory = (row: AdminExhibition) => {
  router.push(`/admin/inventory?exhibitionId=${row.id}`)
}

const handleDelete = async (row: AdminExhibition) => {
  try {
    await adminExhibitionApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
