<template>
  <div class="carousel-management">
    <el-card>
      <template #header>
        <span>轮播图管理</span>
      </template>

      <!-- 首页轮播图部分 -->
      <div class="carousel-section">
        <h3 class="section-title">首页轮播图</h3>
        <div class="carousel-items">
          <!-- 第一个轮播图项（特殊格式） -->
          <div class="carousel-item">
            <div class="image-upload-wrapper">
              <el-upload
                class="carousel-image-uploader"
                action="#"
                :show-file-list="false"
                :before-upload="(file: File) => handleImageUpload(file, 0)"
              >
                <img v-if="carouselItems[0].image" :src="carouselItems[0].image" class="carousel-image" />
                <div v-else class="image-placeholder">
                  <el-icon class="placeholder-icon"><Plus v-if="!carouselItems[0].image" /><Close v-else /></el-icon>
                </div>
              </el-upload>
              <el-icon v-if="carouselItems[0].image" class="remove-icon" @click="handleRemoveImage(0)"><Close /></el-icon>
            </div>
            <div class="form-fields">
              <div class="field-item">
                <label>当前展出</label>
                <el-input v-model="carouselItems[0].exhibitionName" />
              </div>
              <div class="field-item">
                <label>前往购票</label>
                <el-input v-model="carouselItems[0].buttonText" />
              </div>
              <div class="field-item">
                <el-input v-model="carouselItems[0].buttonLink" />
              </div>
            </div>
          </div>

          <!-- 其他4个轮播图项 -->
          <div v-for="(item, index) in carouselItems.slice(1)" :key="index + 1" class="carousel-item">
            <div class="image-upload-wrapper">
              <el-upload
                class="carousel-image-uploader"
                action="#"
                :show-file-list="false"
                :before-upload="(file: File) => handleImageUpload(file, index + 1)"
              >
                <img v-if="item.image" :src="item.image" class="carousel-image" />
                <div v-else class="image-placeholder">
                  <el-icon class="placeholder-icon"><Plus v-if="!item.image" /><Close v-else /></el-icon>
                </div>
              </el-upload>
              <el-icon v-if="item.image" class="remove-icon" @click="handleRemoveImage(index + 1)"><Close /></el-icon>
            </div>
            <div class="form-fields">
              <div class="field-item">
                <label>主标题</label>
                <div class="input-with-limit">
                  <el-input v-model="item.mainTitle" :maxlength="10" show-word-limit />
                  <span class="char-limit">10字以内</span>
                </div>
              </div>
              <div class="field-item field-item-large">
                <label>次标题</label>
                <div class="input-with-limit">
                  <el-input v-model="item.subTitle" :maxlength="30" show-word-limit type="textarea" :rows="2" />
                  <span class="char-limit">30字以内</span>
                </div>
              </div>
              <div class="field-item">
                <label>按钮文字</label>
                <div class="input-with-limit">
                  <el-input v-model="item.buttonText" :maxlength="5" show-word-limit />
                  <span class="char-limit">5字以内</span>
                </div>
              </div>
              <div class="field-item">
                <label>按钮超链接</label>
                <el-input v-model="item.buttonLink" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 关于展厅部分 -->
      <div class="about-section">
        <div class="section-header">
          <h3 class="section-title">关于展厅</h3>
          <span class="char-limit-hint">500字以内</span>
        </div>
        <div class="about-content">
          <div class="text-area-wrapper">
            <el-input
              v-model="aboutContent.description"
              type="textarea"
              :rows="12"
              :maxlength="500"
              show-word-limit
              class="about-textarea"
            />
            <el-button type="primary" class="save-button" @click="handleSave">保存</el-button>
          </div>
          <div class="image-uploads">
            <div class="image-upload-item">
              <label>背景插图</label>
              <div class="image-upload-wrapper-about">
                <el-upload
                  class="about-image-uploader"
                  action="#"
                  :show-file-list="false"
                  :before-upload="(file: File) => handleAboutImageUpload(file, 'background')"
                >
                  <img v-if="aboutContent.backgroundImage" :src="aboutContent.backgroundImage" class="about-image" />
                  <div v-else class="about-image-placeholder">
                    <el-icon class="placeholder-icon"><Plus /></el-icon>
                  </div>
                </el-upload>
                <el-icon v-if="aboutContent.backgroundImage" class="remove-icon" @click="handleRemoveAboutImage('background')"><Close /></el-icon>
              </div>
            </div>
            <div class="image-upload-item">
              <label>页面插图</label>
              <div class="image-upload-wrapper-about">
                <el-upload
                  class="about-image-uploader"
                  action="#"
                  :show-file-list="false"
                  :before-upload="(file: File) => handleAboutImageUpload(file, 'page')"
                >
                  <img v-if="aboutContent.pageImage" :src="aboutContent.pageImage" class="about-image" />
                  <div v-else class="about-image-placeholder">
                    <el-icon class="placeholder-icon"><Plus /></el-icon>
                  </div>
                </el-upload>
                <el-icon v-if="aboutContent.pageImage" class="remove-icon" @click="handleRemoveAboutImage('page')"><Close /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Close } from '@element-plus/icons-vue'

interface CarouselItem {
  image: string
  exhibitionName?: string
  mainTitle?: string
  subTitle?: string
  buttonText: string
  buttonLink: string
}

const carouselItems = ref<CarouselItem[]>([
  {
    image: '',
    exhibitionName: '',
    buttonText: '',
    buttonLink: ''
  },
  {
    image: '',
    mainTitle: '',
    subTitle: '',
    buttonText: '',
    buttonLink: ''
  },
  {
    image: '',
    mainTitle: '',
    subTitle: '',
    buttonText: '',
    buttonLink: ''
  },
  {
    image: '',
    mainTitle: '',
    subTitle: '',
    buttonText: '',
    buttonLink: ''
  },
  {
    image: '',
    mainTitle: '',
    subTitle: '',
    buttonText: '',
    buttonLink: ''
  }
])

const aboutContent = reactive({
  description: '',
  backgroundImage: '',
  pageImage: ''
})

const handleImageUpload = (file: File, index: number): boolean => {
  const reader = new FileReader()
  reader.onload = (e) => {
    carouselItems.value[index].image = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false
}

const handleRemoveImage = (index: number) => {
  carouselItems.value[index].image = ''
}

const handleAboutImageUpload = (file: File, type: 'background' | 'page'): boolean => {
  const reader = new FileReader()
  reader.onload = (e) => {
    if (type === 'background') {
      aboutContent.backgroundImage = e.target?.result as string
    } else {
      aboutContent.pageImage = e.target?.result as string
    }
  }
  reader.readAsDataURL(file)
  return false
}

const handleRemoveAboutImage = (type: 'background' | 'page') => {
  if (type === 'background') {
    aboutContent.backgroundImage = ''
  } else {
    aboutContent.pageImage = ''
  }
}

const handleSave = async () => {
  try {
    // TODO: 调用API保存数据
    // await carouselApi.saveCarousel({ carouselItems: carouselItems.value, aboutContent })
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const loadData = async () => {
  try {
    // TODO: 调用API加载数据
    // const data = await carouselApi.getCarousel()
    // if (data) {
    //   carouselItems.value = data.carouselItems || carouselItems.value
    //   Object.assign(aboutContent, data.aboutContent || {})
    // }
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.carousel-management {
  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
  }

  .carousel-section {
    margin-bottom: 40px;

    .carousel-items {
      display: flex;
      gap: 20px;
      flex-wrap: nowrap;

      .carousel-item {
        flex: 1;
        min-width: 0;
        background: #fff;
        border: 1px solid #e8e8e8;
        border-radius: 8px;
        padding: 16px;
        display: flex;
        flex-direction: column;
        gap: 16px;

        .image-upload-wrapper {
          position: relative;
          width: 100%;

          .carousel-image-uploader {
            width: 100%;

            :deep(.el-upload) {
              width: 100%;
              border: 1px solid #dcdfe6;
              border-radius: 4px;
              cursor: pointer;
              position: relative;
              overflow: hidden;
              background-color: #f5f7fa;
              transition: all 0.3s;

              &:hover {
                border-color: #409eff;
              }
            }

            .carousel-image {
              width: 100%;
              height: 200px;
              object-fit: cover;
              display: block;
            }

            .image-placeholder {
              width: 100%;
              height: 200px;
              display: flex;
              align-items: center;
              justify-content: center;
              background-color: #f5f7fa;

              .placeholder-icon {
                font-size: 32px;
                color: #8c939d;
              }
            }
          }

          .remove-icon {
            position: absolute;
            top: 8px;
            right: 8px;
            font-size: 20px;
            color: #fff;
            background-color: rgba(0, 0, 0, 0.5);
            border-radius: 50%;
            padding: 4px;
            cursor: pointer;
            z-index: 10;

            &:hover {
              background-color: rgba(0, 0, 0, 0.7);
            }
          }
        }

        .form-fields {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .field-item {
            display: flex;
            flex-direction: column;
            gap: 6px;

            label {
              font-size: 14px;
              color: #606266;
              font-weight: 500;
            }

            .input-with-limit {
              display: flex;
              align-items: center;
              gap: 8px;

              :deep(.el-input) {
                flex: 1;
              }

              .char-limit {
                font-size: 12px;
                color: #909399;
                white-space: nowrap;
                padding: 4px 8px;
                background-color: #f5f7fa;
                border-radius: 4px;
              }
            }
          }
        }
      }
    }
  }

  .about-section {
    .section-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;

      .char-limit-hint {
        font-size: 14px;
        color: #909399;
      }
    }

    .about-content {
      display: flex;
      gap: 24px;
      align-items: flex-start;

      .text-area-wrapper {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        min-height: 100%;

        .about-textarea {
          :deep(.el-textarea__inner) {
            height: 462px;
            min-height: 462px;
          }
        }

        .save-button {
          align-self: flex-end;
          min-width: 100px;
        }
      }

      .image-uploads {
        display: flex;
        flex-direction: column;
        gap: 20px;
        width: 300px;
        align-self: flex-start;

        .image-upload-item {
          display: flex;
          flex-direction: column;
          gap: 12px;

          label {
            font-size: 14px;
            color: #606266;
            font-weight: 500;
          }

          .image-upload-wrapper-about {
            position: relative;
            width: 100%;

            .about-image-uploader {
              width: 100%;

              :deep(.el-upload) {
                width: 100%;
                border: 1px solid #dcdfe6;
                border-radius: 4px;
                cursor: pointer;
                position: relative;
                overflow: hidden;
                background-color: #f5f7fa;
                transition: all 0.3s;

                &:hover {
                  border-color: #409eff;
                }
              }

              .about-image {
                width: 100%;
                height: 200px;
                object-fit: cover;
                display: block;
              }

              .about-image-placeholder {
                width: 100%;
                height: 200px;
                display: flex;
                align-items: center;
                justify-content: center;
                background-color: #f5f7fa;

                .placeholder-icon {
                  font-size: 32px;
                  color: #8c939d;
                }
              }
            }

            .remove-icon {
              position: absolute;
              top: 8px;
              right: 8px;
              font-size: 20px;
              color: #fff;
              background-color: rgba(0, 0, 0, 0.5);
              border-radius: 50%;
              padding: 4px;
              cursor: pointer;
              z-index: 10;

              &:hover {
                background-color: rgba(0, 0, 0, 0.7);
              }
            }
          }
        }
      }
    }
  }
}
</style>

