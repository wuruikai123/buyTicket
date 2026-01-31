-- 查看管理员账号
SELECT * FROM admin_user;

-- 如果没有管理员账号，创建一个
-- INSERT INTO admin_user (username, password, status, create_time) 
-- VALUES ('admin', '123456', 1, NOW());
