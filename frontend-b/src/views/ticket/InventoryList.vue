<template>
  <div class="inventory-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存管理</span>
          <el-button type="primary" @click="handleCreate">创建库存</el-button>
        </div>
      </template>

      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="展览">
          <el-select v-model="searchForm.exhibitionId" placeholder="全部" clearable style="width: 200px">
            <el-option
              v-for="item in exhibitions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.ticketDate"
            type="date"
            placeholder="选择日期"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="exhibitionName" label="展览名称" width="200" />
        <el-table-column prop="ticketDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="150" />
        <el-table-column prop="totalCount" label="总票数" width="100" />
        <el-table-column prop="soldCount" label="已售" width="100" />
        <el-table-column prop="remainingCount" label="剩余" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleAdjust(row)">调整</el-button>
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ticketApi } from '@/api/ticket'
import { exhibitionApi } from '@/api/exhibition'

const loading = ref(false)
const tableData = ref<any[]>([])
const exhibitions = ref<any[]>([])

const searchForm = reactive({
  exhibitionId: undefined as number | undefined,
  ticketDate: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getStatusType = (status: string) => {
  const types: Record<string, string> = { '充足': 'success', '紧张': 'warning', '售罄': 'danger' }
  return types[status] || 'info'
}

const loadExhibitions = async () => {
  try {
    const data = await exhibitionApi.getList({ page: 1, size: 100 })
    exhibitions.value = data.records
  } catch (error) {
    console.error('加载展览列表失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await ticketApi.getInventoryList({
      page: pagination.page,
      size: pagination.size,
      ...searchForm,
      ticketDate: searchForm.ticketDate ? new Date(searchForm.ticketDate).toISOString().split('T')[0] : undefined
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.exhibitionId = undefined
  searchForm.ticketDate = ''
  handleSearch()
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const handleCreate = () => {
  ElMessage.info('创建库存功能待实现')
}

const handleEdit = (row: any) => {
  ElMessage.info('编辑库存功能待实现')
}

const handleAdjust = (row: any) => {
  ElMessage.info('调整库存功能待实现')
}

onMounted(() => {
  loadExhibitions()
  loadData()
})
</script>

<style scoped lang="scss">
.inventory-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
    font-size: 16px;
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>

