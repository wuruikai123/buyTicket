@echo off
chcp 65001 >nul
echo 测试后端 API 接口
echo.

echo 测试 Dashboard 统计接口:
curl http://localhost:8082/api/v1/admin/stats/dashboard
echo.
echo.

echo 测试用户列表接口:
curl "http://localhost:8082/api/v1/admin/user/list?page=1&size=10"
echo.
echo.

echo 测试展览列表接口:
curl "http://localhost:8082/api/v1/admin/exhibition/list?page=1&size=10"
echo.
echo.

pause
