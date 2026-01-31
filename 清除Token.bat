@echo off
echo ========================================
echo 清除浏览器Token工具
echo ========================================
echo.
echo 请按照以下步骤操作：
echo.
echo 1. 打开浏览器（Chrome/Edge）
echo 2. 按 F12 打开开发者工具
echo 3. 切换到 Console（控制台）标签
echo 4. 复制下面的命令并粘贴到控制台
echo 5. 按回车执行
echo.
echo ========================================
echo 命令：
echo ========================================
echo localStorage.removeItem('token');
echo localStorage.removeItem('userInfo');
echo location.reload();
echo ========================================
echo.
echo 执行后页面会自动刷新，token已清除
echo.
pause
