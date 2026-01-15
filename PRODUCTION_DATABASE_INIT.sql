-- ============================================
-- 展览门票管理系统 - 生产环境数据库初始化脚本
-- 版本: 1.0
-- 创建日期: 2026-01-15
-- 说明: 包含所有表结构、索引、初始数据
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 创建数据库（如果不存在）
-- ============================================
CREATE DATABASE IF NOT EXISTS `buyticket` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `buyticket`;

-- ============================================
-- 2. 用户表
-- ============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` LONGTEXT DEFAULT NULL COMMENT '头像URL或Base64',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色 (USER:普通用户, ADMIN:管理员, SELLER:商家)',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0:禁用, 1:启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 3. 用户地址表
-- ============================================
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
    `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址 (0:否, 1:是)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

-- ============================================
-- 4. 展览表
-- ============================================
DROP TABLE IF EXISTS `exhibition`;
CREATE TABLE `exhibition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '展览名称',
    `description` TEXT DEFAULT NULL COMMENT '展览描述',
    `location` VARCHAR(255) DEFAULT NULL COMMENT '展览地点',
    `start_date` DATE DEFAULT NULL COMMENT '开始日期',
    `end_date` DATE DEFAULT NULL COMMENT '结束日期',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '门票价格',
    `cover_image` LONGTEXT DEFAULT NULL COMMENT '封面图片URL或Base64',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0:下架, 1:上架)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_date` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='展览表';

-- ============================================
-- 5. 门票库存表
-- ============================================
DROP TABLE IF EXISTS `ticket_inventory`;
CREATE TABLE `ticket_inventory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exhibition_id` BIGINT NOT NULL COMMENT '展览ID',
    `ticket_date` DATE NOT NULL COMMENT '门票日期',
    `time_slot` VARCHAR(50) NOT NULL COMMENT '时间段 (如: 09:00-12:00)',
    `total_count` INT DEFAULT 0 COMMENT '总票数',
    `sold_count` INT DEFAULT 0 COMMENT '已售票数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exhibition_date_time` (`exhibition_id`, `ticket_date`, `time_slot`),
    KEY `idx_exhibition_id` (`exhibition_id`),
    KEY `idx_ticket_date` (`ticket_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门票库存表';

-- ============================================
-- 6. 门票订单表
-- ============================================
DROP TABLE IF EXISTS `ticket_order`;
CREATE TABLE `ticket_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号（唯一）',
    `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态 (0:待支付, 1:待使用, 2:已使用, 3:已取消)',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门票订单表';

-- ============================================
-- 7. 订单详情表（门票）
-- ============================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `exhibition_id` BIGINT NOT NULL COMMENT '关联展览ID',
    `exhibition_name` VARCHAR(100) DEFAULT NULL COMMENT '展览名称快照',
    `ticket_date` DATE DEFAULT NULL COMMENT '使用日期',
    `time_slot` VARCHAR(50) DEFAULT NULL COMMENT '时间段',
    `quantity` INT DEFAULT NULL COMMENT '数量',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '单价',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_exhibition_id` (`exhibition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门票订单详情表';

-- ============================================
-- 8. 文创产品表
-- ============================================
DROP TABLE IF EXISTS `sys_product`;
CREATE TABLE `sys_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '单价',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `cover_image` LONGTEXT DEFAULT NULL COMMENT '封面图片URL或Base64',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0:下架, 1:上架)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文创产品表';

-- ============================================
-- 9. 购物车表
-- ============================================
DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================
-- 10. 商城订单表
-- ============================================
DROP TABLE IF EXISTS `mall_order`;
CREATE TABLE `mall_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(32) UNIQUE COMMENT '订单号（唯一）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态 (0:待支付, 1:待发货, 2:已发货, 3:已完成, 4:已取消)',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城订单表';

-- ============================================
-- 11. 商城订单详情表
-- ============================================
DROP TABLE IF EXISTS `mall_order_item`;
CREATE TABLE `mall_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '关联商城订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称快照',
    `product_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片快照',
    `quantity` INT DEFAULT NULL COMMENT '数量',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '单价',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商城订单详情表';

-- ============================================
-- 12. 轮播图表
-- ============================================
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
    `image_url` LONGTEXT DEFAULT NULL COMMENT '图片URL或Base64',
    `link_url` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    `sort_order` INT DEFAULT 0 COMMENT '排序（数字越小越靠前）',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0:禁用, 1:启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- ============================================
-- 13. 插入初始数据
-- ============================================

-- 插入管理员账号（密码: admin123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN', 1);

-- 插入商家账号（密码: seller123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('seller', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '商家', 'SELLER', 1);

-- 插入测试用户（密码: user123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `role`, `status`) VALUES
('user001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试用户', '13800138000', 'USER', 1);

-- 插入示例展览数据
INSERT INTO `exhibition` (`name`, `description`, `location`, `start_date`, `end_date`, `price`, `status`) VALUES
('故宫博物院珍宝展', '展示故宫博物院珍藏的历代珍宝，包括玉器、瓷器、书画等', '国家博物馆一楼展厅', '2026-01-01', '2026-12-31', 80.00, 1),
('现代艺术展', '汇集国内外知名艺术家的现代艺术作品', '当代艺术馆', '2026-02-01', '2026-06-30', 60.00, 1),
('科技创新展', '展示最新的科技创新成果和未来科技趋势', '科技馆三楼', '2026-03-01', '2026-08-31', 50.00, 1);

-- 插入门票库存（为每个展览创建未来30天的库存）
-- 这里只插入示例数据，实际使用时需要根据需求批量生成
INSERT INTO `ticket_inventory` (`exhibition_id`, `ticket_date`, `time_slot`, `total_count`, `sold_count`) VALUES
(1, '2026-02-01', '09:00-12:00', 100, 0),
(1, '2026-02-01', '14:00-17:00', 100, 0),
(1, '2026-02-02', '09:00-12:00', 100, 0),
(1, '2026-02-02', '14:00-17:00', 100, 0),
(2, '2026-02-01', '09:00-12:00', 80, 0),
(2, '2026-02-01', '14:00-17:00', 80, 0),
(3, '2026-03-01', '09:00-12:00', 120, 0),
(3, '2026-03-01', '14:00-17:00', 120, 0);

-- 插入示例文创产品
INSERT INTO `sys_product` (`name`, `description`, `price`, `stock`, `status`) VALUES
('故宫文创笔记本', '精美的故宫主题笔记本，采用优质纸张', 39.90, 500, 1),
('青花瓷书签', '精致的青花瓷图案书签套装', 29.90, 300, 1),
('文创帆布包', '印有展览主题图案的环保帆布包', 59.90, 200, 1),
('艺术明信片套装', '包含10张精美艺术明信片', 19.90, 1000, 1),
('文创冰箱贴', '创意冰箱贴，多款图案可选', 15.90, 800, 1);

-- 插入示例轮播图
INSERT INTO `banner` (`title`, `link_url`, `sort_order`, `status`) VALUES
('故宫珍宝展', '/exhibition/1', 1, 1),
('现代艺术展', '/exhibition/2', 2, 1),
('科技创新展', '/exhibition/3', 3, 1);

-- ============================================
-- 14. 创建视图（可选）
-- ============================================

-- 订单统计视图
CREATE OR REPLACE VIEW `v_order_statistics` AS
SELECT 
    DATE(create_time) as order_date,
    COUNT(*) as order_count,
    SUM(total_amount) as total_amount,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pending_count,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as paid_count,
    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as used_count,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as cancelled_count
FROM `ticket_order`
GROUP BY DATE(create_time);

-- 库存预警视图
CREATE OR REPLACE VIEW `v_inventory_warning` AS
SELECT 
    ti.id,
    e.name as exhibition_name,
    ti.ticket_date,
    ti.time_slot,
    ti.total_count,
    ti.sold_count,
    (ti.total_count - ti.sold_count) as remaining_count,
    ROUND((ti.sold_count / ti.total_count * 100), 2) as sold_percentage
FROM `ticket_inventory` ti
LEFT JOIN `exhibition` e ON ti.exhibition_id = e.id
WHERE (ti.total_count - ti.sold_count) < 20
ORDER BY ti.ticket_date, ti.time_slot;

-- ============================================
-- 15. 创建存储过程（可选）
-- ============================================

-- 批量生成门票库存的存储过程
DELIMITER $$

DROP PROCEDURE IF EXISTS `sp_generate_ticket_inventory`$$

CREATE PROCEDURE `sp_generate_ticket_inventory`(
    IN p_exhibition_id BIGINT,
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_total_count INT
)
BEGIN
    DECLARE v_current_date DATE;
    SET v_current_date = p_start_date;
    
    WHILE v_current_date <= p_end_date DO
        -- 上午场
        INSERT INTO `ticket_inventory` (`exhibition_id`, `ticket_date`, `time_slot`, `total_count`, `sold_count`)
        VALUES (p_exhibition_id, v_current_date, '09:00-12:00', p_total_count, 0)
        ON DUPLICATE KEY UPDATE `total_count` = p_total_count;
        
        -- 下午场
        INSERT INTO `ticket_inventory` (`exhibition_id`, `ticket_date`, `time_slot`, `total_count`, `sold_count`)
        VALUES (p_exhibition_id, v_current_date, '14:00-17:00', p_total_count, 0)
        ON DUPLICATE KEY UPDATE `total_count` = p_total_count;
        
        SET v_current_date = DATE_ADD(v_current_date, INTERVAL 1 DAY);
    END WHILE;
END$$

DELIMITER ;

-- ============================================
-- 16. 设置权限（根据实际情况调整）
-- ============================================

-- 创建应用数据库用户（请修改密码）
-- CREATE USER 'buyticket'@'localhost' IDENTIFIED BY 'your_strong_password_here';
-- GRANT ALL PRIVILEGES ON buyticket.* TO 'buyticket'@'localhost';
-- FLUSH PRIVILEGES;

-- ============================================
-- 17. 完成
-- ============================================

SET FOREIGN_KEY_CHECKS = 1;

-- 显示所有表
SHOW TABLES;

-- 显示数据库信息
SELECT 
    '数据库初始化完成' as message,
    DATABASE() as database_name,
    COUNT(*) as table_count
FROM information_schema.tables 
WHERE table_schema = DATABASE();

-- ============================================
-- 使用说明
-- ============================================
-- 1. 在 MySQL 客户端执行此脚本：
--    mysql -u root -p < PRODUCTION_DATABASE_INIT.sql
--
-- 2. 或者在 MySQL 命令行中：
--    source /path/to/PRODUCTION_DATABASE_INIT.sql
--
-- 3. 或者在 Navicat/MySQL Workbench 中直接运行
--
-- 4. 初始账号：
--    管理员: admin / admin123
--    商家: seller / seller123
--    用户: user001 / user123
--
-- 5. 生成更多库存数据：
--    CALL sp_generate_ticket_inventory(1, '2026-02-01', '2026-02-28', 100);
--
-- ============================================
