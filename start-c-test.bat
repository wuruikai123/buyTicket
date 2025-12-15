@echo off
echo ========================================
echo C端核销功能测试 - 快速启动
echo ========================================
echo.

echo 步骤1: 创建seller管理员账号...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket < create-seller-account.sql
echo.

echo 步骤2: 准备测试数据...
echo 正在重置测试订单状态...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "UPDATE ticket_order SET status = 1 WHERE order_no = 'T1734240000000TEST1';"
echo.

echo 步骤3: 验证测试订单...
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "SELECT id, order_no, status, contact_name FROM ticket_order WHERE order_no='T1734240000000TEST1';"
echo.

echo 步骤4: 启动说明
echo.
echo 请在新的命令行窗口中执行以下命令：
echo.
echo 窗口1 - 启动后端:
echo   cd shared-backend
echo   mvn spring-boot:run
echo.
echo 窗口2 - 启动C端:
echo   cd frontend-c
echo   npm run dev
echo.
echo 然后访问: http://localhost:5174
echo 登录账号: seller
echo 登录密码: 123456
echo 测试订单号: T1734240000000TEST1
echo.

pause
