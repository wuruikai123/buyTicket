# 数据库列修复指南

## 🔴 问题说明

执行 SQL 时报错：`#1060 - Duplicate column name 'pay_time'`

**原因**：列已经存在，不能重复添加。

---

## ✅ 解决方案

### 方案 1：使用自动修复脚本（推荐）

执行 `FIX_DATABASE_COLUMNS.sql`，这个脚本会：
- ✅ 自动检查列是否存在
- ✅ 只添加缺失的列
- ✅ 不会报错

```bash
mysql -u buyticket -p buyticket < FIX_DATABASE_COLUMNS.sql
```

---

### 方案 2：手动检查并修复

#### 步骤 1：检查当前表结构

```sql
USE buyticket;

-- 查看 ticket_order 表结构
DESC ticket_order;

-- 查看 mall_order 表结构
DESC mall_order;
```

#### 步骤 2：根据检查结果，只执行缺失的列

**如果 ticket_order 没有 pay_time 列**：
```sql
ALTER TABLE ticket_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
```

**如果 ticket_order 没有 order_no 列**：
```sql
ALTER TABLE ticket_order ADD COLUMN order_no VARCHAR(32) UNIQUE COMMENT '订单号（唯一）' AFTER id;
```

**如果 mall_order 没有 pay_time 列**：
```sql
ALTER TABLE mall_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
```

**如果 mall_order 没有 order_no 列**：
```sql
ALTER TABLE mall_order ADD COLUMN order_no VARCHAR(32) UNIQUE COMMENT '订单号（唯一）' AFTER id;
```

---

### 方案 3：删除后重新添加（不推荐，会丢失数据）

**⚠️ 警告**：这会删除列中的所有数据！

```sql
-- 删除 pay_time 列（如果存在）
ALTER TABLE ticket_order DROP COLUMN IF EXISTS pay_time;
ALTER TABLE mall_order DROP COLUMN IF EXISTS pay_time;

-- 重新添加
ALTER TABLE ticket_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
ALTER TABLE mall_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
```

---

## 🔍 验证修复结果

执行以下 SQL 检查所有必需的列是否存在：

```sql
USE buyticket;

-- 检查 ticket_order 表
SELECT 
    'ticket_order' AS table_name,
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'buyticket' 
AND TABLE_NAME = 'ticket_order'
AND COLUMN_NAME IN ('order_no', 'pay_time')
ORDER BY ORDINAL_POSITION;

-- 检查 mall_order 表
SELECT 
    'mall_order' AS table_name,
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'buyticket' 
AND TABLE_NAME = 'mall_order'
AND COLUMN_NAME IN ('order_no', 'pay_time')
ORDER BY ORDINAL_POSITION;
```

**期望结果**：应该看到 4 行记录
- ticket_order.order_no
- ticket_order.pay_time
- mall_order.order_no
- mall_order.pay_time

---

## 📋 完整的表结构检查

```sql
-- 查看完整的 ticket_order 表结构
SHOW CREATE TABLE ticket_order\G

-- 查看完整的 mall_order 表结构
SHOW CREATE TABLE mall_order\G
```

**期望的表结构**：

### ticket_order 应该包含：
- ✅ id
- ✅ order_no (VARCHAR(32), UNIQUE)
- ✅ user_id
- ✅ total_amount
- ✅ status
- ✅ contact_name
- ✅ contact_phone
- ✅ create_time
- ✅ pay_time (DATETIME, NULL)

### mall_order 应该包含：
- ✅ id
- ✅ order_no (VARCHAR(32), UNIQUE)
- ✅ user_id
- ✅ total_amount
- ✅ status
- ✅ receiver_name
- ✅ receiver_phone
- ✅ receiver_address
- ✅ create_time
- ✅ pay_time (DATETIME, NULL)

---

## 🎯 快速修复命令

如果你确定要重新创建表（会丢失所有数据），可以执行：

```sql
USE buyticket;

-- 备份数据（可选）
CREATE TABLE ticket_order_backup AS SELECT * FROM ticket_order;
CREATE TABLE mall_order_backup AS SELECT * FROM mall_order;

-- 删除表
DROP TABLE IF EXISTS ticket_order;
DROP TABLE IF EXISTS mall_order;

-- 重新创建表（使用 PRODUCTION_DATABASE_INIT.sql 中的建表语句）
```

然后执行 `PRODUCTION_DATABASE_INIT.sql` 中的建表语句。

---

## 💡 推荐做法

1. **先检查**：执行 `DESC ticket_order;` 和 `DESC mall_order;`
2. **看结果**：确认哪些列缺失
3. **只添加缺失的列**：不要重复添加已存在的列
4. **验证**：执行验证 SQL 确认所有列都存在

---

## 🆘 如果还是报错

请提供以下信息：

1. 当前表结构：
```sql
DESC ticket_order;
DESC mall_order;
```

2. 错误信息的完整截图

3. MySQL 版本：
```sql
SELECT VERSION();
```

这样我可以提供更精确的解决方案。
