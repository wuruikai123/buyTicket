<template>
    <div class="ticket-purchase">
        <!-- 顶部导航栏 -->
        <div class="header">
            <button class="back-button" @click="goBack">
                <el-icon><ArrowLeft /></el-icon>
            </button>
        </div>

        <!-- 展览信息区域（左右布局） -->
        <div class="exhibition-header">
            <div class="exhibition-image">
                <template v-if="exhibition.coverImage">
                    <img class="exhibition-img" :src="exhibition.coverImage" alt="展览封面" />
                </template>
                <span v-else class="no-image">暂无图片</span>
            </div>
            <div class="exhibition-basic-info">
                <h2 class="exhibition-name">{{ exhibition.name }}</h2>
                <p class="exhibition-short-desc">{{ exhibition.shortDescription }}</p>
                <div class="exhibition-tags" v-if="exhibition.tags?.length">
                    <span class="tag" v-for="tag in parseTags(exhibition.tags)" :key="tag">{{ tag }}</span>
                </div>
                <div class="exhibition-time">
                    <span class="time-label">展览时间:</span>
                    <span class="time-value">{{ exhibition.dateRange }}</span>
                </div>
            </div>
        </div>

        <!-- 展览详情区域 -->
        <div class="exhibition-details">
            <h3 class="details-title">展览详情</h3>
            <p class="details-description">{{ exhibition.description }}</p>
            <div class="details-content-blocks" v-if="exhibition.images?.length">
                <div 
                    v-for="(img, index) in exhibition.images" 
                    :key="index" 
                    class="content-block"
                    :style="{ backgroundImage: `url(${img})` }"
                ></div>
            </div>
            <div class="details-content-blocks" v-else>
                <div class="content-block placeholder">
                    <span>暂无详情图片</span>
                </div>
            </div>
        </div>

        <!-- 底部固定购票栏 -->
        <div class="bottom-purchase-bar">
            <div class="price-info">
                <span class="price-text">¥ {{ exhibition.price }}元 / 人次</span>
            </div>
            <button class="purchase-button" @click="handlePurchase">购票</button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
import { exhibitionApi, type Exhibition } from '@/api/exhibition';

const router = useRouter();
const route = useRoute();

// 展览数据
interface ExhibitionDetail extends Exhibition {
    shortDescription?: string;
    dateRange?: string;
    images?: string[];
}

const exhibition = ref<ExhibitionDetail>({
    id: 0,
    name: '',
    description: '',
    price: 0
} as ExhibitionDetail);

// 解析标签
const parseTags = (tags: string | string[] | undefined): string[] => {
    if (!tags) return [];
    if (Array.isArray(tags)) return tags;
    try {
        return JSON.parse(tags);
    } catch {
        return tags.split(',').map(t => t.trim()).filter(Boolean);
    }
};

// 返回上一页
const goBack = () => {
    router.back();
};

// 处理购票
const handlePurchase = () => {
    // 跳转到日期选择页面
    router.push(`/date-choose/${exhibition.value.id}`);
};

// 加载展览数据
const loadExhibitionData = async (id: number) => {
    try {
        const data = await exhibitionApi.getDetail(id);
        if (data) {
            // 解析介绍插图
            let imagesArray: string[] = [];
            if (data.detailImages) {
                try {
                    imagesArray = JSON.parse(data.detailImages);
                } catch (e) {
                    console.error('解析介绍插图失败:', e);
                }
            }
            
            exhibition.value = {
                ...data,
                // 兼容前端字段名
                shortDescription: data.shortDesc || '',
                dateRange: data.startDate && data.endDate ? `${data.startDate} — ${data.endDate}` : '待定',
                images: imagesArray
            };
        }
    } catch (e) {
        console.error(e);
    }
};

// 从路由参数获取展览ID，加载对应数据
onMounted(() => {
    const exhibitionId = route.params.id as string;
    if (exhibitionId) {
        loadExhibitionData(parseInt(exhibitionId));
    }
});
</script>

<style scoped>
.ticket-purchase {
    min-height: 100vh;
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;
    padding-bottom: 0px;
    position: relative;
    overflow-x: hidden;
}

/* 顶部导航栏 */
.header {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    background-color: white;
    position: sticky;
    top: 0;
    z-index: 100;
}

.back-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    background: none;
    border: none;
    cursor: pointer;
    color: #333;
    padding: 0;
    border-radius: 50%;
    transition: background-color 0.3s ease;
}

.back-button:hover {
    background-color: #f0f0f0;
}

.back-button .el-icon {
    font-size: 24px;
}

/* 展览信息区域（左右布局） */
.exhibition-header {
    display: flex;
    gap: 16px;
    padding: 20px;
    background-color: white;
    margin-bottom: 0;
}

.exhibition-image {
    width: 120px;
    border-radius: 8px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.exhibition-img {
    width: 100%;
    height: auto;
    display: block;
    border-radius: 8px;
    object-fit: contain;
}

.exhibition-image .no-image {
    font-size: 12px;
    color: #999;
}

.exhibition-basic-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-width: 0;
}

.exhibition-name {
    font-size: 20px;
    font-weight: bold;
    color: #333;
    margin: 0;
    line-height: 1.4;
}

.exhibition-short-desc {
    font-size: 14px;
    color: #666;
    margin: 0;
    line-height: 1.5;
}

.exhibition-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.tag {
    padding: 4px 12px;
    background-color: #f5f5f5;
    color: #333;
    border-radius: 12px;
    font-size: 12px;
    border: none;
}

.exhibition-time {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 4px;
}

.time-label {
    font-size: 14px;
    color: #666;
}

.time-value {
    font-size: 14px;
    color: #333;
}

/* 展览详情区域 */
.exhibition-details {
    background-color: white;
    padding: 20px;
    margin-bottom: 0;
    flex: 1;
}

.details-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0 0 16px 0;
}

.details-description {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin: 0 0 20px 0;
    white-space: pre-wrap;
}

.details-content-blocks {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.content-block {
    width: 100%;
    height: 200px;
    background-color: #d0d0d0;
    background-size: cover;
    background-position: center;
    border-radius: 8px;
}

.content-block.placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 14px;
}

/* 底部固定购票栏 */
.bottom-purchase-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100vw;
    max-width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background-color: white;
    border-top: 1px solid #e0e0e0;
    z-index: 1000;
    box-sizing: border-box;
    margin: 0;
}

.price-info {
    display: flex;
    align-items: center;
    flex: 1;
}

.price-text {
    font-size: 16px;
    color: #333;
    font-weight: 500;
}

.purchase-button {
    padding: 12px 32px;
    background-color: #e8e8e8;
    color: #333;
    border: none;
    border-radius: 6px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.3s ease;
    white-space: nowrap;
    margin-left: 16px;
}

.purchase-button:hover {
    background-color: #d8d8d8;
}

.purchase-button:active {
    background-color: #c8c8c8;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .exhibition-header {
        padding: 16px;
        gap: 12px;
    }

    .exhibition-image {
        width: 100px;
        height: 100px;
    }

    .exhibition-name {
        font-size: 18px;
    }

    .exhibition-short-desc {
        font-size: 13px;
    }

    .exhibition-details {
        padding: 16px;
    }

    .details-title {
        font-size: 16px;
    }

    .details-description {
        font-size: 13px;
    }

    .content-block {
        height: 150px;
    }

    .bottom-purchase-bar {
        padding: 12px 16px;
        width: 100%;
    }

    .price-text {
        font-size: 15px;
    }

    .purchase-button {
        padding: 10px 24px;
        font-size: 15px;
        margin-left: 12px;
    }
}

@media (max-width: 390px) {
    .exhibition-header {
        flex-direction: column;
    }

    .exhibition-image {
        width: 100%;
        height: 200px;
    }

    .bottom-purchase-bar {
        padding: 12px 16px;
        width: 100%;
    }

    .price-text {
        font-size: 14px;
    }

    .purchase-button {
        padding: 10px 20px;
        font-size: 14px;
        margin-left: 8px;
    }
}
</style>

