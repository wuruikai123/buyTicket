# C端核销记录问题修复说明

## 问题描述
C端的"销售记录"页面（Records.vue）无法显示核销记录，页面一直显示"该日期暂无核销记录"。

## 问题原因
后端有两个核销接口：

1. **OrderController.verifyTicketOrderByNo** (`POST /api/v1/order/ticket/verify`)
   - C端扫码核销使用
   - ❌ **BUG**: 只设置了 `status=2`，但没有设置 `verifyTime` 字段

2. **AdminOrderController.verifyTicketOrder** (`POST /api/v1/admin/order/ticket/{id}/verify`)
   - B端订单核销页面使用
   - ✅ 正确设置了 `verifyTime`

而查询核销记录的接口 `GET /api/v1/order/ticket/records?date=yyyy-MM-dd` 依赖 `verifyTime` 字段来筛选指定日期的记录：

```java
queryWrapper.eq(TicketOrder::getStatus, 2) // 已使用
           .ge(TicketOrder::getVerifyTime, startOfDay)
           .le(TicketOrder::getVerifyTime, endOfDay)
```

如果 `verifyTime` 为 null，即使订单状态是"已使用"，也不会被查询出来。

## 解决方案
修复 `OrderController.verifyTicketOrderByNo` 方法，在更新订单状态时同时设置核销时间：

```java
// 更新订单状态为已使用
order.setStatus(2);
order.setVerifyTime(java.time.LocalDateTime.now()); // 设置核销时间
ticketOrderService.updateById(order);
```

## 修改文件
- `shared-backend/src/main/java/com/buyticket/controller/OrderController.java`
- `shared-backend/src/main/java/com/buyticket/controller/admin/AdminOrderController.java`

## 测试步骤
1. 重启后端服务
2. 在C端扫码核销一个订单
3. 在C端"销售记录"页面选择今天的日期
4. 应该能看到刚才核销的订单记录

## 数据库字段
确保 `ticket_order` 表有 `verify_time` 字段：
```sql
ALTER TABLE ticket_order ADD COLUMN verify_time DATETIME COMMENT '核销时间';
```

如果之前已经核销过订单但没有 `verify_time`，可以执行以下SQL修复历史数据：
```sql
-- 将已使用订单的 verify_time 设置为 update_time（近似值）
UPDATE ticket_order 
SET verify_time = update_time 
WHERE status = 2 AND verify_time IS NULL;
```
