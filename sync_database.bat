@echo off
REM 数据库同步脚本
REM 从远程服务器同步数据库到本地

echo ========================================
echo 数据库同步工具
echo ========================================
echo.

REM 配置信息
set REMOTE_HOST=47.121.192.245
set REMOTE_PORT=3306
set REMOTE_USER=root
set REMOTE_DB=buy_ticket
set LOCAL_USER=root
set LOCAL_DB=buy_ticket
set BACKUP_FILE=backup_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql

echo 步骤1: 从远程服务器导出数据库...
echo 远程服务器: %REMOTE_HOST%
echo 数据库名: %REMOTE_DB%
echo.

REM 导出远程数据库
mysqldump -h %REMOTE_HOST% -P %REMOTE_PORT% -u %REMOTE_USER% -p %REMOTE_DB% > %BACKUP_FILE%

if %errorlevel% neq 0 (
    echo 错误: 导出失败！
    echo 请检查:
    echo 1. 远程服务器是否可访问
    echo 2. MySQL 用户名和密码是否正确
    echo 3. 数据库名是否正确
    pause
    exit /b 1
)

echo 导出成功！文件: %BACKUP_FILE%
echo.

echo 步骤2: 导入到本地数据库...
echo 本地数据库: %LOCAL_DB%
echo.

REM 导入到本地
mysql -u %LOCAL_USER% -p %LOCAL_DB% < %BACKUP_FILE%

if %errorlevel% neq 0 (
    echo 错误: 导入失败！
    echo 请检查:
    echo 1. 本地 MySQL 是否运行
    echo 2. 本地数据库 %LOCAL_DB% 是否存在
    echo 3. 用户名和密码是否正确
    pause
    exit /b 1
)

echo.
echo ========================================
echo 同步完成！
echo 备份文件: %BACKUP_FILE%
echo ========================================
pause
