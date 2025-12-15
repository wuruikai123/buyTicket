@echo off
echo ========================================
echo 测试C端核销功能
echo ========================================
echo.

echo 1. 检查后端是否运行在8082端口...
curl -s http://localhost:8082/api/v1/admin/order/ticket/today-count
echo.
echo.

echo 2. 测试核销接口（订单号：T1734240000000TEST1）...
curl -X POST http://localhost:8082/api/v1/admin/order/ticket/verify ^
  -H "Content-Type: application/json" ^
  -d "{\"orderNo\":\"T1734240000000TEST1\"}"
echo.
echo.

echo 3. 查询订单状态...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "SELECT id, order_no, status, contact_name FROM ticket_order WHERE order_no='T1734240000000TEST1';"
echo.

pause
