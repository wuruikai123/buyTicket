-- 更新表字段类型（解决字段长度不足问题）
-- 使用 continue-on-error=true 配置，即使表不存在也不会中断

-- 修改 exhibition 表的 cover_image 字段为 LONGTEXT
ALTER TABLE `exhibition` MODIFY COLUMN `cover_image` LONGTEXT DEFAULT NULL COMMENT '封面图片URL或Base64';

-- 修改 sys_product 表的 cover_image 字段为 LONGTEXT
ALTER TABLE `sys_product` MODIFY COLUMN `cover_image` LONGTEXT DEFAULT NULL COMMENT '封面图片URL或Base64';

-- 修改 mall_order_item 表的 product_image 字段为 LONGTEXT
ALTER TABLE `mall_order_item` MODIFY COLUMN `product_image` LONGTEXT DEFAULT NULL COMMENT '商品图片快照';

-- 为 ticket_inventory 表添加 create_time 字段
-- 注意：如果字段已存在会报错，但 continue-on-error=true 会忽略
ALTER TABLE `ticket_inventory` ADD COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
