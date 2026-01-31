<template>
  <div class="sales-record">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">销售记录</span>
          <span class="total-count">总计{{ totalCount }}张</span>
        </div>
      </template>

      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="搜索门票订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入门票订单号"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="待使用" :value="1" />
            <el-option label="已使用" :value="2" />
            <el-option label="已作废" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column label="展览名称&门票订单号" min-width="250">
          <template #default="{ row }">
            <div class="exhibition-order-info">
              <div class="exhibition-name">
                {{ row.exhibitionName || (row.items && row.items[0]?.exhibitionName) || '-' }}
              </div>
              <div class="order-no">{{ row.orderNo || row.id || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户账号" min-width="150">
          <template #default="{ row }">
            {{ row.contactPhone || row.userId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="warning">待使用</el-tag>
            <el-tag v-else-if="row.status === 2" type="success">已使用</el-tag>
            <el-tag v-else-if="row.status === 4" type="info">已作废</el-tag>
            <el-tag v-else type="info">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 1 || row.status === 2" 
              link 
              type="danger" 
              @click="handleVoid(row)"
            >
              作废
            </el-button>
            <span v-else style="color: #999;">-</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const totalCount = ref(0)

const searchForm = reactive({
  orderNo: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formatDateTime = (dateTime: string | undefined) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}年${month}月${day}日 ${hours}:${minutes}:${seconds}`
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.orderNo) {
      params.orderNo = searchForm.orderNo
    }
    if (searchForm.status !== undefined) {
      params.status = searchForm.status
    }
    const data: any = await orderApi.getTicketOrderList(params)
    tableData.value = data.records || []
    pagination.total = data.total || 0
    totalCount.value = pagination.total
  } catch (error) {
    ElMessage.error('加载数据失败')
    tableData.value = []
    pagination.total = 0
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleSizeChange = () => loadData()
const handlePageChange = () => loadData()

const handleVoid = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要作废该订单吗？作废后订单将无法使用，此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post(`/admin/order/ticket/${row.id}/void`)
    ElMessage.success('作废成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '作废失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.sales-record {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
    font-size: 16px;

    .title {
      font-size: 18px;
    }

    .total-count {
      font-size: 14px;
      font-weight: normal;
      color: #666;
    }
  }

  .search-form {
    margin-bottom: 20px;
  }

  .exhibition-order-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .exhibition-name {
      font-size: 14px;
      color: #303133;
      font-weight: 500;
    }

    .order-no {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
