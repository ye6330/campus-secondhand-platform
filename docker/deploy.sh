#!/bin/bash
# ===== 部署脚本：在项目根目录执行 =====

echo "===== 1. 编译所有微服务 ====="
cd "$(dirname "$0")/.."

mvn clean package -DskipTests

echo ""
echo "===== 2. 检查 jar 包是否生成 ====="
ls -lh gateway-service/target/*.jar
ls -lh user-service/target/*.jar
ls -lh product-service/target/*.jar
ls -lh order-service/target/*.jar
ls -lh message-service/target/*.jar
ls -lh admin-service/target/*.jar

echo ""
echo "===== 3. 启动全部服务 ====="
cd docker
docker compose up -d

echo ""
echo "===== 4. 检查所有容器状态 ====="
docker compose ps

echo ""
echo "===== 部署完成 ====="
echo "Nacos 控制台: http://服务器IP:8848/nacos"
echo "前端页面:      http://服务器IP"
echo "RabbitMQ 控制台: http://服务器IP:15672"
