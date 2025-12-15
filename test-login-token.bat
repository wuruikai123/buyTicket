@echo off
chcp 65001 >nul
echo ========================================
echo 测试登录和Token
echo ========================================
echo.

echo [1] 测试登录接口...
curl -X POST http://localhost:8082/api/v1/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"seller\",\"password\":\"123456\"}" ^
  -s -o login-response.json

echo 登录响应：
type login-response.json
echo.
echo.

REM 提取token（简化版，实际需要JSON解析）
for /f "tokens=2 delims=:," %%a in ('findstr /C:"token" login-response.json') do (
    set TOKEN=%%a
)
set TOKEN=%TOKEN:"=%
set TOKEN=%TOKEN: =%

echo [2] 提取的Token：
echo %TOKEN%
echo.

echo [3] 测试不带Bearer前缀的请求...
curl -X GET http://localhost:8082/api/v1/admin/order/ticket/today-count ^
  -H "Authorization: %TOKEN%" ^
  -s

echo.
echo.

echo [4] 测试带Bearer前缀的请求...
curl -X GET http://localhost:8082/api/v1/admin/order/ticket/today-count ^
  -H "Authorization: Bearer %TOKEN%" ^
  -s

echo.
echo.

del login-response.json

pause
