-- 创建应用专用数据库用户的SQL脚本
-- 使用方法：
-- 1. 如果你知道root密码，先用 mysql -u root -p 登录
-- 2. 然后执行：source create_app_user.sql
-- 3. 或者直接运行：mysql -u root -p < create_app_user.sql

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS buy_ticket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建应用专用用户（推荐方式，更安全）
-- 将 'app_password' 替换为你想要的密码
CREATE USER IF NOT EXISTS 'buyticket'@'localhost' IDENTIFIED BY 'app_password';

-- 授予权限
GRANT ALL PRIVILEGES ON buy_ticket.* TO 'buyticket'@'localhost';
FLUSH PRIVILEGES;

-- 显示创建的用户
SELECT User, Host FROM mysql.user WHERE User = 'buyticket';

