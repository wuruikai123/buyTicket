<template>
  <div class="exhibition-list-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>展览列表</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="展览名称" min-width="180" />
        <el-table-column prop="startDate" label="开始日期" min-width="120" />
        <el-table-column prop="endDate" label="结束日期" min-width="120" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="票价" width="100" />
        <el-table-column prop="soldTickets" label="已售" width="90" />
        <el-table-column prop="totalTickets" label="总票数" width="90" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 1"
              size="small"
              type="success"
              @click="handleUpdateStatus(row, 1)"
            >
              上架
            </el-button>
            <el-button
              v-if="row.status === 1"
              size="small"
              type="warning"
              @click="handleUpdateStatus(row, 0)"
            >
              下架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { exhibitionApi } from '@/api/exhibition'

const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getStatusText = (status: number) => {
  if (status === 0) return '未上架'
  if (status === 1) return '上架'
  if (status === 2) return '下架'
  return '-'
}

const getStatusType = (status: number) => {
  if (status === 0) return 'info'
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  return 'default'
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await exhibitionApi.getList({
      page: pagination.page,
      size: pagination.size
    })
    const data = res?.data ?? res
    if (data?.records) {
      tableData.value = data.records
      pagination.total = data.total || 0
    } else if (Array.isArray(data)) {
      tableData.value = data
      pagination.total = data.length
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error('加载展览列表失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleUpdateStatus = async (row: any, status: number) => {
  const action = status === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}该展览吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await exhibitionApi.updateStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    await loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.exhibition-list-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }

  .pagination-wrap {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
