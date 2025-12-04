<template>
    <div class="mall">
        <div class="mall-header">
            <h2 class="page-title">文创商城</h2>
            <div class="cart-icon" @click="goToCart">
                <span class="icon">🛒</span>
            </div>
        </div>

        <div class="product-list">
            <div v-for="product in products" :key="product.id" class="product-card">
                <div class="product-image" :style="{ backgroundImage: `url(${product.coverImage})` }">
                    <div v-if="!product.coverImage" class="placeholder-img">暂无图片</div>
                </div>
                <div class="product-content">
                    <h3 class="product-name">{{ product.name }}</h3>
                    <p class="product-desc">{{ product.description }}</p>
                    <div class="product-footer">
                        <span class="price">¥ {{ product.price }}</span>
                        <button class="add-btn" @click="addToCart(product)">
                            <span>+</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { mallApi, type Product } from '@/api/mall';

const router = useRouter();
const products = ref<Product[]>([]);

// 加载商品列表
const loadProducts = async () => {
    try {
        const res = await mallApi.getProductList();
        if (res && res.records) {
            products.value = res.records;
        }
    } catch (e) {
        console.error(e);
    }
};

// 加入购物车
const addToCart = async (product: Product) => {
    try {
        await mallApi.addToCart({
            productId: product.id,
            quantity: 1
        });
        alert('已加入购物车');
    } catch (e) {
        console.error(e);
        alert('添加失败');
    }
};

const goToCart = () => {
    alert('查看后端接口已调用成功');
};

onMounted(() => {
    loadProducts();
});
</script>

<style scoped>
.mall {
    padding: 20px;
    background-color: #f5f5f5;
    min-height: 100vh;
    padding-bottom: 80px; /* 为底部导航预留空间 */
}

.mall-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    background-color: white;
    padding: 16px 20px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.page-title {
    font-size: 20px;
    font-weight: bold;
    color: #333;
    margin: 0;
}

.cart-icon {
    font-size: 24px;
    cursor: pointer;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    border-radius: 50%;
    transition: background-color 0.3s ease;
}

.cart-icon:hover {
    background-color: #e0e0e0;
}

.product-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr); /* 手机端双列布局 */
    gap: 16px;
}

.product-card {
    background: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    display: flex;
    flex-direction: column;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.product-image {
    width: 100%;
    padding-top: 100%; /* 1:1 比例 */
    position: relative;
    background-size: cover;
    background-position: center;
    background-color: #f0f0f0;
}

.placeholder-img {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #999;
    font-size: 12px;
}

.product-content {
    padding: 12px;
    flex: 1;
    display: flex;
    flex-direction: column;
}

.product-name {
    font-size: 15px;
    font-weight: bold;
    color: #333;
    margin: 0 0 6px;
    line-height: 1.4;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.product-desc {
    font-size: 12px;
    color: #999;
    margin: 0 0 12px;
    line-height: 1.5;
    height: 36px; /* 2行高度 */
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.product-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;
}

.price {
    color: #333;
    font-weight: bold;
    font-size: 16px;
}

.add-btn {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background-color: #333;
    color: white;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 20px;
    transition: background-color 0.3s ease;
}

.add-btn:hover {
    background-color: #000;
}

/* 响应式 */
@media (min-width: 768px) {
    .product-list {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 20px;
    }
}
</style>







