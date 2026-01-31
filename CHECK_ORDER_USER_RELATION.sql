-- 检查订单和用户的关系

-- 1. 查看所有用户
SELECT id, username, phone FROM sys_user;

-- 2. 查看所有门票订单及其用户
SELECT 
    t.id AS order_id,
    t.order_no,
    t.user_id,
    u.username,
    t.total_amount,
    t.status,
    t.create_time
FROM ticket_order t
LEFT JOIN sys_user u ON t.user_id = u.id
ORDER BY t.create_time DESC;

-- 3. 统计每个用户的订单数量
SELECT 
    u.id,
    u.username,
    COUNT(t.id) AS order_count
FROM sys_user u
LEFT JOIN ticket_order t ON u.id = t.user_id
GROUP BY u.id, u.username
ORDER BY order_count DESC;

-- 4. 查看订单项详情
SELECT 
    oi.id,
    oi.order_id,
    t.user_id,
    u.username,
    oi.exhibition_name,
    oi.ticket_date,
    oi.time_slot,
    oi.quantity
FROM order_item oi
JOIN ticket_order t ON oi.order_id = t.id
LEFT JOIN sys_user u ON t.user_id = u.id
ORDER BY oi.order_id DESC;
