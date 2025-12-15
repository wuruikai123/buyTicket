@echo off
echo ========================================
echo 修复订单号字段错误
echo ========================================
echo.

echo 正在连接数据库并执行修复脚本...
echo.

mysql -u root -p buy_ticket < shared-backend\src\main\resources\sql\fix_order_no_field.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo 修复成功！
    echo ========================================
    echo.
    echo 请重启后端服务以应用更改
) else (
    echo.
    echo ========================================
    echo 修复失败，请检查错误信息
    echo ========================================
)

pause
