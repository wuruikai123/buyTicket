-- ============================================
-- 简单版本：安全地添加缺失的列
-- 使用方法：直接在 MySQL 客户端或 Navicat 中执行
-- ============================================

USE buyticket;

-- 方法：先查看当前表结构
SELECT '当前 ticket_order 表结构：' AS info;
DESC ticket_order;

SELECT '当前 mall_order 表结构：' AS info;
DESC mall_order;

-- ============================================
-- 如果上面显示没有 pay_time 列，执行下面的 SQL
-- 如果已经有了，就跳过对应的 SQL
-- ============================================

-- 1. 为 ticket_order 添加 pay_time（如果报错说已存在，忽略即可）
-- ALTER TABLE ticket_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;

-- 2. 为 mall_order 添加 pay_time（如果报错说已存在，忽略即可）
-- ALTER TABLE mall_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;

-- 3. 为 mall_order 添加 order_no（如果报错说已存在，忽略即可）
-- ALTER TABLE mall_order ADD COLUMN order_no VARCHAR(32) UNIQUE COMMENT '订单号（唯一）' AFTER id;

-- 4. 为 ticket_order 添加 order_no（如果报错说已存在，忽略即可）
-- ALTER TABLE ticket_order ADD COLUMN order_no VARCHAR(32) UNIQUE COMMENT '订单号（唯一）' AFTER id;

-- ============================================
-- 验证：查看最终的表结构
-- ============================================

SELECT '最终 ticket_order 表结构：' AS info;
DESC ticket_order;

SELECT '最终 mall_order 表结构：' AS info;
DESC mall_order;

-- ============================================
-- 检查结果
-- ============================================

SELECT 
    'ticket_order 是否有 pay_time' AS check_item,
    IF(EXISTS(
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'buyticket' 
        AND TABLE_NAME = 'ticket_order' 
        AND COLUMN_NAME = 'pay_time'
    ), '✓ 存在', '✗ 不存在') AS result;

SELECT 
    'ticket_order 是否有 order_no' AS check_item,
    IF(EXISTS(
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'buyticket' 
        AND TABLE_NAME = 'ticket_order' 
        AND COLUMN_NAME = 'order_no'
    ), '✓ 存在', '✗ 不存在') AS result;

SELECT 
    'mall_order 是否有 pay_time' AS check_item,
    IF(EXISTS(
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'buyticket' 
        AND TABLE_NAME = 'mall_order' 
        AND COLUMN_NAME = 'pay_time'
    ), '✓ 存在', '✗ 不存在') AS result;

SELECT 
    'mall_order 是否有 order_no' AS check_item,
    IF(EXISTS(
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'buyticket' 
        AND TABLE_NAME = 'mall_order' 
        AND COLUMN_NAME = 'order_no'
    ), '✓ 存在', '✗ 不存在') AS result;
