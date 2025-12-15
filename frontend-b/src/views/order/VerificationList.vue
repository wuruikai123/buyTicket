<template>
  <div class="verification-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单核销管理</span>
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
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待使用" :value="1" />
            <el-option label="已使用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="订单ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="exhibitionName" label="展览名称" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">待支付</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning">待使用</el-tag>
            <el-tag v-else-if="row.status === 2" type="success">已使用</el-tag>
            <el-tag v-else-if="row.status === 3" type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 1" 
              link 
              type="primary" 
              @click="handleVerify(row)"
            >
              核销
            </el-button>
            <el-button link type="info" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
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
        </el-descriptions-item>
        <el-descriptions-item label="展览名称">{{ detailData.exhibitionName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailData.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ detailData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="核销时间">{{ detailData.verifyTime || '未核销' }}</el-descriptions-item>
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
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '获取数据失败')
  }
}

const handleVerifyByOrderNo = async () => {
  if (!verifyOrderNo.value.trim()) {
    ElMessage.warning('请输入订单号')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要核销订单 ' + verifyOrderNo.value + ' 吗？核销后将无法撤销。', '提示', {
      type: 'warning'
    })
    // 使用 admin 端点
    await request.post('/order/ticket/verify', { orderNo: verifyOrderNo.value.trim() })
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
    await ElMessageBox.confirm('确定要核销这个订单吗？核销后将无法撤销。', '提示', {
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

const handleDetail = async (row: Order) => {
  try {
    const data = await orderApi.getTicketOrderDetail(row.id)
    detailData.value = data
    detailVisible.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '获取详情失败')
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
</style>
