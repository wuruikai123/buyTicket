@echo off
echo ========================================
echo 测试 API 接口连通性
echo ========================================
echo.

echo 1. 测试统计接口
curl -X GET "http://localhost:8082/api/v1/admin/stats/dashboard"
echo.
echo.

echo 2. 测试用户列表接口
curl -X GET "http://localhost:8082/api/v1/admin/user/list?page=1&size=10"
echo.
echo.

echo 3. 测试今日核销数量接口
curl -X GET "http://localhost:8082/api/v1/admin/order/ticket/today-count"
echo.
echo.

echo ========================================
echo 测试完成
echo ========================================
pause
