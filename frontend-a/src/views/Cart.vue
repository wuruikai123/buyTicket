<template>
  <div class="cart-page">
    <!-- 顶部导航栏 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="page-title">购物车</h1>
      <div class="header-placeholder"></div>
    </div>

    <!-- 购物车列表 -->
    <div class="cart-content">
      <!-- 空购物车状态 -->
      <div v-if="cartItems.length === 0" class="empty-state">
        <div class="empty-icon">🛒</div>
        <p class="empty-text">购物车是空的</p>
        <button class="go-mall-btn" @click="goToMall">去逛逛商城</button>
      </div>

      <!-- 购物车商品列表 -->
      <div v-else class="cart-list">
        <div 
          v-for="item in cartItems" 
          :key="item.id" 
          class="cart-item"
        >
          <div class="item-checkbox">
            <input 
              type="checkbox" 
              :checked="item.selected" 
              @change="toggleItemSelect(item)"
            />
          </div>
          <div class="item-image" :style="{ backgroundImage: item.coverImage ? `url(${item.coverImage})` : '' }">
            <span v-if="!item.coverImage" class="no-image">暂无图片</span>
          </div>
          <div class="item-info">
            <h3 class="item-name">{{ item.productName }}</h3>
            <p class="item-price">¥{{ item.price }}</p>
          </div>
          <div class="item-quantity">
            <button class="qty-btn" @click="decreaseQty(item)" :disabled="item.quantity <= 1">
              <el-icon><Minus /></el-icon>
            </button>
            <span class="qty-num">{{ item.quantity }}</span>
            <button class="qty-btn" @click="increaseQty(item)">
              <el-icon><Plus /></el-icon>
            </button>
          </div>
          <div class="item-subtotal">
            <span>¥{{ calculateSubtotal(item.price, item.quantity) }}</span>
          </div>
          <button class="delete-btn" @click="deleteItem(item)">
            <el-icon><Delete /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <!-- 底部结算栏 -->
    <div v-if="cartItems.length > 0" class="bottom-bar">
      <div class="select-all">
        <input 
          type="checkbox" 
          :checked="isAllSelected" 
          @change="toggleSelectAll"
        />
        <span>全选</span>
      </div>
      <div class="total-info">
        <span class="selected-count">已选 {{ selectedCount }} 件</span>
        <span class="total-amount">合计: <em>¥{{ selectedTotal }}</em></span>
      </div>
      <button 
        class="checkout-btn" 
        :disabled="selectedCount === 0"
        @click="goToCheckout"
      >
        结算
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Minus, Plus, Delete } from '@element-plus/icons-vue'
import { mallApi } from '@/api/mall'
import { 
  calculateSubtotal, 
  calculateSelectedTotal, 
  calculateSelectedCount,
  isAllSelected as checkAllSelected,
  toggleSelectAll as doToggleSelectAll,
  type CartItemVO 
} from '@/utils/cartUtils'

const router = useRouter()
const cartItems = ref<CartItemVO[]>([])

// 计算属性
const selectedCount = computed(() => calculateSelectedCount(cartItems.value))
const selectedTotal = computed(() => calculateSelectedTotal(cartItems.value))
const isAllSelected = computed(() => checkAllSelected(cartItems.value))

// 加载购物车
const loadCart = async () => {
  try {
    const res = await mallApi.getCartList()
    if (res) {
      cartItems.value = res.map((item: any) => ({
        ...item,
        selected: false
      }))
    }
  } catch (e) {
    console.error('加载购物车失败', e)
  }
}

// 切换单个商品选中
const toggleItemSelect = (item: CartItemVO) => {
  item.selected = !item.selected
}

// 切换全选
const toggleSelectAll = () => {
  const newState = !isAllSelected.value
  cartItems.value = doToggleSelectAll(cartItems.value, newState)
}

// 增加数量
const increaseQty = async (item: CartItemVO) => {
  try {
    await mallApi.updateCart({ id: item.id, quantity: item.quantity + 1 })
    item.quantity++
  } catch (e) {
    console.error('更新数量失败', e)
  }
}

// 减少数量
const decreaseQty = async (item: CartItemVO) => {
  if (item.quantity <= 1) return
  try {
    await mallApi.updateCart({ id: item.id, quantity: item.quantity - 1 })
    item.quantity--
  } catch (e) {
    console.error('更新数量失败', e)
  }
}

// 删除商品
const deleteItem = async (item: CartItemVO) => {
  try {
    await mallApi.removeCartItem(String(item.id))
    cartItems.value = cartItems.value.filter(i => i.id !== item.id)
  } catch (e) {
    console.error('删除失败', e)
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 去商城
const goToMall = () => {
  router.push('/mall')
}

// 去结算
const goToCheckout = () => {
  const selectedItems = cartItems.value.filter(item => item.selected)
  // 将选中商品存储到 sessionStorage
  sessionStorage.setItem('checkoutItems', JSON.stringify(selectedItems))
  router.push('/mall-checkout')
}

onMounted(() => {
  loadCart()
})
</script>


<style scoped>
.cart-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 80px;
}

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
  border-radius: 50%;
}

.back-button:hover {
  background-color: #f0f0f0;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.header-placeholder {
  width: 40px;
}

.cart-content {
  padding: 16px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background-color: white;
  border-radius: 12px;
  margin-top: 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #999;
  margin: 0 0 24px;
}

.go-mall-btn {
  padding: 12px 32px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.go-mall-btn:hover {
  background-color: #66b1ff;
}

/* 购物车列表 */
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: white;
  border-radius: 12px;
}

.item-checkbox input {
  width: 20px;
  height: 20px;
  cursor: pointer;
}

.item-image {
  width: 80px;
  height: 80px;
  background-color: #f0f0f0;
  background-size: cover;
  background-position: center;
  border-radius: 8px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-image {
  font-size: 12px;
  color: #999;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 14px;
  color: #f56c6c;
  margin: 0;
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-num {
  min-width: 24px;
  text-align: center;
  font-size: 14px;
}

.item-subtotal {
  min-width: 60px;
  text-align: right;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.delete-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-btn:hover {
  color: #f56c6c;
}

/* 底部结算栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  z-index: 100;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
}

.select-all input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.total-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-right: 16px;
}

.selected-count {
  font-size: 12px;
  color: #999;
}

.total-amount {
  font-size: 14px;
  color: #333;
}

.total-amount em {
  font-style: normal;
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.checkout-btn {
  padding: 12px 24px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.checkout-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.checkout-btn:not(:disabled):hover {
  background-color: #f78989;
}

/* 响应式 */
@media (max-width: 480px) {
  .cart-item {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .item-image {
    width: 60px;
    height: 60px;
  }
  
  .item-quantity {
    order: 1;
    width: 100%;
    justify-content: flex-start;
    padding-left: 32px;
    margin-top: 8px;
  }
  
  .item-subtotal {
    order: 2;
    margin-left: auto;
  }
  
  .delete-btn {
    order: 3;
  }
}
</style>
