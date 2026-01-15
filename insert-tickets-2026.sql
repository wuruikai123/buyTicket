-- 创建2026年1月10日以后的门票库存
-- 使用方式：在MySQL中执行此脚本

USE buy_ticket;

-- 为所有进行中的展览创建2026年1月10日之后的票务库存
-- 注意：需要先执行 insert-exhibition-data.sql 创建展览数据

-- 获取所有进行中的展览ID（status=1）
-- 这里假设展览ID为5-12（根据实际情况调整）

-- 1. 梵高艺术展 (假设ID=5, 展期到2025-05-31，已过期，不创建)
-- 2. 敦煌文化展 (假设ID=6, 展期到2025-07-15，已过期，不创建)
-- 3. 现代科技互动展 (假设ID=7, 展期到2025-06-30，已过期，不创建)
-- 4. 故宫文物珍品展 (假设ID=8, 展期到2025-08-10，已过期，不创建)
-- 5. 印象派大师作品展 (假设ID=9, 展期到2025-06-30，已过期，不创建)
-- 6. 恐龙化石展 (假设ID=10, 展期到2025-12-31，已过期，不创建)
-- 7. 当代摄影艺术展 (假设ID=11, 展期到2025-07-31，已过期，不创建)
-- 8. 海洋生物展 (假设ID=12, 展期到2025-09-20，已过期，不创建)

-- 由于原有展览都在2025年结束，我们需要创建新的2026年展览

-- ========== 创建2026年新展览 ==========

-- 1. 中国航天成就展 (2026全年)
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('逐梦星辰：中国航天成就展', '探索中国航天发展历程', 
        '展示中国航天事业从东方红一号到空间站建设的辉煌历程，包括火箭模型、宇航服、月球土壤等珍贵展品。', 
        '2026-01-10', '2026-12-31', 1, 100.00, '/images/space.jpg', '科技,航天,爱国');

SET @space_id = LAST_INSERT_ID();

-- 2. 莫奈特展 (2026上半年)
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('莫奈：光影诗人', '印象派大师莫奈作品回顾展', 
        '汇集莫奈一生各个时期的代表作品，包括《日出·印象》、《睡莲》系列等，深入了解印象派艺术的魅力。', 
        '2026-01-15', '2026-06-30', 1, 150.00, '/images/monet.jpg', '艺术,印象派,热门');

SET @monet_id = LAST_INSERT_ID();

-- 3. 数字艺术沉浸展 (2026全年)
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('无界：数字艺术沉浸体验', '最新数字艺术互动展', 
        '运用最新的投影技术、传感器和AI，打造360度沉浸式艺术空间，让观众成为艺术的一部分。', 
        '2026-01-20', '2026-12-31', 1, 180.00, '/images/digital_art.jpg', '艺术,科技,互动,热门');

SET @digital_id = LAST_INSERT_ID();

-- 4. 丝绸之路文明展 (2026全年)
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('丝路传奇：东西方文明交流史', '探索丝绸之路的历史与文化', 
        '通过文物、复原场景和多媒体展示，重现丝绸之路上的商贸往来和文化交流，感受东西方文明的碰撞与融合。', 
        '2026-02-01', '2026-12-31', 1, 120.00, '/images/silk_road.jpg', '历史,文化,推荐');

SET @silk_id = LAST_INSERT_ID();

-- 5. 恐龙世界大冒险 (2026全年，亲子热门)
INSERT INTO exhibition (name, short_desc, description, start_date, end_date, status, price, cover_image, tags)
VALUES ('恐龙世界大冒险', '全新升级的恐龙主题展', 
        '全新引进的恐龙化石和仿真模型，配合AR/VR技术，让孩子们身临其境体验侏罗纪时代。含恐龙骑乘体验。', 
        '2026-01-10', '2026-12-31', 1, 120.00, '/images/dino2026.jpg', '科普,亲子,互动,热门');

SET @dino_id = LAST_INSERT_ID();

-- ========== 为2026年展览创建票务库存 ==========

-- 中国航天成就展 - 1月10日至1月31日（工作日和周末）
INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @space_id, DATE_ADD('2026-01-10', INTERVAL n DAY), '09:00-12:00', 150, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21) numbers;

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @space_id, DATE_ADD('2026-01-10', INTERVAL n DAY), '14:00-17:00', 150, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21) numbers;

-- 莫奈特展 - 1月15日至2月15日
INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @monet_id, DATE_ADD('2026-01-15', INTERVAL n DAY), '09:00-12:00', 120, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 
      UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30) numbers;

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @monet_id, DATE_ADD('2026-01-15', INTERVAL n DAY), '14:00-17:00', 120, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 
      UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30) numbers;

-- 数字艺术沉浸展 - 1月20日至2月20日
INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @digital_id, DATE_ADD('2026-01-20', INTERVAL n DAY), '09:00-12:00', 100, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 
      UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30) numbers;

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @digital_id, DATE_ADD('2026-01-20', INTERVAL n DAY), '14:00-17:00', 100, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 
      UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30) numbers;

-- 丝绸之路文明展 - 2月1日至3月1日
INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @silk_id, DATE_ADD('2026-02-01', INTERVAL n DAY), '09:00-12:00', 180, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 
      UNION SELECT 25 UNION SELECT 26 UNION SELECT 27) numbers;

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @silk_id, DATE_ADD('2026-02-01', INTERVAL n DAY), '14:00-17:00', 180, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 
      UNION SELECT 25 UNION SELECT 26 UNION SELECT 27) numbers;

-- 恐龙世界大冒险 - 1月10日至2月10日（亲子热门，库存更多）
INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @dino_id, DATE_ADD('2026-01-10', INTERVAL n DAY), '09:00-12:00', 200, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 
      UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30) numbers;

INSERT INTO ticket_inventory (exhibition_id, ticket_date, time_slot, total_count, sold_count)
SELECT @dino_id, DATE_ADD('2026-01-10', INTERVAL n DAY), '14:00-17:00', 200, 0
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 
      UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 
      UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 
      UNION SELECT 19 UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 
      UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30) numbers;

-- 更新轮播图（添加2026年新展览）
INSERT INTO banner (title, image_url, link_type, link_id, sort_order, status)
VALUES 
    ('逐梦星辰：中国航天成就展', '/images/space.jpg', 1, @space_id, 5, 1),
    ('莫奈：光影诗人', '/images/monet.jpg', 1, @monet_id, 6, 1),
    ('无界：数字艺术沉浸体验', '/images/digital_art.jpg', 1, @digital_id, 7, 1);

-- 查询插入结果
SELECT '2026年展览和票务数据创建完成！' AS message;
SELECT CONCAT('共创建 ', COUNT(*), ' 个2026年展览') AS exhibition_count 
FROM exhibition WHERE start_date >= '2026-01-10';

SELECT CONCAT('共创建 ', COUNT(*), ' 条2026年票务库存') AS inventory_count 
FROM ticket_inventory WHERE ticket_date >= '2026-01-10';

-- 显示2026年展览信息
SELECT 
    e.id AS 展览ID,
    e.name AS 展览名称,
    e.short_desc AS 简介,
    e.start_date AS 开始日期,
    e.end_date AS 结束日期,
    e.price AS 票价,
    e.tags AS 标签,
    COUNT(DISTINCT ti.ticket_date) AS 可预订天数,
    SUM(ti.total_count) AS 总票数
FROM exhibition e
LEFT JOIN ticket_inventory ti ON e.id = ti.exhibition_id
WHERE e.start_date >= '2026-01-10'
GROUP BY e.id
ORDER BY e.start_date;

-- 显示各展览的票务库存明细（前10天）
SELECT 
    e.name AS 展览名称,
    ti.ticket_date AS 日期,
    ti.time_slot AS 时间段,
    ti.total_count AS 总票数,
    ti.sold_count AS 已售,
    (ti.total_count - ti.sold_count) AS 剩余
FROM ticket_inventory ti
JOIN exhibition e ON ti.exhibition_id = e.id
WHERE ti.ticket_date >= '2026-01-10' AND ti.ticket_date <= '2026-01-20'
ORDER BY e.name, ti.ticket_date, ti.time_slot
LIMIT 50;
