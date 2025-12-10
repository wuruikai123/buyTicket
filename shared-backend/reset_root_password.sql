-- 重置root密码的SQL脚本
-- 使用方法：
-- 1. 如果你知道root密码，先用 mysql -u root -p 登录
-- 2. 然后执行：source reset_root_password.sql
-- 3. 或者直接运行：mysql -u root -p < reset_root_password.sql

-- 将 'your_new_password' 替换为你想要的新密码
ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_new_password';
FLUSH PRIVILEGES;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS buy_ticket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

