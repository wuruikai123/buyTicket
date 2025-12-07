<template>
  <div class="inventory-batch">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>批量创建库存</span>
          <el-button @click="$router.push('/admin/inventory')">返回列表</el-button>
        </div>
      </template>

      <el-alert
        title="批量创建说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        选择展览、日期范围和时间段后，系统将自动为每个日期的每个时间段创建库存记录。
      </el-alert>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 700px"
      >
        <el-form-item label="选择展览" prop="exhibitionId">
          <el-select
            v-model="form.exhibitionId"
            placeholder="请选择展览"
            filterable
            style="width: 100%"
            @change="handleExhibitionChange"
          >
            <el-option
              v-for="item in exhibitions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="时间段" prop="timeSlots">
          <div class="time-slots">
            <el-checkbox-group v-model="form.timeSlots">
              <el-checkbox v-for="slot in defaultTimeSlots" :key="slot" :value="slot">
                {{ slot }}
              </el-checkbox>
            </el-checkbox-group>
            <div class="custom-slot">
              <el-input
                v-model="customSlot"
                placeholder="自定义时间段，如 18:00-20:00"
                style="width: 200px"
              />
              <el-button type="primary" link @click="addCustomSlot">添加</el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="每场票数" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" :max="9999" />
          <span style="margin-left: 8px">张</span>
        </el-form-item>

        <!-- 预览 -->
        <el-form-item label="创建预览">
          <div class="preview-info">
            <p>将创建 <strong>{{ previewCount }}</strong> 条库存记录</p>
            <p v-if="selectedExhibition">
              展览：{{ selectedExhibition.name }}
            </p>
            <p v-if="form.dateRange?.length === 2">
              日期：{{ form.dateRange[0] }} 至 {{ form.dateRange[1] }}（共 {{ dayCount }} 天）
            </p>
            <p v-if="form.timeSlots.length">
              时间段：{{ form.timeSlots.join('、') }}
            </p>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="!previewCount">
            确认创建
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 创建结果 -->
    <el-dialog v-model="resultVisible" title="创建结果" width="500px">
      <el-result
        :icon="resultSuccess ? 'success' : 'error'"
        :title="resultSuccess ? '批量创建成功' : '创建失败'"
        :sub-title="resultMessage"
      >
        <template #extra>
          <el-button type="primary" @click="goToList">查看库存列表</el-button>
          <el-button @click="resultVisible = false">继续创建</el-button>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { adminExhibitionApi, adminInventoryApi, type AdminExhibition } from '@/api/admin'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const resultVisible = ref(false)
const resultSuccess = ref(false)
const resultMessage = ref('')

const exhibitions = ref<AdminExhibition[]>([])
const selectedExhibition = ref<AdminExhibition | null>(null)
const customSlot = ref('')

const defaultTimeSlots = [
  '09:00-11:00',
  '11:00-13:00',
  '13:00-15:00',
  '15:00-17:00',
  '17:00-19:00'
]

const form = reactive({
  exhibitionId: undefined as number | undefined,
  dateRange: [] as string[],
  timeSlots: [] as string[],
  totalCount: 100
})

const rules: FormRules = {
  exhibitionId: [{ required: true, message: '请选择展览', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  timeSlots: [{ required: true, message: '请选择至少一个时间段', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入每场票数', trigger: 'blur' }]
}

// 计算天数
const dayCount = computed(() => {
  if (form.dateRange?.length !== 2) return 0
  const start = new Date(form.dateRange[0])
  const end = new Date(form.dateRange[1])
  return Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
})

// 预览数量
const previewCount = computed(() => {
  return dayCount.value * form.timeSlots.length
})

// 禁用日期（展览日期范围外）
const disabledDate = (date: Date) => {
  if (!selectedExhibition.value) return false
  const start = new Date(selectedExhibition.value.startDate)
  const end = new Date(selectedExhibition.value.endDate)
  start.setHours(0, 0, 0, 0)
  end.setHours(23, 59, 59, 999)
  return date < start || date > end
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

// 展览变更
const handleExhibitionChange = (id: number) => {
  selectedExhibition.value = exhibitions.value.find(e => e.id === id) || null
  // 自动设置日期范围为展览时间
  if (selectedExhibition.value) {
    form.dateRange = [selectedExhibition.value.startDate, selectedExhibition.value.endDate]
  }
}

// 添加自定义时间段
const addCustomSlot = () => {
  const pattern = /^\d{2}:\d{2}-\d{2}:\d{2}$/
  if (!pattern.test(customSlot.value)) {
    ElMessage.warning('时间段格式不正确，请使用 HH:mm-HH:mm 格式')
    return
  }
  if (form.timeSlots.includes(customSlot.value)) {
    ElMessage.warning('该时间段已存在')
    return
  }
  form.timeSlots.push(customSlot.value)
  customSlot.value = ''
}

// 提交
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await adminInventoryApi.batchCreate({
      exhibitionId: form.exhibitionId!,
      startDate: form.dateRange[0],
      endDate: form.dateRange[1],
      timeSlots: form.timeSlots,
      totalCount: form.totalCount
    })
    resultSuccess.value = true
    resultMessage.value = `成功创建 ${previewCount.value} 条库存记录`
    resultVisible.value = true
  } catch (e: any) {
    resultSuccess.value = false
    resultMessage.value = e.message || '创建失败，请重试'
    resultVisible.value = true
  } finally {
    submitting.value = false
  }
}

// 重置
const handleReset = () => {
  formRef.value?.resetFields()
  selectedExhibition.value = null
}

// 跳转列表
const goToList = () => {
  router.push(`/admin/inventory?exhibitionId=${form.exhibitionId}`)
}

onMounted(loadExhibitions)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time-slots {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.time-slots :deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.custom-slot {
  display: flex;
  align-items: center;
  gap: 8px;
}
.preview-info {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  line-height: 1.8;
}
.preview-info strong {
  color: #409eff;
  font-size: 18px;
}
</style>
