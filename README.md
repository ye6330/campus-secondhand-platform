# campus-secondhand-platform

基于微服务架构的校园二手交易平台。

技术栈：Java 11、Spring Boot 2.7、Spring Cloud Alibaba、MyBatis-Plus、MySQL、Redis、RabbitMQ、Nacos、Vue 3、Element Plus、Nginx。

## 系统架构

```text
浏览器
   │
   ▼
Nginx（Linux Docker，托管前端 + 反向代理）
   │  /api
   ▼
Gateway 网关（campus-gateway-service:9000）
   │
   ├── /api/users/**        ▼ user-service（用户、登录、文件上传）
   ├── /api/products/**
   ├── /api/categories/**
   ├── /api/favorites/**    ▼ product-service（商品、收藏、评论）
   ├── /api/comments/**
   ├── /api/orders/**       ▼ order-service（订单）
   ├── /api/messages/**
   ├── /api/notifications/**▼ message-service（通知、私信 WebSocket）
   ├── /api/reports/**
   └── /api/operation-logs/**▼ admin-service（后台、举报、操作日志）

中间件（Linux Docker）：
MySQL  /  Redis  /  RabbitMQ  /  Nacos
```

微服务之间通过 OpenFeign + Nacos 服务发现互相调用。

服务注册中心 Nacos：`server-addr: 192.168.209.128:8848`

服务名清单（注册在 Nacos）：

| 服务名 | 端口 |
|---|---|
| campus-gateway-service | 9000 |
| campus-user-service | 9101 |
| campus-product-service | 9102 |
| campus-order-service | 9103 |
| campus-message-service | 9104 |
| campus-admin-service | 9105 |
| seata-server | 8091 |

## 模块结构

- `gateway-service`：网关，路由转发、跨域
- `user-service`：注册登录、JWT、验证码、OSS 文件上传
- `product-service`：商品发布/编辑/下架/售出、收藏、评论、Redis 详情缓存
- `order-service`：下单、确认、拒绝、取消、商品状态流转、事务回滚
- `message-service`：系统通知（MQ 异步）、私信（WebSocket 实时推送）
- `admin-service`：举报处理、操作日志查询（AOP 记录）
- `common/common-core`：公共常量、DTO、统一响应
- `common/common-web`：异常处理、日志实体、Knife4j 文档
- `common/common-security`：JWT 过滤器、用户上下文

## Seata 分布式事务

订单下单 / 确认 / 拒绝 / 取消涉及跨服务写库（order-service 写订单表、product-service 改商品状态），通过 Seata AT 模式保证全局事务一致性：

- `order-service` 为全局事务发起方（`@GlobalTransactional`），Feign 自动透传 XID
- `product-service` 为分支参与方（`@Transactional` + Seata 数据源代理自动纳入分支）
- 参与库（`trade_order_db`、`trade_product_db`）需建 `undo_log` 表：`order-service/sql/seata_undo_log.sql`、`product-service/sql/seata_undo_log.sql`
- Seata Server 由 docker-compose 的 `seata-server` 容器提供（file 模式存储，注册到 Nacos）
- 通知写入仍走 RabbitMQ 异步（MQ 消息事务，不纳入全局事务），属于最终一致性设计

## 环境要求

- JDK 11
- Maven 3.6+
- Node.js 18+
- Docker + Docker Compose（Linux 服务器，建议 2 核 4G）
- 服务器与开发机在同一内网（本项目：Linux `192.168.209.128`，开发机 `192.168.209.1`）

## 启动步骤

### 1. 启动中间件（Linux，Docker）

项目 `docker/` 目录下已提供 Compose 编排（MySQL/Redis/RabbitMQ/Nacos/Nginx）：

```bash
cd docker
docker compose up -d
docker compose ps
```

如果服务器已有这些容器，直接启动即可：

```bash
docker start campus-mysql redis campus-rabbitmq campus-nacos campus-seata-server campus-nginx
```

后端连接中间件地址统一为 `192.168.209.128`，配置在各服务 `application.yml` / `bootstrap.yml` 中。

### 2. 启动 Java 微服务（IDEA）

用 IntelliJ IDEA 打开根目录 `pom.xml`（Maven 项目），依次运行：

```text
1. user-service
2. product-service
3. order-service
4. message-service
5. admin-service
6. gateway-service（建议最后启动）
```

每个服务启动成功的标志是控制台出现：

```text
Started XxxServiceApplication
```

启动后访问 Nacos 控制台确认服务注册：

```text
http://192.168.209.128:8848/nacos
```

服务列表应能看到 6 个服务且健康状态正常。

### 2.1 初始化 Seata undo_log 表

Seata AT 模式需要在每个参与全局事务的数据库执行建表脚本：

```bash
mysql -h 192.168.209.128 -uroot -p123456 trade_order_db < order-service/sql/seata_undo_log.sql
mysql -h 192.168.209.128 -uroot -p123456 trade_product_db < product-service/sql/seata_undo_log.sql
```

### 3. 部署前端（Linux Nginx）

前端 `frontend/` 为 Vue 3 项目。开发时本地运行：

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`（Vite 已配置 `/api` 与 `/ws` 代理）。

生产部署时本地构建：

```bash
cd frontend
npm run build
```

将生成的 `frontend/dist` 上传到 Linux Nginx 容器的宿主机挂载目录：

```text
/root/campus-nginx/frontend/dist   （可自行调整 root 配置）
```

Nginx 配置文件：`docker/nginx/default.conf`（前端文件挂载、`/api` 转发到开发机网关 `192.168.209.1:9000`、`/ws/private` WebSocket 转发）。

修改前端后只需替换 `dist` 并重启 Nginx：

```bash
docker restart campus-nginx
```

### 4. 访问系统

```text
前端页面： http://192.168.209.128
Nacos：    http://192.168.209.128:8848/nacos
RabbitMQ： http://192.168.209.128:15672
```

## 核心业务流程

```text
买家下单 ──► 商品进入"交易中" ──► 卖家确认 ──► 商品"已售出"
                     │
                     └──► 卖家拒绝/取消 ──► 商品恢复"已上架"
```

- 交易中 / 已售出的商品禁止编辑，前后端双重校验
- 商品下架/售出时隐藏其他用户的收藏，重新上架后恢复
- 关键操作（下单、确认、拒绝、取消、商品上下架、举报处理）均在事务中执行，失败自动回滚
- 操作日志通过自定义注解 `@OperationLog` + AOP 切面自动落库
- 订单通知通过 RabbitMQ 异步发送到 `message-service`
- 私信通过 WebSocket 实时推送：前端 -> Nginx -> 网关 -> message-service

## 数据库

MySQL 初始化脚本位于 `sql/` 目录，服务对应库：

| 库 | 服务 |
|---|---|
| trade_user_db | user-service |
| trade_product_db | product-service、admin-service |
| trade_order_db | order-service |
| trade_message_db | message-service |

## 安全说明

- JWT 拦截器负责登录校验，网关统一跨域
- 数据库密码、Redis 密码等敏感信息不要提交到 Git；OSS 密钥推送前改为环境变量 `${OSS_ACCESS_KEY_ID}` / `${OSS_ACCESS_KEY_SECRET}`，本地保留硬编码