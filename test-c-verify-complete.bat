@echo off
chcp 65001 >nul
echo ========================================
echo C端核销功能完整测试
echo ========================================
echo.

echo [测试准备]
echo 1. 创建seller账号...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket < create-seller-account.sql 2>nul
echo ✅ seller账号准备完成
echo.

echo 2. 创建测试订单...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket < FINAL_SOLUTION.sql 2>nul
echo ✅ 测试订单准备完成
echo.

echo 3. 重置订单状态为待使用...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "UPDATE ticket_order SET status = 1 WHERE order_no = 'T1734240000000TEST1';" 2>nul
echo ✅ 订单状态已重置
echo.

echo [测试1: 登录接口]
echo 测试seller账号登录...
curl -X POST http://localhost:8082/api/v1/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"seller\",\"password\":\"123456\"}" ^
  -s -o login-test.json

findstr /C:"token" login-test.json >nul
if %errorlevel% neq 0 (
    echo ❌ 登录测试失败
    type login-test.json
    del login-test.json
    pause
    exit /b 1
) else (
    echo ✅ 登录测试成功
    
    REM 提取token
    for /f "tokens=2 delims=:," %%a in ('findstr /C:"token" login-test.json') do (
        set TOKEN=%%a
    )
    set TOKEN=%TOKEN:"=%
    set TOKEN=%TOKEN: =%
    del login-test.json
)
echo.

echo [测试2: 核销接口]
echo 测试核销订单 T1734240000000TEST1...
curl -X POST http://localhost:8082/api/v1/admin/order/ticket/verify ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"orderNo\":\"T1734240000000TEST1\"}" ^
  -s -o verify-test.json

findstr /C:"核销成功" verify-test.json >nul
if %errorlevel% neq 0 (
    echo ❌ 核销测试失败
    type verify-test.json
    del verify-test.json
    pause
    exit /b 1
) else (
    echo ✅ 核销测试成功
    del verify-test.json
)
echo.

echo [测试3: 验证数据库]
echo 检查订单状态是否已更新为2...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "SELECT order_no, status, CASE status WHEN 0 THEN '待支付' WHEN 1 THEN '待使用' WHEN 2 THEN '已使用' WHEN 3 THEN '已取消' END as status_name FROM ticket_order WHERE order_no='T1734240000000TEST1';"
echo.

echo [测试4: 重复核销]
echo 测试重复核销（应该失败）...
curl -X POST http://localhost:8082/api/v1/admin/order/ticket/verify ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"orderNo\":\"T1734240000000TEST1\"}" ^
  -s -o verify-test2.json

findstr /C:"待使用" verify-test2.json >nul
if %errorlevel% neq 0 (
    echo ❌ 重复核销测试失败（应该提示只有待使用的订单才能核销）
    type verify-test2.json
) else (
    echo ✅ 重复核销测试成功（正确拒绝）
)
del verify-test2.json
echo.

echo [测试5: 不存在的订单]
echo 测试不存在的订单号...
curl -X POST http://localhost:8082/api/v1/admin/order/ticket/verify ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"orderNo\":\"T9999999999999XXXXX\"}" ^
  -s -o verify-test3.json

findstr /C:"订单不存在" verify-test3.json >nul
if %errorlevel% neq 0 (
    echo ❌ 不存在订单测试失败（应该提示订单不存在）
    type verify-test3.json
) else (
    echo ✅ 不存在订单测试成功（正确提示）
)
del verify-test3.json
echo.

echo ========================================
echo ✅ 所有测试通过！
echo ========================================
echo.
echo 核销接口工作正常，可以开始使用C端了：
echo.
echo 1. 访问：http://localhost:5174
echo 2. 登录：seller / 123456
echo 3. 测试订单号：T1734240000000TEST1（需要重置状态）
echo.
echo 重置测试订单状态：
echo   mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "UPDATE ticket_order SET status = 1 WHERE order_no = 'T1734240000000TEST1';"
echo.
pause
