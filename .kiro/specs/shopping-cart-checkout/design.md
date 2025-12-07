# Design Document

## Overview

本设计文档描述购物车页面和商城订单结算功能的技术实现方案。该功能将在 frontend-a（用户端）实现，复用现有的后端 API（CartController、OrderController）。

主要包含两个新页面：
1. **购物车页面 (Cart.vue)** - 展示购物车商品、支持数量修改和选择结算
2. **订单确认页 (MallCheckout.vue)** - 填写收货信息、确认订单并支付

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend-A (Vue 3)                    │
├─────────────────────────────────────────────────────────┤
│  Views                                                   │
│  ├── Mall.vue (已有，添加购物车角标)                      │
│  ├── Cart.vue (新增)                                     │
│  └── MallCheckout.vue (新增)                             │
├─────────────────────────────────────────────────────────┤
│  API Layer                                               │
│  └── mall.ts (已有，复用现有API)                          │
├─────────────────────────────────────────────────────────┤
│  Router                                                  │
│  └── index.ts (添加新路由)                               │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  Shared-Backend (Spring Boot)            │
├─────────────────────────────────────────────────────────┤
│  Controllers (已有)                                      │
│  ├── CartController (/api/v1/mall/cart/*)               │
│  └── OrderController (/api/v1/order/mall/*)             │
└─────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. 路由配置

在 `frontend-a/src/router/index.ts` 添加：

```typescript
{
  path: 'cart',
  name: 'Cart',
  component: () => import('@/views/Cart.vue'),
},
{
  path: 'mall-checkout',
  name: 'MallCheckout',
  component: () => import('@/views/MallCheckout.vue'),
}
```

### 2. Cart.vue 组件结构

```
Cart.vue
├── Header (返回按钮 + 标题)
├── CartList
│   ├── CartItem (循环)
│   │   ├── Checkbox (选择)
│   │   ├── ProductImage
│   │   ├── ProductInfo (名称、价格)
│   │   ├── QuantityControl (+/- 按钮)
│   │   └── DeleteButton
│   └── EmptyState (购物车为空时显示)
└── BottomBar
    ├── SelectAll Checkbox
    ├── TotalAmount
    └── CheckoutButton
```

### 3. MallCheckout.vue 组件结构

```
MallCheckout.vue
├── Header (返回按钮 + 标题)
├── ContactSection
│   ├── ContactNameInput
│   └── ContactPhoneInput
├── AddressSection
│   └── AddressInput (简化版，单行地址)
├── OrderItemsSection
│   └── OrderItem (循环，只读展示)
├── OrderSummary
│   ├── ItemCount
│   └── TotalAmount
└── BottomBar
    ├── TotalDisplay
    └── PayButton
```

### 4. API 接口 (已有，复用)

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取购物车 | GET | /mall/cart/list | 返回购物车商品列表 |
| 更新数量 | PUT | /mall/cart/update | 更新商品数量 |
| 删除商品 | DELETE | /mall/cart/remove | 删除购物车商品 |
| 创建订单 | POST | /order/mall/create | 创建商城订单 |
| 支付 | POST | /order/pay | 支付订单 |

## Data Models

### CartItem (前端展示用)

```typescript
interface CartItemVO {
  id: number           // 购物车项ID
  productId: number    // 商品ID
  productName: string  // 商品名称
  price: number        // 商品单价
  quantity: number     // 数量
  coverImage: string   // 商品图片
  selected: boolean    // 是否选中 (前端状态)
}
```

### CheckoutData (结算页数据)

```typescript
interface CheckoutData {
  items: CartItemVO[]      // 选中的商品
  contactName: string      // 联系人姓名
  contactPhone: string     // 联系电话
  address: string          // 收货地址
  totalAmount: number      // 总金额
}
```

### MallOrderCreateRequest (创建订单请求)

```typescript
interface MallOrderCreateRequest {
  contactName: string
  contactPhone: string
  address: string
  totalAmount: number
  items: {
    productId: number
    quantity: number
    unitPrice: number
  }[]
}
```



## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Based on the prework analysis, the following properties can be combined and tested:

### Property 1: Subtotal calculation correctness
*For any* Cart_Item with price P and quantity Q, the displayed subtotal SHALL equal P × Q.
**Validates: Requirements 1.3**

### Property 2: Total amount calculation correctness
*For any* list of Cart_Items, the total amount SHALL equal the sum of all item subtotals (price × quantity for each item).
**Validates: Requirements 1.4, 2.4**

### Property 3: Quantity increase updates state correctly
*For any* Cart_Item with quantity Q, after increasing quantity, the new quantity SHALL equal Q + 1 and the subtotal SHALL be recalculated.
**Validates: Requirements 2.1**

### Property 4: Delete removes item from cart
*For any* Cart_Item in the cart, after deletion, the item SHALL NOT exist in the cart list.
**Validates: Requirements 2.3**

### Property 5: Select all toggles all items
*For any* cart with N items, toggling "select all" to true SHALL result in all N items having selected=true, and toggling to false SHALL result in all items having selected=false.
**Validates: Requirements 3.2**

### Property 6: Selected items calculation correctness
*For any* cart state, the selected count SHALL equal the number of items with selected=true, and the selected total SHALL equal the sum of subtotals for selected items only.
**Validates: Requirements 3.3**

### Property 7: Form validation prevents incomplete submission
*For any* checkout form, if contactName is empty OR contactPhone is empty OR address is empty, the form SHALL be invalid and submission SHALL be prevented.
**Validates: Requirements 4.3, 4.4, 5.5**

### Property 8: Cart badge count matches item count
*For any* cart state, the badge count displayed on the cart icon SHALL equal the total number of items in the cart.
**Validates: Requirements 6.2**

## Error Handling

| 场景 | 处理方式 |
|------|----------|
| 购物车加载失败 | 显示错误提示，提供重试按钮 |
| 更新数量失败 | 显示错误提示，恢复原数量 |
| 删除商品失败 | 显示错误提示，保留商品 |
| 创建订单失败 | 显示错误信息，允许重试 |
| 支付失败 | 显示错误信息，允许重试或取消 |
| 网络超时 | 显示网络错误提示 |

## Testing Strategy

### Unit Testing
- 测试计算函数：subtotal、total、selected count/amount
- 测试表单验证逻辑
- 测试状态管理函数

### Property-Based Testing
使用 **fast-check** 库进行属性测试：

1. **计算属性测试**：生成随机购物车数据，验证 subtotal 和 total 计算正确性
2. **状态变更测试**：生成随机操作序列，验证状态一致性
3. **表单验证测试**：生成随机表单数据，验证验证逻辑正确性

每个属性测试配置运行 100 次迭代。

测试文件命名：`*.test.ts`，与源文件同目录或在 `__tests__` 目录下。
