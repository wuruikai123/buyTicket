<template>
    <div class="exhibitions">
        <!-- 展览列表 -->
        <div class="exhibition-list">
            <div 
                v-for="exhibition in exhibitions" 
                :key="exhibition.id" 
                class="exhibition-card"
            >
                <div class="exhibition-image-wrap">
                    <div 
                        class="exhibition-image"
                        :style="exhibition.coverImage ? { backgroundImage: `url(${exhibition.coverImage})` } : {}"
                    />
                </div>
                <div class="exhibition-content">
                    <h3 class="exhibition-title">{{ exhibition.name }}</h3>
                    <p class="exhibition-description">{{ exhibition.shortDesc || exhibition.description || '暂无介绍' }}</p>
                    <p class="exhibition-price">¥{{ formatPrice(exhibition.price) }}</p>
                </div>
                <div class="exhibition-action">
                    <button class="buy-button" @click="goToTicket(exhibition.id)">购票</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { exhibitionApi, type Exhibition } from '@/api/exhibition';

const router = useRouter();

// 展览列表数据（只显示进行中）
const exhibitions = ref<Exhibition[]>([]);

function formatPrice(price?: number) {
    if (price == null) return '0.00';
    return Number(price).toFixed(2);
}

function goToTicket(exhibitionId: number) {
    router.push(`/ticket/${exhibitionId}`);
}

async function loadExhibitions() {
    try {
        const list = await exhibitionApi.getList('ongoing');
        exhibitions.value = list ?? [];
    } catch (e) {
        console.error(e);
        exhibitions.value = [];
    }
}

onMounted(() => {
    loadExhibitions();
});
</script>

<style scoped>
.exhibitions {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding-bottom: 20px;
}

/* 展览列表 */
.exhibition-list {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-width: 100%;
    width: 100%;
    box-sizing: border-box;
    overflow-x: hidden;
}

.exhibition-card {
    background-color: white;
    border-radius: 12px;
    display: flex;
    padding: 14px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    gap: 14px;
    align-items: flex-start;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    transition: box-shadow 0.3s ease;
    overflow: hidden;
}

.exhibition-card:active {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* 封面图 宽13:高16 与首页一致 */
.exhibition-image-wrap {
    flex-shrink: 0;
    width: 100px;
    min-width: 100px;
    max-width: 100px;
}

.exhibition-image {
    width: 100%;
    aspect-ratio: 13 / 16;
    background-color: #e9ecef;
    border-radius: 8px;
    background-size: cover;
    background-position: center;
    position: relative;
    overflow: hidden;
}

.exhibition-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 0;
    max-width: 100%;
    overflow: hidden;
}

.exhibition-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0;
    line-height: 1.4;
    word-break: break-word;
    overflow-wrap: break-word;
    hyphens: auto;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.exhibition-description {
    font-size: 13px;
    color: #666;
    margin: 0;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    word-break: break-word;
    overflow-wrap: break-word;
}

.exhibition-price {
    font-size: 16px;
    font-weight: 600;
    color: #e53327;
    margin: 0;
    margin-top: auto;
    white-space: nowrap;
}

.exhibition-action {
    display: flex;
    align-items: flex-end;
    flex-shrink: 0;
    padding-bottom: 2px;
}

.buy-button {
    padding: 10px 20px;
    background-color: #213d7c;
    color: #ffffff;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s ease;
    white-space: nowrap;
    font-weight: 500;
    min-width: 70px;
    text-align: center;
    -webkit-tap-highlight-color: transparent;
    user-select: none;
}

.buy-button:active {
    transform: scale(0.95);
    background-color: #1a2f63;
}

/* 超小屏幕 (iPhone SE, 小型安卓机 ≤375px) */
@media (max-width: 375px) {
    .exhibition-list {
        padding: 12px;
        gap: 10px;
    }

    .exhibition-card {
        padding: 12px;
        gap: 12px;
        border-radius: 10px;
    }

    .exhibition-image-wrap {
        width: 80px;
        min-width: 80px;
        max-width: 80px;
    }

    .exhibition-title {
        font-size: 14px;
        line-height: 1.3;
    }

    .exhibition-description {
        font-size: 12px;
        line-height: 1.4;
    }

    .exhibition-price {
        font-size: 14px;
    }

    .buy-button {
        padding: 8px 14px;
        font-size: 12px;
        min-width: 56px;
    }
}

/* 小屏幕 (iPhone 12/13 mini, 标准安卓机 376-414px) */
@media (min-width: 376px) and (max-width: 414px) {
    .exhibition-list {
        padding: 14px;
        gap: 11px;
    }

    .exhibition-card {
        padding: 13px;
        gap: 13px;
    }

    .exhibition-image-wrap {
        width: 90px;
        min-width: 90px;
        max-width: 90px;
    }

    .exhibition-title {
        font-size: 15px;
    }

    .exhibition-description {
        font-size: 12px;
    }

    .exhibition-price {
        font-size: 15px;
    }

    .buy-button {
        padding: 9px 17px;
        font-size: 13px;
        min-width: 64px;
    }
}

/* 中等屏幕 (iPhone 12/13/14, 大部分安卓机 415-767px) */
@media (min-width: 415px) and (max-width: 767px) {
    .exhibition-list {
        padding: 16px;
        gap: 12px;
    }

    .exhibition-card {
        padding: 14px;
        gap: 14px;
    }

    .exhibition-image-wrap {
        width: 100px;
        min-width: 100px;
        max-width: 100px;
    }

    .exhibition-title {
        font-size: 16px;
    }

    .exhibition-description {
        font-size: 13px;
    }

    .exhibition-price {
        font-size: 16px;
    }

    .buy-button {
        padding: 10px 20px;
        font-size: 14px;
        min-width: 70px;
    }
}

/* 大屏幕 (平板和桌面) */
@media (min-width: 768px) {
    .exhibition-list {
        padding: 20px;
        gap: 16px;
        max-width: 800px;
        margin: 0 auto;
    }

    .exhibition-card {
        padding: 16px;
        gap: 16px;
    }

    .exhibition-image-wrap {
        width: 120px;
        min-width: 120px;
    }

    .exhibition-title {
        font-size: 18px;
    }

    .exhibition-description {
        font-size: 14px;
    }

    .exhibition-price {
        font-size: 17px;
    }

    .buy-button {
        padding: 10px 24px;
        font-size: 15px;
    }

    .buy-button:hover {
        background-color: #1a2f63;
        transform: translateY(-1px);
    }
}

/* 超大屏幕 */
@media (min-width: 1024px) {
    .exhibition-list {
        max-width: 900px;
    }
}

/* 横屏适配 */
@media (max-height: 500px) and (orientation: landscape) {
    .exhibition-list {
        padding: 12px;
    }

    .exhibition-card {
        padding: 10px;
    }

    .exhibition-image-wrap {
        width: 70px;
        min-width: 70px;
    }

    .exhibition-title {
        font-size: 14px;
        -webkit-line-clamp: 1;
    }

    .exhibition-description {
        font-size: 11px;
        -webkit-line-clamp: 1;
    }

    .buy-button {
        padding: 6px 14px;
        font-size: 12px;
    }
}

/* 确保在所有设备上文字不会溢出 */
* {
    box-sizing: border-box;
}

/* 防止iOS Safari缩放 */
@supports (-webkit-touch-callout: none) {
    .exhibition-card {
        -webkit-tap-highlight-color: rgba(0, 0, 0, 0.05);
    }
}
</style>

