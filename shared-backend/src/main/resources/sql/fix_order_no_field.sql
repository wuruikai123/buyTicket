-- 修复 order_no 字段，允许在插入时为空，然后再更新
-- 或者确保字段有默认值

-- 方案1：修改字段为允许NULL（推荐用于过渡）
ALTER TABLE ticket_order 
MODIFY COLUMN order_no VARCHAR(32) NULL COMMENT '订单号（唯一）';

-- 为现有的空订单号生成新的安全订单号
UPDATE ticket_order 
SET order_no = CONCAT(
    'T',
    UNIX_TIMESTAMP(IFNULL(create_time, NOW())) * 1000 + id % 1000,
    UPPER(SUBSTRING(MD5(CONCAT(id, create_time, RAND())), 1, 6))
)
WHERE order_no IS NULL 
   OR order_no = '' 
   OR order_no = '0';

-- 添加唯一索引（如果还没有）
ALTER TABLE ticket_order 
ADD UNIQUE INDEX uk_order_no (order_no);

-- 查看结果
SELECT id, order_no, status, create_time 
FROM ticket_order 
ORDER BY id DESC 
LIMIT 10;
