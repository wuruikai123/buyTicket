<template>
  <div class="inventory-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存管理</span>
          <div>
            <el-button type="primary" @click="handleBatchCreate">批量创建</el-button>
            <el-button type="success" @click="handleCreate">单条创建</el-button>
          </div>
        </div>
      </template>

      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="展览">
          <el-select v-model="searchForm.exhibitionId" placeholder="全部" clearable style="width: 200px">
            <el-option
              v-for="item in exhibitions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.ticketDate"
            type="date"
            placeholder="选择日期"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="exhibitionId" label="展览ID" width="80" />
        <el-table-column prop="ticketDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="150" />
        <el-table-column prop="totalCount" label="总票数" width="100" />
        <el-table-column prop="soldCount" label="已售" width="100" />
        <el-table-column label="剩余" width="100">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.totalCount - row.soldCount < 10 }">
              {{ row.totalCount - row.soldCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">{{ getStatusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 单条创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑库存' : '创建库存'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="展览" required>
          <el-select v-model="form.exhibitionId" placeholder="请选择展览" style="width: 100%">
            <el-option v-for="item in exhibitions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" required>
          <el-date-picker v-model="form.ticketDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时间段" required>
          <el-input v-model="form.timeSlot" placeholder="如: 09:00-12:00" />
        </el-form-item>
        <el-form-item label="总票数" required>
          <el-input-number v-model="form.totalCount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="已售票数" v-if="isEdit">
          <el-input-number v-model="form.soldCount" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>


    <!-- 批量创建对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量创建库存" width="500px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="展览" required>
          <el-select v-model="batchForm.exhibitionId" placeholder="请选择展览" style="width: 100%">
            <el-option v-for="item in exhibitions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" required>
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时间段" required>
          <el-checkbox-group v-model="batchForm.timeSlots">
            <el-checkbox label="09:00-12:00">09:00-12:00</el-checkbox>
            <el-checkbox label="12:00-14:00">12:00-14:00</el-checkbox>
            <el-checkbox label="14:00-16:00">14:00-16:00</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="每场票数" required>
          <el-input-number v-model="batchForm.totalCount" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ticketApi } from '@/api/ticket'
import { exhibitionApi } from '@/api/exhibition'

const loading = ref(false)
const tableData = ref<any[]>([])
const exhibitions = ref<any[]>([])
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({
  exhibitionId: undefined as number | undefined,
  ticketDate: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: undefined as number | undefined,
  exhibitionId: undefined as number | undefined,
  ticketDate: '',
  timeSlot: '',
  totalCount: 100,
  soldCount: 0
})

const batchForm = reactive({
  exhibitionId: undefined as number | undefined,
  dateRange: [] as Date[],
  timeSlots: ['09:00-12:00', '12:00-14:00', '14:00-16:00'],
  totalCount: 100
})

const getStatusType = (row: any) => {
  const remaining = row.totalCount - row.soldCount
  if (remaining === 0) return 'danger'
  if (remaining < row.totalCount * 0.2) return 'warning'
  return 'success'
}

const getStatusText = (row: any) => {
  const remaining = row.totalCount - row.soldCount
  if (remaining === 0) return '售罄'
  if (remaining < row.totalCount * 0.2) return '紧张'
  return '充足'
}

const loadExhibitions = async () => {
  try {
    const data = await exhibitionApi.getList({ page: 1, size: 100 })
    exhibitions.value = data.records || []
  } catch (error) {
    console.error('加载展览列表失败', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.exhibitionId) params.exhibitionId = searchForm.exhibitionId
    if (searchForm.ticketDate) {
      params.ticketDate = new Date(searchForm.ticketDate).toISOString().split('T')[0]
    }
    const data = await ticketApi.getInventoryList(params)
    tableData.value = data.records || []
    pagination.total = data.total || 0
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
  searchForm.exhibitionId = undefined
  searchForm.ticketDate = ''
  handleSearch()
}

const handleSizeChange = () => loadData()
const handlePageChange = () => loadData()

const handleCreate = () => {
  isEdit.value = false
  form.id = undefined
  form.exhibitionId = undefined
  form.ticketDate = ''
  form.timeSlot = ''
  form.totalCount = 100
  form.soldCount = 0
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.id = row.id
  form.exhibitionId = row.exhibitionId
  form.ticketDate = row.ticketDate
  form.timeSlot = row.timeSlot
  form.totalCount = row.totalCount
  form.soldCount = row.soldCount
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.exhibitionId || !form.ticketDate || !form.timeSlot) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const data = {
      ...form,
      ticketDate: typeof form.ticketDate === 'string' ? form.ticketDate : new Date(form.ticketDate).toISOString().split('T')[0]
    }
    if (isEdit.value) {
      await ticketApi.updateInventory(data)
      ElMessage.success('更新成功')
    } else {
      await ticketApi.createInventory(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该库存记录吗？', '提示', { type: 'warning' })
    await ticketApi.deleteInventory(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleBatchCreate = () => {
  batchForm.exhibitionId = undefined
  batchForm.dateRange = []
  batchForm.timeSlots = ['09:00-12:00', '12:00-14:00', '14:00-16:00']
  batchForm.totalCount = 100
  batchDialogVisible.value = true
}

const handleBatchSubmit = async () => {
  if (!batchForm.exhibitionId || batchForm.dateRange.length !== 2 || batchForm.timeSlots.length === 0) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const formatDate = (d: Date) => d.toISOString().split('T')[0]
    await ticketApi.batchCreateInventory({
      exhibitionId: batchForm.exhibitionId,
      startDate: formatDate(batchForm.dateRange[0]),
      endDate: formatDate(batchForm.dateRange[1]),
      timeSlots: batchForm.timeSlots,
      totalCount: batchForm.totalCount
    })
    ElMessage.success('批量创建成功')
    batchDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('批量创建失败')
  }
}

onMounted(() => {
  loadExhibitions()
  loadData()
})
</script>

<style scoped lang="scss">
.inventory-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
    font-size: 16px;
  }
  .search-form {
    margin-bottom: 20px;
  }
  .text-danger {
    color: #f56c6c;
    font-weight: bold;
  }
}
</style>
