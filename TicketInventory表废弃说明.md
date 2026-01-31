# TicketInventory 表废弃说明

## 背景
系统原本设计了 `ticket_inventory` 表用于管理每日库存，但实际使用中发现：
1. 该表需要手动创建库存记录，操作繁琐
2. 与 Exhibition 表的 `morningTickets` 和 `afternoonTickets` 字段功能重复
3. 导致库存验证失败，影响用户购票

## 新的库存管理方案

### 库存数据来源
直接使用 `Exhibition` 表的字段：
- `morningTickets`：09:00-12:00 时段的总票数
- `afternoonTickets`：14:00-17:00 时段的总票数

### 已售票数统计
从 `OrderItem` 和 `TicketOrder` 表实时计算：
```sql
SELECT SUM(oi.quantity)
FROM order_item oi
JOIN ticket_order t ON oi.order_id = t.id
WHERE oi.exhibition_id = ?
  AND oi.ticket_date = ?
  AND oi.time_slot = ?
  AND t.status IN (1, 2)  -- 1:待使用, 2:已使用
```

### 剩余票数计算
```
剩余票数 = Exhibition.morningTickets/afternoonTickets - 已售票数
```

## 已修改的文件

### 1. TicketInventoryController.java
**路径**：`shared-backend/src/main/java/com/buyticket/controller/TicketInventoryController.java`

**修改内容**：
- `getAvailability()` 方法不再查询 `ticket_inventory` 表
- 改为从 Exhibition 表获取总票数，从 OrderItem 表统计已售票数

### 2. TicketOrderServiceImpl.java
**路径**：`shared-backend/src/main/java/com/buyticket/service/impl/TicketOrderServiceImpl.java`

**修改内容**：
- `createOrder()` 方法不再查询和更新 `ticket_inventory` 表
- 改为使用与 `getAvailability()` 相同的逻辑验证库存
- 不再扣减 `ticket_inventory` 的 `soldCount` 字段

## 未使用的代码（可选择性删除）

### 后端文件
1. `shared-backend/src/main/java/com/buyticket/entity/TicketInventory.java`
2. `shared-backend/src/main/java/com/buyticket/mapper/TicketInventoryMapper.java`
3. `shared-backend/src/main/java/com/buyticket/service/TicketInventoryService.java`
4. `shared-backend/src/main/java/com/buyticket/service/impl/TicketInventoryServiceImpl.java`
5. `shared-backend/src/main/java/com/buyticket/controller/admin/AdminTicketInventoryController.java`

### 前端文件
1. `frontend-b/src/api/ticket.ts` 中的库存相关方法：
   - `getInventoryList()`
   - `getInventoryDetail()`
   - `createInventory()`
   - `updateInventory()`
   - `deleteInventory()`
   - `batchCreateInventory()`

### 数据库表
- `ticket_inventory` 表（可保留但不使用）

## 注意事项

### B端页面说明
- **路由 `/ticket/inventory`**：虽然路径名为 inventory，但实际显示的是"销售记录"（订单列表）
- **InventoryList.vue**：调用的是 `orderApi.getTicketOrderList()`，不是库存接口
- **WarningList.vue**：同样调用的是订单接口

这些页面不需要修改，因为它们从未真正使用过 TicketInventory 相关接口。

### 如果需要删除
如果确定不再需要 TicketInventory 相关功能，可以：
1. 删除上述后端文件
2. 删除前端 API 中的相关方法
3. 保留数据库表（以防数据恢复需要）

### 如果需要保留
如果担心将来可能需要，可以：
1. 保留所有代码不删除
2. 在代码中添加 `@Deprecated` 注解标记废弃
3. 添加注释说明不再使用

## 优势

### 新方案的优点
1. **简化操作**：B端只需在展览表单中设置门票数量，无需额外创建库存记录
2. **实时准确**：库存数据实时从订单表计算，不会出现不同步问题
3. **易于维护**：减少了一个数据表的维护成本
4. **用户友好**：A端用户可以立即看到正确的剩余票数

### 数据一致性
- 总票数：来自 Exhibition 表（单一数据源）
- 已售票数：实时统计 OrderItem 表（不会遗漏或重复）
- 剩余票数：动态计算（始终准确）

## 测试验证

### 验证步骤
1. B端创建展览，设置 morningTickets 和 afternoonTickets
2. A端访问购票页面，查看剩余票数是否正确
3. 创建订单并支付
4. 再次查看剩余票数，应该减少相应数量
5. 核销订单后，剩余票数不变（已使用的订单仍然占用库存）

### 预期结果
- 剩余票数 = Exhibition 设置的票数 - 已支付和已使用的订单票数
- 不会出现"库存不足"的错误（除非真的售罄）
- 数据始终保持一致

## 相关文档
- [A端剩余票数显示修复说明.md](./A端剩余票数显示修复说明.md)
