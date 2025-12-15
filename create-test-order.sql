-- 创建测试订单用于核销测试

-- 1. 插入测试订单
INSERT INTO ticket_order (
    order_no, 
    user_id, 
    total_amount, 
    status, 
    contact_name, 
    contact_phone, 
    create_time
) VALUES (
    'T1734240000000TEST1',  -- 测试订单号
    1,                       -- 用户ID (zhangsan)
    150.00,                  -- 金额
    1,                       -- 状态：待使用
    '测试用户',
    '13800138000',
    NOW()
);

-- 2. 获取刚插入的订单ID
SET @order_id = LAST_INSERT_ID();

-- 3. 插入订单项
INSERT INTO order_item (
    order_id,
    exhibition_id,
    exhibition_name,
    ticket_date,
    time_slot,
    quantity,
    price
) VALUES (
    @order_id,
    1,                                    -- 展览ID
    '2025年当代艺术双年展',              -- 展览名称
    '2025-12-20',                         -- 日期
    '09:00-12:00',                        -- 时间段
    1,                                    -- 数量
    150.00                                -- 价格
);

-- 4. 查看创建的订单
SELECT 
    o.id,
    o.order_no,
    o.status,
    o.contact_name,
    o.contact_phone,
    i.exhibition_name,
    i.ticket_date,
    i.time_slot
FROM ticket_order o
LEFT JOIN order_item i ON o.id = i.order_id
WHERE o.order_no = 'T1734240000000TEST1';

-- 5. 测试查询（模拟后端查询）
SELECT * FROM ticket_order WHERE order_no = 'T1734240000000TEST1';
