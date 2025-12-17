<template>
    <div class="date-choose">
        <!-- 顶部导航栏 -->
        <div class="header">
            <button class="back-button" @click="goBack">
                <el-icon><ArrowLeft /></el-icon>
            </button>
            <h1 class="page-title">{{ exhibition.name }}</h1>
            <div class="header-placeholder"></div>
        </div>

        <!-- 日期选择区域 -->
        <div class="date-selector">
            <div class="year-month-selector">
                <div class="selector-item">
                    <span class="selector-value">{{ selectedYear }}年</span>
                    <div class="selector-arrows">
                        <el-icon class="arrow-up" @click="changeYear(1)"><ArrowUp /></el-icon>
                        <el-icon class="arrow-down" @click="changeYear(-1)"><ArrowDown /></el-icon>
                    </div>
                </div>
                <div class="selector-item">
                    <span class="selector-value">{{ selectedMonth }}月</span>
                    <div class="selector-arrows">
                        <el-icon class="arrow-up" @click="changeMonth(1)"><ArrowUp /></el-icon>
                        <el-icon class="arrow-down" @click="changeMonth(-1)"><ArrowDown /></el-icon>
                    </div>
                </div>
            </div>

            <!-- 日历 -->
            <div class="calendar">
                <div class="weekdays">
                    <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
                </div>
                <div class="calendar-grid">
                    <div 
                        v-for="date in calendarDates" 
                        :key="date.key"
                        class="calendar-date"
                        :class="{ 
                            'other-month': date.otherMonth,
                            'selected': date.selected,
                            'today': date.isToday,
                'empty': date.day === 0,
                'disabled': date.disabled
                        }"
            @click="date.day !== 0 ? selectDate(date) : null"
                    >
                        {{ date.day !== 0 ? date.day : '' }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 时间段选择 -->
        <div class="time-selector">
            <div class="time-selector-item">
                <span class="time-value">{{ selectedTimeSlot }}</span>
                <div class="selector-arrows">
                    <el-icon class="arrow-up" @click="changeTimeSlot(1)"><ArrowUp /></el-icon>
                    <el-icon class="arrow-down" @click="changeTimeSlot(-1)"><ArrowDown /></el-icon>
                </div>
            </div>
        </div>

        <!-- 剩余票数 -->
        <div class="availability">
            <span class="availability-text">剩余{{ remainingTickets }}张</span>
        </div>

        <!-- 确认按钮 -->
        <div class="confirm-section">
            <button class="confirm-button" @click="handleConfirm">确认</button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ArrowLeft, ArrowUp, ArrowDown } from '@element-plus/icons-vue';
import { exhibitionApi } from '@/api/exhibition';
import { ticketApi } from '@/api/ticket';

interface CalendarDate {
    key: string;
    day: number;
    date: Date;
    otherMonth: boolean;
    selected: boolean;
    isToday: boolean;
    disabled: boolean;
}

const router = useRouter();
const route = useRoute();

// 展览信息
const exhibition = ref({
    id: 0,
    name: ''
});

// 选中的日期和时间
const now = new Date();
const selectedYear = ref(now.getFullYear());
const selectedMonth = ref(now.getMonth() + 1);
const selectedDate = ref<Date | null>(now);
const selectedTimeSlot = ref('12:00-14:00'); // 注意格式需与数据库一致
const remainingTickets = ref(0);

// 星期标签
const weekdays = ['一', '二', '三', '四', '五', '六', '日'];

// 时间段选项 (需与数据库一致)
const timeSlots = [
    '09:00-12:00',
    '12:00-14:00',
    '14:00-16:00'
];

// 计算日历日期 (保持原有逻辑，略微调整)
const calendarDates = computed(() => {
    const dates: CalendarDate[] = [];
    const year = selectedYear.value;
    const month = selectedMonth.value - 1;
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const firstDayWeek = (firstDay.getDay() + 6) % 7;
    
    for (let i = 0; i < firstDayWeek; i++) {
        dates.push({
            key: `empty-${i}`,
            day: 0,
            date: new Date(),
            otherMonth: true,
            selected: false,
            isToday: false,
            disabled: true
        });
    }
    
    for (let day = 1; day <= lastDay.getDate(); day++) {
        const date = new Date(year, month, day);
        const isSelected = selectedDate.value && 
            date.getFullYear() === selectedDate.value.getFullYear() &&
            date.getMonth() === selectedDate.value.getMonth() &&
            date.getDate() === selectedDate.value.getDate();
        const today = new Date();
        const isToday = date.getFullYear() === today.getFullYear() &&
            date.getMonth() === today.getMonth() &&
            date.getDate() === today.getDate();
        const isPast = date < todayStart;
        
        dates.push({
            key: `${year}-${month + 1}-${day}`,
            day,
            date,
            otherMonth: false,
            selected: isSelected || false,
            isToday,
            disabled: isPast
        });
    }
    return dates;
});

// 改变年份
const changeYear = (delta: number) => {
    const minYear = now.getFullYear();
    const minMonth = now.getMonth() + 1;
    const newYear = selectedYear.value + delta;
    if (newYear < minYear) return;
    selectedYear.value = newYear;
    if (selectedYear.value === minYear && selectedMonth.value < minMonth) {
        selectedMonth.value = minMonth;
    }
};

// 改变月份
const changeMonth = (delta: number) => {
    const minYear = now.getFullYear();
    const minMonth = now.getMonth() + 1;

    let newYear = selectedYear.value;
    let newMonth = selectedMonth.value + delta;

    if (newMonth < 1) {
        newMonth = 12;
        newYear -= 1;
    }
    if (newMonth > 12) {
        newMonth = 1;
        newYear += 1;
    }

    if (newYear < minYear || (newYear === minYear && newMonth < minMonth)) {
        selectedYear.value = minYear;
        selectedMonth.value = minMonth;
        return;
    }

    selectedYear.value = newYear;
    selectedMonth.value = newMonth;
};

// 选择日期
const selectDate = (dateInfo: CalendarDate) => {
    if (!dateInfo.otherMonth && !dateInfo.disabled) {
        selectedDate.value = dateInfo.date;
        updateRemainingTickets();
    }
};

// 改变时间段
const changeTimeSlot = (delta: number) => {
    const currentIndex = timeSlots.indexOf(selectedTimeSlot.value);
    if (currentIndex !== -1) {
        let newIndex = currentIndex + delta;
        if (newIndex < 0) newIndex = timeSlots.length - 1;
        if (newIndex >= timeSlots.length) newIndex = 0;
        selectedTimeSlot.value = timeSlots[newIndex];
        updateRemainingTickets();
    }
};

// 格式化日期 YYYY-MM-DD
const formatDate = (date: Date) => {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
};

// 更新剩余票数
const updateRemainingTickets = async () => {
    if (!selectedDate.value || !exhibition.value.id) return;
    
    try {
        const res = await ticketApi.getAvailability({
            exhibitionId: exhibition.value.id,
            date: formatDate(selectedDate.value),
            timeSlot: selectedTimeSlot.value
        });
        if (res) {
            remainingTickets.value = res.remainingCount;
        }
    } catch (e) {
        console.error(e);
        remainingTickets.value = 0;
    }
};

// 返回上一页
const goBack = () => {
    router.back();
};

// 确认选择
const handleConfirm = () => {
    if (!selectedDate.value) {
        alert('请选择日期');
        return;
    }
    
    // 检查库存是否足够
    if (remainingTickets.value <= 0) {
        alert('该时间段已售罄，请选择其他时间');
        return;
    }
    
    // 跳转到购票信息页面
    router.push({
        path: `/ticket-info/${exhibition.value.id}`,
        query: {
            date: formatDate(selectedDate.value),
            timeSlot: selectedTimeSlot.value,
            exhibitionName: exhibition.value.name
        }
    });
};

// 加载展览信息
const loadExhibitionInfo = async (id: number) => {
    try {
        const data = await exhibitionApi.getDetail(id);
        if (data) {
            exhibition.value.id = data.id;
            exhibition.value.name = data.name;
            // 加载完展览信息后，获取一次余票
            updateRemainingTickets();
        }
    } catch (e) {
        console.error(e);
    }
};

onMounted(() => {
    const exhibitionId = route.params.id as string;
    if (exhibitionId) {
        const id = parseInt(exhibitionId);
        exhibition.value.id = id;
        loadExhibitionInfo(id);
    }
});
</script>

<style scoped>
.date-choose {
    padding-bottom: 20px;
}

/* 顶部导航栏 */
.header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background-color: white;
    position: sticky;
    top: 0;
    z-index: 100;
    border-bottom: 1px solid #e0e0e0;
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

.page-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0;
    flex: 1;
    text-align: center;
}

.header-placeholder {
    width: 40px;
}

/* 日期选择区域 */
.date-selector {
    background-color: white;
    padding: 20px;
    margin-bottom: 12px;
}

.year-month-selector {
    display: flex;
    gap: 16px;
    margin-bottom: 20px;
}

.selector-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    flex: 1;
    background-color: white;
}

.selector-value {
    font-size: 16px;
    color: #333;
}

.selector-arrows {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.arrow-up,
.arrow-down {
    font-size: 12px;
    color: #666;
    cursor: pointer;
    transition: color 0.3s ease;
}

.arrow-up:hover,
.arrow-down:hover {
    color: #409eff;
}

/* 日历 */
.calendar {
    margin-top: 20px;
}

.weekdays {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 4px;
    margin-bottom: 4px;
}

.weekday {
    text-align: center;
    font-size: 14px;
    color: #666;
    padding: 1px 0;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    background-color: white;
}

.calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 4px;
}

.calendar-date {
    aspect-ratio: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    background-color: white;
    transition: all 0.3s ease;
}

.calendar-date:hover:not(.other-month) {
    background-color: #f0f0f0;
    border-color: #d0d0d0;
}

.calendar-date.other-month {
    color: #ccc;
    cursor: default;
    background-color: #fafafa;
}

.calendar-date.empty {
    border: none;
    background-color: transparent;
    cursor: default;
}

.calendar-date.empty:hover {
    background-color: transparent;
}

.calendar-date.disabled {
    color: #c0c4cc;
    border-style: dashed;
    text-decoration: line-through;
    cursor: not-allowed;
    background-color: #f8f8f8;
}

.calendar-date.disabled:hover {
    background-color: #f8f8f8;
    border-color: #e0e0e0;
}

.calendar-date.selected {
    background-color: #e0e0e0;
    color: #333;
    font-weight: bold;
    border-color: #409eff;
}

.calendar-date.today {
    color: #409eff;
}

/* 时间段选择 */
.time-selector {
    background-color: white;
    padding: 20px;
    margin-bottom: 12px;
}

.time-selector-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    background-color: white;
}

.time-value {
    font-size: 16px;
    color: #333;
}

/* 剩余票数 */
.availability {
    text-align: center;
    padding: 16px 20px;
    background-color: white;
    margin-bottom: 12px;
}

.availability-text {
    font-size: 16px;
    color: #333;
}

/* 确认按钮 */
.confirm-section {
    padding: 20px;
    background-color: white;
    position: sticky;
    bottom: 0;
}

.confirm-button {
    width: 100%;
    padding: 16px;
    background-color: #e8e8e8;
    color: #333;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.confirm-button:hover {
    background-color: #d8d8d8;
}

.confirm-button:active {
    background-color: #c8c8c8;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .date-selector,
    .time-selector,
    .availability {
        padding: 16px;
    }

    .calendar-date {
        font-size: 13px;
    }
}
</style>

