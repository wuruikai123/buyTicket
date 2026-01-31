#!/bin/bash

echo "=========================================="
echo "测试后端接口 - 诊断 301 重定向问题"
echo "=========================================="
echo ""

echo "1. 测试管理员登录（不带尾部斜杠）"
echo "命令: curl -v -X POST http://127.0.0.1:8089/api/v1/admin/auth/login"
echo "------------------------------------------"
curl -v -X POST http://127.0.0.1:8089/api/v1/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' 2>&1 | grep -E "(HTTP|Location|{)"
echo ""
echo ""

echo "2. 测试管理员登录（带尾部斜杠）"
echo "命令: curl -v -X POST http://127.0.0.1:8089/api/v1/admin/auth/login/"
echo "------------------------------------------"
curl -v -X POST http://127.0.0.1:8089/api/v1/admin/auth/login/ \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' 2>&1 | grep -E "(HTTP|Location|{)"
echo ""
echo ""

echo "3. 测试用户登录（对比）"
echo "命令: curl -v -X POST http://127.0.0.1:8089/api/v1/user/login"
echo "------------------------------------------"
curl -v -X POST http://127.0.0.1:8089/api/v1/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}' 2>&1 | grep -E "(HTTP|Location|{)"
echo ""
echo ""

echo "4. 列出所有可用的 API 端点"
echo "命令: curl http://127.0.0.1:8089/actuator/mappings"
echo "------------------------------------------"
curl -s http://127.0.0.1:8089/actuator/mappings 2>&1 | head -20
echo ""
echo ""

echo "=========================================="
echo "诊断完成"
echo "=========================================="
