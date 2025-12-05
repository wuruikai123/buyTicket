<template>
  <div class="ticket-order-list">
    <el-card>
      <template #header>
        <span>门票订单</span>
      </template>

      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="订单号/联系人">
          <el-input
            v-model="searchForm.keyword"
            placeholder="订单号/联系人/展览名称"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待支付" :value="0" />
            <el-option label="待使用" :value="1" />
            <el-option label="已使用" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="exhibitionName" label="展览名称" width="200" />
        <el-table-column prop="ticketDate" label="票务日期" width="120" />
        <el-table-column prop="ticketCount" label="票数" width="80" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="success"
              @click="handleVerify(row)"
            >
              核销
            </el-button>
            <el-button
              v-if="row.status !== 3"
              link
              type="danger"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
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

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getStatusType = (status: number) => {
  const types: Record<number, string> = { 0: 'warning', 1: 'info', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = { 0: '待支付', 1: '待使用', 2: '已使用', 3: '已取消' }
  return texts[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await orderApi.getTicketOrderList({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
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
  searchForm.keyword = ''
  searchForm.status = undefined
  handleSearch()
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const handleView = async (row: any) => {
  try {
    const data = await orderApi.getTicketOrderDetail(row.id)
    ElMessageBox.alert(JSON.stringify(data, null, 2), '订单详情', {
      confirmButtonText: '确定'
    })
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  }
}

const handleVerify = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要核销该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await orderApi.verifyTicketOrder(row.id)
    ElMessage.success('核销成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('核销失败')
    }
  }
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await orderApi.cancelTicketOrder(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.ticket-order-list {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>

