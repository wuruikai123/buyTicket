@echo off
chcp 65001 >nul
echo ========================================
echo 支付宝内网穿透测试
echo ========================================
echo.

set NATAPP_URL=http://p22294f8.natappfree.cc
set LOCAL_URL=http://localhost:8082

echo 【步骤 1】测试内网穿透地址是否可访问
echo ----------------------------------------
echo 测试地址: %NATAPP_URL%
echo.
curl -I %NATAPP_URL% 2>nul
if %errorlevel% equ 0 (
    echo ✅ 内网穿透地址可访问
) else (
    echo ❌ 内网穿透地址无法访问
    echo.
    echo 请检查：
    echo 1. natapp 是否正在运行
    echo 2. 地址是否正确: %NATAPP_URL%
    echo.
    pause
    exit /b 1
)
echo.

echo 【步骤 2】测试本地后端服务
echo ----------------------------------------
echo 测试地址: %LOCAL_URL%
echo.
curl -s %LOCAL_URL%/api/v1/payment/alipay/notify -X POST 2>nul
if %errorlevel% equ 0 (
    echo ✅ 本地后端服务正常
) else (
    echo ❌ 本地后端服务未启动
    echo.
    echo 请先启动后端服务：
    echo cd shared-backend
    echo mvn spring-boot:run
    echo.
    pause
    exit /b 1
)
echo.

echo 【步骤 3】测试内网穿透转发
echo ----------------------------------------
echo 测试: %NATAPP_URL%/api/v1/payment/alipay/notify
echo.
curl -s -X POST %NATAPP_URL%/api/v1/payment/alipay/notify 2>nul
if %errorlevel% equ 0 (
    echo ✅ 内网穿透转发正常
    echo.
    echo 支付宝可以通过这个地址访问你的后端服务
) else (
    echo ❌ 内网穿透转发失败
    echo.
    echo 请检查 natapp 配置是否正确
)
echo.

echo 【步骤 4】查看当前配置
echo ----------------------------------------
echo 异步通知地址（notify_url）:
echo   %NATAPP_URL%/api/v1/payment/alipay/notify
echo   （支付宝会POST请求这个地址）
echo.
echo 同步通知地址（return_url）:
echo   http://localhost:3000/order/success
echo   （用户浏览器会跳转到这个地址）
echo.
echo 配置文件:
echo   shared-backend/src/main/java/com/buyticket/config/AlipayConfig.java
echo ----------------------------------------
echo.

echo 【步骤 5】完整测试流程
echo ----------------------------------------
echo 1. ✓ 确保 natapp 正在运行
echo 2. ✓ 确保后端服务已启动（端口 8080）
echo 3. ⏳ 启动前端服务（端口 3000）
echo 4. ⏳ 创建订单
echo 5. ⏳ 发起支付
echo 6. ⏳ 在支付宝沙箱完成支付
echo 7. ⏳ 查看后端日志，确认收到异步通知
echo ----------------------------------------
echo.

echo 【测试命令】
echo ----------------------------------------
echo 测试异步通知接口:
echo curl -X POST %NATAPP_URL%/api/v1/payment/alipay/notify
echo.
echo 测试本地接口:
echo curl -X POST %LOCAL_URL%/api/v1/payment/alipay/notify
echo ----------------------------------------
echo.

echo ✅ 内网穿透配置测试完成！
echo.
pause
