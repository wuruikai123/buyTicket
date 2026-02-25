<template>
    <div class="home">
        <!-- 轮播图区域 -->
        <div class="banner-section" v-if="banners.length > 0">
            <el-carousel 
                ref="carouselRef"
                class="banner-carousel"
                :interval="5000" 
                arrow="never"
                :autoplay="true"
            >
                <el-carousel-item v-for="banner in banners" :key="banner.id">
                    <div 
                        class="banner-item"
                        :style="{ backgroundImage: `url(${banner.imageUrl})` }"
                        @click="handleBannerClick(banner)"
                        @touchstart="handleTouchStart"
                        @touchmove="handleTouchMove"
                        @touchend="handleTouchEnd"
                    >
                        <div class="banner-overlay"></div>
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

        <!-- 四个快捷跳转，元素之间虚线间隔高度80% -->
        <div class="quick-nav">
            <a class="quick-nav-item" href="/about" @click.prevent="router.push('/about')">
                <img class="quick-nav-icon" :src="quickNavIcons.about" alt="展馆介绍" />
                <span>展馆介绍</span>
            </a>
            <span class="quick-nav-divider" aria-hidden="true" />
            <a class="quick-nav-item" href="/exhibitions" @click.prevent="router.push('/exhibitions')">
                <img class="quick-nav-icon" :src="quickNavIcons.exhibitions" alt="票务预约" />
                <span>票务预约</span>
            </a>
            <span class="quick-nav-divider" aria-hidden="true" />
            <a class="quick-nav-item" :href="guideUrl" target="_blank" rel="noopener noreferrer">
                <img class="quick-nav-icon" :src="quickNavIcons.guide" alt="参观指南" />
                <span>参观指南</span>
            </a>
            <span class="quick-nav-divider" aria-hidden="true" />
            <a class="quick-nav-item" :href="mapUrl" target="_blank" rel="noopener noreferrer">
                <img class="quick-nav-icon" :src="quickNavIcons.location" alt="位置导航" />
                <span>位置导航</span>
            </a>
        </div>

        <!-- 立即预约（展览列表）区域 -->
        <div class="upcoming-exhibitions">
            <div class="section-header">
                <h3 class="section-title">
                    <img class="section-title-icon" :src="sectionDecorIcon" alt="" />
                    立即预约
                </h3>
                <a class="more-link" @click="router.push('/exhibitions')">更多 ></a>
            </div>
            <div class="exhibition-cards">
                <div 
                    v-for="exhibition in displayExhibitions" 
                    :key="exhibition.id" 
                    class="exhibition-card"
                    @click="goToExhibition(exhibition.id)"
                >
                    <div class="card-image">
                        <img 
                            v-if="exhibition.coverImage" 
                            :src="exhibition.coverImage" 
                            :alt="exhibition.name"
                            class="card-image-img"
                        />
                    </div>
                    <p class="card-title">{{ exhibition.name }}</p>
                    <p class="card-price">¥{{ formatPrice(exhibition.price) }}</p>
                </div>
            </div>
        </div>

        <!-- ICP备案号 -->
        <div class="icp-section">
            <a 
                href="https://beian.miit.gov.cn/" 
                target="_blank" 
                rel="noopener noreferrer"
                class="icp-link"
            >
                皖ICP备2025105818号
            </a>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Expand } from '@element-plus/icons-vue';
import request from '@/utils/request';
import { exhibitionApi, type Exhibition } from '@/api/exhibition';

const router = useRouter();

// 轮播图引用
const carouselRef = ref();

// 触摸事件相关
const touchStartX = ref(0);
const touchEndX = ref(0);
const touchStartTime = ref(0);

const mapUrl = 'https://mp.weixin.qq.com/s/tHf7U7Cdqo2qEbU1x5WerA';
const guideUrl = 'https://mp.weixin.qq.com/s/bvW4x1yGWHbNHCmEotEmGQ';

const quickNavIcons = {
    about: '/pageAssets/icon-about.svg',
    exhibitions: '/pageAssets/icon-booking.svg', // 票务预约.svg
    guide: '/pageAssets/icon-guide.svg',
    location: '/pageAssets/icon-location.svg',
};

const sectionDecorIcon = '/pageAssets/icon-section.svg';

// 轮播图数据
interface Banner {
    id: number;
    title: string;
    imageUrl: string;
    linkUrl: string;
    sortOrder: number;
}
const banners = ref<Banner[]>([]);

// 近期展览列表
const upcomingExhibitions = reactive<Exhibition[]>([]);

// 首页只显示两排共6个
const displayExhibitions = computed(() => upcomingExhibitions.slice(0, 6));

function formatPrice(price?: number) {
    if (price == null) return '0.00';
    return Number(price).toFixed(2);
}

// 处理轮播图点击 - 跳转到外部链接
const handleBannerClick = (banner: Banner) => {
    // 如果是滑动操作，不触发点击
    const touchDuration = Date.now() - touchStartTime.value;
    const touchDistance = Math.abs(touchEndX.value - touchStartX.value);
    
    // 如果滑动距离超过30px或时间超过300ms，认为是滑动而非点击
    if (touchDistance > 30 || touchDuration > 300) {
        return;
    }
    
    if (banner.linkUrl) {
        let url = banner.linkUrl;
        // 如果链接没有协议前缀，自动添加 https://
        if (!url.startsWith('http://') && !url.startsWith('https://')) {
            url = 'https://' + url;
        }
        window.open(url, '_blank');
    }
};

// 触摸开始
const handleTouchStart = (e: TouchEvent) => {
    touchStartX.value = e.touches[0].clientX;
    touchStartTime.value = Date.now();
};

// 触摸移动
const handleTouchMove = (e: TouchEvent) => {
    touchEndX.value = e.touches[0].clientX;
};

// 触摸结束
const handleTouchEnd = () => {
    const distance = touchStartX.value - touchEndX.value;
    const minSwipeDistance = 50; // 最小滑动距离
    
    if (Math.abs(distance) > minSwipeDistance && carouselRef.value) {
        if (distance > 0) {
            // 向左滑动，显示下一张
            carouselRef.value.next();
        } else {
            // 向右滑动，显示上一张
            carouselRef.value.prev();
        }
    }
    
    // 重置
    touchStartX.value = 0;
    touchEndX.value = 0;
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
        // 获取近期展览（显示所有）
        const list = await exhibitionApi.getList();
        if (list) {
            // 格式化日期
            const formattedList = list.map(item => ({
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
    background-color: #006bbd;
}

/* 汉堡菜单 */
.menu-icon {
    position: absolute;
    top: 20px;
    right: 20px;
    z-index: 10;
    cursor: pointer;
    color: #fff;
    padding: 8px;
    border-radius: 4px;
}

/* 轮播图区域 ① 宽16:高12 */
/* 已在上面定义，这里删除重复 */

/* ① 宽16:高12 => height = (12/16)*100vw = 75vw */
.banner-carousel :deep(.el-carousel__container),
.banner-carousel :deep(.el-carousel__item) {
    height: 75vw;
}

/* 轮播图区域 ① 宽16:高12 */
.banner-section {
    width: 100%;
    background-color: #006bbd;
    margin: 0;
    padding: 0;
    position: relative;
}

.banner-item {
    width: 100%;
    height: 100%;
    min-height: 200px;
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



/* 无轮播图占位 ① 宽16:高12 */
.banner-placeholder {
    width: 100%;
    height: 75vw;
    min-height: 200px;
    position: relative;
    background: #006bbd;
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

/* 四个快捷跳转 - 白底 */
.quick-nav {
    display: flex;
    justify-content: space-around;
    align-items: center;
    background-color: #ffffff;
    margin: 0 16px 16px;
    padding: 20px 12px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.quick-nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    text-decoration: none;
    color: #333;
    font-size: 13px;
    cursor: pointer;
    transition: opacity 0.2s ease;
    flex: 1;
}

.quick-nav-item:hover {
    opacity: 0.9;
}

.quick-nav-divider {
    width: 0;
    height: 80%;
    border-left: 1px dashed rgba(0, 0, 0, 0.2);
    flex-shrink: 0;
    align-self: center;
}

.quick-nav-icon {
    width: 40px;
    height: 40px;
    object-fit: contain;
}

/* 立即预约（展览列表）区域 ② 宽13:高16 一排三个 两排 底部标题和价格 */
.upcoming-exhibitions {
    flex: 1;
    background-color: #006bbd;
    padding: 40px 20px 20px;
    min-height: 40vh;
}

/* 立即预约标题行 - 白底 */
.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    background-color: #ffffff;
    padding: 14px 16px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
    font-size: 26px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 10px;
}

.section-title-icon {
    width: 28px;
    height: 28px;
    object-fit: contain;
}

.more-link {
    color: #006bbd;
    text-decoration: none;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s ease;
    cursor: pointer;
}

.more-link:hover {
    color: #004a8f;
    transform: translateX(2px);
}

.exhibition-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    max-width: 1200px;
    margin: 0 auto;
}

.exhibition-card {
    background-color: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
    cursor: pointer;
    display: flex;
    flex-direction: column;
}

.exhibition-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

/* ② 图片框比例 宽13:高16 */
.card-image {
    width: 100%;
    aspect-ratio: 13 / 16;
    background-color: #e9ecef;
    background-size: cover;
    background-position: center;
    position: relative;
    overflow: hidden;
}

.card-image-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.3s ease;
}

.exhibition-card:hover .card-image-img {
    transform: scale(1.05);
}

.card-image::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, transparent 60%, rgba(0, 0, 0, 0.08));
}

.card-title {
    font-size: 15px;
    color: #1a1a1a;
    margin: 10px 12px 4px 12px;
    font-weight: 600;
    line-height: 1.35;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    flex: 1;
    min-height: 0;
}

.card-price {
    font-size: 15px;
    color: #e53327;
    font-weight: 600;
    margin: 0 12px 12px 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .quick-nav {
        margin: 0 12px 12px;
        padding: 16px 8px;
    }

    .quick-nav-icon {
        width: 36px;
        height: 36px;
    }

    .quick-nav-item {
        font-size: 12px;
    }

    .banner-section {
        margin-bottom: 8px;
    }



    .upcoming-exhibitions {
        padding: 20px 16px;
    }

    .section-title {
        font-size: 22px;
    }

    .exhibition-cards {
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;
    }

    .card-title {
        font-size: 14px;
        margin: 8px 10px 4px 10px;
    }

    .card-price {
        font-size: 14px;
        margin: 0 10px 10px 10px;
    }
}

@media (max-width: 480px) {
    .quick-nav {
        margin: 0 10px 10px;
        padding: 14px 6px;
    }

    .quick-nav-icon {
        width: 32px;
        height: 32px;
    }

    .quick-nav-item {
        font-size: 11px;
    }



    .upcoming-exhibitions {
        padding: 16px 12px;
    }

    .section-title {
        font-size: 20px;
    }

    .section-title-icon {
        width: 24px;
        height: 24px;
    }

    .exhibition-cards {
        grid-template-columns: repeat(3, 1fr);
        gap: 10px;
    }

    .card-title {
        font-size: 13px;
        margin: 6px 8px 2px 8px;
    }

    .card-price {
        font-size: 13px;
        margin: 0 8px 8px 8px;
    }
}

/* ICP备案号 */
.icp-section {
    background-color: #006bbd;
    padding: 20px 20px 20px 20px;
    text-align: center;
}

.icp-link {
    color: #8dbfe6;
    font-size: 13px;
    text-decoration: none;
    transition: opacity 0.3s;
}

.icp-link:hover {
    opacity: 0.8;
    text-decoration: underline;
}
</style>
