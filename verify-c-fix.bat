@echo off
chcp 65001 >nul
echo ========================================
echo C端核销功能修复验证
echo ========================================
echo.

echo [1/5] 检查seller账号...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "SELECT username, status FROM admin_user WHERE username='seller';" 2>nul
if %errorlevel% neq 0 (
    echo ❌ seller账号不存在，正在创建...
    mysql -h localhost -P 3306 -u root -p0615 buy_ticket < create-seller-account.sql
    echo ✅ seller账号已创建
) else (
    echo ✅ seller账号存在
)
echo.

echo [2/5] 检查测试订单...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "SELECT order_no, status FROM ticket_order WHERE order_no='T1734240000000TEST1';" 2>nul
if %errorlevel% neq 0 (
    echo ❌ 测试订单不存在，正在创建...
    mysql -h localhost -P 3306 -u root -p0615 buy_ticket < FINAL_SOLUTION.sql
    echo ✅ 测试订单已创建
) else (
    echo ✅ 测试订单存在
)
echo.

echo [3/5] 重置测试订单状态为待使用...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "UPDATE ticket_order SET status = 1 WHERE order_no = 'T1734240000000TEST1';"
echo ✅ 订单状态已重置
echo.

echo [4/5] 检查后端是否运行...
curl -s http://localhost:8082/api/v1/admin/order/ticket/today-count >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 后端未运行在8082端口
    echo.
    echo 请在新窗口运行：
    echo   cd shared-backend
    echo   mvn spring-boot:run
    echo.
    pause
    exit /b 1
) else (
    echo ✅ 后端运行正常
)
echo.

echo [5/5] 测试登录接口...
curl -X POST http://localhost:8082/api/v1/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"seller\",\"password\":\"123456\"}" ^
  -s -o login-response.json
  
findstr /C:"token" login-response.json >nul
if %errorlevel% neq 0 (
    echo ❌ 登录接口测试失败
    type login-response.json
    del login-response.json
    pause
    exit /b 1
) else (
    echo ✅ 登录接口测试成功
    del login-response.json
)
echo.

echo ========================================
echo ✅ 所有检查通过！
echo ========================================
echo.
echo 现在可以测试C端核销功能：
echo.
echo 1. 启动C端（如果还未启动）：
echo    cd frontend-c
echo    npm run dev
echo.
echo 2. 访问：http://localhost:5174
echo.
echo 3. 登录信息：
echo    账号：seller
echo    密码：123456
echo.
echo 4. 测试订单号：T1734240000000TEST1
echo.
pause
