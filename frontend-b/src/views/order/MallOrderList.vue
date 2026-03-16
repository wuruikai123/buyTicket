<template>
  <div class="mall-order-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商城订单</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="undefined" />
            <el-option label="待支付" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="订单ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">待支付</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning">待发货</el-tag>
            <el-tag v-else-if="row.status === 2" type="primary">已发货</el-tag>
            <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
            <el-tag v-else-if="row.status === 4" type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="primary"
              @click="handleShip(row)"
            >发货</el-button>
            <el-button
              v-if="row.status === 0"
              link
              type="danger"
              @click="handleCancel(row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag v-if="detailData.status === 0" type="info">待支付</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="warning">待发货</el-tag>
          <el-tag v-else-if="detailData.status === 2" type="primary">已发货</el-tag>
          <el-tag v-else-if="detailData.status === 3" type="success">已完成</el-tag>
          <el-tag v-else-if="detailData.status === 4" type="danger">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ detailData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'

const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchForm = reactive({ status: undefined as number | undefined })
const detailVisible = ref(false)
const detailData = ref<any>({})

const fetchData = async () => {
  try {
    const res = await orderApi.getMallOrderList({
      page: currentPage.value,
      size: pageSize.value,
      status: searchForm.status
    })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '获取数据失败')
  }
}

const handleDetail = async (row: any) => {
  try {
    const data = await orderApi.getMallOrderDetail(row.id)
    detailData.value = data
    detailVisible.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '获取详情失败')
  }
}

const handleShip = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入物流单号', '发货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '物流单号'
    })
    await orderApi.shipMallOrder(row.id, {
      logisticsCompany: '其他',
      logisticsNo: value || ''
    })
    ElMessage.success('发货成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '发货失败')
  }
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消订单吗？', '提示', { type: 'warning' })
    await orderApi.cancelMallOrder(row.id)
    ElMessage.success('取消成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '取消失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.mall-order-list {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
