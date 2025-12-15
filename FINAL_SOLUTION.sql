-- 最终解决方案：创建测试订单并测试核销

-- 1. 确保 order_no 字段存在且允许NULL
ALTER TABLE ticket_order 
MODIFY COLUMN order_no VARCHAR(32) NULL COMMENT '订单号（唯一）';

-- 2. 删除可能存在的旧测试数据
DELETE FROM order_item WHERE order_id IN (SELECT id FROM ticket_order WHERE order_no LIKE '%TEST%');
DELETE FROM ticket_order WHERE order_no LIKE '%TEST%';

-- 3. 插入一个完整的测试订单
INSERT INTO ticket_order (
    order_no, 
    user_id, 
    total_amount, 
    status, 
    contact_name, 
    contact_phone, 
    create_time
) VALUES (
    'T1734240000000TEST1',
    1,
    150.00,
    1,
    '测试用户',
    '13800138000',
    NOW()
);

-- 4. 获取刚插入的订单ID
SET @test_order_id = LAST_INSERT_ID();

-- 5. 插入订单项
INSERT INTO order_item (
    order_id,
    exhibition_id,
    exhibition_name,
    ticket_date,
    time_slot,
    quantity,
    price
) VALUES (
    @test_order_id,
    1,
    '2025年当代艺术双年展',
    '2025-12-20',
    '09:00-12:00',
    1,
    150.00
);

-- 6. 验证插入结果
SELECT 
    '测试订单创建成功' as message,
    id,
    order_no,
    status,
    contact_name,
    contact_phone
FROM ticket_order 
WHERE order_no = 'T1734240000000TEST1';

-- 7. 测试核销（手动更新状态）
-- UPDATE ticket_order SET status = 2 WHERE order_no = 'T1734240000000TEST1';

-- 8. 查看所有订单
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
ORDER BY id DESC 
LIMIT 10;
