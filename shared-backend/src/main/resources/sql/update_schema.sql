-- 更新数据库表结构
-- 执行此脚本以修复字段长度问题和添加库存字段

-- ==========================================
-- 1. 修复图片字段长度问题
-- ==========================================

-- 修改 exhibition 表的 cover_image 字段为 LONGTEXT
ALTER TABLE `exhibition` MODIFY COLUMN `cover_image` LONGTEXT DEFAULT NULL COMMENT '封面图片URL或Base64';

-- 修改 sys_product 表的 cover_image 字段为 LONGTEXT
ALTER TABLE `sys_product` MODIFY COLUMN `cover_image` LONGTEXT DEFAULT NULL COMMENT '封面图片URL或Base64';

-- 修改 mall_order_item 表的 product_image 字段为 LONGTEXT
ALTER TABLE `mall_order_item` MODIFY COLUMN `product_image` LONGTEXT DEFAULT NULL COMMENT '商品图片快照';

-- ==========================================
-- 2. 确保 ticket_inventory 表有库存字段
-- ==========================================

-- 添加 create_time 字段（如果不存在会报错，可忽略）
ALTER TABLE `ticket_inventory` ADD COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- 确保 total_count 和 sold_count 字段存在
-- 如果字段已存在会报错，可忽略
ALTER TABLE `ticket_inventory` ADD COLUMN `total_count` INT DEFAULT 0 COMMENT '总票数';
ALTER TABLE `ticket_inventory` ADD COLUMN `sold_count` INT DEFAULT 0 COMMENT '已售票数';

-- ==========================================
-- 3. 更新测试数据 - 为展览添加库存
-- ==========================================

-- 清空旧的库存数据
DELETE FROM `ticket_inventory`;

-- 为展览1（当代艺术双年展）添加库存
INSERT INTO `ticket_inventory` (`exhibition_id`, `ticket_date`, `time_slot`, `total_count`, `sold_count`) VALUES
(1, '2025-12-07', '09:00-12:00', 100, 10),
(1, '2025-12-07', '12:00-14:00', 100, 20),
(1, '2025-12-07', '14:00-16:00', 100, 30),
(1, '2025-12-08', '09:00-12:00', 100, 10),
(1, '2025-12-08', '12:00-14:00', 100, 20),
(1, '2025-12-08', '14:00-16:00', 100, 30),
(1, '2025-12-09', '09:00-12:00', 100, 5),
(1, '2025-12-09', '12:00-14:00', 100, 15),
(1, '2025-12-09', '14:00-16:00', 100, 25),
(1, '2025-12-10', '09:00-12:00', 100, 0),
(1, '2025-12-10', '12:00-14:00', 100, 0),
(1, '2025-12-10', '14:00-16:00', 100, 0);

-- 为展览2（印象派大师作品展）添加库存
INSERT INTO `ticket_inventory` (`exhibition_id`, `ticket_date`, `time_slot`, `total_count`, `sold_count`) VALUES
(2, '2025-12-07', '09:00-12:00', 50, 10),
(2, '2025-12-07', '12:00-14:00', 50, 20),
(2, '2025-12-07', '14:00-16:00', 50, 15),
(2, '2025-12-08', '09:00-12:00', 50, 45),
(2, '2025-12-08', '12:00-14:00', 50, 48),
(2, '2025-12-08', '14:00-16:00', 50, 50),
(2, '2025-12-09', '09:00-12:00', 50, 10),
(2, '2025-12-09', '12:00-14:00', 50, 20),
(2, '2025-12-09', '14:00-16:00', 50, 30);

-- 为展览3（未来科技互动展）添加库存
INSERT INTO `ticket_inventory` (`exhibition_id`, `ticket_date`, `time_slot`, `total_count`, `sold_count`) VALUES
(3, '2025-12-07', '09:00-12:00', 80, 0),
(3, '2025-12-07', '12:00-14:00', 80, 0),
(3, '2025-12-07', '14:00-16:00', 80, 0),
(3, '2025-12-08', '09:00-12:00', 80, 0),
(3, '2025-12-08', '12:00-14:00', 80, 0),
(3, '2025-12-08', '14:00-16:00', 80, 0);

-- ==========================================
-- 4. 添加测试订单数据
-- ==========================================

-- 清空旧订单数据
DELETE FROM `order_item`;
DELETE FROM `ticket_order`;

-- 添加门票订单测试数据（用户ID=1，即zhangsan）
INSERT INTO `ticket_order` (`id`, `user_id`, `total_amount`, `status`, `contact_name`, `contact_phone`, `create_time`) VALUES
(1, 1, 150.00, 1, '张三', '13800138000', '2025-12-06 10:30:00'),
(2, 1, 240.00, 0, '张三', '13800138000', '2025-12-07 09:15:00'),
(3, 1, 80.00, 2, '李四', '13900139000', '2025-12-05 14:20:00');

-- 添加订单详情
INSERT INTO `order_item` (`order_id`, `exhibition_id`, `exhibition_name`, `ticket_date`, `time_slot`, `quantity`, `price`) VALUES
(1, 1, '2025年当代艺术双年展', '2025-12-08', '09:00-12:00', 1, 150.00),
(2, 2, '印象派大师作品展', '2025-12-09', '12:00-14:00', 2, 120.00),
(3, 3, '未来科技互动展', '2025-12-07', '14:00-16:00', 1, 80.00);
