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
            <div class="exhibition-image"></div>
            <div class="exhibition-basic-info">
                <h2 class="exhibition-name">{{ exhibition.name }}</h2>
                <p class="exhibition-short-desc">{{ exhibition.shortDescription }}</p>
                <div class="exhibition-tags">
                    <span class="tag">美团</span>
                    <span class="tag">抖音</span>
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
            <div class="details-content-blocks">
                <div class="content-block"></div>
                <div class="content-block"></div>
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

// 定义展览数据类型
interface Exhibition {
    id: number;
    name: string;
    shortDescription: string;
    description: string;
    dateRange: string;
    price: number;
}

const router = useRouter();
const route = useRoute();

// 展览数据
const exhibition = ref<Exhibition>({
    id: 0,
    name: 'XXXXXXXXXX展',
    shortDescription: 'XXXXXXXXXXXXXXXX XXXX',
    description: 'XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX',
    dateRange: '2025/10/11 — 2025/10/12',
    price: 150
});

// 返回上一页
const goBack = () => {
    router.back();
};

// 处理购票
const handlePurchase = () => {
    // 跳转到日期选择页面
    router.push(`/date-choose/${exhibition.value.id}`);
};

// 从路由参数获取展览ID，加载对应数据
onMounted(() => {
    const exhibitionId = route.params.id as string;
    if (exhibitionId) {
        // TODO: 根据ID从API加载展览数据
        // 这里使用假数据
        loadExhibitionData(parseInt(exhibitionId));
    }
});

// 加载展览数据（假数据示例）
const loadExhibitionData = (id: number) => {
    // 这里应该从API获取数据，现在使用假数据
    const mockData: Record<number, Exhibition> = {
        1: {
            id: 1,
            name: 'XXXXXXXXXX展',
            shortDescription: 'XXXXXXXXXXXXXXXX XXXX',
            description: 'XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXX',
            dateRange: '2025/10/11 — 2025/10/12',
            price: 150
        },
        2: {
            id: 2,
            name: '印象派大师作品展',
            shortDescription: '经典印象派艺术作品展示',
            description: '本次展览将展出多位印象派大师的经典作品，包括莫奈、雷诺阿、德加等艺术家的珍贵画作。展览涵盖了印象派发展的各个重要阶段，为观众呈现一场视觉盛宴。',
            dateRange: '2025/09/30 — 2025/10/15',
            price: 120
        },
        3: {
            id: 3,
            name: '现代雕塑艺术展',
            shortDescription: '当代雕塑艺术精品展示',
            description: '汇集了国内外知名雕塑家的优秀作品，展现现代雕塑艺术的多样性和创新性。展览将传统与现代相结合，为观众带来全新的艺术体验。',
            dateRange: '2025/10/12 — 2025/10/25',
            price: 100
        }
    };
    
    if (mockData[id]) {
        exhibition.value = mockData[id];
    }
};
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
    height: 120px;
    background-color: #d0d0d0;
    border-radius: 8px;
    flex-shrink: 0;
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
    border-radius: 8px;
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

