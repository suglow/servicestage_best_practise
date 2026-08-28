# Spring Cloud Huawei Nacos 示例

一个可直接运行的 Spring Cloud Huawei 与 Nacos 集成示例。项目以精简的电商调用链展示服务注册与发现、配置管理、网关路由、灰度发布，以及重试、限流、隔离、舱壁和熔断等治理能力。

## 能学到什么

- 使用 Nacos namespace 和 group 隔离服务与配置
- 通过 `RestTemplate` 和 OpenFeign 按服务名调用
- 在 Gateway 与 Consumer 两层验证灰度路由
- 用独立的小型模块观察重试、实例隔离、舱壁和熔断行为
- 用一个脚本运行全部或单个端到端用例

## 环境要求

- Docker 24+ 与 Docker Compose v2
- 本地构建和单元测试需要 JDK 17、Maven 3.9+
- 首次构建需要访问 Maven Central 和 Docker Hub

## 3 分钟启动

启动核心示例（Nacos、双版本商品服务、订单服务和网关）：

```bash
docker compose up -d --build
bash verify.sh --tc 1
bash verify.sh --tc 3G
```

启动全部示例并执行全量验证：

```bash
docker compose --profile all up -d --build
bash verify.sh
```

查看状态和停止环境：

```bash
docker compose --profile all ps
docker compose --profile all down
```

Nacos 控制台地址为 <http://localhost:8848/nacos/>，默认 namespace 为 `dev`。

## 按需启动

| 启动方式 | 服务数 | 用途 |
| --- | ---: | --- |
| `docker compose up -d --build` | 6 | 核心调用、灰度、限流、网关 |
| `docker compose --profile feign up -d --build` | 7 | 核心示例 + OpenFeign |
| `docker compose --profile governance up -d --build` | 14 | 核心示例 + 治理专题 |
| `docker compose --profile all up -d --build` | 15 | 所有用例 |

`nacos-init` 是一次性初始化任务，完成 namespace 与灰度配置发布后正常退出，不是常驻业务服务。

## 验证方式

```bash
bash verify.sh --list
bash verify.sh --tc 2
bash verify.sh --tc 2F
bash verify.sh --tc 7
bash verify.sh
```

远程 Docker 主机或自定义 namespace 可通过环境变量指定：

```bash
DEMO_HOST=192.168.1.20 NACOS_NAMESPACE=dev bash verify.sh --tc 1
```

执行静态检查、Compose 校验与单元测试：

```bash
bash check.sh
```

## 模块一览

| 模块 | 端口 | 启动范围 | 主要内容 |
| --- | ---: | --- | --- |
| `product-service` | 8081 | 默认 | 商品服务 v1 |
| `product-service-v2` | 8083 | 默认 | 商品服务 v2 |
| `order-service` | 8082 | 默认 | RestTemplate 调用、灰度与限流 |
| `gateway` | 8080 | 默认 | 路由与全链路灰度 |
| `order-service-feign` | 8084 | feign | OpenFeign 调用与降级 |
| `retry-provider` / `retry-consumer` | 8090 / 8091 | governance | 重试策略 |
| `isolation-provider` / `isolation-consumer` | 8092 / 8093 | governance | 故障实例隔离 |
| `bulkhead-provider` / `bulkhead-consumer` | 8094 / 8095 | governance | 并发舱壁 |
| `circuit-breaker-provider` / `circuit-breaker-consumer` | 8096 / 8097 | governance | Provider 错误率与慢调用熔断 |
| `api-common` | - | 构建依赖 | 公共 DTO |

## 目录导航

```text
.
├── api-common/                 # 公共模型
├── product-service*/           # 核心 Provider
├── order-service*/             # 两种 Consumer 调用方式
├── gateway/                    # API Gateway
├── *-provider/、*-consumer/    # 治理专题
├── scripts/                    # Nacos 初始化与远程配置
├── docs/                       # 架构、教程和用例
├── docker-compose.yml
├── verify.sh                   # 端到端验证
└── check.sh                    # 本地与 CI 校验
```

## 继续阅读

- [架构与配置](docs/ARCHITECTURE.md)
- [配置生命周期](docs/CONFIGURATION.md)
- [循序教程](docs/TUTORIAL.md)
- [测试用例](docs/TEST_CASES.md)
- [故障排查](docs/TROUBLESHOOTING.md)
- [教程与用例矩阵](docs/TUTORIAL_TESTCASE_MATRIX.md)
