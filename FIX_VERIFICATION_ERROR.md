# 修复 C 端核销 500 错误

## 问题原因
数据库中 `ticket_order` 表的 `order_no` 字段不存在或为空，导致查询失败。

## 快速修复步骤

### 步骤 1：连接数据库

**Docker 环境：**
```bash
docker exec -it buyticket-mysql mysql -ubuyticket -pbuyticket123 buy_ticket
```

**本地环境：**
```bash
mysql -h127.0.0.1 -P3307 -ubuyticket -pbuyticket123 buy_ticket
```

### 步骤 2：执行修复脚本

复制以下 SQL 并在 MySQL 中执行：

```sql
USE buy_ticket;

-- 检查 order_no 字段是否存在
SHOW COLUMNS FROM ticket_order LIKE 'order_no';

-- 如果字段不存在，添加它（如果已存在会报错，可以忽略）
ALTER TABLE ticket_order 
ADD COLUMN order_no VARCHAR(32) DEFAULT '' COMMENT '订单号（唯一）' AFTER id;

-- 为所有订单生成订单号
UPDATE ticket_order 
SET order_no = CONCAT('T', DATE_FORMAT(IFNULL(create_time, NOW()), '%Y%m%d'), LPAD(id, 8, '0'))
WHERE order_no IS NULL OR order_no = '' OR order_no = '0';

-- 验证修复结果
SELECT 
    COUNT(*) as total_orders,
    SUM(CASE WHEN order_no IS NULL OR order_no = '' THEN 1 ELSE 0 END) as orders_without_no
FROM ticket_order;

-- 查看订单示例
SELECT id, order_no, contact_name, status FROM ticket_order LIMIT 5;
```

### 步骤 3：重启后端

**Docker：**
```bash
docker-compose restart backend
```

**本地开发：**
停止后端进程，然后重新运行：
```bash
cd shared-backend
mvn spring-boot:run
```

### 步骤 4：获取测试订单号

在 MySQL 中执行：
```sql
SELECT order_no, contact_name, status 
FROM ticket_order 
WHERE status = 1 
  AND order_no IS NOT NULL 
  AND order_no != ''
LIMIT 1;
```

复制返回的 `order_no`，例如：`T2025121100000001`

### 步骤 5：测试核销功能

1. 打开 C 端核销页面：http://localhost:5175
2. 输入步骤 4 获取的订单号
3. 点击"核销"按钮
4. 应该显示核销成功

## 验证修复

### 方法 1：使用 curl 测试

```bash
# 查询订单（替换为实际订单号）
curl "http://localhost:8080/api/v1/admin/order/ticket/query?orderNo=T2025121100000001"

# 应该返回订单信息，而不是 500 错误
```

### 方法 2：查看后端日志

**Docker：**
```bash
docker-compose logs -f backend | grep -i error
```

**本地开发：**
查看控制台输出，不应该有错误信息。

## 常见问题

### Q1: ALTER TABLE 报错 "Duplicate column name"
**A:** 说明字段已存在，跳过 ALTER TABLE，直接执行 UPDATE 语句。

### Q2: 执行 UPDATE 后还是报错
**A:** 
1. 检查是否真的更新成功：
   ```sql
   SELECT COUNT(*) FROM ticket_order WHERE order_no IS NULL OR order_no = '';
   ```
   应该返回 0

2. 重启后端服务

### Q3: 没有状态为 1 的订单用于测试
**A:** 创建一个测试订单：
1. 访问用户端：http://localhost:5173
2. 登录：zhangsan / 123456
3. 购买一张门票
4. 支付订单（支付密码：123456）
5. 在数据库中查询新订单的 order_no

### Q4: 还是 500 错误
**A:** 查看详细错误信息：
```bash
# Docker
docker-compose logs backend --tail=50

# 本地
# 查看控制台最后 50 行日志
```

将错误信息发给开发人员。

## 预防措施

为避免将来出现类似问题：

1. **在 schema.sql 中确保 order_no 字段存在**
   - 文件位置：`shared-backend/src/main/resources/sql/schema.sql`
   - 已包含 order_no 字段定义

2. **创建订单时自动生成订单号**
   - 在 `TicketOrderServiceImpl.java` 中添加订单号生成逻辑

3. **定期检查数据完整性**
   ```sql
   SELECT COUNT(*) FROM ticket_order WHERE order_no IS NULL OR order_no = '';
   ```

## 成功标志

修复成功后，你应该能够：
1. ✅ 在 C 端输入订单号查询订单
2. ✅ 核销订单成功
3. ✅ 首页显示今日核销数量
4. ✅ 查看核销记录

## 需要帮助？

如果以上步骤都无法解决问题，请提供：
1. 后端日志（最后 50 行）
2. 数据库表结构：`DESC ticket_order;`
3. 订单数据示例：`SELECT * FROM ticket_order LIMIT 1;`
