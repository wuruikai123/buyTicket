-- 检查订单号是否存在
SELECT 
    id, 
    order_no, 
    user_id,
    status, 
    total_amount,
    contact_name, 
    contact_phone,
    create_time 
FROM ticket_order 
WHERE order_no = 'T1765772332101ZTIV8Y';

-- 查看所有订单
SELECT 
    id, 
    order_no, 
    status, 
    contact_name,
    create_time 
FROM ticket_order 
ORDER BY id DESC 
LIMIT 10;

-- 检查 order_no 字段是否有空值
SELECT COUNT(*) as empty_order_no_count
FROM ticket_order 
WHERE order_no IS NULL OR order_no = '' OR order_no = '0';

-- 查看订单号格式
SELECT 
    id,
    order_no,
    LENGTH(order_no) as order_no_length,
    status
FROM ticket_order 
WHERE order_no IS NOT NULL 
  AND order_no != ''
ORDER BY id DESC 
LIMIT 5;
