<template>
    <div class="exhibitions">
        <!-- 顶部导航栏 -->
        <div class="nav-tabs">
            <div 
                class="tab-item" 
                :class="{ active: isOngoingActive }"
                @click="switchTab('ongoing')"
            >
                进行中
            </div>
            <div 
                class="tab-item" 
                :class="{ active: isUpcomingActive }"
                @click="switchTab('upcoming')"
            >
                待开始
            </div>
        </div>
        <div class="nav-divider"></div>

        <!-- 排序/筛选栏 -->
        <div class="filter-bar">
            <div class="sort-options">
                <span class="sort-label">排序</span>
                <span class="sort-separator">|</span>
                <span class="sort-value">开展时间最近优先</span>
            </div>
        </div>

        <!-- 展览列表 -->
        <div class="exhibition-list">
            <div 
                v-for="exhibition in (exhibitions as unknown as Exhibition[])" 
                :key="exhibition.id" 
                class="exhibition-card"
            >
                <div class="exhibition-image"></div>
                <div class="exhibition-content">
                    <h3 class="exhibition-title">{{ exhibition.name }}</h3>
                    <p class="exhibition-description">{{ exhibition.description }}</p>
                    <div class="exhibition-time">
                        <span class="time-label">展览时间:</span>
                        <span class="time-value">{{ exhibition.dateRange }}</span>
                    </div>
                </div>
                <div class="exhibition-action">
                    <button class="buy-button" @click="goToTicket(exhibition.id)">购票</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';

// 定义展览数据类型
interface Exhibition {
    id: number;
    name: string;
    description: string;
    dateRange: string;
    status: 'ongoing' | 'upcoming';
}

// 当前激活的标签页
const activeTab = ref<'ongoing' | 'upcoming'>('ongoing');

const router = useRouter();

// 切换标签页
const switchTab = (tab: 'ongoing' | 'upcoming') => {
    activeTab.value = tab;
};

// 计算属性用于类型检查
const isOngoingActive = computed(() => activeTab.value === 'ongoing');
const isUpcomingActive = computed(() => activeTab.value === 'upcoming');

// 跳转到购票页面
const goToTicket = (exhibitionId: number) => {
    router.push(`/ticket/${exhibitionId}`);
};

// 展览假数据
const allExhibitions: Exhibition[] = [
    {
        id: 1,
        name: 'XXXXXXXXXXXXXXXX展',
        description: 'XXXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXXX',
        dateRange: '2025/10/11 - 2025/10/12',
        status: 'ongoing'
    },
    {
        id: 2,
        name: 'XXXXXXXXXXXXXXXX展',
        description: 'XXXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXXX',
        dateRange: '2025/10/11 - 2025/10/12',
        status: 'ongoing'
    },
    {
        id: 3,
        name: 'XXXXXXXXXXXXXXXX展',
        description: 'XXXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXXX',
        dateRange: '2025/10/11 - 2025/10/12',
        status: 'ongoing'
    },
    {
        id: 4,
        name: 'XXXXXXXXXXXXXXXX展',
        description: 'XXXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXXX',
        dateRange: '2025/10/11 - 2025/10/12',
        status: 'ongoing'
    },
    {
        id: 5,
        name: 'XXXXXXXXXXXXXXXX展',
        description: 'XXXXXXXXXXXXXXXXXXXXXXXXX XXXXXXXXXXXXXXXXXXXXXXXXX',
        dateRange: '2025/10/11 - 2025/10/12',
        status: 'ongoing'
    }
];

// 根据当前标签页过滤展览
const exhibitions = ref<Exhibition[]>(allExhibitions.filter(ex => ex.status === activeTab.value));

// 监听标签页变化，更新展览列表
watch(activeTab, (newTab) => {
    exhibitions.value = allExhibitions.filter(ex => ex.status === newTab);
});
</script>

<style scoped>
.exhibitions {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding-bottom: 20px;
}

/* 顶部导航栏 */
.nav-tabs {
    display: flex;
    background-color: #666;
    padding: 0;
}

.tab-item {
    flex: 1;
    padding: 16px 20px;
    text-align: center;
    font-size: 16px;
    color: #999;
    cursor: pointer;
    position: relative;
    transition: color 0.3s ease;
    background-color: #666;
}

.tab-item.active {
    color: #fff;
    font-weight: bold;
}

.tab-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background-color: #409eff;
}

/* 导航栏分隔线 */
.nav-divider {
    height: 2px;
    background-color: #409eff;
    margin: 0;
}

/* 排序/筛选栏 */
.filter-bar {
    background-color: #f5f5f5;
    padding: 12px 20px;
    display: flex;
    justify-content: flex-start;
    align-items: center;
    font-size: 14px;
    color: #666;
    border-bottom: 1px solid #e0e0e0;
}

.sort-options {
    display: flex;
    align-items: center;
    gap: 8px;
}

.sort-label {
    color: #333;
}

.sort-separator {
    color: #ccc;
}

.sort-value {
    color: #666;
}

/* 展览列表 */
.exhibition-list {
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.exhibition-card {
    background-color: white;
    border-radius: 8px;
    display: flex;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    gap: 16px;
    align-items: flex-start;
}

.exhibition-image {
    width: 120px;
    height: 120px;
    background-color: #d0d0d0;
    border-radius: 8px;
    flex-shrink: 0;
}

.exhibition-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-width: 0;
}

.exhibition-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0;
    line-height: 1.4;
}

.exhibition-description {
    font-size: 14px;
    color: #999;
    margin: 0;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.exhibition-time {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 4px;
}

.time-label {
    font-size: 12px;
    color: #999;
}

.time-value {
    font-size: 14px;
    color: #666;
}

.exhibition-action {
    display: flex;
    align-items: flex-end;
    flex-shrink: 0;
    padding-bottom: 4px;
}

.buy-button {
    padding: 8px 20px;
    background-color: #f5f5f5;
    color: #666;
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s ease;
    white-space: nowrap;
}

.buy-button:hover {
    background-color: #e8e8e8;
    border-color: #d0d0d0;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .exhibition-list {
        padding: 16px;
        gap: 12px;
    }

    .exhibition-card {
        padding: 12px;
        gap: 12px;
    }

    .exhibition-image {
        width: 100px;
        height: 100px;
    }

    .exhibition-title {
        font-size: 16px;
    }

    .exhibition-description {
        font-size: 13px;
    }

    .time-label {
        font-size: 11px;
    }

    .time-value {
        font-size: 13px;
    }

    .buy-button {
        padding: 6px 16px;
        font-size: 13px;
    }
}

@media (max-width: 390px) {
    .exhibition-card {
        flex-direction: column;
    }

    .exhibition-image {
        width: 100%;
        height: 180px;
    }

    .exhibition-action {
        width: 100%;
        justify-content: center;
        padding-top: 8px;
        padding-bottom: 0;
    }

    .buy-button {
        width: 100%;
    }
}
</style>

