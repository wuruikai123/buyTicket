-- 创建数据库 (如果在本地执行，请先手动创建数据库 buy_ticket)
 CREATE DATABASE IF NOT EXISTS buy_ticket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 USE buy_ticket;

SET NAMES utf8mb4;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `uid` VARCHAR(50) DEFAULT NULL COMMENT '用户唯一标识',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `balance` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '余额',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 展览表
CREATE TABLE IF NOT EXISTS `exhibition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '展览名称',
    `short_desc` VARCHAR(255) DEFAULT NULL COMMENT '短描述',
    `description` TEXT DEFAULT NULL COMMENT '详细介绍',
    `start_date` DATE DEFAULT NULL COMMENT '开始日期',
    `end_date` DATE DEFAULT NULL COMMENT '结束日期',
    `status` TINYINT DEFAULT 0 COMMENT '状态 (0:待开始, 1:进行中, 2:已结束)',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '票价',
    `cover_image` LONGTEXT DEFAULT NULL COMMENT '封面图片URL或Base64',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='展览表';

-- 3. 票务库存表
CREATE TABLE IF NOT EXISTS `ticket_inventory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exhibition_id` BIGINT NOT NULL COMMENT '关联展览ID',
    `ticket_date` DATE NOT NULL COMMENT '票务日期',
    `time_slot` VARCHAR(50) NOT NULL COMMENT '时间段',
    `total_count` INT DEFAULT 0 COMMENT '总票数',
    `sold_count` INT DEFAULT 0 COMMENT '已售票数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exhibition_date_slot` (`exhibition_id`, `ticket_date`, `time_slot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票务库存表';

-- 4. 订单表 (门票订单)
CREATE TABLE IF NOT EXISTS `ticket_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态 (0:待支付, 1:待使用, 2:已使用, 3:已取消)',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门票订单表';

-- 5. 订单详情表 (门票)
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `exhibition_id` BIGINT NOT NULL COMMENT '关联展览ID',
    `exhibition_name` VARCHAR(100) DEFAULT NULL COMMENT '展览名称快照',
    `ticket_date` DATE DEFAULT NULL COMMENT '使用日期',
    `time_slot` VARCHAR(50) DEFAULT NULL COMMENT '时间段',
    `quantity` INT DEFAULT NULL COMMENT '数量',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '单价',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门票订单详情表';

-- 6. 文创产品表
CREATE TABLE IF NOT EXISTS `sys_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '单价',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `cover_image` LONGTEXT DEFAULT NULL COMMENT '封面图片URL或Base64',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (0:下架, 1:上架)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文创产品表';

-- 7. 购物车表
CREATE TABLE IF NOT EXISTS `cart_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 8. 商城订单表
CREATE TABLE IF NOT EXISTS `mall_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态 (0:待支付, 1:待发货, 2:已发货, 3:已完成, 4:已取消)',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单表';

-- 9. 商城订单详情表
CREATE TABLE IF NOT EXISTS `mall_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '关联商城订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称快照',
    `product_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片快照',
    `quantity` INT DEFAULT NULL COMMENT '数量',
    `price` DECIMAL(10, 2) DEFAULT NULL COMMENT '单价',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单详情表';

-- 10. 管理员表
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (1:启用 0:禁用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ==========================================
-- 初始化测试数据 (使用 INSERT IGNORE 避免重复报错)
-- ==========================================

-- 0. 管理员数据
INSERT IGNORE INTO admin_user (id, username, password, real_name, status) VALUES 
(1, 'admin', '123456', '超级管理员', 1);

-- 1. 用户数据
INSERT IGNORE INTO sys_user (id, username, password, uid, phone, balance) VALUES 
(1, 'zhangsan', '123456', 'UID001', '13800138000', 1000.00),
(2, 'lisi', '123456', 'UID002', '13900139000', 500.00);

-- 2. 展览数据
INSERT IGNORE INTO exhibition (id, name, short_desc, description, start_date, end_date, status, price, cover_image, tags) VALUES 
(1, '2025年当代艺术双年展', '年度重磅 | 全球顶尖艺术家汇聚', '这是一场汇聚了全球顶尖当代艺术家的盛会，涵盖绘画、雕塑、装置艺术等多种形式。展览将持续全年，定期更换展品。', '2025-01-01', '2025-12-31', 1, 150.00, '/images/exhibition_current.jpg', '热门,推荐'),
(2, '印象派大师作品展', '经典回顾 | 莫奈、雷诺阿真迹', '精选莫奈、雷诺阿等印象派大师的经典画作，带您重回光与影的时代。', '2025-09-30', '2025-10-15', 1, 120.00, '/images/exhibition_1.jpg', '艺术,经典'),
(3, '未来科技互动展', '沉浸体验 | 探索未来生活', '通过最新的VR/AR技术，带您提前体验2050年的未来生活方式。适合亲子家庭。', '2025-11-01', '2025-11-30', 0, 80.00, '/images/exhibition_2.jpg', '科技,亲子'),
(4, '古代青铜器特展', '国宝云集 | 穿越千年', '展出商周时期的珍贵青铜器，感受中华文明的厚重与精美。', '2025-05-01', '2025-06-30', 2, 0.00, '/images/exhibition_1.jpg', '历史,免费');

-- 3. 票务库存数据 (针对展览2：印象派)
INSERT IGNORE INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count) VALUES 
(2, '2025-10-11', '09:00-12:00', 100, 10),
(2, '2025-10-11', '12:00-14:00', 100, 45),
(2, '2025-10-11', '14:00-16:00', 100, 80),
(2, '2025-10-12', '09:00-12:00', 100, 5),
(2, '2025-10-12', '12:00-14:00', 100, 20);

-- 4. 文创产品数据
INSERT IGNORE INTO sys_product (id, name, description, price, stock, cover_image, status) VALUES 
(1, '艺术帆布袋', '印有梵高星空图案的优质帆布袋，耐磨耐用。', 59.90, 100, '/images/product_1.jpg', 1),
(2, '复古笔记本', '精美复古封面，进口道林纸，书写流畅。', 29.90, 200, '/images/product_2.jpg', 1),
(3, '博物馆联名马克杯', '骨瓷材质，手绘图案，限量发售。', 88.00, 50, '/images/product_1.jpg', 1),
(4, '创意金属书签', '镂空设计，精美包装，送礼佳品。', 15.00, 500, '/images/product_2.jpg', 1);

-- 5. 购物车数据 (测试用户 zhangsan)
INSERT IGNORE INTO cart_item (user_id, product_id, quantity) VALUES 
(1, 1, 1),
(1, 3, 2);

