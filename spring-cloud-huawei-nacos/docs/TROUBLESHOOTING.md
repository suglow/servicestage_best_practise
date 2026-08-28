# 故障排查

优先运行最小相关用例，并根据失败编号定位服务：

```bash
bash verify.sh --list
bash verify.sh --tc 1
docker compose --profile all ps
```

## Nacos 未就绪

现象：TC-1.1 超时，或其他容器一直等待 `nacos-init`。

```bash
docker compose ps -a nacos nacos-init
docker compose logs nacos
curl -s http://localhost:8848/nacos/v1/console/health/readiness
```

检查 8848、9848、9849 端口是否被占用，以及 Docker 是否有足够内存。Nacos 首次启动时间通常长于普通业务容器。

## nacos-init 退出

`Exited (0)` 表示初始化成功，不是故障。非零退出时查看：

```bash
docker compose logs nacos-init
```

日志应依次出现 Nacos ready、namespace 创建或复用、配置发布、配置回读和 `Initialization completed`。若发布成功但回读失败，检查 namespace、group 和 Data ID 是否一致。

## 服务没有注册

先确认应用自身健康和启动日志：

```bash
docker compose --profile all ps
docker compose --profile all logs product-service product-service-v2 order-service
```

查询 Product 实例：

```bash
curl -sG http://localhost:8848/nacos/v1/ns/instance/list \
  --data-urlencode serviceName=product-service \
  --data-urlencode groupName=CORE_GROUP \
  --data-urlencode namespaceId=dev
```

如果容器健康但查询为空，重点检查 `NACOS_SERVER_ADDR`、`NACOS_NAMESPACE` 和注册 group。

## namespace 不一致

现象：服务端口可直接访问，但注册查询、配置读取或服务名调用失败。

确保启动和验证使用相同值：

```bash
NACOS_NAMESPACE=test docker compose --profile all up -d --build
NACOS_NAMESPACE=test bash verify.sh --tc 1
```

不带环境变量重新执行验证时会恢复使用默认 `dev`，从而查询另一套环境。

## 灰度路由未命中预期版本

按以下顺序检查：

1. TC-1.2 是否确认 `gray-routing.yaml` 已发布。
2. TC-1.3 是否确认 8081、8083 两个实例都已注册。
3. 请求头是否是 `X-Gray-Tag: gray`。
4. Gateway、Order 是否订阅同一 namespace 和 `CORE_GROUP`。

```bash
bash verify.sh --tc 1
bash verify.sh --tc 3
bash verify.sh --tc 3G
docker compose logs gateway order-service
```

修改规则后先重新运行 `nacos-init`。若配置已回读但行为未变化，再重建订阅规则的服务。完整流程见[配置生命周期](CONFIGURATION.md)。

## 单个用例一直等待

单用例不会自动启动容器，必须先启动包含该功能的 profile：

| 用例 | 所需范围 |
| --- | --- |
| TC-1、2、3、3G、4、5、6 | 默认 profile |
| TC-2F | `feign` 或 `all` |
| TC-7～10 | `governance` 或 `all` |

```bash
docker compose --profile governance up -d --build
bash verify.sh --tc 9
```

## 限流用例结果受上次请求影响

限流按刷新窗口维护状态。连续手工请求后立即运行 TC-5，可能仍处于同一窗口。等待一个刷新周期后重试：

```bash
sleep 5
bash verify.sh --tc 5
```

## 治理用例失败

治理 Consumer 依赖对应 Provider 健康后启动。先检查服务对和日志，再只运行失败用例：

```bash
docker compose --profile governance ps
docker compose --profile governance logs retry-provider retry-consumer
docker compose --profile governance logs isolation-provider isolation-consumer
docker compose --profile governance logs bulkhead-provider bulkhead-consumer
docker compose --profile governance logs circuit-breaker-provider circuit-breaker-consumer
```

TC-8 和 TC-10 会恢复测试状态并重置 Provider 计数。不要用健康检查请求代替业务断言判断策略是否生效。

## 清理本地演示数据

确认不需要保留当前 Nacos 演示配置后，可以删除 Compose 容器和命名卷：

```bash
docker compose --profile all down --volumes
```

该命令会删除本项目的 `nacos-data` 命名卷，下次启动会重新创建 namespace 和灰度配置。

## 构建或依赖下载失败

首次构建需要下载 Maven 依赖和 Docker 基础镜像。检查网络、代理和磁盘空间后重新运行：

```bash
docker compose --profile all build
```

Dockerfile 的 Maven 层由所有应用镜像共享；源码或 POM 没有变化时，后续构建应复用缓存。
