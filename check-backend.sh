#!/bin/bash

echo "========================================"
echo "检查后端服务状态"
echo "========================================"
echo ""

echo "检查端口 8080 是否被占用..."
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo ""
    echo "✓ 端口 8080 已被占用，后端可能正在运行"
    echo ""
    echo "测试 API 连接..."
    curl -s http://localhost:8080/api/v1/admin/stats/dashboard | head -n 5
    echo ""
    echo ""
    echo "如果看到 JSON 数据，说明后端运行正常"
else
    echo ""
    echo "✗ 端口 8080 未被占用，后端服务未运行"
    echo ""
    echo "请启动后端服务："
    echo "  cd shared-backend"
    echo "  ./mvnw spring-boot:run"
    echo ""
fi
