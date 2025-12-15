@echo off
echo ========================================
echo 测试核销 API
echo ========================================
echo.

REM 测试订单号（需要替换为实际的订单号）
set ORDER_NO=T1765772332101ZTIV8Y

echo 1. 测试查询订单接口
echo GET /api/v1/admin/order/ticket/query?orderNo=%ORDER_NO%
echo.
curl -s "http://localhost:8080/api/v1/admin/order/ticket/query?orderNo=%ORDER_NO%"
echo.
echo.

echo 2. 测试今日核销数量接口
echo GET /api/v1/admin/order/ticket/today-count
echo.
curl -s "http://localhost:8080/api/v1/admin/order/ticket/today-count"
echo.
echo.

echo 3. 测试核销接口（需要待使用状态的订单）
echo POST /api/v1/admin/order/ticket/verify
echo.
curl -s -X POST "http://localhost:8080/api/v1/admin/order/ticket/verify" -H "Content-Type: application/json" -d "{\"orderNo\":\"%ORDER_NO%\"}"
echo.
echo.

pause
