<template>
    <div class="home">
        <div class="menu-icon">
            <el-icon size="30">
                <Expand />
            </el-icon>
        </div>

        <!-- 当前展出区域 -->
        <div class="current-exhibition">
            <div class="exhibition-content">
                <h2 class="exhibition-title">当前展出</h2>
                <p class="exhibition-name">{{ currentExhibition.name }}</p>
                <button class="ticket-button" @click="toBuyTicket">前往购票</button>
            </div>
        </div>

        <!-- 近期展览区域 -->
        <div class="upcoming-exhibitions">
            <div class="section-header">
                <h3 class="section-title">近期展览</h3>
                <a href="#" class="more-link">更多 ></a>
            </div>
            <div class="exhibition-cards">
                <div v-for="exhibition in upcomingExhibitions" :key="exhibition.id" class="exhibition-card">
                    <div class="card-image"></div>
                    <p class="card-title">{{ exhibition.name }}</p>
                    <p class="card-date">{{ exhibition.date }}</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Expand } from '@element-plus/icons-vue';

// 定义展览数据类型
interface Exhibition {
    id: number;
    name: string;
    date: string;
}

const router = useRouter();

// 当前展出假数据
const currentExhibition = reactive<Exhibition>({
    id: 1,
    name: '2025年当代艺术双年展',
    date: '2025/01/01 - 2025/12/31'
});

// 近期展览假数据列表
const upcomingExhibitions = reactive<Exhibition[]>([
    {
        id: 2,
        name: '印象派大师作品展',
        date: '2025/09/30'
    },
    {
        id: 3,
        name: '现代雕塑艺术展',
        date: '2025/10/12'
    },
    {
        id: 4,
        name: '数字艺术未来展',
        date: '2025/10/25'
    },
    {
        id: 5,
        name: '传统工艺文化展',
        date: '2025/11/08'
    }
]);

const toBuyTicket = () => {
    router.push('/mall')
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

/* 当前展出区域 */
.current-exhibition {
    flex: 2;
    background-color: #e0e0e0;
    position: relative;
    display: flex;
    align-items: flex-end;
    padding: 40px;
    min-height: 60vh;
}

.exhibition-content {
    width: 100%;
}

.exhibition-title {
    font-size: 32px;
    font-weight: bold;
    color: #333;
    margin-bottom: 16px;
}

.exhibition-name {
    font-size: 18px;
    color: #666;
    margin-bottom: 24px;
    line-height: 1.5;
}

.ticket-button {
    background-color: #666;
    color: white;
    border: none;
    padding: 12px 32px;
    border-radius: 8px;
    font-size: 16px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.ticket-button:hover {
    background-color: #66b1ff;
}

/* 近期展览区域 */
.upcoming-exhibitions {
    flex: 1;
    background-color: #f5f5f5;
    padding: 30px 20px;
    min-height: 40vh;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.section-title {
    font-size: 24px;
    font-weight: bold;
    color: #333;
    margin: 0;
}

.more-link {
    color: #409eff;
    text-decoration: none;
    font-size: 14px;
    transition: color 0.3s ease;
}

.more-link:hover {
    color: #66b1ff;
}

.exhibition-cards {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    justify-content: flex-start;
}

.exhibition-card {
    flex: 0 0 calc(50% - 8px);
    background-color: white;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.exhibition-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-image {
    width: 100%;
    height: 180px;
    background-color: #d0d0d0;
    margin-bottom: 12px;
}

.card-title {
    font-size: 16px;
    color: #333;
    margin: 0 12px 8px 12px;
    font-weight: 500;
}

.card-date {
    font-size: 14px;
    color: #666;
    margin: 0 12px 16px 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .current-exhibition {
        min-height: 50vh;
        padding: 30px 20px;
    }

    .exhibition-title {
        font-size: 28px;
    }

    .exhibition-name {
        font-size: 16px;
    }

    .exhibition-card {
        flex: 0 0 calc(50% - 8px);
    }

    .card-image {
        height: 150px;
    }
}

@media (max-width: 480px) {
    .exhibition-card {
        flex: 0 0 100%;
    }
}
</style>
