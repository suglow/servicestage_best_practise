# Sermant CSE Demo

这是一个最小可运行链路：普通 Spring Boot Provider 由 Sermant JavaAgent 注册到 ServiceComb Service Center，Spring Cloud Huawei Consumer 通过服务发现调用它。

```text
order-service :8080  ──服务发现──>  Service Center :30100
                                      ▲
product-service :8081 ──Sermant───────┘
local-cse 提供 Service Center、KIE 和 Dashboard
```

Provider 业务代码不引用 ServiceComb SDK；Sermant 只在容器启动时通过 `-javaagent` 接管注册侧。Consumer 使用 Spring Cloud Huawei 完成发现侧调用。

## 快速开始

环境要求：Docker Engine、Docker Compose v2、bash、curl、python3。构建 Sermant Provider 需要访问 GitHub 和 Maven 仓库。

```bash
docker compose up -d --build
docker compose ps
bash verify.sh
```

手工请求：

```bash
curl http://localhost:30100/health
curl http://localhost:8081/api/products/1001
curl http://localhost:8080/api/orders/products/1001
```

停止环境：

```bash
docker compose down
```

如果端口冲突，可复制 `.env.example` 为 `.env` 并覆盖宿主机端口。容器内部仍使用 `local-cse:30100`、`local-cse:30110`。

## 项目结构

```text
.
├── api-common/             # 共享 Product DTO
├── product-service/        # 纯 Spring Boot Provider + Sermant Agent
├── order-service/          # Spring Cloud Huawei Consumer
├── local-cse/              # 本地 Service Center/KIE 离线环境
├── docs/                   # 启动、架构、测试和兼容性说明
├── Dockerfile              # 通用 Maven 构建；plain/sermant 两个运行 target
├── docker-compose.yml      # 三容器核心链路
├── check.sh                # 静态检查和 Maven 单测
└── verify.sh               # 已启动环境的端到端验证
```

## 构建与验证

```bash
# 只执行静态检查和单元测试（需要 Java 8）
bash check.sh

# 查看用例或执行单个用例
bash verify.sh --list
bash verify.sh --tc 2
```

完整说明见 [docs/README.md](docs/README.md)。Sermant 与 Java/Spring Boot 版本约束见 [docs/SERMANT_COMPATIBILITY.md](docs/SERMANT_COMPATIBILITY.md)。
