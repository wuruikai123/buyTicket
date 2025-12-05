<template>
  <div class="warning-list">
    <el-card>
      <template #header>
        <span>库存预警</span>
      </template>

      <el-alert
        title="提示：以下库存需要关注，剩余票数不足30%或已售罄"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="exhibitionName" label="展览名称" width="200" />
        <el-table-column prop="ticketDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="150" />
        <el-table-column prop="totalCount" label="总票数" width="100" />
        <el-table-column prop="soldCount" label="已售" width="100" />
        <el-table-column prop="remainingCount" label="剩余" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.remainingCount === 0 ? '#f56c6c' : '#e6a23c' }">
              {{ row.remainingCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '售罄' ? 'danger' : 'warning'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdjust(row)">调整库存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ticketApi } from '@/api/ticket'

const loading = ref(false)
const tableData = ref<any[]>([])

const loadData = async () => {
  loading.value = true
  try {
    const data = await ticketApi.getWarningList()
    tableData.value = data
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdjust = (row: any) => {
  ElMessage.info('调整库存功能待实现')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.warning-list {
}
</style>

