-- ============================================
-- 安全地添加缺失的列
-- 如果列已存在，会自动跳过，不会报错
-- ============================================

USE buyticket;

-- ============================================
-- 1. 检查并添加 ticket_order 表的 pay_time 列
-- ============================================

-- 先删除可能存在的临时存储过程
DROP PROCEDURE IF EXISTS add_pay_time_to_ticket_order;

DELIMITER $$

CREATE PROCEDURE add_pay_time_to_ticket_order()
BEGIN
    -- 检查列是否存在
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'buyticket' 
        AND TABLE_NAME = 'ticket_order' 
        AND COLUMN_NAME = 'pay_time'
    ) THEN
        -- 列不存在，添加它
        ALTER TABLE ticket_order 
        ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
        SELECT 'ticket_order 表已添加 pay_time 列' AS message;
    ELSE
        -- 列已存在，跳过
        SELECT 'ticket_order 表的 pay_time 列已存在，跳过' AS message;
    END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL add_pay_time_to_ticket_order();

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS add_pay_time_to_ticket_order;

-- ============================================
-- 2. 检查并添加 mall_order 表的 pay_time 列
-- ============================================

DROP PROCEDURE IF EXISTS add_pay_time_to_mall_order;

DELIMITER $$

CREATE PROCEDURE add_pay_time_to_mall_order()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'buyticket' 
        AND TABLE_NAME = 'mall_order' 
        AND COLUMN_NAME = 'pay_time'
    ) THEN
        ALTER TABLE mall_order 
        ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
        SELECT 'mall_order 表已添加 pay_time 列' AS message;
    ELSE
        SELECT 'mall_order 表的 pay_time 列已存在，跳过' AS message;
    END IF;
END$$

DELIMITER ;

CALL add_pay_time_to_mall_order();
DROP PROCEDURE IF EXISTS add_pay_time_to_mall_order;

-- ============================================
-- 3. 检查并添加 mall_order 表的 order_no 列
-- ============================================

DROP PROCEDURE IF EXISTS add_order_no_to_mall_order;

DELIMITER $$

CREATE PROCEDURE add_order_no_to_mall_order()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'buyticket' 
        AND TABLE_NAME = 'mall_order' 
        AND COLUMN_NAME = 'order_no'
    ) THEN
        ALTER TABLE mall_order 
        ADD COLUMN order_no VARCHAR(32) UNIQUE COMMENT '订单号（唯一）' AFTER id;
        SELECT 'mall_order 表已添加 order_no 列' AS message;
    ELSE
        SELECT 'mall_order 表的 order_no 列已存在，跳过' AS message;
    END IF;
END$$

DELIMITER ;

CALL add_order_no_to_mall_order();
DROP PROCEDURE IF EXISTS add_order_no_to_mall_order;

-- ============================================
-- 4. 检查并添加 ticket_order 表的 order_no 列（如果需要）
-- ============================================

DROP PROCEDURE IF EXISTS add_order_no_to_ticket_order;

DELIMITER $$

CREATE PROCEDURE add_order_no_to_ticket_order()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'buyticket' 
        AND TABLE_NAME = 'ticket_order' 
        AND COLUMN_NAME = 'order_no'
    ) THEN
        ALTER TABLE ticket_order 
        ADD COLUMN order_no VARCHAR(32) UNIQUE COMMENT '订单号（唯一）' AFTER id;
        SELECT 'ticket_order 表已添加 order_no 列' AS message;
    ELSE
        SELECT 'ticket_order 表的 order_no 列已存在，跳过' AS message;
    END IF;
END$$

DELIMITER ;

CALL add_order_no_to_ticket_order();
DROP PROCEDURE IF EXISTS add_order_no_to_ticket_order;

-- ============================================
-- 5. 验证所有列是否存在
-- ============================================

SELECT 
    '验证结果' AS title,
    (SELECT COUNT(*) FROM information_schema.COLUMNS 
     WHERE TABLE_SCHEMA = 'buyticket' 
     AND TABLE_NAME = 'ticket_order' 
     AND COLUMN_NAME = 'pay_time') AS ticket_order_pay_time,
    (SELECT COUNT(*) FROM information_schema.COLUMNS 
     WHERE TABLE_SCHEMA = 'buyticket' 
     AND TABLE_NAME = 'ticket_order' 
     AND COLUMN_NAME = 'order_no') AS ticket_order_order_no,
    (SELECT COUNT(*) FROM information_schema.COLUMNS 
     WHERE TABLE_SCHEMA = 'buyticket' 
     AND TABLE_NAME = 'mall_order' 
     AND COLUMN_NAME = 'pay_time') AS mall_order_pay_time,
    (SELECT COUNT(*) FROM information_schema.COLUMNS 
     WHERE TABLE_SCHEMA = 'buyticket' 
     AND TABLE_NAME = 'mall_order' 
     AND COLUMN_NAME = 'order_no') AS mall_order_order_no;

-- ============================================
-- 6. 显示表结构
-- ============================================

SELECT '=== ticket_order 表结构 ===' AS info;
DESC ticket_order;

SELECT '=== mall_order 表结构 ===' AS info;
DESC mall_order;

-- ============================================
-- 完成
-- ============================================

SELECT '数据库列修复完成！' AS message;
