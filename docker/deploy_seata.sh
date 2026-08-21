#!/bin/bash
# ===== Seata 部署脚本：在 Linux 服务器上执行 =====
# 功能：1) 初始化 undo_log 表  2) 启动 seata-server  3) 重启订单/商品服务

set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
MYSQL_HOST="192.168.209.128"
MYSQL_USER="root"
MYSQL_PASS="123456"

echo "===== 0. 检查 docker 是否可用 ====="
if ! command -v docker >/dev/null 2>&1; then
  echo "未检测到 docker,请先安装 Docker"
  exit 1
fi
if ! command -v mysql >/dev/null 2>&1; then
  echo "未检测到 mysql 客户端,请安装: yum install mysql 或 apt install mysql-client"
  exit 1
fi

echo ""
echo "===== 1. 初始化 Seata undo_log 表 ====="
mysql -h "$MYSQL_HOST" -uroot -p"$MYSQL_PASS" trade_order_db < "$PROJECT_DIR/order-service/sql/seata_undo_log.sql"
mysql -h "$MYSQL_HOST" -uroot -p"$MYSQL_PASS" trade_product_db < "$PROJECT_DIR/product-service/sql/seata_undo_log.sql"
echo "undo_log 表创建完成"

echo ""
echo "===== 2. 构建最新业务镜像 ====="
cd "$PROJECT_DIR"
mvn clean package -DskipTests
cd docker
docker compose build order-service product-service

echo ""
echo "===== 3. 启动 seata-server ====="
docker compose up -d seata-server
echo "等待 seata-server 启动并注册到 Nacos..."
sleep 15
docker compose ps seata-server

echo ""
echo "===== 4. 重启订单/商品服务 ====="
docker compose up -d order-service product-service

echo ""
echo "===== 5. 验证 ====="
sleep 20
echo "--- Seata Server 状态 ---"
docker compose ps seata-server
echo ""
echo "--- 各服务容器状态 ---"
docker compose ps order-service product-service
echo ""
echo "--- undo_log 表 ---"
mysql -h "$MYSQL_HOST" -uroot -p"$MYSQL_PASS" -e "SHOW TABLES FROM trade_order_db LIKE 'undo_log'; SHOW TABLES FROM trade_product_db LIKE 'undo_log';"

echo ""
echo "===== Seata 部署完成 ====="
echo "Seata 控制台/端口: 192.168.209.128:8091"
echo "请到 Nacos http://192.168.209.128:8848/nacos 确认 seata-server 已注册"
echo "然后打开前端页面实际下单,观察 order-service 与 product-service 日志"
