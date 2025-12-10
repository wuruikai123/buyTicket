<template>
  <div class="exhibition-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑展览信息' : '创建展览信息' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="exhibition-form-content"
      >
        <div class="form-layout">
          <!-- 左侧：主图上传 -->
          <div class="main-image-section">
            <el-form-item prop="coverImage">
              <el-upload
                class="main-image-uploader"
                action="#"
                :show-file-list="false"
                :before-upload="beforeUpload"
              >
                <img v-if="form.coverImage" :src="form.coverImage" class="main-image" />
                <div v-else class="main-image-placeholder">
                  <el-icon class="upload-icon"><Plus /></el-icon>
                </div>
              </el-upload>
            </el-form-item>
            
            <!-- 门票价格 -->
            <el-form-item label="门票价格" prop="price" class="price-item">
              <div class="price-input-wrapper">
                <el-input-number
                  v-model="form.price"
                  :min="0"
                  :precision="2"
                  class="price-input"
                />
                <span class="price-unit">元/人次</span>
              </div>
            </el-form-item>
          </div>

          <!-- 右侧：表单字段 -->
          <div class="form-fields-section">
            <el-form-item label="展览名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入展览名称" />
            </el-form-item>
            
            <div class="date-range-item">
              <el-form-item prop="startDate" class="date-field">
                <template #label>
                  <span>开始时间</span>
                </template>
                <el-date-picker
                  v-model="form.startDate"
                  type="date"
                  placeholder="开始时间"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item prop="endDate" class="date-field">
                <template #label>
                  <span>结束时间</span>
                </template>
                <el-date-picker
                  v-model="form.endDate"
                  type="date"
                  placeholder="结束时间"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
            
            <el-form-item label="展览副标题" prop="shortDesc">
              <el-input
                v-model="form.shortDesc"
                placeholder="请输入展览副标题"
              />
            </el-form-item>
            
            <el-form-item label="展览介绍" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="8"
                placeholder="请输入展览介绍"
              />
            </el-form-item>
          </div>
        </div>

        <!-- 介绍插图 -->
        <el-form-item label="介绍插图" class="detail-images-section">
          <div class="detail-images-list">
            <el-upload
              v-for="(item, index) in detailImagesList"
              :key="index"
              class="detail-image-item"
              action="#"
              :show-file-list="false"
              :before-upload="(file: File) => handleDetailImageUpload(file, index)"
            >
              <img v-if="item.url" :src="item.url" class="detail-image" />
              <div v-else class="detail-image-placeholder">
                <el-icon v-if="index < 2" class="add-icon"><Plus /></el-icon>
                <el-icon v-else class="remove-icon" @click.stop="handleRemoveDetailImage(index)"><Close /></el-icon>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        
        <!-- 保存按钮 -->
        <el-form-item class="submit-section">
          <el-button 
            type="default" 
            @click="handleSubmit" 
            :loading="loading"
            class="save-button"
          >
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Close } from '@element-plus/icons-vue'
import { exhibitionApi } from '@/api/exhibition'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const detailImagesList = ref<Array<{ url: string }>>([
  { url: '' },
  { url: '' },
  { url: '' },
  { url: '' },
  { url: '' },
  { url: '' }
])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  name: '',
  shortDesc: '',
  description: '',
  startDate: '',
  endDate: '',
  price: 0,
  coverImage: '',
  tags: [] as string[],
  status: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入展览名称', trigger: 'blur' }],
  shortDesc: [{ required: true, message: '请输入展览副标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入展览介绍', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  price: [{ required: true, message: '请输入门票价格', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请上传主图', trigger: 'change' }]
}

const beforeUpload = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.coverImage = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false
}

const handleDetailImageUpload = (file: File, index: number): boolean => {
  const reader = new FileReader()
  reader.onload = (e) => {
    detailImagesList.value[index].url = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false
}

const handleRemoveDetailImage = (index: number) => {
  detailImagesList.value[index].url = ''
}

const loadData = async () => {
  if (!isEdit.value) return
  
  try {
    const data: any = await exhibitionApi.getDetail(Number(route.params.id))
    
    // 处理 tags: 字符串转数组
    let tagsArray: string[] = []
    if (data.tags) {
        tagsArray = typeof data.tags === 'string' ? data.tags.split(',') : data.tags
    }

    Object.assign(form, {
      ...data,
      startDate: data.startDate,
      endDate: data.endDate,
      tags: tagsArray
    })
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 转换 tags 数组为字符串，以匹配后端实体类类型
        const submitData = {
          ...form,
          tags: Array.isArray(form.tags) ? form.tags.join(',') : form.tags
        }

        if (isEdit.value) {
          await exhibitionApi.update({ id: Number(route.params.id), ...submitData })
        } else {
          await exhibitionApi.create(submitData)
        }
        ElMessage.success('保存成功')
        router.push('/exhibition/list')
      } catch (error) {
        console.error(error)
        ElMessage.error('保存失败')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.exhibition-form {
  .exhibition-form-content {
    .form-layout {
      display: flex;
      gap: 30px;
      margin-bottom: 30px;

      .main-image-section {
        flex-shrink: 0;
        width: 300px;

        .main-image-uploader {
          :deep(.el-upload) {
            width: 100%;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: all 0.3s;
            background-color: #f5f7fa;

            &:hover {
              border-color: #409eff;
            }
          }

          .main-image {
            width: 200px;
            height: 400px;
            object-fit: cover;
            display: block;
          }

          .main-image-placeholder {
            width: 200px;
            height: 400px;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f5f7fa;

            .upload-icon {
              font-size: 48px;
              color: #8c939d;
            }
          }
        }

        .price-item {
          margin-top: 20px;
          margin-bottom: 0;

          :deep(.el-form-item__label) {
            font-weight: 500;
          }

          .price-input-wrapper {
            display: flex;
            align-items: center;
            gap: 8px;

            .price-input {
              flex: 1;
            }

            .price-unit {
              font-size: 14px;
              color: #606266;
              white-space: nowrap;
            }
          }
        }
      }

      .form-fields-section {
        flex: 1;
        min-width: 0;

        .date-range-item {
          display: flex;
          gap: 16px;
          margin-bottom: 22px;

          .date-field {
            flex: 1;
            margin-bottom: 0;
          }
        }
      }
    }

    .detail-images-section {
      margin-top: 30px;

      :deep(.el-form-item__label) {
        font-weight: 500;
      }

      .detail-images-list {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;

        .detail-image-item {
          :deep(.el-upload) {
            width: 120px;
            height: 120px;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: all 0.3s;
            background-color: #f5f7fa;

            &:hover {
              border-color: #409eff;
            }
          }

          .detail-image {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
          }

          .detail-image-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f5f7fa;

            .add-icon {
              font-size: 32px;
              color: #8c939d;
            }

            .remove-icon {
              font-size: 32px;
              color: #8c939d;
              cursor: pointer;

              &:hover {
                color: #f56c6c;
              }
            }
          }
        }
      }
    }

    .submit-section {
      margin-top: 30px;
      margin-bottom: 0;
      text-align: center;

      :deep(.el-form-item__content) {
        margin-left: 0 !important;
      }

      .save-button {
        min-width: 120px;
        height: 40px;
        background-color: #e4e7ed;
        border-color: #dcdfe6;
        color: #303133;
        font-size: 14px;

        &:hover {
          background-color: #d3d4d6;
          border-color: #c0c4cc;
          color: #303133;
        }
      }
    }
  }
}
</style>

