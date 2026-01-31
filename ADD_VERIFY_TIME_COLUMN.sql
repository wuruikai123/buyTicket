-- 添加核销时间字段到门票订单表
-- 用于记录订单核销的具体时间，支持C端核销记录查询功能

-- 添加字段（如果已存在会报错，可以忽略）
ALTER TABLE ticket_order 
ADD COLUMN verify_time DATETIME NULL COMMENT '核销时间' AFTER pay_time;

-- 为已核销的订单（status=2）设置默认核销时间（如果为空）
-- 使用 create_time 作为默认值
UPDATE ticket_order 
SET verify_time = create_time 
WHERE status = 2 AND verify_time IS NULL;

-- 添加索引以优化按核销时间查询
CREATE INDEX idx_verify_time ON ticket_order(verify_time);
CREATE INDEX idx_status_verify_time ON ticket_order(status, verify_time);
