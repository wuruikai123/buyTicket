-- 为sys_user表添加status字段
-- 用于实现用户冻结/解冻功能

-- 添加status字段
ALTER TABLE sys_user 
ADD COLUMN status INT DEFAULT 1 COMMENT '用户状态：0=冻结，1=正常';

-- 更新现有用户的状态为正常
UPDATE sys_user SET status = 1 WHERE status IS NULL;

-- 查看结果
SELECT id, username, phone, status, create_time FROM sys_user;
