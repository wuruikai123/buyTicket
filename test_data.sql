-- 测试数据插入脚本
-- 用于测试支付流程

USE buy_ticket;

-- 1. 插入轮播图数据
INSERT INTO banner (id, title, image_url, link_url, sort_order, status, create_time, update_time) VALUES
(1, '测试轮播图1', 'https://picsum.photos/800/400?random=1', '/exhibitions', 1, 1, NOW(), NOW()),
(2, '测试轮播图2', 'https://picsum.photos/800/400?random=2', '/exhibitions', 2, 1, NOW(), NOW());

-- 2. 插入展览数据
INSERT INTO exhibition (id, name, description, cover_image, start_date, end_date, location, price, status, create_time, update_time) VALUES
(1, '测试展览 - 现代艺术展', '这是一个测试展览，用于测试支付流程。展览包含多个时间段和票务选项。', 'https://picsum.photos/600/400?random=3', '2026-03-18', '2026-04-18', '测试展览馆A厅', 100.00, 'ongoing', NOW(), NOW()),
(2, '测试展览 - 科技互动展', '科技与艺术的完美结合，适合全家参观。', 'https://picsum.photos/600/400?random=4', '2026-03-20', '2026-04-20', '测试展览馆B厅', 80.00, 'ongoing', NOW(), NOW());

-- 3. 插入展览库存数据（多个日期和时间段）
-- 展览1的库存
INSERT INTO exhibition_time_slot_inventory (exhibition_id, date, time_slot, total_tickets, sold_tickets, available_tickets, create_time, update_time) VALUES
-- 今天
(1, CURDATE(), '09:00-12:00', 100, 0, 100, NOW(), NOW()),
(1, CURDATE(), '14:00-17:00', 100, 0, 100, NOW(), NOW()),
(1, CURDATE(), '18:00-21:00', 100, 0, 100, NOW(), NOW()),
-- 明天
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00-12:00', 100, 0, 100, NOW(), NOW()),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00-17:00', 100, 0, 100, NOW(), NOW()),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00-21:00', 100, 0, 100, NOW(), NOW()),
-- 后天
(1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00-12:00', 100, 0, 100, NOW(), NOW()),
(1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00-17:00', 100, 0, 100, NOW(), NOW()),
(1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00-21:00', 100, 0, 100, NOW(), NOW());

-- 展览2的库存
INSERT INTO exhibition_time_slot_inventory (exhibition_id, date, time_slot, total_tickets, sold_tickets, available_tickets, create_time, update_time) VALUES
-- 今天
(2, CURDATE(), '10:00-13:00', 80, 0, 80, NOW(), NOW()),
(2, CURDATE(), '15:00-18:00', 80, 0, 80, NOW(), NOW()),
-- 明天
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00-13:00', 80, 0, 80, NOW(), NOW()),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00-18:00', 80, 0, 80, NOW(), NOW());

-- 4. 插入测试用户（如果需要）
-- 密码是 123456 的 BCrypt 加密
INSERT INTO user (id, username, password, phone, email, nickname, avatar, status, create_time, update_time) VALUES
(1, 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lzTOkJnqUYsnlqWIa', '13800138000', 'test@example.com', '测试用户', NULL, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 5. 插入商品数据（用于商城测试）
INSERT INTO sys_product (id, name, description, price, stock, cover_image, category, status, create_time, update_time) VALUES
(1, '测试商品 - 展览纪念品', '精美的展览纪念品，限量发售', 50.00, 100, 'https://picsum.photos/400/400?random=5', '纪念品', 1, NOW(), NOW()),
(2, '测试商品 - 艺术画册', '展览作品精选画册', 120.00, 50, 'https://picsum.photos/400/400?random=6', '图书', 1, NOW(), NOW());

SELECT '测试数据插入完成！' AS message;
SELECT '展览数量:', COUNT(*) FROM exhibition;
SELECT '库存记录数:', COUNT(*) FROM exhibition_time_slot_inventory;
SELECT '轮播图数量:', COUNT(*) FROM banner;
SELECT '商品数量:', COUNT(*) FROM sys_product;
