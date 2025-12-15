-- 检查用户订单数据

-- 1. 查看所有用户
SELECT id, username, uid, phone FROM sys_user LIMIT 10;

-- 2. 查看门票订单
SELECT * FROM ticket_order LIMIT 10;

-- 3. 查看订单项
SELECT * FROM order_item LIMIT 10;

-- 4. 查看用户ID为1的订单（假设你点击的是第一个用户）
SELECT 
    o.id as order_id,
    o.order_no,
    o.user_id,
    o.total_amount,
    o.status,
    o.create_time,
    i.exhibition_name,
    i.ticket_date,
    i.time_slot,
    i.price,
    i.quantity
FROM ticket_order o
LEFT JOIN order_item i ON o.id = i.order_id
WHERE o.user_id = 1;

-- 5. 检查是否有订单但没有订单项
SELECT 
    o.id,
    o.order_no,
    COUNT(i.id) as item_count
FROM ticket_order o
LEFT JOIN order_item i ON o.id = i.order_id
GROUP BY o.id, o.order_no
HAVING item_count = 0;
