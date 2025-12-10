# MySQL数据库连接问题解决方案

## 问题
Navicat连接MySQL时出现错误：`Access denied for user 'root'@'localhost'`

## 解决方案

### 方法1：使用重置脚本（推荐）

1. **以管理员身份运行PowerShell或命令提示符**
   - 右键点击"开始"菜单
   - 选择"Windows PowerShell (管理员)" 或 "命令提示符 (管理员)"

2. **运行重置脚本**
   ```powershell
   cd C:\Users\Lenovo\Desktop\12.7ddl\shared-backend
   .\reset_mysql_password.bat
   ```

3. **按照脚本提示操作**
   - 脚本会自动停止MySQL服务
   - 以跳过权限表方式启动MySQL
   - 重置root密码为 `root123`
   - 重启MySQL服务

4. **在Navicat中连接**
   - 主机: localhost
   - 端口: 3306
   - 用户名: root
   - 密码: root123

### 方法2：手动重置密码

如果脚本无法运行，可以手动执行以下步骤：

#### 步骤1：停止MySQL服务（需要管理员权限）
```powershell
net stop MySQL
```

#### 步骤2：以跳过权限表方式启动MySQL
打开新的命令提示符窗口，执行：
```powershell
cd D:\mysql\mysql-9.5.0-winx64\bin
mysqld --skip-grant-tables --console
```
**注意：保持这个窗口打开，不要关闭**

#### 步骤3：重置密码
打开另一个命令提示符窗口，执行：
```powershell
cd D:\mysql\mysql-9.5.0-winx64\bin
mysql -u root
```

然后在MySQL命令行中执行：
```sql
USE mysql;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';
FLUSH PRIVILEGES;
EXIT;
```

#### 步骤4：重启MySQL服务
1. 关闭步骤2中的mysqld窗口（按Ctrl+C）
2. 然后执行：
```powershell
net start MySQL
```

### 方法3：创建应用专用用户（更安全）

如果你成功用root登录了，可以创建一个专门的应用用户：

1. **用root登录MySQL**
```powershell
mysql -u root -p
# 输入密码: root123
```

2. **执行以下SQL**
```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS buy_ticket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建应用用户
CREATE USER 'buyticket'@'localhost' IDENTIFIED BY 'buyticket123';

-- 授予权限
GRANT ALL PRIVILEGES ON buy_ticket.* TO 'buyticket'@'localhost';
FLUSH PRIVILEGES;
```

3. **更新application.properties**
将用户名和密码改为：
```
spring.datasource.username=buyticket
spring.datasource.password=buyticket123
```

## 验证连接

重置密码后，测试连接：
```powershell
mysql -u root -proot123 -e "SELECT 'Connection successful!' as status;"
```

## 注意事项

- 重置后的默认密码是 `root123`，建议在生产环境中修改为更安全的密码
- 如果MySQL安装路径不同，请修改脚本中的路径
- 所有操作都需要管理员权限

