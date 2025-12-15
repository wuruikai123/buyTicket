# 订单号字段错误修复指南

## 错误原因

数据库中 `ticket_order` 表的 `order_no` 字段被设置为 NOT NULL 且没有默认值，导致插入新订单时报错：
```
Field 'order_no' doesn't have a default value
```

## 解决方案

### 方案1：执行 SQL 修复脚本（推荐）

1. 连接到数据库：
```bash
mysql -u root -p buy_ticket
```

2. 执行修复脚本：
```bash
source shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

或者直接执行：
```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

### 方案2：手动执行 SQL

```sql
-- 1. 修改字段允许NULL
ALTER TABLE ticket_order 
MODIFY COLUMN order_no VARCHAR(32) NULL COMMENT '订单号（唯一）';

-- 2. 为现有空订单号生成新值
UPDATE ticket_order 
SET order_no = CONCAT(
    'T',
    UNIX_TIMESTAMP(IFNULL(create_time, NOW())) * 1000 + id % 1000,
    UPPER(SUBSTRING(MD5(CONCAT(id, create_time, RAND())), 1, 6))
)
WHERE order_no IS NULL 
   OR order_no = '' 
   OR order_no = '0';

-- 3. 添加唯一索引
ALTER TABLE ticket_order 
ADD UNIQUE INDEX uk_order_no (order_no);
```

## 代码修改

已修改 `TicketOrderServiceImpl.java`，在保存订单前就生成订单号：

```java
// 生成安全的订单号
String orderNo = generateSecureOrderNo();
order.setOrderNo(orderNo);

// 保存订单（此时order_no已有值）
this.save(order);
```

## 验证步骤

1. 执行 SQL 脚本后，重启后端服务

2. 测试创建新订单：
   - 访问 A 端
   - 选择展览购买门票
   - 完成支付
   - 查看订单详情，确认订单号正常显示

3. 检查数据库：
```sql
SELECT id, order_no, status, create_time 
FROM ticket_order 
ORDER BY id DESC 
LIMIT 10;
```

确认所有订单都有订单号，格式类似：`T1702345678901ABC123`

## 注意事项

1. **执行前备份数据库**：
```bash
mysqldump -u root -p buy_ticket > backup_$(date +%Y%m%d_%H%M%S).sql
```

2. **生产环境**：建议在低峰期执行

3. **唯一索引**：如果已存在 `uk_order_no` 索引，会报错但不影响功能

4. **订单号格式**：新格式为 `T + 13位时间戳 + 6位随机字符`

## 常见问题

### Q: 执行 SQL 时报错 "Duplicate entry"
A: 说明已存在唯一索引，可以忽略或先删除再添加：
```sql
ALTER TABLE ticket_order DROP INDEX uk_order_no;
ALTER TABLE ticket_order ADD UNIQUE INDEX uk_order_no (order_no);
```

### Q: 旧订单的订单号会改变吗？
A: 只有空的或无效的订单号会被更新，已有有效订单号的不会改变

### Q: 如何回滚？
A: 使用备份恢复：
```bash
mysql -u root -p buy_ticket < backup_YYYYMMDD_HHMMSS.sql
```
