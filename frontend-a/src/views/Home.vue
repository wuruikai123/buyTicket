<template>
    <div class="home">
        <div class="menu-icon">
            <el-icon size="30">
                <Expand />
            </el-icon>
        </div>

        <!-- 当前展出区域 -->
        <div 
            class="current-exhibition"
            :style="currentExhibition.coverImage ? { backgroundImage: `url(${currentExhibition.coverImage})` } : {}"
        >
            <div class="exhibition-content">
                <h2 class="exhibition-title">当前展出</h2>
                <p class="exhibition-name">{{ currentExhibition.name }}</p>
                <button class="ticket-button" @click="toBuyTicket">前往购票</button>
            </div>
            <div class="overlay"></div>
        </div>

        <!-- 近期展览区域 -->
        <div class="upcoming-exhibitions">
            <div class="section-header">
                <h3 class="section-title">近期展览</h3>
                <a href="#" class="more-link">更多 ></a>
            </div>
            <div class="exhibition-cards">
                <div v-for="exhibition in upcomingExhibitions" :key="exhibition.id" class="exhibition-card">
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
import { reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Expand } from '@element-plus/icons-vue';
import { exhibitionApi, type Exhibition } from '@/api/exhibition';

const router = useRouter();

// 当前展出
const currentExhibition = reactive<Partial<Exhibition>>({});

// 近期展览列表
const upcomingExhibitions = reactive<Exhibition[]>([]);

// 获取数据
onMounted(async () => {
    try {
        // 1. 获取当前展出
        const current = await exhibitionApi.getCurrent();
        if (current) {
            Object.assign(currentExhibition, current);
            // 后端可能返回 startDate/endDate，前端展示需要拼接
            if (current.startDate && current.endDate) {
                currentExhibition.date = `${current.startDate} - ${current.endDate}`;
            }
        }

        // 2. 获取近期展览 (假设我们获取待开始的)
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
    background-size: cover;
    background-position: center;
    position: relative;
    display: flex;
    align-items: flex-end;
    padding: 40px;
    min-height: 60vh;
}

.overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, rgba(0,0,0,0) 0%, rgba(0,0,0,0.6) 100%);
    z-index: 1;
}

.exhibition-content {
    width: 100%;
    position: relative;
    z-index: 2;
}

.exhibition-title {
    font-size: 32px;
    font-weight: bold;
    color: white;
    margin-bottom: 16px;
    text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.exhibition-name {
    font-size: 18px;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 24px;
    line-height: 1.5;
    text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

.ticket-button {
    background-color: white;
    color: #333;
    border: none;
    padding: 12px 32px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.3s ease;
}

.ticket-button:hover {
    background-color: #f0f0f0;
    transform: translateY(-2px);
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
    background-size: cover;
    background-position: center;
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
