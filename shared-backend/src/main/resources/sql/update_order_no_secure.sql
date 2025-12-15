-- 更新订单号为更安全的格式
-- 格式：T + 时间戳(13位) + 随机6位字母数字

-- 为现有订单生成新的安全订单号
UPDATE ticket_order 
SET order_no = CONCAT(
    'T',
    UNIX_TIMESTAMP(IFNULL(create_time, NOW())) * 1000 + id % 1000,
    SUBSTRING(MD5(CONCAT(id, create_time)), 1, 6)
)
WHERE order_no IS NULL 
   OR order_no = '' 
   OR order_no = '0'
   OR LENGTH(order_no) < 15;

-- 确保订单号唯一性
ALTER TABLE ticket_order 
MODIFY COLUMN order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号（唯一）';

-- 查看更新结果
SELECT id, order_no, status, create_time 
FROM ticket_order 
ORDER BY id DESC 
LIMIT 10;
