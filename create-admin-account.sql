-- 创建管理员账号
-- 用户名: admin
-- 密码: admin123

INSERT INTO sys_user (username, password, phone, status, create_time)
VALUES ('admin', 'admin123', '13800138000', 1, NOW())
ON DUPLICATE KEY UPDATE username = username;

-- 查看创建结果
SELECT id, username, phone, status, create_time 
FROM sys_user 
WHERE username = 'admin';
