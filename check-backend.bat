@echo off
echo ========================================
echo 检查后端服务状态
echo ========================================
echo.

echo 检查端口 8080 是否被占用...
netstat -ano | findstr :8080

if %ERRORLEVEL% EQU 0 (
    echo.
    echo 端口 8080 已被占用，后端可能正在运行
    echo.
    echo 测试 API 连接...
    curl -s http://localhost:8080/api/v1/admin/stats/dashboard
    echo.
) else (
    echo.
    echo 端口 8080 未被占用，后端服务未运行
    echo.
    echo 请启动后端服务：
    echo   cd shared-backend
    echo   mvnw.cmd spring-boot:run
    echo.
)

pause
