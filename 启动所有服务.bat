@echo off
chcp 65001 >nul
echo ========================================
echo 启动展览门票系统所有服务
echo ========================================
echo.

echo 【服务端口分配】
echo ----------------------------------------
echo 后端服务:      http://localhost:8082
echo 用户端(A端):   http://localhost:3000
echo 管理端(B端):   http://localhost:3001
echo 卖家端(C端):   http://localhost:3002
echo MySQL数据库:   localhost:3307
echo ----------------------------------------
echo.

echo 【步骤 1】检查 MySQL 是否运行
echo ----------------------------------------
netstat -ano | findstr :3307 >nul
if %errorlevel% equ 0 (
    echo ✅ MySQL 正在运行（端口 3307）
) else (
    echo ❌ MySQL 未运行
    echo 请先启动 MySQL 或 Docker:
    echo   docker-compose up -d mysql
    pause
    exit /b 1
)
echo.

echo 【步骤 2】启动后端服务
echo ----------------------------------------
echo 正在启动后端服务...
echo 命令: cd shared-backend ^&^& mvn spring-boot:run
echo.
start "后端服务 (8080)" cmd /k "cd shared-backend && mvn spring-boot:run"
echo ⏳ 等待后端服务启动（30秒）...
timeout /t 30 /nobreak >nul
echo.

echo 【步骤 3】检查后端服务
echo ----------------------------------------
curl -s http://localhost:8082/api/v1/banner/list >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 后端服务启动成功
) else (
    echo ⚠️  后端服务可能还在启动中，请稍后检查
)
echo.

echo 【步骤 4】启动前端服务
echo ----------------------------------------
echo 正在启动用户端（A端）- 端口 3000...
start "用户端 (3000)" cmd /k "cd frontend-a && npm run dev"
timeout /t 3 /nobreak >nul

echo 正在启动管理端（B端）- 端口 3001...
start "管理端 (3001)" cmd /k "cd frontend-b && npm run dev"
timeout /t 3 /nobreak >nul

echo 正在启动卖家端（C端）- 端口 3002...
start "卖家端 (3002)" cmd /k "cd frontend-c && npm run dev"
echo.

echo ⏳ 等待前端服务启动（15秒）...
timeout /t 15 /nobreak >nul
echo.

echo ========================================
echo ✅ 所有服务启动完成！
echo ========================================
echo.
echo 【访问地址】
echo ----------------------------------------
echo 用户端:  http://localhost:3000
echo 管理端:  http://localhost:3001
echo 卖家端:  http://localhost:3002
echo 后端API: http://localhost:8082
echo ----------------------------------------
echo.
echo 【测试支付功能】
echo ----------------------------------------
echo 1. 访问用户端: http://localhost:3000
echo 2. 登录系统
echo 3. 选择展览并创建订单
echo 4. 点击支付按钮
echo 5. 在支付宝沙箱完成支付
echo ----------------------------------------
echo.
echo 按任意键打开用户端...
pause >nul
start http://localhost:3000
