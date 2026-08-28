# Spring Cloud Huawei ServiceComb 示例

基于 Spring Boot 3、Spring Cloud Huawei 和 ServiceComb Local CSE 的微服务治理示例，覆盖注册发现、配置中心、灰度发布、限流、重试、实例隔离、舱壁、Consumer 侧熔断和故障注入。

## 能学到什么

- 使用 Service Center 完成服务注册、发现和按服务名调用
- 使用 KIE 发布灰度、治理和业务配置
- 通过 Gateway、OpenFeign 和商品双版本观察全链路灰度
- 用独立 Provider/Consumer 验证重试、隔离、舱壁和熔断
- 通过 Provider 调用计数区分普通失败、请求短路和故障注入
- 使用一个脚本运行全部或单个端到端用例

## 快速开始

前置要求：Docker、Docker Compose v2、`bash`、`curl`、`python3`，以及可访问 Maven Central 和 Docker Registry 的网络环境。

> `local-cse/Local-CSE-2.1.8-linux-amd64.zip` 仅包含 amd64 程序。ARM64 主机需启用 amd64 容器模拟或替换对应架构的软件包。

```bash
# 启动核心链路：Local CSE + Gateway + Order + Product
docker compose up -d --build

# 验证注册发现、Feign 调用和网关路由
bash verify.sh --tc 1
bash verify.sh --tc 2
bash verify.sh --tc 6
```

首次构建会解压 `local-cse/Local-CSE-2.1.8-linux-amd64.zip`，并在容器内构建各 Maven 模块。

按学习目标启用其他示例：

```bash
# 核心链路 + KIE 配置和灰度发布
docker compose --profile config up -d --build

# 核心链路 + 重试、隔离、舱壁、熔断和故障注入
docker compose --profile governance up -d --build

# 启动全部示例
IMAGE_TAG=1.0.0 docker compose --profile all up -d --build
bash verify.sh
```

## 模块一览

| 类型 | 服务 | 端口 | 启动范围 | 主要内容 |
|---|---|---:|---|---|
| 基础设施 | Service Center / KIE / Dashboard | 30100 / 30110 / 30103 | 默认 | 注册中心、配置中心和管理界面 |
| 核心链路 | gateway / product v1 / order | 8080 / 8081 / 8082 | 默认 | Gateway、OpenFeign 和服务发现 |
| 灰度版本 | product v2 | 8083 | config | 商品双版本与权重路由 |
| 配置演示 | kie-config-demo | 8084 | config | KIE 值类型和业务配置绑定 |
| 重试 | retry-provider / retry-consumer | 8090 / 8091 | governance | 调用失败重试 |
| 实例隔离 | isolation-provider / isolation-consumer | 8092 / 8093 | governance | 故障实例判断 |
| 舱壁 | bulkhead-provider / bulkhead-consumer | 8094 / 8095 | governance | 并发许可与拒绝 |
| Consumer 熔断 | circuit-breaker-provider / circuit-breaker-consumer | 8096 / 8097 | governance | 入站请求短路与 Provider 计数 |
| 故障注入 | fault-injection-normal-provider / fault-injection-error-provider / fault-injection-consumer | 8100 / 8101 / 8102 | governance | 概率注入与下游调用计数 |

## 目录结构

```text
.
├── docker-compose.yml       # 完整运行编排
├── Dockerfile               # Maven 模块通用镜像构建
├── pom.xml                  # Maven 聚合工程
├── local-cse/               # Service Center 与 KIE
├── kie-init/                # KIE 治理规则初始化
├── api-common/              # 公共 DTO
├── gateway/                 # API 网关
├── *-service/               # 业务服务
├── *-provider/              # 治理演示 Provider
├── *-consumer/              # 治理演示 Consumer
├── verify.sh                # 自动验证脚本
└── docs/                    # 教程、架构及测试资料
```

## 文档

- [文档导航](docs/README.md)
- [5 分钟快速体验](docs/GETTING_STARTED.md)
- [完整实战教程](docs/TUTORIAL.md)
- [架构与设计原理](docs/ARCHITECTURE.md)
- [测试用例](docs/TEST_CASES.md)
- [教程与测试矩阵](docs/TUTORIAL_TESTCASE_MATRIX.md)
- [Consumer 熔断测试](docs/CIRCUIT_BREAKER_TEST.md)
- [KIE 配置指南](docs/KIE_CONFIG.md)

## 常用命令

```bash
# 查看日志
docker compose logs -f gateway

# 运行单组测试，例如注册发现
bash verify.sh --list
bash verify.sh --tc 1

# 不启动容器，执行静态检查和 Maven 单元测试
bash check.sh
# 受限环境可指定可写的 Maven 仓库
MAVEN_REPO_LOCAL=/tmp/servicecomb-m2 bash check.sh

# 远程 Docker 或端口转发场景可指定验证主机
DEMO_HOST=127.0.0.1 bash verify.sh --tc 1

# 停止并删除容器
docker compose down
```

## Compose profiles

| Profile | 服务范围 | 适合场景 |
|---|---|---|
| 默认 | Local CSE、KIE 初始化、Gateway、Order、Product v1 | 第一次体验 |
| `config` | 默认服务 + Product v2 + KIE 配置演示 | 灰度和动态配置 |
| `governance` | 默认服务 + 高级治理 Provider/Consumer | 单项治理实验 |
| `all` | 全部 18 个服务 | 运行完整验证脚本 |

> Compose 默认使用 `latest` 镜像标签；需要可复现或并行验证时，可通过 `IMAGE_TAG` 指定标签。核心业务服务提供 HTTP healthcheck，`kie-init` 显示 `Exited (0)` 属于正常状态。
