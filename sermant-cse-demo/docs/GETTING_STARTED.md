# 快速开始

## 1. 检查环境

需要 Docker Compose v2、bash、curl、python3。Maven 和 Docker 构建分别使用 Java 8 运行时；宿主机只做 `check.sh` 时也需要 Java 8。

## 2. 启动

在项目根目录执行：

```bash
docker compose up -d --build
docker compose ps
```

首次构建会编译 Maven 模块并下载 Sermant 2.0.0 Agent。等待 `local-cse`、`product-service` 和 `order-service` 进入运行/健康状态。

## 3. 验证

```bash
curl -fsS http://localhost:30100/health
curl -fsS http://localhost:8080/actuator/health
bash verify.sh
```

验证成功同时说明：Service Center 可用、Provider 已注册、注册来源是 Sermant，以及 Consumer 能通过逻辑服务名获得 Provider 数据。

## 4. 停止

```bash
docker compose down
```

端口可通过 `.env` 覆盖；变量示例见项目根目录 `.env.example`。
