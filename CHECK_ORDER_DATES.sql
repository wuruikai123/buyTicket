-- 检查订单的创建时间分布

-- 1. 查看所有订单的创建时间
SELECT 
    id,
    order_no,
    create_time,
    total_amount,
    status
FROM ticket_order
ORDER BY create_time DESC
LIMIT 20;

-- 2. 查看订单按日期分组的统计
SELECT 
    DATE(create_time) as 日期,
    COUNT(*) as 订单数,
    SUM(total_amount) as 销售额
FROM ticket_order
GROUP BY DATE(create_time)
ORDER BY 日期 DESC;

-- 3. 查看近7日的订单数
SELECT 
    DATE(create_time) as 日期,
    COUNT(*) as 订单数
FROM ticket_order
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
GROUP BY DATE(create_time)
ORDER BY 日期;

-- 4. 查看今天的日期
SELECT CURDATE() as 今天, NOW() as 当前时间;

-- 5. 查看最早和最晚的订单时间
SELECT 
    MIN(create_time) as 最早订单时间,
    MAX(create_time) as 最晚订单时间,
    COUNT(*) as 总订单数
FROM ticket_order;
