-- 添加时间段门票数量字段到展览表
-- 用于为不同时间段设置不同的门票数量

-- 添加9-12点门票数量字段
ALTER TABLE exhibition 
ADD COLUMN morning_tickets INT DEFAULT 100 COMMENT '9-12点门票数量' AFTER tickets_per_hour;

-- 添加14-17点门票数量字段
ALTER TABLE exhibition 
ADD COLUMN afternoon_tickets INT DEFAULT 100 COMMENT '14-17点门票数量' AFTER morning_tickets;

-- 为已有展览设置默认值（使用 tickets_per_hour 的值）
UPDATE exhibition 
SET morning_tickets = COALESCE(tickets_per_hour, 100),
    afternoon_tickets = COALESCE(tickets_per_hour, 100)
WHERE morning_tickets IS NULL OR afternoon_tickets IS NULL;
