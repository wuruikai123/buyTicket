-- 添加每小时门票数量字段到展览表
-- 用于设置每个展览每小时可售门票数量

-- 添加字段（如果已存在会报错，可以忽略）
ALTER TABLE exhibition 
ADD COLUMN tickets_per_hour INT DEFAULT 100 COMMENT '每小时门票数量' AFTER price;

-- 为已有展览设置默认值
UPDATE exhibition 
SET tickets_per_hour = 100 
WHERE tickets_per_hour IS NULL OR tickets_per_hour = 0;
