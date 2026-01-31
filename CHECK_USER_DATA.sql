-- 检查用户数据的SQL脚本

-- 1. 查看所有唯一的userId
SELECT '所有唯一用户' as 统计项, COUNT(DISTINCT user_id) as 数量
FROM ticket_order
WHERE user_id IS NOT NULL;

-- 2. 查看userId为NULL的订单数
SELECT 'userId为NULL的订单' as 统计项, COUNT(*) as 数量
FROM ticket_order
WHERE user_id IS NULL;

-- 3. 列出所有唯一的userId
SELECT DISTINCT user_id
FROM ticket_order
WHERE user_id IS NOT NULL
ORDER BY user_id;

-- 4. 每个用户的订单数
SELECT 
    user_id,
    COUNT(*) as 订单数,
    MIN(create_time) as 首次下单时间,
    MAX(create_time) as 最近下单时间
FROM ticket_order
WHERE user_id IS NOT NULL
GROUP BY user_id
ORDER BY user_id;

-- 5. 近7日每天的累计用户数
SELECT 
    DATE(create_time) as 日期,
    (SELECT COUNT(DISTINCT user_id) 
     FROM ticket_order t2 
     WHERE t2.create_time <= DATE_ADD(DATE(t1.create_time), INTERVAL 1 DAY)
       AND t2.user_id IS NOT NULL) as 累计用户数
FROM ticket_order t1
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
  AND user_id IS NOT NULL
GROUP BY DATE(create_time)
ORDER BY 日期;

-- 6. 查看user表的数据（如果存在）
SELECT '用户表数据' as 说明;
SELECT * FROM user LIMIT 10;
