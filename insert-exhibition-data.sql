-- 插入展览和门票数据
-- 使用方式：在MySQL中执行此脚本

USE buy_ticket;

-- 1. 梵高艺术展
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('梵高艺术展：星空之旅', '探索梵高的艺术世界', 
        '欣赏《星夜》、《向日葵》等经典作品的高清复制品，感受后印象派大师的独特魅力。展览采用沉浸式体验设计，让您仿佛置身于梵高的画作之中。', 
        '2025-02-01', '2025-05-31', 1, 120.00, '/images/vangogh.jpg', '艺术,热门');

SET @exhibition_id_1 = LAST_INSERT_ID();

-- 为梵高展添加票务库存（示例：2月份前两周）
INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
VALUES 
    (@exhibition_id_1, '2025-02-01', '09:00-12:00', 100, 0),
    (@exhibition_id_1, '2025-02-01', '14:00-17:00', 100, 0),
    (@exhibition_id_1, '2025-02-02', '09:00-12:00', 100, 0),
    (@exhibition_id_1, '2025-02-02', '14:00-17:00', 100, 0),
    (@exhibition_id_1, '2025-02-08', '09:00-12:00', 150, 0),
    (@exhibition_id_1, '2025-02-08', '14:00-17:00', 150, 0),
    (@exhibition_id_1, '2025-02-09', '09:00-12:00', 150, 0),
    (@exhibition_id_1, '2025-02-09', '14:00-17:00', 150, 0);

-- 2. 敦煌文化展
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('敦煌：丝路明珠', '数字技术重现敦煌莫高窟', 
        '通过最新的数字技术重现敦煌莫高窟的壁画和雕塑，带您穿越千年，领略丝绸之路的辉煌文明。展览包含VR体验区，让您身临其境感受敦煌艺术。', 
        '2025-03-15', '2025-07-15', 1, 150.00, '/images/dunhuang.jpg', '历史,文化,推荐');

SET @exhibition_id_2 = LAST_INSERT_ID();

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
VALUES 
    (@exhibition_id_2, '2025-03-15', '09:00-12:00', 120, 0),
    (@exhibition_id_2, '2025-03-15', '14:00-17:00', 120, 0),
    (@exhibition_id_2, '2025-03-16', '09:00-12:00', 120, 0),
    (@exhibition_id_2, '2025-03-16', '14:00-17:00', 120, 0);

-- 3. 现代科技互动展
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('未来世界：科技互动体验展', '探索未来生活方式', 
        '体验VR、AR、全息投影等前沿科技，探索人工智能、量子计算等未来科技的无限可能。适合亲子家庭，寓教于乐。', 
        '2025-01-20', '2025-06-30', 1, 180.00, '/images/tech.jpg', '科技,亲子,互动');

SET @exhibition_id_3 = LAST_INSERT_ID();

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
VALUES 
    (@exhibition_id_3, '2025-01-25', '09:00-12:00', 80, 0),
    (@exhibition_id_3, '2025-01-25', '14:00-17:00', 80, 0),
    (@exhibition_id_3, '2025-01-26', '09:00-12:00', 80, 0),
    (@exhibition_id_3, '2025-01-26', '14:00-17:00', 80, 0);

-- 4. 故宫文物珍品展
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('紫禁城的记忆：故宫珍宝展', '明清文物精品展', 
        '精选故宫博物院珍藏的明清文物，包括瓷器、书画、玉器等，展现皇家艺术的精髓。每件展品都配有详细的历史背景介绍。', 
        '2025-02-10', '2025-08-10', 1, 100.00, '/images/gugong.jpg', '历史,文物,经典');

SET @exhibition_id_4 = LAST_INSERT_ID();

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
VALUES 
    (@exhibition_id_4, '2025-02-10', '09:00-12:00', 200, 0),
    (@exhibition_id_4, '2025-02-10', '14:00-17:00', 200, 0),
    (@exhibition_id_4, '2025-02-11', '09:00-12:00', 200, 0),
    (@exhibition_id_4, '2025-02-11', '14:00-17:00', 200, 0);

-- 5. 印象派大师作品展
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('光影之美：印象派大师作品展', '莫奈、雷诺阿经典作品', 
        '汇集莫奈、雷诺阿、德加等印象派大师的经典作品，感受光影变化的艺术魅力。展览还原了19世纪巴黎的艺术氛围。', 
        '2025-03-01', '2025-06-30', 1, 130.00, '/images/impressionism.jpg', '艺术,经典,热门');

SET @exhibition_id_5 = LAST_INSERT_ID();

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
VALUES 
    (@exhibition_id_5, '2025-03-01', '09:00-12:00', 150, 0),
    (@exhibition_id_5, '2025-03-01', '14:00-17:00', 150, 0),
    (@exhibition_id_5, '2025-03-02', '09:00-12:00', 150, 0),
    (@exhibition_id_5, '2025-03-02', '14:00-17:00', 150, 0);

-- 6. 恐龙化石展
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('侏罗纪探秘：恐龙化石大展', '走进远古时代', 
        '展出完整的恐龙骨架化石，包括霸王龙、三角龙等，带孩子们走进远古时代。配有AR互动体验，让恐龙"复活"。', 
        '2025-01-15', '2025-12-31', 1, 100.00, '/images/dinosaur.jpg', '科普,亲子,热门');

SET @exhibition_id_6 = LAST_INSERT_ID();

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
VALUES 
    (@exhibition_id_6, '2025-01-18', '09:00-12:00', 200, 0),
    (@exhibition_id_6, '2025-01-18', '14:00-17:00', 200, 0),
    (@exhibition_id_6, '2025-01-19', '09:00-12:00', 200, 0),
    (@exhibition_id_6, '2025-01-19', '14:00-17:00', 200, 0);

-- 7. 当代摄影艺术展
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('镜头下的世界：当代摄影艺术展', '国内外知名摄影师作品', 
        '展示国内外知名摄影师的作品，涵盖人文、风光、纪实等多种题材。部分作品首次在国内展出。', 
        '2025-04-01', '2025-07-31', 0, 80.00, '/images/photography.jpg', '摄影,艺术');

SET @exhibition_id_7 = LAST_INSERT_ID();

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
VALUES 
    (@exhibition_id_7, '2025-04-01', '09:00-12:00', 100, 0),
    (@exhibition_id_7, '2025-04-01', '14:00-17:00', 100, 0);

-- 8. 海洋生物展
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('深海奇遇：海洋生物科普展', '探索海洋生物多样性', 
        '通过标本、模型和多媒体展示海洋生物的多样性，了解海洋生态保护的重要性。含海洋剧场表演。', 
        '2025-02-20', '2025-09-20', 1, 120.00, '/images/ocean.jpg', '科普,亲子,环保');

SET @exhibition_id_8 = LAST_INSERT_ID();

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
VALUES 
    (@exhibition_id_8, '2025-02-20', '09:00-12:00', 150, 0),
    (@exhibition_id_8, '2025-02-20', '14:00-17:00', 150, 0),
    (@exhibition_id_8, '2025-02-21', '09:00-12:00', 150, 0),
    (@exhibition_id_8, '2025-02-21', '14:00-17:00', 150, 0);

-- 添加轮播图数据
INSERT INTO banner (title, image_url, link_type, link_id, sort_order, status)
VALUES 
    ('梵高艺术展：星空之旅', '/images/vangogh.jpg', 1, @exhibition_id_1, 1, 1),
    ('敦煌：丝路明珠', '/images/dunhuang.jpg', 1, @exhibition_id_2, 2, 1),
    ('未来世界：科技互动体验展', '/images/tech.jpg', 1, @exhibition_id_3, 3, 1),
    ('侏罗纪探秘：恐龙化石大展', '/images/dinosaur.jpg', 1, @exhibition_id_6, 4, 1);

-- 添加更多文创产品
INSERT INTO sys_product (name, description, price, stock, cover_image, status)
VALUES 
    ('梵高星空T恤', '100%纯棉，印有梵高《星夜》图案，舒适透气', 89.00, 200, '/images/product_tshirt.jpg', 1),
    ('敦煌飞天丝巾', '真丝材质，手绘敦煌飞天图案，优雅大方', 168.00, 100, '/images/product_scarf.jpg', 1),
    ('恐龙拼装模型', '高仿真恐龙骨架模型，适合8岁以上儿童', 128.00, 150, '/images/product_dinosaur.jpg', 1),
    ('艺术明信片套装', '精选10张经典艺术作品明信片，附赠信封', 39.00, 300, '/images/product_postcard.jpg', 1),
    ('博物馆徽章套装', '金属材质，5枚一套，收藏佳品', 58.00, 200, '/images/product_badge.jpg', 1);

-- 查询插入结果
SELECT '展览数据插入完成！' AS message;
SELECT CONCAT('共插入 ', COUNT(*), ' 个展览') AS exhibition_count FROM exhibition WHERE id >= @exhibition_id_1;
SELECT CONCAT('共插入 ', COUNT(*), ' 条票务库存') AS inventory_count FROM ticket_inventory WHERE exhibition_id >= @exhibition_id_1;
SELECT CONCAT('共插入 ', COUNT(*), ' 个轮播图') AS banner_count FROM banner WHERE link_id >= @exhibition_id_1;
SELECT CONCAT('当前文创产品总数：', COUNT(*), ' 个') AS product_count FROM sys_product;

-- 显示所有新增展览及其票务信息
SELECT 
    e.id AS 展览ID,
    e.name AS 展览名称,
    e.short_desc AS 简介,
    e.start_date AS 开始日期,
    e.end_date AS 结束日期,
    e.price AS 票价,
    e.status AS 状态,
    e.tags AS 标签,
    COUNT(DISTINCT ti.id) AS 票务库存记录数
FROM exhibition e
LEFT JOIN ticket_inventory ti ON e.id = ti.exhibition_id
WHERE e.id >= @exhibition_id_1
GROUP BY e.id
ORDER BY e.id;
