# A端剩余票数显示修复说明

## 问题描述
1. A端购票页面（datechoose.vue）显示"剩余0张"
2. 创建订单时提示"库存不足"

## 根本原因
系统中有两套库存逻辑：
1. **TicketInventory表**：原本用于库存管理，但可能没有数据
2. **Exhibition表**：存储每个时间段的总票数（morningTickets、afternoonTickets）

之前的代码依赖 `TicketInventory` 表，导致：
- 查询剩余票数时返回0
- 创建订单时提示"库存不足"

## 解决方案

### 修改的文件
1. `shared-backend/src/main/java/com/buyticket/controller/TicketInventoryController.java`
2. `shared-backend/src/main/java/com/buyticket/service/impl/TicketOrderServiceImpl.java`

### 统一的库存计算逻辑

**总票数来源**：Exhibition表
- 09:00-12:00 时段 → `morningTickets`
- 14:00-17:00 时段 → `afternoonTickets`

**已售票数统计**：
1. 从 `OrderItem` 表查询指定展览、日期、时间段的所有订单项
2. 关联 `TicketOrder` 表，只统计状态为 1（待使用）或 2（已使用）的订单
3. 累加这些订单项的 `quantity` 字段

**剩余票数计算**：
```
剩余票数 = 总票数 - 已售票数
```

### 修改1：查询剩余票数接口
`TicketInventoryController.getAvailability()`
- 不再依赖 `TicketInventory` 表
- 使用统一的库存计算逻辑
- 返回准确的剩余票数

### 修改2：创建订单库存验证
`TicketOrderServiceImpl.createOrder()`
- 不再依赖 `TicketInventory` 表
- 使用统一的库存计算逻辑验证库存
- 验证通过后创建订单（不再更新 TicketInventory 表）
- 错误信息更详细：显示剩余票数和需要票数

## 数据流程
```
前端调用 → /api/v1/ticket/availability
         ↓
查询 Exhibition 表（获取总票数）
         ↓
查询 OrderItem 表（获取订单项）
         ↓
查询 TicketOrder 表（过滤已支付订单）
         ↓
计算：总票数 - 已售票数
         ↓
返回剩余票数
```

## 库存验证逻辑
创建订单时：
1. 对每个订单项验证库存
2. 如果剩余票数 < 购买数量，抛出异常
3. 异常信息：`库存不足: 2026-01-31 14:00-17:00 (剩余X张，需要Y张)`
4. 验证通过后创建订单和订单项

## 测试验证
1. 访问A端购票页面
2. 选择展览、日期、时间段
3. 查看"剩余X张"是否正确显示
4. 尝试购买门票，应该能正常创建订单
5. 如果库存不足，会显示详细的错误信息

## 相关文件
- 前端：`frontend-a/src/views/buyTicket/datechoose.vue`
- API：`frontend-a/src/api/ticket.ts`
- 后端控制器：`shared-backend/src/main/java/com/buyticket/controller/TicketInventoryController.java`
- 后端服务：`shared-backend/src/main/java/com/buyticket/service/impl/TicketOrderServiceImpl.java`
