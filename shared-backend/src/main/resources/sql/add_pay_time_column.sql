-- 为 ticket_order 表添加 pay_time 列（如果不存在）
ALTER TABLE ticket_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;

-- 为 mall_order 表添加 pay_time 列（如果不存在）
ALTER TABLE mall_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
