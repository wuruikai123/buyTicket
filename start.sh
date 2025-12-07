#!/bin/bash

echo "=========================================="
echo "  展览购票系统 Docker 启动脚本"
echo "=========================================="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker未运行，请先启动Docker"
    exit 1
fi

# 停止并删除旧容器
echo "正在停止旧容器..."
docker-compose down

# 构建并启动
echo "正在构建并启动服务..."
docker-compose up --build -d

# 等待服务启动
echo "等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo "=========================================="
echo "  服务状态"
echo "=========================================="
docker-compose ps

echo ""
echo "=========================================="
echo "  访问地址"
echo "=========================================="
echo "用户端:   http://localhost:3000"
echo "管理端:   http://localhost:3001"
echo "后端API:  http://localhost:8080"
echo "MySQL:    localhost:3306"
echo ""
echo "管理端登录: admin / 123456"
echo "用户端登录: zhangsan / 123456"
echo "=========================================="
