-- 为 mall_order 表添加 order_no 列（如果不存在）
-- 注意：MySQL 5.7 不支持 IF NOT EXISTS，如果列已存在会报错，可以忽略
ALTER TABLE mall_order ADD COLUMN order_no VARCHAR(32) UNIQUE COMMENT '订单号（唯一）' AFTER id;

-- 为现有的订单生成订单号（如果有的话）
UPDATE mall_order SET order_no = CONCAT('MO', DATE_FORMAT(create_time, '%Y%m%d'), LPAD(id, 8, '0')) WHERE order_no IS NULL;
