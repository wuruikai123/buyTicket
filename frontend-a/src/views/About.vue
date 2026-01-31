<template>
  <div class="about-page">
    <!-- 背景图片层 -->
    <div class="background-layer" :style="{ backgroundImage: config.backgroundImage ? `url(${config.backgroundImage})` : '' }"></div>
    
    <!-- 深色渐变层 -->
    <div class="overlay-layer"></div>
    
    <!-- 内容层 -->
    <div class="content-layer">
      <!-- 返回按钮 -->
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      
      <!-- 顶部背景图区域（16:9） -->
      <div class="top-banner" :style="{ backgroundImage: config.backgroundImage ? `url(${config.backgroundImage})` : '' }">
        <div class="banner-overlay"></div>
        <h1 class="banner-title">{{ config.title || '关于展厅' }}</h1>
      </div>
      
      <!-- 正文区域 -->
      <div class="content-section">
        <p class="content-text">{{ config.content }}</p>
      </div>
      
      <!-- 介绍图片区域 -->
      <div class="intro-images">
        <div v-if="config.introImage1" class="intro-image" :style="{ backgroundImage: `url(${config.introImage1})` }"></div>
        <div v-if="config.introImage2" class="intro-image" :style="{ backgroundImage: `url(${config.introImage2})` }"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

interface AboutConfig {
  title: string
  content: string
  backgroundImage: string
  introImage1: string
  introImage2: string
}

const config = ref<AboutConfig>({
  title: '关于展厅',
  content: '',
  backgroundImage: '',
  introImage1: '',
  introImage2: ''
})

const goBack = () => {
  router.back()
}

const loadConfig = async () => {
  try {
    const data: any = await request.get('/about/config')
    if (data) {
      config.value = data
    }
  } catch (error) {
    console.error('加载关于展厅配置失败', error)
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.about-page {
  position: relative;
  min-height: 100vh;
  background-color: #2c201d;
  overflow-x: hidden;
}

/* 背景图片层（不显示，仅用于数据） */
.background-layer {
  display: none;
}

/* 深色渐变层（不需要了） */
.overlay-layer {
  display: none;
}

/* 内容层 */
.content-layer {
  position: relative;
  z-index: 3;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.back-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: none;
  color: #ffffff;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 10;
}

.back-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

/* 顶部背景图区域（16:9比例） */
.top-banner {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(44, 32, 29, 0) 0%,
    rgba(44, 32, 29, 0.3) 50%,
    rgba(44, 32, 29, 0.7) 100%
  );
}

.banner-title {
  position: relative;
  z-index: 2;
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  text-align: center;
  letter-spacing: 3px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

/* 正文区域 */
.content-section {
  background-color: #2c201d;
  padding: 30px 20px;
}

.content-text {
  font-size: 15px;
  line-height: 1.9;
  color: #ffffff;
  margin: 0;
  text-align: justify;
  white-space: pre-wrap;
  letter-spacing: 0.5px;
}

/* 介绍图片区域 */
.intro-images {
  background-color: #2c201d;
  padding: 0 20px 30px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.intro-image {
  width: 100%;
  aspect-ratio: 16 / 9;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  border-radius: 0;
}

@media (max-width: 768px) {
  .banner-title {
    font-size: 26px;
  }
  
  .content-section {
    padding: 25px 16px;
  }
  
  .content-text {
    font-size: 14px;
    line-height: 1.8;
  }
  
  .intro-images {
    padding: 0 16px 25px 16px;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .banner-title {
    font-size: 22px;
    letter-spacing: 2px;
  }
  
  .content-section {
    padding: 20px 14px;
  }
  
  .content-text {
    font-size: 13px;
  }
}
</style>
