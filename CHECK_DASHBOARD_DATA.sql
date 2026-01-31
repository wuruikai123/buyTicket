-- 检查Dashboard数据的SQL脚本

-- 1. 今日核销门票数
SELECT '今日核销门票数' as 统计项, COUNT(*) as 数量
FROM ticket_order 
WHERE status = 2 
  AND DATE(verify_time) = CURDATE();

-- 2. 今日创建的订单数
SELECT '今日创建订单数' as 统计项, COUNT(*) as 数量
FROM ticket_order 
WHERE DATE(create_time) = CURDATE();

-- 3. 总订单数
SELECT '总订单数' as 统计项, COUNT(*) as 数量
FROM ticket_order;

-- 4. 总用户数（唯一userId）
SELECT '总用户数' as 统计项, COUNT(DISTINCT user_id) as 数量
FROM ticket_order 
WHERE user_id IS NOT NULL;

-- 5. 近7日每天的订单数
SELECT 
    DATE(create_time) as 日期,
    COUNT(*) as 订单数
FROM ticket_order 
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
GROUP BY DATE(create_time)
ORDER BY 日期;

-- 6. 近7日每天的核销数
SELECT 
    DATE(verify_time) as 日期,
    COUNT(*) as 核销数
FROM ticket_order 
WHERE status = 2
  AND verify_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
GROUP BY DATE(verify_time)
ORDER BY 日期;

-- 7. 近7日每天的销售额
SELECT 
    DATE(create_time) as 日期,
    SUM(total_amount) as 销售额
FROM ticket_order 
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
  AND status NOT IN (0, 3)  -- 排除待支付和已取消
GROUP BY DATE(create_time)
ORDER BY 日期;

-- 8. 检查verify_time为空的已核销订单
SELECT 
    '已核销但verify_time为空' as 问题,
    COUNT(*) as 数量
FROM ticket_order 
WHERE status = 2 AND verify_time IS NULL;

-- 9. 查看今日核销的订单详情
SELECT 
    id,
    order_no,
    contact_name,
    status,
    verify_time,
    create_time
FROM ticket_order 
WHERE status = 2 
  AND DATE(verify_time) = CURDATE()
ORDER BY verify_time DESC;

-- 10. 近7日累计用户数（每天的累计值）
SELECT 
    DATE(create_time) as 日期,
    COUNT(DISTINCT user_id) as 当日新增用户,
    (SELECT COUNT(DISTINCT user_id) 
     FROM ticket_order t2 
     WHERE t2.create_time <= DATE_ADD(DATE(t1.create_time), INTERVAL 1 DAY)
       AND t2.user_id IS NOT NULL) as 累计用户数
FROM ticket_order t1
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
  AND user_id IS NOT NULL
GROUP BY DATE(create_time)
ORDER BY 日期;
