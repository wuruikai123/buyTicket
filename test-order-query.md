# 测试订单查询接口

## 问题诊断

当前 C 端核销报 500 错误，可能的原因：

1. 数据库中 `order_no` 字段不存在
2. 订单号为空或 NULL
3. 后端代码有其他错误

## 解决步骤

### 1. 检查数据库

连接到 MySQL 数据库：
```bash
# Docker 环境
docker exec -it buyticket-mysql mysql -ubuyticket -pbuyticket123 buy_ticket

# 本地环境
mysql -h127.0.0.1 -P3307 -ubuyticket -pbuyticket123 buy_ticket
```

检查表结构：
```sql
DESC ticket_order;
```

查看订单数据：
```sql
SELECT id, order_no, user_id, contact_name, status, create_time 
FROM ticket_order 
ORDER BY id DESC 
LIMIT 5;
```

### 2. 运行更新脚本

如果 `order_no` 字段不存在或为空，运行：
```sql
-- 添加字段（如果不存在）
ALTER TABLE ticket_order 
ADD COLUMN order_no VARCHAR(32) NOT NULL DEFAULT '' COMMENT '订单号（唯一）' AFTER id;

-- 为已存在的订单生成订单号
UPDATE ticket_order 
SET order_no = CONCAT('T', DATE_FORMAT(create_time, '%Y%m%d'), LPAD(id, 8, '0'))
WHERE order_no = '' OR order_no IS NULL;

-- 添加唯一索引
ALTER TABLE ticket_order 
ADD UNIQUE INDEX uk_order_no (order_no);
```

### 3. 验证数据

```sql
-- 检查是否所有订单都有订单号
SELECT COUNT(*) as total, 
       SUM(CASE WHEN order_no IS NULL OR order_no = '' THEN 1 ELSE 0 END) as empty_order_no
FROM ticket_order;

-- 查看订单号示例
SELECT id, order_no, contact_name, status 
FROM ticket_order 
WHERE order_no IS NOT NULL AND order_no != ''
LIMIT 5;
```

### 4. 测试接口

使用 curl 测试：
```bash
# 查询订单（替换为实际的订单号）
curl "http://localhost:8080/api/v1/admin/order/ticket/query?orderNo=T2025121100000001"

# 获取今日核销数量
curl "http://localhost:8080/api/v1/admin/order/ticket/today-count"
```

### 5. 重启后端

如果修改了数据库，需要重启后端：
```bash
# Docker
docker-compose restart backend

# 本地开发
# 停止后端，然后重新运行
cd shared-backend
mvn spring-boot:run
```

## 常见问题

### Q: 订单号字段已存在但还是报错？
A: 检查是否有订单的 order_no 为空，运行 UPDATE 语句生成订单号

### Q: 如何获取一个有效的订单号进行测试？
A: 
```sql
SELECT order_no FROM ticket_order WHERE status = 1 LIMIT 1;
```

### Q: 500 错误但看不到具体信息？
A: 查看后端日志：
```bash
# Docker
docker-compose logs -f backend

# 本地开发
# 查看控制台输出
```
