# 校园二手交易平台

基于 Spring Cloud Alibaba 的校园二手交易平台。项目采用前后端分离与微服务架构，覆盖用户认证、商品发布与审核、收藏评论、订单交易、实时私信、通知治理、后台审计和中文商品搜索等完整业务链路。

## 项目亮点

- 微服务拆分：Gateway 统一入口，Nacos 提供服务注册发现，服务之间通过 OpenFeign 调用。
- 交易一致性：下单、确认、拒绝、取消接入 Seata AT 分布式事务；商品状态与订单数据跨库一致提交或回滚。
- 并发控制：商品进入交易中前使用数据库行锁，避免同一商品被多个买家同时下单。
- 搜索优化：商品数据同步至 Elasticsearch，接入 IK 中文分词，支持关键词检索。
- 缓存优化：商品详情使用 Redis 缓存，减少热点商品的数据库查询压力。
- 实时通信：私信使用 WebSocket 实时推送，系统通知通过 RabbitMQ 异步投递。
- 可审计性：关键业务操作使用自定义 `@OperationLog` 注解和 AOP 自动记录操作日志，后台可查询。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 后端 | Java 11、Spring Boot 2.7、Spring Cloud 2021、Spring Cloud Alibaba |
| 微服务 | Gateway、Nacos、OpenFeign、Seata AT |
| 数据层 | MySQL 8、MyBatis-Plus、Redis、Elasticsearch + IK |
| 消息与通信 | RabbitMQ、WebSocket |
| 安全与治理 | Spring Security、JWT、图形验证码、AOP 操作日志 |
| 前端 | Vue 3、Vite、Element Plus |
| 部署 | Docker Compose、Nginx |

## 系统架构

```text
浏览器
  │
  ├── 本地开发：Vite（5173）
  └── 部署访问：Nginx（静态资源 / API / WebSocket 反向代理）
                         │
                         ▼
                Gateway（9000，统一路由 / 跨域）
                         │
       ┌─────────────────┼─────────────────┐
       ▼                 ▼                 ▼
 user-service      product-service     order-service
 用户/JWT/OSS       商品/收藏/评论       下单/交易状态
    9101            Redis/ES，9102         9103
       │                 │                 │
       └─────────────────┼───────┬─────────┘
                         ▼       ▼
                message-service  admin-service
                通知/私信 WS       举报/操作日志
                    9104             9105

基础设施（Linux Docker 或独立服务器）
  MySQL  /  Redis  /  RabbitMQ  /  Nacos  /  Seata Server  /  Elasticsearch
```

服务之间使用 OpenFeign 调用；Nacos 提供服务发现；Seata Server 注册到 Nacos，协调订单与商品服务的跨库事务。

## 模块说明

| 模块 | 端口 | 职责 |
| --- | ---: | --- |
| `gateway-service` | 9000 | 网关路由、跨域处理、统一 API 入口 |
| `user-service` | 9101 | 注册登录、JWT、验证码、个人资料、头像与 OSS 上传 |
| `product-service` | 9102 | 商品 CRUD、审核、收藏、评论、浏览量、Redis 缓存、ES 搜索 |
| `order-service` | 9103 | 下单、确认、拒绝、取消、订单查询、交易状态流转 |
| `message-service` | 9104 | RabbitMQ 通知消费、私信、未读统计、WebSocket 推送 |
| `admin-service` | 9105 | 商品举报治理、操作日志后台查询 |
| `common/*` | - | 统一响应、DTO、异常处理、安全上下文、AOP 与 Knife4j 支持 |

## 核心功能

| 角色 | 功能 |
| --- | --- |
| 游客 | 商品浏览、分类与关键词搜索、查看卖家在售商品 |
| 普通用户 | 注册登录、图形验证码、个人资料、头像上传、收藏、评论、私信、通知、举报 |
| 卖家 | 发布/编辑/删除商品、上下架、查看订单、确认或拒绝购买意向、上传交易现场图 |
| 买家 | 浏览商品、发起购买意向、取消待确认订单、查看购买订单与通知 |
| 管理员 | 商品审核、举报处理、订单查询、关键操作日志查询 |

## 核心业务流程

### 商品交易状态

```text
买家下单
  │
  ▼
商品：已上架 ──► 交易中 ──► 卖家确认 ──► 已售出
                   │
                   └────► 卖家拒绝 / 买家取消 ──► 已上架
```

- 交易中和已售出的商品禁止编辑，前后端均进行校验。
- 商品下架或售出时，其他用户的收藏会被隐藏；重新上架后恢复。
- 下单时商品记录使用 `SELECT ... FOR UPDATE` 行锁，避免并发下单导致状态竞争。

### 分布式事务（Seata AT）

下单、确认、拒绝、取消会同时修改订单库和商品库。`order-service` 使用 `@GlobalTransactional` 发起全局事务，Feign 自动传递 XID；`product-service` 通过 Seata 数据源代理注册 AT 分支事务。

```text
order-service 发起全局事务
  ├── product-service：修改 product.status
  ├── order-service：写入或更新 orders
  └── 任一步失败：Seata 根据 undo_log 自动回滚所有已提交分支
```

- 事务参与库：`trade_order_db`、`trade_product_db`。
- 两个库均需创建 `undo_log` 表，脚本位于 `order-service/sql/seata_undo_log.sql` 和 `product-service/sql/seata_undo_log.sql`。
- RabbitMQ 通知保持异步最终一致性，不纳入 Seata 全局事务。

### 通知、私信与搜索

```text
订单操作 ──► RabbitMQ ──► message-service ──► 系统通知

用户发送私信 ──► WebSocket ──► message-service ──► 对方前端实时刷新

商品新增/更新/状态变更 ──► Elasticsearch 索引 ──► IK 中文关键词检索
```

## 本地开发启动

当前推荐开发模式：**Linux Docker 运行中间件，本地 IDEA 运行 Java 微服务，Vite 运行前端。** 开发机与 Linux 服务器需要网络互通。

### 1. 准备中间件

环境要求：JDK 11、Maven 3.6+、Node.js 18+、Docker 与 Docker Compose。

启动 MySQL、Redis、RabbitMQ、Nacos 等基础容器后，确认以下服务可访问：

```text
Nacos：    http://192.168.209.128:8848/nacos
RabbitMQ： http://192.168.209.128:15672
```

Elasticsearch 需运行在 `192.168.209.128:9200`，并安装与服务端版本匹配的 IK 分词插件。

### 2. 部署 Seata Server

首次执行以下命令，在 Linux 服务器启动 Seata Server 并注册至 Nacos。将 `192.168.209.128` 替换为实际服务器 IP；若 Nacos 账号密码已修改，也需要同步替换。

```bash
docker run -d --name campus-seata-server \
  --restart unless-stopped \
  -p 8091:8091 \
  -p 7091:7091 \
  -e SEATA_IP=192.168.209.128 \
  -e SEATA_PORT=8091 \
  -e seata.registry.type=nacos \
  -e seata.registry.nacos.serverAddr=192.168.209.128:8848 \
  -e seata.registry.nacos.group=SEATA_GROUP \
  -e seata.registry.nacos.username=nacos \
  -e seata.registry.nacos.password=nacos \
  seataio/seata-server:1.6.1
```

验证 Seata Server 已注册：

```bash
curl "http://192.168.209.128:8848/nacos/v1/ns/instance/list?serviceName=seata-server&groupName=SEATA_GROUP"
```

返回的 `hosts` 中存在 `192.168.209.128:8091` 且 `healthy` 为 `true` 即表示成功。已有容器可直接启动：

```bash
docker start campus-seata-server
```

### 3. 初始化 Seata 回滚日志表

在 MySQL 中分别执行：

```bash
mysql -h 192.168.209.128 -uroot -p trade_order_db < order-service/sql/seata_undo_log.sql
mysql -h 192.168.209.128 -uroot -p trade_product_db < product-service/sql/seata_undo_log.sql
```

### 4. 启动后端服务

使用 IntelliJ IDEA 打开根目录 `pom.xml`，按以下顺序运行：

```text
1. user-service
2. product-service
3. order-service
4. message-service
5. admin-service
6. gateway-service
```

各服务启动成功后，在 Nacos 控制台确认六个业务服务健康注册；Seata Server 以 `SEATA_GROUP@@seata-server` 注册。

### 4.1 环境变量配置（重要）

中间件地址、数据库密码、OSS 密钥均通过环境变量注入，未配置时会回退到以下默认值：数据库/Nacos/Redis/RabbitMQ/ES/Seata 指向 `192.168.209.128`，OSS Access Key 为空。

本地开发时，在项目根目录创建 `.env`（该文件已被 `.gitignore` 忽略，不会提交）：

```properties
DB_HOST=192.168.209.128
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=你的MySQL密码
NACOS_ADDR=192.168.209.128:8848
REDIS_HOST=192.168.209.128
REDIS_PORT=6379
REDIS_PASSWORD=你的Redis密码
RABBITMQ_HOST=192.168.209.128
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
ES_HOST=192.168.209.128
ES_PORT=9200
SEATA_ADDR=192.168.209.128:8091
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
OSS_BUCKET=你的Bucket名称
OSS_ACCESS_KEY_ID=你的AccessKeyId
OSS_ACCESS_KEY_SECRET=你的AccessKeySecret
```

各服务启动时会自动读取项目根目录及模块上级目录的 `.env`，无需在 IDEA 中重复配置环境变量。Docker Compose 部署时，变量从 `docker/.env` 读取，模板见 `docker/.env.example`。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。Vite 已将 `/api` 和 `/ws` 代理到本地网关 `http://localhost:9000`。

## Nginx 前端部署

构建前端：

```bash
cd frontend
npm run build
```

将 `frontend/dist` 部署到 Nginx 静态资源挂载目录后重启 Nginx：

```bash
docker restart campus-nginx
```

Windows 上也可使用 `deploy-frontend.ps1` 一键完成构建、上传与容器重启：

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy-frontend.ps1
```

Nginx 配置位于 `docker/nginx/default.conf`，负责前端静态资源、`/api` 网关请求和 `/ws/private` WebSocket 请求转发。

## 分布式事务验证

可通过故障注入验证 Seata 回滚：在 `OrderServiceImpl.create()` 中，商品进入“交易中”且订单写入后临时抛出异常。预期结果：订单不会保存，商品状态会由“交易中”自动回滚为“已上架”。

Seata Server 日志出现以下关键记录，表示事务实际生效：

```text
Begin new global transaction
Register branch successfully
Rollback branch transaction successfully
Rollback global transaction successfully
```

验证后必须删除临时异常代码并重启 `order-service`。

## 数据库划分

| 数据库 | 服务 | 主要表 |
| --- | --- | --- |
| `trade_user_db` | user-service、message-service | `user`、`notification`、`private_message` |
| `trade_product_db` | product-service、admin-service | `product`、`favorite`、`comment`、`operation_log`、`undo_log` |
| `trade_order_db` | order-service | `orders`、`operation_log`、`undo_log` |

各服务数据库脚本存放在对应模块的 `sql/` 目录。

## 安全说明

- 网关与服务端基于 JWT 进行登录态校验，接口按角色控制访问范围。
- OSS Access Key、数据库密码、Redis 密码等敏感信息不得提交到 Git。
- 推送前将 OSS 密钥保留为占位符或环境变量，例如 `${OSS_ACCESS_KEY_ID}`、`${OSS_ACCESS_KEY_SECRET}`。
