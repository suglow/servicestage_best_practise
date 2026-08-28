# 5 分钟快速体验

本指南只启动核心链路：

```text
Gateway :8080 → Order :8082 → Product :8081
                    ↓
          Service Center :30100
          KIE Config Center :30110
```

## 1. 检查环境

需要：

- Docker Engine 和 Docker Compose v2
- `bash`、`curl`、`python3`
- 可下载 Maven 依赖及 Docker 基础镜像的网络
- 建议至少预留 4 GiB 内存和 5 GiB 磁盘空间

Local CSE 离线包是 `linux-amd64` 版本。ARM64 主机需要启用 amd64 容器模拟，或替换为匹配架构的 CSE 包。

```bash
docker version
docker compose version
curl --version
python3 --version
```

## 2. 启动核心服务

在项目根目录执行：

```bash
docker compose up -d --build
docker compose ps
```

等待 `local-cse` 健康后，应看到以下 5 个服务：

- `local-cse`
- `kie-init`
- `gateway`
- `order-service`
- `product-service`

`kie-init` 是一次性初始化任务，成功后显示 `Exited (0)` 属于正常状态。

## 3. 验证核心能力

```bash
# Service Center 健康状态
curl -s http://localhost:30100/health

# 通过 Gateway 访问 Product
curl -s http://localhost:8080/api/products/1 | python3 -m json.tool

# Order 通过 Feign 和服务发现调用 Product
curl -s http://localhost:8082/api/orders/products | python3 -m json.tool

# 自动验证核心链路
bash verify.sh --tc 1
bash verify.sh --tc 2
bash verify.sh --tc 6
```

以上请求成功说明已经完成：

1. Spring Cloud Huawei 服务注册。
2. ServiceComb 服务发现。
3. OpenFeign 服务间调用。
4. Gateway 基于服务名的负载均衡路由。

## 4. 继续学习

```bash
# 动态配置和灰度发布
docker compose --profile config up -d --build
bash verify.sh --tc 3

# 全部治理能力
docker compose --profile all up -d --build
bash verify.sh
```

完整说明见[实战教程](TUTORIAL.md)。

## 5. 停止环境

```bash
docker compose --profile all down
```
