-- 为 ticket_order 表添加订单号字段

-- 添加 order_no 字段
ALTER TABLE `ticket_order` ADD COLUMN `order_no` VARCHAR(32) COMMENT '订单号（唯一）' AFTER `id`;

-- 为现有订单生成订单号
UPDATE `ticket_order` SET `order_no` = CONCAT('T', DATE_FORMAT(create_time, '%Y%m%d'), LPAD(id, 8, '0')) WHERE `order_no` IS NULL;

-- 添加唯一索引
ALTER TABLE `ticket_order` ADD UNIQUE KEY `uk_order_no` (`order_no`);
