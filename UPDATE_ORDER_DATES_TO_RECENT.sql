-- 将订单的创建时间更新到最近7天，以便在Dashboard中显示

-- 1. 先查看当前订单的时间分布
SELECT 
    DATE(create_time) as 日期,
    COUNT(*) as 订单数
FROM ticket_order
GROUP BY DATE(create_time)
ORDER BY 日期 DESC;

-- 2. 更新订单时间到最近7天（随机分布）
-- 注意：这会修改数据，请先备份！

-- 将前5个订单更新到今天
UPDATE ticket_order 
SET create_time = DATE_ADD(CURDATE(), INTERVAL FLOOR(RAND() * 24) HOUR)
WHERE id IN (SELECT id FROM (SELECT id FROM ticket_order ORDER BY id LIMIT 5) AS t);

-- 将接下来5个订单更新到昨天
UPDATE ticket_order 
SET create_time = DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL FLOOR(RAND() * 24) HOUR)
WHERE id IN (SELECT id FROM (SELECT id FROM ticket_order ORDER BY id LIMIT 5, 5) AS t);

-- 将接下来5个订单更新到前天
UPDATE ticket_order 
SET create_time = DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL FLOOR(RAND() * 24) HOUR)
WHERE id IN (SELECT id FROM (SELECT id FROM ticket_order ORDER BY id LIMIT 10, 5) AS t);

-- 3. 验证更新结果
SELECT 
    DATE(create_time) as 日期,
    COUNT(*) as 订单数,
    SUM(total_amount) as 销售额
FROM ticket_order
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
GROUP BY DATE(create_time)
ORDER BY 日期;
