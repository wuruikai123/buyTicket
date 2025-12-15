@echo off
echo ========================================
echo 启动开发环境
echo ========================================
echo.

echo [1/3] 启动后端服务...
cd shared-backend
start "Backend Server" cmd /k "mvn spring-boot:run"
cd ..

echo 等待后端启动（30秒）...
timeout /t 30 /nobreak

echo.
echo [2/3] 启动用户端前端...
cd frontend-a
start "Frontend-A (User)" cmd /k "npm run dev"
cd ..

echo.
echo [3/3] 启动卖家端前端...
cd frontend-c
start "Frontend-C (Seller)" cmd /k "npm run dev"
cd ..

echo.
echo ========================================
echo 启动完成！
echo ========================================
echo.
echo 访问地址：
echo - 用户端: http://localhost:5173
echo - 卖家端: http://localhost:5175
echo - 后端API: http://localhost:8080
echo.
echo 按任意键关闭此窗口...
pause > nul
