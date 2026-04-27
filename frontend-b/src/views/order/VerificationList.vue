<template>
  <div class="verification-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单号">
          <el-input v-model="verifyOrderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleVerifyByOrderNo">核销订单</el-button>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="全部" :value="undefined" />
            <el-option label="待使用" :value="1" />
            <el-option label="已使用" :value="2" />
            <el-option label="已取消" :value="3" />
            <el-option label="退款中" :value="5" />
            <el-option label="已退款" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="订单ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="exhibitionName" label="展览名称" />
        <el-table-column prop="contactName" label="真实姓名" width="100" />
        <el-table-column prop="contactPhone" label="手机号" width="180" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">待支付</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning">待使用</el-tag>
            <el-tag v-else-if="row.status === 2" type="success">已使用</el-tag>
            <el-tag v-else-if="row.status === 3" type="danger">已取消</el-tag>
            <el-tag v-else-if="row.status === 5" type="warning">退款中</el-tag>
            <el-tag v-else-if="row.status === 6" type="info">已退款</el-tag>
            <div v-if="row.status === 5" style="color: #e6a23c; font-size: 12px; margin-top: 4px;">
              申请退款
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column prop="refundTime" label="退款时间" width="180">
          <template #default="{ row }">
            {{ row.refundTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 1" 
              link 
              type="primary" 
              @click="handleVerify(row)"
            >
              核销
            </el-button>
            <el-button 
              v-if="row.status === 2" 
              link 
              type="warning" 
              @click="handleReset(row)"
            >
              重置
            </el-button>
            <el-button 
              v-if="row.status === 1 || row.status === 5" 
              link 
              type="danger" 
              @click="handleRefund(row)"
            >
              退款
            </el-button>
            <el-button link class="detail-btn" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag v-if="detailData.status === 0" type="info">待支付</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="warning">待使用</el-tag>
          <el-tag v-else-if="detailData.status === 2" type="success">已使用</el-tag>
          <el-tag v-else-if="detailData.status === 3" type="danger">已取消</el-tag>
          <el-tag v-else-if="detailData.status === 5" type="warning">退款中</el-tag>
          <el-tag v-else-if="detailData.status === 6" type="info">已退款</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="展览名称">{{ detailData.exhibitionName }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ detailData.contactName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ detailData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="核销时间">{{ detailData.verifyTime || '未核销' }}</el-descriptions-item>
        <el-descriptions-item label="退款时间">{{ detailData.refundTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'
import request from '@/utils/request'

interface Order {
  id: number
  orderNo: string
  exhibitionName: string
  contactName: string
  contactPhone: string
  totalAmount: number
  status: number
  createTime: string
  verifyTime?: string
}

const tableData = ref<Order[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  status: undefined as number | undefined
})

const verifyOrderNo = ref('')
const detailVisible = ref(false)
const detailData = ref<Partial<Order>>({})

const fetchData = async () => {
  try {
    const res = await orderApi.getTicketOrderList({
      page: currentPage.value,
      size: pageSize.value,
      status: searchForm.status
    })

    let records = res.records || []
    // 兜底：当后端状态尚未及时汇总到母订单时，前端也保证“退款中”可见
    if (searchForm.status === 5) {
      records = records.filter((row: any) => row.status === 5 || !!row.refundRequestTime)
    }

    // 兼容后端未正确分页/total返回0的场景
    const backendTotal = Number(searchForm.status === 5 ? records.length : (res.total || 0))
    const fallbackTotal = backendTotal > 0 ? backendTotal : records.length
    total.value = fallbackTotal

    const maxPage = Math.max(1, Math.ceil(fallbackTotal / pageSize.value))
    if (currentPage.value > maxPage) {
      currentPage.value = maxPage
      await fetchData()
      return
    }

    // 若后端返回全量数据（未分页），前端按当前分页做切片展示
    const needClientSlice = backendTotal === 0 && records.length > pageSize.value
    if (needClientSlice) {
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      tableData.value = records.slice(start, end)
    } else {
      tableData.value = records
    }

    if (fallbackTotal === 0) {
      currentPage.value = 1
    }
  } catch (e: any) {
    ElMessage.error(e.message || '获取数据失败')
    tableData.value = []
    total.value = 0
    currentPage.value = 1
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handleVerifyByOrderNo = async () => {
  if (!verifyOrderNo.value.trim()) {
    ElMessage.warning('请输入订单号')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要核销订单 ' + verifyOrderNo.value + ' 吗？', '提示', {
      type: 'warning'
    })
    await request.post('/admin/order/ticket/verify', { orderNo: verifyOrderNo.value.trim() })
    ElMessage.success('核销成功')
    verifyOrderNo.value = ''
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '核销失败')
    }
  }
}

const handleVerify = async (row: Order) => {
  try {
    await ElMessageBox.confirm('确定要核销这个订单吗？', '提示', {
      type: 'warning'
    })
    await orderApi.verifyTicketOrder(row.id)
    ElMessage.success('核销成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '核销失败')
    }
  }
}

const handleReset = async (row: Order) => {
  try {
    await ElMessageBox.confirm('确定要重置这个订单的核销状态吗？订单将恢复为待使用状态。', '提示', {
      type: 'warning'
    })
    await request.post(`/admin/order/ticket/${row.id}/reset`)
    ElMessage.success('重置成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '重置失败')
    }
  }
}

const handleDetail = async (row: Order) => {
  try {
    const data = await orderApi.getTicketOrderDetail(row.id)
    detailData.value = data
    detailVisible.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '获取详情失败')
  }
}

const handleRefund = async (row: Order) => {
  try {
    await ElMessageBox.confirm('确定要退款这个订单吗？退款后将恢复库存。', '提示', {
      type: 'warning'
    })
    await request.post(`/admin/order/ticket/${row.id}/refund`)
    ElMessage.success('退款成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '退款失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.verification-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}

.detail-btn {
  color: #000000 !important;
}
</style>
