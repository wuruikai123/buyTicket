-- 修复订单号问题
-- 请在 MySQL 中手动执行此脚本

USE buy_ticket;

-- 1. 检查表结构
SHOW COLUMNS FROM ticket_order LIKE 'order_no';

-- 2. 如果 order_no 字段不存在，添加它
-- ALTER TABLE ticket_order 
-- ADD COLUMN order_no VARCHAR(32) DEFAULT '' COMMENT '订单号（唯一）' AFTER id;

-- 3. 为所有没有订单号的订单生成订单号
UPDATE ticket_order 
SET order_no = CONCAT('T', DATE_FORMAT(IFNULL(create_time, NOW()), '%Y%m%d'), LPAD(id, 8, '0'))
WHERE order_no IS NULL OR order_no = '' OR order_no = '0';

-- 4. 验证：查看所有订单是否都有订单号
SELECT 
    COUNT(*) as total_orders,
    SUM(CASE WHEN order_no IS NULL OR order_no = '' THEN 1 ELSE 0 END) as orders_without_no,
    SUM(CASE WHEN order_no IS NOT NULL AND order_no != '' THEN 1 ELSE 0 END) as orders_with_no
FROM ticket_order;

-- 5. 查看示例订单
SELECT id, order_no, user_id, contact_name, status, create_time 
FROM ticket_order 
ORDER BY id DESC 
LIMIT 10;

-- 6. 如果需要，添加唯一索引（注意：如果已存在会报错，可以忽略）
-- ALTER TABLE ticket_order ADD UNIQUE INDEX uk_order_no (order_no);

-- 7. 查找一个可用于测试的订单号（状态为待使用）
SELECT order_no, contact_name, status 
FROM ticket_order 
WHERE status = 1 AND order_no IS NOT NULL AND order_no != ''
LIMIT 1;
