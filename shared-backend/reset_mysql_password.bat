@echo off
echo ========================================
echo MySQL Root密码重置脚本
echo ========================================
echo.
echo 此脚本将帮助你重置MySQL root密码
echo 需要管理员权限运行
echo.
pause

echo.
echo 步骤1: 停止MySQL服务...
net stop MySQL
if %errorlevel% neq 0 (
    echo 错误: 无法停止MySQL服务，请以管理员身份运行此脚本
    pause
    exit /b 1
)

echo.
echo 步骤2: 以跳过权限表方式启动MySQL...
echo 注意: 这将打开一个新窗口，请不要关闭它
echo 请手动执行以下命令（在新窗口中）:
echo cd D:\mysql\mysql-9.5.0-winx64\bin
echo mysqld --skip-grant-tables --console
echo.
echo 按任意键继续（确保已在新窗口启动mysqld）...
pause >nul

echo.
echo 等待MySQL启动（5秒）...
timeout /t 5 /nobreak >nul

echo.
echo 步骤3: 重置root密码...
cd /d D:\mysql\mysql-9.5.0-winx64\bin
mysql -u root -e "USE mysql; ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123'; FLUSH PRIVILEGES;"

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo 密码重置成功！
    echo 新密码: root123
    echo ========================================
    echo.
    echo 请关闭MySQL窗口（mysqld进程），然后按任意键继续...
    pause >nul
    
    echo.
    echo 步骤4: 重启MySQL服务...
    net start MySQL
    
    echo.
    echo ========================================
    echo 完成！现在可以使用以下信息连接：
    echo 用户名: root
    echo 密码: root123
    echo ========================================
) else (
    echo.
    echo 错误: 密码重置失败
)

pause

