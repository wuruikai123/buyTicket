#!/bin/bash

echo "========================================"
echo "启动开发环境"
echo "========================================"
echo ""

echo "[1/3] 启动后端服务..."
cd shared-backend
gnome-terminal -- bash -c "mvn spring-boot:run; exec bash" 2>/dev/null || \
xterm -e "mvn spring-boot:run" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd \"'$(pwd)'\" && mvn spring-boot:run"' 2>/dev/null || \
(mvn spring-boot:run &)
cd ..

echo "等待后端启动（30秒）..."
sleep 30

echo ""
echo "[2/3] 启动用户端前端..."
cd frontend-a
gnome-terminal -- bash -c "npm run dev; exec bash" 2>/dev/null || \
xterm -e "npm run dev" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd \"'$(pwd)'\" && npm run dev"' 2>/dev/null || \
(npm run dev &)
cd ..

echo ""
echo "[3/3] 启动卖家端前端..."
cd frontend-c
gnome-terminal -- bash -c "npm run dev; exec bash" 2>/dev/null || \
xterm -e "npm run dev" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd \"'$(pwd)'\" && npm run dev"' 2>/dev/null || \
(npm run dev &)
cd ..

echo ""
echo "========================================"
echo "启动完成！"
echo "========================================"
echo ""
echo "访问地址："
echo "- 用户端: http://localhost:5173"
echo "- 卖家端: http://localhost:5175"
echo "- 后端API: http://localhost:8080"
echo ""
