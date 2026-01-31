<template>
    <div class="home">
        <div class="menu-icon" @click="goToAbout">
            <el-icon size="30">
                <Expand />
            </el-icon>
        </div>

        <!-- 轮播图区域 -->
        <div class="banner-section" v-if="banners.length > 0">
            <el-carousel 
                height="500px" 
                :interval="5000" 
                arrow="hover"
                :autoplay="true"
            >
                <el-carousel-item v-for="banner in banners" :key="banner.id">
                    <div 
                        class="banner-item"
                        :style="{ backgroundImage: `url(${banner.coverImage})` }"
                        @click="handleBannerClick(banner)"
                    >
                        <div class="banner-overlay"></div>
                        <div class="banner-content">
                            <p class="banner-subtitle">{{ banner.title }}</p>
                            <button class="banner-button">
                                <span>立即预约</span>
                                <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M5 12h14M12 5l7 7-7 7"/>
                                </svg>
                            </button>
                        </div>
                    </div>
                </el-carousel-item>
            </el-carousel>
        </div>
        
        <!-- 无轮播图时的占位 -->
        <div class="banner-placeholder" v-else>
            <div class="placeholder-content">
                <h2>欢迎来到展览购票系统</h2>
                <p>精彩展览即将呈现</p>
            </div>
        </div>

        <!-- 近期展览区域 -->
        <div class="upcoming-exhibitions">
            <div class="section-header">
                <h3 class="section-title">近期展览</h3>
                <a class="more-link" @click="router.push('/exhibitions')">更多 ></a>
            </div>
            <div class="exhibition-cards">
                <div 
                    v-for="exhibition in upcomingExhibitions" 
                    :key="exhibition.id" 
                    class="exhibition-card"
                    @click="goToExhibition(exhibition.id)"
                >
                    <div 
                        class="card-image"
                        :style="exhibition.coverImage ? { backgroundImage: `url(${exhibition.coverImage})` } : {}"
                    ></div>
                    <p class="card-title">{{ exhibition.name }}</p>
                    <p class="card-date">{{ exhibition.date }}</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Expand } from '@element-plus/icons-vue';
import request from '@/utils/request';
import { exhibitionApi, type Exhibition } from '@/api/exhibition';

const router = useRouter();

// 轮播图数据
interface Banner {
    id: number;
    title: string;
    exhibitionId: number;
    exhibitionName: string;
    coverImage: string;
    sortOrder: number;
}
const banners = ref<Banner[]>([]);

// 近期展览列表
const upcomingExhibitions = reactive<Exhibition[]>([]);

// 处理轮播图点击 - 跳转到展览详情
const handleBannerClick = (banner: Banner) => {
    router.push(`/ticket/${banner.exhibitionId}`);
};

// 获取数据
onMounted(async () => {
    // 获取轮播图
    try {
        const data: any = await request.get('/banner/list');
        banners.value = (data?.data ?? data ?? []) as Banner[];
    } catch (e) {
        console.error('获取轮播图失败', e);
    }
    
    try {
        // 获取近期展览 (只显示前2个)
        const list = await exhibitionApi.getList();
        if (list) {
            // 格式化日期，只取前2个展示在首页
            const formattedList = list.slice(0, 2).map(item => ({
                ...item,
                date: item.startDate ? item.startDate : '待定'
            }));
            upcomingExhibitions.push(...formattedList);
        }
    } catch (error) {
        console.error('获取展览数据失败', error);
    }
});

const goToExhibition = (exhibitionId: number) => {
    router.push(`/ticket/${exhibitionId}`)
}

const goToAbout = () => {
    router.push('/about')
}
</script>

<style scoped>
.home {
    position: relative;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f5f5f5;
}

/* 汉堡菜单 */
.menu-icon {
    position: absolute;
    top: 20px;
    right: 20px;
    z-index: 10;
    cursor: pointer;
    color: #333;
    padding: 8px;
    border-radius: 4px;
}

/* 轮播图区域 */
.banner-section {
    width: 100%;
    background-color: #fff;
    margin: 0;
    padding: 0;
    position: relative;
}

/* 自定义轮播图箭头样式 */
.banner-section :deep(.el-carousel__arrow) {
    background-color: rgba(255, 255, 255, 0.3);
    backdrop-filter: blur(10px);
    width: 50px;
    height: 50px;
    border-radius: 50%;
    transition: all 0.3s ease;
}

.banner-section :deep(.el-carousel__arrow:hover) {
    background-color: rgba(255, 255, 255, 0.5);
    transform: scale(1.1);
}

.banner-section :deep(.el-carousel__arrow--left) {
    left: 20px;
}

.banner-section :deep(.el-carousel__arrow--right) {
    right: 20px;
}

.banner-item {
    width: 100%;
    height: 500px;
    background-size: cover;
    background-position: center;
    position: relative;
    cursor: pointer;
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
        rgba(0, 0, 0, 0) 0%,
        rgba(0, 0, 0, 0.15) 70%,
        rgba(0, 0, 0, 0.4) 100%
    );
    z-index: 1;
    pointer-events: none;
}

.banner-content {
    position: absolute;
    bottom: 60px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 2;
    text-align: center;
    color: white;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 0 20px;
    width: 100%;
    max-width: 600px;
}

.banner-subtitle {
    font-size: 20px;
    font-weight: 500;
    margin: 0;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    letter-spacing: 0.5px;
}

.banner-button {
    display: flex;
    align-items: center;
    gap: 10px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    padding: 14px 36px;
    border-radius: 50px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.banner-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.banner-button:active {
    transform: translateY(0);
}

.arrow-icon {
    width: 18px;
    height: 18px;
    transition: transform 0.3s ease;
}

.banner-button:hover .arrow-icon {
    transform: translateX(3px);
}

/* 无轮播图占位 */
.banner-placeholder {
    width: 100%;
    height: 400px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
}

.placeholder-content {
    text-align: center;
    color: white;
}

.placeholder-content h2 {
    font-size: 36px;
    font-weight: bold;
    margin: 0 0 16px 0;
}

.placeholder-content p {
    font-size: 18px;
    margin: 0;
    opacity: 0.9;
}

/* 近期展览区域 */
.upcoming-exhibitions {
    flex: 1;
    background-color: #f8f9fa;
    padding: 40px 20px;
    min-height: 40vh;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.section-title {
    font-size: 26px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0;
    position: relative;
    padding-left: 16px;
}

.section-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 2px;
}

.more-link {
    color: #667eea;
    text-decoration: none;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s ease;
    cursor: pointer;
}

.more-link:hover {
    color: #764ba2;
    transform: translateX(2px);
}

.exhibition-cards {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
}

.exhibition-card {
    background-color: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    cursor: pointer;
}

.exhibition-card:hover {
    transform: translateY(-6px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-image {
    width: 100%;
    height: 200px;
    background-color: #e9ecef;
    background-size: cover;
    background-position: center;
    position: relative;
    overflow: hidden;
}

.card-image::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, transparent 60%, rgba(0, 0, 0, 0.1));
}

.exhibition-card:hover .card-image {
    transform: scale(1.05);
}

.card-title {
    font-size: 17px;
    color: #1a1a1a;
    margin: 16px 16px 8px 16px;
    font-weight: 600;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.card-date {
    font-size: 14px;
    color: #6c757d;
    margin: 0 16px 16px 16px;
    display: flex;
    align-items: center;
    gap: 6px;
}

.card-date::before {
    content: '📅';
    font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    /* 压缩轮播高度，减少与下一块的留白 */
    .banner-section {
        margin-bottom: 8px;
    }
    .banner-section :deep(.el-carousel__container) {
        height: 360px;
    }
    .banner-item,
    .banner-placeholder {
        height: 360px;
    }

    .banner-content {
        bottom: 32px;
        gap: 12px;
    }

    .banner-subtitle {
        font-size: 18px;
    }

    .banner-button {
        padding: 12px 26px;
        font-size: 15px;
    }

    .banner-section :deep(.el-carousel__arrow) {
        width: 40px;
        height: 40px;
    }

    .upcoming-exhibitions {
        padding: 20px 16px;
    }

    .section-title {
        font-size: 22px;
    }

    .exhibition-cards {
        gap: 16px;
    }

    .card-image {
        height: 160px;
    }

    .card-title {
        font-size: 16px;
        margin: 14px 14px 8px 14px;
    }

    .card-date {
        font-size: 13px;
        margin: 0 14px 14px 14px;
    }
}

@media (max-width: 480px) {
    /* 更小屏幕进一步压缩轮播高度与内边距 */
    .banner-section :deep(.el-carousel__container) {
        height: 460px;
    }
    .banner-item,
    .banner-placeholder {
        height: 460px;
    }

    .banner-content {
        bottom: 28px;
    }

    .banner-subtitle {
        font-size: 16px;
    }

    .banner-button {
        padding: 10px 22px;
        font-size: 14px;
    }

    .arrow-icon {
        width: 16px;
        height: 16px;
    }

    .banner-section :deep(.el-carousel__arrow) {
        width: 36px;
        height: 36px;
    }

    .banner-section :deep(.el-carousel__arrow--left) {
        left: 10px;
    }

    .banner-section :deep(.el-carousel__arrow--right) {
        right: 10px;
    }

    .upcoming-exhibitions {
        padding: 28px 16px;
    }

    .section-title {
        font-size: 20px;
        padding-left: 12px;
    }

    .section-title::before {
        width: 3px;
        height: 20px;
    }

    .exhibition-cards {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .card-image {
        height: 180px;
    }
}
</style>
