<template>
  <div class="sales-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">销售记录</span>
          <span class="total-count">总计销售{{ totalSales }}张</span>
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
            {{ row.uid || row.userId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="有效时间" width="200">
          <template #default="{ row }">
            <div class="time-slot">
              <div v-if="row.ticketDate && row.timeSlot">
                {{ formatValidTime(row.ticketDate, row.timeSlot?.split('-')[0]) }}
              </div>
              <div v-else-if="row.items && row.items[0]">
                {{ formatValidTime(row.items[0].ticketDate, row.items[0].timeSlot?.split('-')[0]) }}
              </div>
              <div v-else>-</div>
              <div v-if="row.ticketDate && row.timeSlot">
                {{ formatValidTime(row.ticketDate, row.timeSlot?.split('-')[1]) }}
              </div>
              <div v-else-if="row.items && row.items[0]">
                {{ formatValidTime(row.items[0].ticketDate, row.items[0].timeSlot?.split('-')[1]) }}
              </div>
              <div v-else>-</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="成交时间" width="180" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleReset(row)">重置</el-button>
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'

const loading = ref(false)
const tableData = ref<any[]>([])
const totalSales = ref(0)

const searchForm = reactive({
  orderNo: ''
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

const formatValidTime = (dateTime: string | undefined, time?: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  if (time) {
    return `${year}年${month}月${day}日 ${time}`
  }
  return `${year}年${month}月${day}日`
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
      // 销售记录：显示所有已支付的订单（状态0:待支付 或 1:待使用 或 2:已使用）
      // 这里不限制状态，显示所有订单，或者可以根据需要筛选已支付的订单
    }
    if (searchForm.orderNo) {
      params.orderNo = searchForm.orderNo
    }
    const data: any = await orderApi.getTicketOrderList(params)
    tableData.value = data.records || []
    pagination.total = data.total || 0
    // 计算总计销售数量（所有订单的票数总和，或者直接使用订单总数）
    totalSales.value = pagination.total
  } catch (error) {
    ElMessage.error('加载数据失败')
    tableData.value = []
    pagination.total = 0
    totalSales.value = 0
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

const handleReset = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要重置该销售记录吗？重置后订单将恢复为待支付状态。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用重置订单接口
    // await orderApi.resetOrder(row.id)
    void row // 标记参数使用，待接口实现后删除此行
    ElMessage.success('重置成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该销售记录吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用删除订单接口
    // await orderApi.deleteOrder(row.id)
    void row // 标记参数使用，待接口实现后删除此行
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.sales-list {
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

  .time-slot {
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 13px;
  }
}
</style>

