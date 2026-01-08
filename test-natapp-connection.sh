#!/bin/bash

echo "========================================"
echo "测试内网穿透连接"
echo "========================================"
echo ""

echo "测试 1: 访问本地后端"
echo "URL: http://localhost:8080/api/v1/payment/test/success"
curl -X GET http://localhost:8080/api/v1/payment/test/success
echo ""
echo ""

echo "测试 2: 访问内网穿透地址"
echo "URL: http://z9b46c66.natappfree.cc/api/v1/payment/test/success"
curl -X GET http://z9b46c66.natappfree.cc/api/v1/payment/test/success
echo ""
echo ""

echo "测试 3: 测试支付宝异步通知接口（模拟POST请求）"
echo "URL: http://z9b46c66.natappfree.cc/api/v1/payment/alipay/notify"
curl -X POST http://z9b46c66.natappfree.cc/api/v1/payment/alipay/notify -d "test=1"
echo ""
echo ""

echo "========================================"
echo "测试完成"
echo "========================================"
