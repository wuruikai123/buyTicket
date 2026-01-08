@echo off
chcp 65001 >nul
echo ========================================
echo 支付宝支付配置测试
echo ========================================
echo.

echo 1. 检查内网穿透地址是否可访问
echo 内网穿透地址: http://p22294f8.natappfree.cc
echo.
curl -I http://p22294f8.natappfree.cc 2>nul
if %errorlevel% equ 0 (
    echo ✓ 内网穿透地址可访问
) else (
    echo ✗ 内网穿透地址无法访问，请检查 natapp 是否正在运行
)
echo.

echo 2. 检查本地后端服务
echo 本地后端: http://localhost:8080
echo.
curl -s http://localhost:8080/api/v1/payment/alipay/notify -X POST 2>nul
if %errorlevel% equ 0 (
    echo ✓ 本地后端服务正常
) else (
    echo ✗ 本地后端服务未启动
)
echo.

echo 3. 当前配置信息
echo ----------------------------------------
echo 异步通知地址（notify_url）:
echo   http://p22294f8.natappfree.cc/api/v1/payment/alipay/notify
echo   （支付宝会POST请求这个地址）
echo.
echo 同步通知地址（return_url）:
echo   http://localhost:3000/order/success
echo   （用户浏览器会跳转到这个地址）
echo ----------------------------------------
echo.

echo 4. 测试步骤
echo ----------------------------------------
echo 1) 确保 natapp 正在运行
echo 2) 启动后端服务（端口 8080）
echo 3) 启动前端服务（端口 3000）
echo 4) 创建订单并发起支付
echo 5) 在支付宝沙箱完成支付
echo 6) 查看后端日志，确认收到异步通知
echo ----------------------------------------
echo.

pause
