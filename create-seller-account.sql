-- 创建核销端管理员账号
-- 数据库：buy_ticket

USE buy_ticket;

-- 检查admin_user表是否存在
SHOW TABLES LIKE 'admin_user';

-- 查看现有管理员账号
SELECT id, username, password, status FROM admin_user;

-- 如果seller账号不存在，则创建
INSERT INTO admin_user (username, password, status, create_time)
SELECT 'seller', '123456', 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM admin_user WHERE username = 'seller'
);

-- 验证seller账号已创建
SELECT id, username, password, status, create_time 
FROM admin_user 
WHERE username = 'seller';

-- 说明：
-- username: seller
-- password: 123456
-- status: 1 (启用)
