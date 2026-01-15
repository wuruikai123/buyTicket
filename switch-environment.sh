#!/bin/bash

# 环境切换脚本
# 用法: ./switch-environment.sh [dev|prod]

if [ $# -eq 0 ]; then
    echo "用法: ./switch-environment.sh [dev|prod]"
    echo "  dev  - 切换到开发环境（沙箱）"
    echo "  prod - 切换到生产环境"
    exit 1
fi

ENV=$1
CONFIG_FILE="shared-backend/src/main/resources/application.yml"

if [ "$ENV" = "dev" ]; then
    echo "切换到开发环境（沙箱）..."
    sed -i 's/active: prod/active: dev/g' $CONFIG_FILE
    echo "✅ 已切换到开发环境"
    echo ""
    echo "当前配置："
    echo "  - 环境: 开发环境（沙箱）"
    echo "  - APPID: 9021000158671506"
    echo "  - 网关: https://openapi-sandbox.dl.alipaydev.com/gateway.do"
    echo "  - 回调地址: 需要配置 natapp"
    echo ""
    echo "下一步："
    echo "  1. 启动 natapp"
    echo "  2. 修改 application.yml 中的 notify-url 为 natapp 地址"
    echo "  3. 重启后端服务"
    
elif [ "$ENV" = "prod" ]; then
    echo "切换到生产环境..."
    sed -i 's/active: dev/active: prod/g' $CONFIG_FILE
    echo "✅ 已切换到生产环境"
    echo ""
    echo "当前配置："
    echo "  - 环境: 生产环境"
    echo "  - APPID: 从环境变量读取"
    echo "  - 网关: https://openapi.alipay.com/gateway.do"
    echo "  - 回调地址: https://www.yourdomain.com"
    echo ""
    echo "⚠️  注意："
    echo "  1. 确保已设置环境变量："
    echo "     - ALIPAY_APP_ID"
    echo "     - ALIPAY_MERCHANT_PRIVATE_KEY"
    echo "     - ALIPAY_PUBLIC_KEY"
    echo "  2. 确保域名已配置并可访问"
    echo "  3. 重新打包并部署到服务器"
    
else
    echo "❌ 错误: 无效的环境参数"
    echo "用法: ./switch-environment.sh [dev|prod]"
    exit 1
fi
