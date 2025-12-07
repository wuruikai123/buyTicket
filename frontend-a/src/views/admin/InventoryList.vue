<template>
  <div class="inventory-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存管理</span>
          <el-button type="primary" @click="$router.push('/admin/inventory/batch')">
            <el-icon><Plus /></el-icon>
            批量创建
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="展览">
          <el-select v-model="searchForm.exhibitionId" placeholder="全部展览" clearable filterable>
            <el-option
              v-for="item in exhibitions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="exhibitionName" label="展览名称" min-width="180" />
        <el-table-column prop="ticketDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="120" />
        <el-table-column prop="totalCount" label="总票数" width="100" />
        <el-table-column prop="soldCount" label="已售" width="80">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.soldCount >= row.totalCount }">
              {{ row.soldCount || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="剩余" width="80">
          <template #default="{ row }">
            <span :class="getRemainClass(row)">
              {{ row.totalCount - (row.soldCount || 0) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">{{ getStatusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">调整</el-button>
            <el-popconfirm title="确定删除该库存吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="调整库存" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="展览">
          <el-input :value="editForm.exhibitionName" disabled />
        </el-form-item>
        <el-form-item label="日期">
          <el-input :value="editForm.ticketDate" disabled />
        </el-form-item>
        <el-form-item label="时间段">
          <el-input :value="editForm.timeSlot" disabled />
        </el-form-item>
        <el-form-item label="总票数">
          <el-input-number v-model="editForm.totalCount" :min="editForm.soldCount || 0" />
          <span style="margin-left: 8px; color: #909399">
            已售 {{ editForm.soldCount || 0 }} 张，不能低于已售数量
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminExhibitionApi, adminInventoryApi, type AdminExhibition, type TicketInventory } from '@/api/admin'

const route = useRoute()
const loading = ref(false)
const tableData = ref<TicketInventory[]>([])
const exhibitions = ref<AdminExhibition[]>([])

const searchForm = reactive({
  exhibitionId: undefined as number | undefined,
  dateRange: [] as string[]
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 编辑相关
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive<TicketInventory>({
  id: 0,
  exhibitionId: 0,
  exhibitionName: '',
  ticketDate: '',
  timeSlot: '',
  totalCount: 0,
  soldCount: 0
})

// 获取剩余票数样式
const getRemainClass = (row: TicketInventory) => {
  const remain = row.totalCount - (row.soldCount || 0)
  if (remain <= 0) return 'text-danger'
  if (remain < row.totalCount * 0.1) return 'text-warning'
  return 'text-success'
}

// 获取状态类型
const getStatusType = (row: TicketInventory) => {
  const remain = row.totalCount - (row.soldCount || 0)
  if (remain <= 0) return 'danger'
  if (remain < row.totalCount * 0.1) return 'warning'
  return 'success'
}

// 获取状态文本
const getStatusText = (row: TicketInventory) => {
  const remain = row.totalCount - (row.soldCount || 0)
  if (remain <= 0) return '售罄'
  if (remain < row.totalCount * 0.1) return '紧张'
  return '充足'
}

// 加载展览列表
const loadExhibitions = async () => {
  try {
    const res = await adminExhibitionApi.getList({ size: 100 })
    exhibitions.value = res.records
  } catch (e) {
    console.error(e)
  }
}

// 加载库存数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminInventoryApi.getList({
      page: pagination.page,
      size: pagination.size,
      exhibitionId: searchForm.exhibitionId,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    })
    tableData.value = res.records
    pagination.total = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.exhibitionId = undefined
  searchForm.dateRange = []
  handleSearch()
}

const handleEdit = (row: TicketInventory) => {
  Object.assign(editForm, row)
  editVisible.value = true
}

const handleSaveEdit = async () => {
  editLoading.value = true
  try {
    await adminInventoryApi.update({
      id: editForm.id,
      exhibitionId: editForm.exhibitionId,
      ticketDate: editForm.ticketDate,
      timeSlot: editForm.timeSlot,
      totalCount: editForm.totalCount
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  } finally {
    editLoading.value = false
  }
}

const handleDelete = async (row: TicketInventory) => {
  try {
    await adminInventoryApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(async () => {
  await loadExhibitions()
  // 从URL参数获取展览ID
  if (route.query.exhibitionId) {
    searchForm.exhibitionId = Number(route.query.exhibitionId)
  }
  fetchData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
.text-danger { color: #f56c6c; }
.text-warning { color: #e6a23c; }
.text-success { color: #67c23a; }
</style>
