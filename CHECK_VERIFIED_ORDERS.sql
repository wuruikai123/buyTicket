-- 检查已核销订单的情况

-- 1. 查看所有订单的状态分布
SELECT 
    status,
    CASE status
        WHEN 0 THEN '待支付'
        WHEN 1 THEN '待使用'
        WHEN 2 THEN '已使用'
        WHEN 3 THEN '已取消'
        WHEN 4 THEN '已作废'
        ELSE '未知'
    END as status_name,
    COUNT(*) as count
FROM ticket_order
GROUP BY status;

-- 2. 查看已核销订单（status=2）的详情
SELECT 
    id,
    order_no,
    contact_name,
    contact_phone,
    status,
    total_amount,
    DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') as create_time,
    DATE_FORMAT(update_time, '%Y-%m-%d %H:%i:%s') as update_time,
    DATE_FORMAT(verify_time, '%Y-%m-%d %H:%i:%s') as verify_time
FROM ticket_order
WHERE status = 2
ORDER BY id DESC
LIMIT 20;

-- 3. 检查有多少已核销订单缺少 verify_time
SELECT 
    COUNT(*) as '已核销但缺少verify_time的订单数量'
FROM ticket_order
WHERE status = 2 AND verify_time IS NULL;

-- 4. 查看今天的核销记录
SELECT 
    id,
    order_no,
    contact_name,
    contact_phone,
    DATE_FORMAT(verify_time, '%Y-%m-%d %H:%i:%s') as verify_time
FROM ticket_order
WHERE status = 2 
  AND DATE(verify_time) = CURDATE()
ORDER BY verify_time DESC;

-- 5. 查看最近7天的核销记录
SELECT 
    DATE(verify_time) as date,
    COUNT(*) as count
FROM ticket_order
WHERE status = 2 
  AND verify_time IS NOT NULL
  AND verify_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY DATE(verify_time)
ORDER BY date DESC;

-- 6. 检查 order_item 表是否有数据
SELECT COUNT(*) as 'order_item表记录数' FROM order_item;

-- 7. 查看已核销订单对应的 order_item 信息
SELECT 
    o.id as order_id,
    o.order_no,
    o.status,
    oi.exhibition_name,
    oi.ticket_date,
    oi.time_slot
FROM ticket_order o
LEFT JOIN order_item oi ON o.id = oi.order_id
WHERE o.status = 2
LIMIT 10;
