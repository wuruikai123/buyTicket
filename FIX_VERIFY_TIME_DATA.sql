-- 修复历史核销记录的 verify_time 字段
-- 对于已经核销但没有 verify_time 的订单，使用 update_time 作为近似值

-- 1. 检查有多少已核销订单没有 verify_time
SELECT COUNT(*) as '缺少verify_time的已核销订单数量'
FROM ticket_order 
WHERE status = 2 AND verify_time IS NULL;

-- 2. 查看这些订单的详情
SELECT id, order_no, status, create_time, update_time, verify_time
FROM ticket_order 
WHERE status = 2 AND verify_time IS NULL
LIMIT 10;

-- 3. 修复：将 update_time 设置为 verify_time（近似值）
UPDATE ticket_order 
SET verify_time = update_time 
WHERE status = 2 AND verify_time IS NULL;

-- 4. 验证修复结果
SELECT COUNT(*) as '修复后仍缺少verify_time的订单数量'
FROM ticket_order 
WHERE status = 2 AND verify_time IS NULL;

-- 5. 查看今天的核销记录
SELECT 
    id,
    order_no,
    contact_name,
    contact_phone,
    status,
    DATE_FORMAT(verify_time, '%Y-%m-%d %H:%i:%s') as verify_time
FROM ticket_order 
WHERE status = 2 
  AND DATE(verify_time) = CURDATE()
ORDER BY verify_time DESC;
