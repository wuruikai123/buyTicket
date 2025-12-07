<template>
  <div class="warning-list">
    <el-card>
      <template #header>
        <span>库存预警</span>
      </template>

      <el-alert
        title="提示：以下库存需要关注，剩余票数不足20%或已售罄"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="exhibitionId" label="展览ID" width="80" />
        <el-table-column prop="ticketDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="150" />
        <el-table-column prop="totalCount" label="总票数" width="100" />
        <el-table-column prop="soldCount" label="已售" width="100" />
        <el-table-column label="剩余" width="100">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.totalCount - row.soldCount === 0, 'text-warning': row.totalCount - row.soldCount > 0 }">
              {{ row.totalCount - row.soldCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.totalCount - row.soldCount === 0 ? 'danger' : 'warning'">
              {{ row.totalCount - row.soldCount === 0 ? '售罄' : '紧张' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdjust(row)">调整库存</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="tableData.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无预警数据，库存充足" />
      </div>
    </el-card>

    <!-- 调整库存对话框 -->
    <el-dialog v-model="adjustDialogVisible" title="调整库存" width="400px">
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="当前总票数">
          <span>{{ adjustForm.totalCount }}</span>
        </el-form-item>
        <el-form-item label="已售票数">
          <span>{{ adjustForm.soldCount }}</span>
        </el-form-item>
        <el-form-item label="新总票数" required>
          <el-input-number v-model="adjustForm.newTotalCount" :min="adjustForm.soldCount" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ticketApi } from '@/api/ticket'

const loading = ref(false)
const tableData = ref<any[]>([])
const adjustDialogVisible = ref(false)

const adjustForm = reactive({
  id: 0,
  totalCount: 0,
  soldCount: 0,
  newTotalCount: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await ticketApi.getWarningList()
    tableData.value = data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdjust = (row: any) => {
  adjustForm.id = row.id
  adjustForm.totalCount = row.totalCount
  adjustForm.soldCount = row.soldCount
  adjustForm.newTotalCount = row.totalCount
  adjustDialogVisible.value = true
}

const handleAdjustSubmit = async () => {
  if (adjustForm.newTotalCount < adjustForm.soldCount) {
    ElMessage.warning('新总票数不能小于已售票数')
    return
  }
  try {
    await ticketApi.updateInventory({
      id: adjustForm.id,
      totalCount: adjustForm.newTotalCount
    })
    ElMessage.success('调整成功')
    adjustDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('调整失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.warning-list {
  .text-danger {
    color: #f56c6c;
    font-weight: bold;
  }
  .text-warning {
    color: #e6a23c;
    font-weight: bold;
  }
  .empty-state {
    padding: 40px 0;
  }
}
</style>

