-- 为 ticket_order 表添加 order_no 字段（如果不存在）
-- 此脚本用于更新已有数据库

USE buy_ticket;

-- 检查并添加 order_no 字段
ALTER TABLE ticket_order 
ADD COLUMN IF NOT EXISTS order_no VARCHAR(32) NOT NULL DEFAULT '' COMMENT '订单号（唯一）' AFTER id;

-- 添加唯一索引（如果不存在）
ALTER TABLE ticket_order 
ADD UNIQUE INDEX IF NOT EXISTS uk_order_no (order_no);

-- 为已存在的订单生成订单号（如果 order_no 为空）
UPDATE ticket_order 
SET order_no = CONCAT('T', DATE_FORMAT(create_time, '%Y%m%d'), LPAD(id, 8, '0'))
WHERE order_no = '' OR order_no IS NULL;

-- 验证更新
SELECT id, order_no, user_id, total_amount, status, create_time 
FROM ticket_order 
ORDER BY id DESC 
LIMIT 10;
