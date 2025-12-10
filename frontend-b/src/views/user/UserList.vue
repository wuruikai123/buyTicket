<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">用户列表</span>
          <span class="user-count">当前用户{{ pagination.total }}人</span>
        </div>
      </template>

      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="搜索用户">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/手机号/UID"
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
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="uid" label="用户账号" min-width="150" />
        <el-table-column prop="phone" label="用户手机号" min-width="130" />
        <el-table-column prop="createTime" label="注册时间" min-width="200" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
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

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="900px">
      <div class="user-detail-content" v-if="currentUser">
        <!-- 用户信息部分 -->
        <div class="user-info-section">
          <!-- <h3 class="section-title">用户信息</h3> -->
          <div class="user-info-grid">
            <div class="info-item">
              <span class="info-label">用户名：</span>
              <span class="info-value">{{ currentUser.username || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">用户账号：</span>
              <span class="info-value">{{ currentUser.uid || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">用户手机号：</span>
              <span class="info-value">{{ currentUser.phone || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱：</span>
              <span class="info-value">{{ currentUser.email || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间：</span>
              <span class="info-value">{{ formatDateTime(currentUser.createTime || currentUser.registerTime) }}</span>
            </div>
          </div>
        </div>
        <div class="user-actions">
            <el-button @click="handleFreeze">冻结</el-button>
            <el-button link type="danger" @click="handleLogout">注销</el-button>
          </div>

        <!-- 用户门票订单部分 -->
        <div class="ticket-orders-section">
          <h3 class="section-title">用户门票订单</h3>
          <el-table :data="ticketOrders" v-loading="orderLoading" border style="width: 100%">
            <el-table-column prop="exhibitionName" label="展览名称" min-width="200" />
            <el-table-column prop="validTime" label="有效时间" width="200">
              <template #default="{ row }">
                <div class="time-slot">
                  <div>{{ formatDate(row.ticketDate) }} {{ row.timeSlot?.split('-')[0] }}</div>
                  <div>{{ formatDate(row.ticketDate) }} {{ row.timeSlot?.split('-')[1] }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                {{ row.price || row.unitPrice || 0 }}元
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="成交时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-button size="small" @click="handleInvalidateOrder(row)">作废</el-button>
                <el-button 
                  link 
                  type="primary" 
                  @click="handleRefund(row)"
                  :disabled="row.refundStatus === 1"
                >
                  {{ row.refundStatus === 1 ? '已退款' : '退款' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import { orderApi } from '@/api/order'

const loading = ref(false)
const tableData = ref<any[]>([])
const detailDialogVisible = ref(false)
const currentUser = ref<any>(null)
const ticketOrders = ref<any[]>([])
const orderLoading = ref(false)

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})


const loadData = async () => {
  loading.value = true
  try {
    const data: any = await userApi.getList({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    // 处理分页数据，兼容不同的返回格式
    if (data && typeof data === 'object') {
      if (data.records) {
    tableData.value = data.records
        pagination.total = data.total || 0
      } else if (Array.isArray(data)) {
        tableData.value = data
        pagination.total = data.length
      } else {
        tableData.value = []
        pagination.total = 0
      }
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

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

const formatDate = (date: string | undefined) => {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}年${month}月${day}日`
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const loadTicketOrders = async (userId: number) => {
  orderLoading.value = true
  try {
    const data: any = await orderApi.getTicketOrderList({
      userId,
      page: 1,
      size: 100
    })
    if (data && data.records) {
      ticketOrders.value = data.records
    } else if (Array.isArray(data)) {
      ticketOrders.value = data
    } else {
      ticketOrders.value = []
    }
  } catch (error) {
    ElMessage.error('加载订单数据失败')
    ticketOrders.value = []
  } finally {
    orderLoading.value = false
  }
}

const handleView = async (row: any) => {
  try {
    const data = await userApi.getDetail(row.id)
    currentUser.value = data
    detailDialogVisible.value = true
    // 加载用户的门票订单
    await loadTicketOrders(row.id)
  } catch (error) {
    ElMessage.error('获取用户详情失败')
  }
}

const handleFreeze = async () => {
  if (!currentUser.value) return
  try {
    await ElMessageBox.confirm(
      `确定要${currentUser.value.status === 1 ? '冻结' : '解冻'}该用户吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await userApi.updateStatus(currentUser.value.id, currentUser.value.status === 1 ? 0 : 1)
    ElMessage.success('操作成功')
    currentUser.value.status = currentUser.value.status === 1 ? 0 : 1
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleLogout = async () => {
  if (!currentUser.value) return
  try {
    await ElMessageBox.confirm('确定要注销该用户吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用注销接口
    ElMessage.success('注销成功')
    detailDialogVisible.value = false
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('注销失败')
    }
  }
}

const handleInvalidateOrder = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要作废该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await orderApi.cancelTicketOrder(row.id)
    ElMessage.success('作废成功')
    if (currentUser.value) {
      await loadTicketOrders(currentUser.value.id)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('作废失败')
    }
  }
}

const handleRefund = async (row: any) => {
  if (row.refundStatus === 1) {
    ElMessage.info('该订单已退款')
    return
  }
  try {
    await ElMessageBox.confirm('确定要退款该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用退款接口
    ElMessage.success('退款成功')
    if (currentUser.value) {
      await loadTicketOrders(currentUser.value.id)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('退款失败')
  }
}
}


onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.user-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
    font-size: 16px;

    .title {
      font-size: 18px;
    }

    .user-count {
      font-size: 14px;
      font-weight: normal;
      color: #666;
    }
  }

  .search-form {
    margin-bottom: 20px;
  }

  .user-detail-content {
    .user-info-section {
      margin-bottom: 20px;
      padding: 12px 16px;
      background-color: #f5f7fa;
      border-radius: 4px;

      .section-title {
        margin: 0 0 12px 0;
        font-size: 14px;
        font-weight: bold;
        color: #303133;
      }

      .user-info-grid {
        display: flex;
        flex-wrap: wrap;
        gap: 16px 24px;
        margin-bottom: 12px;

        .info-item {
          display: flex;
          align-items: center;
          font-size: 13px;
          white-space: nowrap;

          .info-label {
            color: #606266;
            margin-right: 6px;
          }

          .info-value {
            color: #303133;
            font-weight: 500;
          }
        }
      }

      .user-actions {
        display: flex;
        gap: 12px;
        margin-top: 12px;
        padding: 10px;
      }
    }

    .ticket-orders-section {
      .section-title {
        margin: 16px 0 16px 0;
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }

      .time-slot {
        display: flex;
        flex-direction: column;
        gap: 4px;
        font-size: 13px;
      }
    }
  }
}
</style>

