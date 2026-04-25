USE buy_ticket;

-- 0.01 元测试票 + 未来一年每天可购库存
-- 说明：
-- 1) 该脚本会创建/更新一个测试展览，ID 固定为 999901
-- 2) 会重建该展览从今天开始未来 365 天的库存
-- 3) 每天两个时段：10:00-12:00、12:00-18:00

SET @test_exhibition_id = 999901;
SET @ticket_price = 0.01;
SET @tickets_per_period = 999;

INSERT INTO exhibition (
    id,
    name,
    short_desc,
    description,
    start_date,
    end_date,
    status,
    price,
    tickets_per_period,
    daily_start_time,
    daily_end_time,
    cover_image,
    detail_images,
    tags,
    create_time
) VALUES (
    @test_exhibition_id,
    '0.01元测试票',
    '支付联调用测试票，全天可测',
    '该展览仅用于支付联调与流程测试，票价 0.01 元。系统已预生成未来一年每日库存，每天都可以下单购买。',
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 365 DAY),
    1,
    @ticket_price,
    @tickets_per_period,
    '10:00',
    '18:00',
    '/images/exhibition_current.jpg',
    JSON_ARRAY('/images/exhibition_current.jpg'),
    '测试,联调,0.01元',
    NOW()
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    short_desc = VALUES(short_desc),
    description = VALUES(description),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    status = VALUES(status),
    price = VALUES(price),
    tickets_per_period = VALUES(tickets_per_period),
    daily_start_time = VALUES(daily_start_time),
    daily_end_time = VALUES(daily_end_time),
    cover_image = VALUES(cover_image),
    detail_images = VALUES(detail_images),
    tags = VALUES(tags);

-- 清掉该测试展览现有库存，避免重复
DELETE FROM exhibition_time_slot_inventory
WHERE exhibition_id = @test_exhibition_id;

-- 生成未来一年每日库存（每天两个时段）
INSERT INTO exhibition_time_slot_inventory (
    exhibition_id,
    ticket_date,
    time_slot,
    total_tickets,
    sold_tickets,
    available_tickets,
    version,
    create_time,
    update_time
)
WITH RECURSIVE seq AS (
    SELECT CURDATE() AS d
    UNION ALL
    SELECT DATE_ADD(d, INTERVAL 1 DAY)
    FROM seq
    WHERE d < DATE_ADD(CURDATE(), INTERVAL 365 DAY)
)
SELECT
    @test_exhibition_id,
    seq.d,
    slots.time_slot,
    @tickets_per_period,
    0,
    @tickets_per_period,
    0,
    NOW(),
    NOW()
FROM seq
CROSS JOIN (
    SELECT '10:00-12:00' AS time_slot
    UNION ALL
    SELECT '12:00-18:00' AS time_slot
) slots;

SELECT id, name, price, start_date, end_date, tickets_per_period, daily_start_time, daily_end_time
FROM exhibition
WHERE id = @test_exhibition_id;

SELECT COUNT(*) AS inventory_rows
FROM exhibition_time_slot_inventory
WHERE exhibition_id = @test_exhibition_id;
