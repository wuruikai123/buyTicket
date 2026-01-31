-- 检查订单表中的userId分布

-- 1. 查看订单表中有多少个唯一的userId
SELECT '订单表中唯一userId数量' as 统计项, COUNT(DISTINCT user_id) as 数量
FROM ticket_order
WHERE user_id IS NOT NULL;

-- 2. 查看订单表中每个userId的订单数
SELECT 
    user_id,
    COUNT(*) as 订单数,
    MIN(create_time) as 首次下单,
    MAX(create_time) as 最近下单
FROM ticket_order
WHERE user_id IS NOT NULL
GROUP BY user_id
ORDER BY user_id;

-- 3. 查看订单表中所有的userId（包括NULL）
SELECT 
    user_id,
    COUNT(*) as 订单数
FROM ticket_order
GROUP BY user_id
ORDER BY user_id;

-- 4. 查看user表中的所有用户
SELECT 
    id,
    username,
    uid
FROM user
ORDER BY id;

-- 5. 检查订单表的userId是否对应user表的uid
SELECT 
    o.user_id as 订单中的userId,
    u.uid as 用户表的uid,
    u.username as 用户名,
    COUNT(o.id) as 订单数
FROM ticket_order o
LEFT JOIN user u ON o.user_id = u.uid
WHERE o.user_id IS NOT NULL
GROUP BY o.user_id, u.uid, u.username
ORDER BY o.user_id;

-- 6. 近7日每天的累计唯一用户数（手动计算）
SELECT 
    '2026-01-25' as 日期,
    (SELECT COUNT(DISTINCT user_id) FROM ticket_order WHERE create_time <= '2026-01-25 23:59:59' AND user_id IS NOT NULL) as 累计用户数
UNION ALL
SELECT 
    '2026-01-26' as 日期,
    (SELECT COUNT(DISTINCT user_id) FROM ticket_order WHERE create_time <= '2026-01-26 23:59:59' AND user_id IS NOT NULL) as 累计用户数
UNION ALL
SELECT 
    '2026-01-27' as 日期,
    (SELECT COUNT(DISTINCT user_id) FROM ticket_order WHERE create_time <= '2026-01-27 23:59:59' AND user_id IS NOT NULL) as 累计用户数
UNION ALL
SELECT 
    '2026-01-28' as 日期,
    (SELECT COUNT(DISTINCT user_id) FROM ticket_order WHERE create_time <= '2026-01-28 23:59:59' AND user_id IS NOT NULL) as 累计用户数
UNION ALL
SELECT 
    '2026-01-29' as 日期,
    (SELECT COUNT(DISTINCT user_id) FROM ticket_order WHERE create_time <= '2026-01-29 23:59:59' AND user_id IS NOT NULL) as 累计用户数
UNION ALL
SELECT 
    '2026-01-30' as 日期,
    (SELECT COUNT(DISTINCT user_id) FROM ticket_order WHERE create_time <= '2026-01-30 23:59:59' AND user_id IS NOT NULL) as 累计用户数
UNION ALL
SELECT 
    '2026-01-31' as 日期,
    (SELECT COUNT(DISTINCT user_id) FROM ticket_order WHERE create_time <= '2026-01-31 23:59:59' AND user_id IS NOT NULL) as 累计用户数;
