-- 检查用户状态
SELECT id, username, phone, status, create_time 
FROM sys_user 
ORDER BY id;

-- 检查status字段是否存在
SHOW COLUMNS FROM sys_user LIKE 'status';

-- 手动冻结一个用户（用于测试）
-- UPDATE sys_user SET status = 0 WHERE username = 'zhangsan';

-- 手动解冻一个用户（用于测试）
-- UPDATE sys_user SET status = 1 WHERE username = 'zhangsan';
