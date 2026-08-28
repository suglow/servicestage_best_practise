# Mesher Python Sidecar Demo

这是一个最小跨语言 Demo：Flask 应用通过 Apache ServiceComb Mesher 接入 Service Center，并与 Spring Cloud Huawei Java 微服务双向调用。

```text
java-consumer :7002 ──服务发现──> Mesher :30101 ──入向代理──> Flask :9000
                                                              │
                                                              └─HTTP_PROXY─> Mesher ──服务发现──> java-provider :7001
                                    local-cse :30100 为三项服务提供注册与发现
```

为保持原示例行为，Flask 与 Mesher 仍运行在同一个容器中；Java Provider 和 Consumer 直接使用 Spring Cloud Huawei 接入通用 ServiceComb 基础设施。

## 快速开始

环境要求：Docker Engine、Docker Compose v2、bash、curl、python3。首次构建需要访问 GitHub、Maven Central 和 PyPI。

```bash
# 先构建 Mesher 基础镜像
docker build -f mesher/Dockerfile -t mesher-sidecar-v181:local mesher

# 构建并启动四个服务
docker compose up -d --build
docker compose ps

# 执行端到端验证
bash verify.sh
```

手工请求：

```bash
curl http://localhost:30100/health
curl http://localhost:7001/api/users
curl http://localhost:9000/show
curl http://localhost:7002/show
```

停止环境：

```bash
docker compose down
```

如果端口冲突，可复制 `.env.example` 为 `.env` 并覆盖宿主机端口；容器内服务地址保持不变。

## 项目结构

```text
.
├── java-provider/          # Spring Cloud Huawei Provider
├── java-consumer/          # Spring Cloud Huawei + Feign Consumer
├── python-app/             # Flask、Mesher 配置和双进程入口
├── mesher/                 # Mesher v1.8.1 可复现构建
├── local-cse/              # 本地 Service Center/KIE 离线环境
├── docs/                   # 启动、架构、测试和兼容性说明
├── Dockerfile              # 两个 Java 模块的通用 Maven 构建
├── docker-compose.yml      # 四服务核心链路
├── check.sh                # 静态检查及 Java/Python 单测
└── verify.sh               # 已启动环境的端到端验证
```

## 构建与验证

源码检查要求宿主机使用 Java 17，并已安装 `python-app/requirements.txt` 中的依赖：

```bash
bash check.sh
bash verify.sh --list
bash verify.sh --tc 5
```

完整说明见 [docs/README.md](docs/README.md)。Mesher 和各运行时版本约束见 [docs/MESHER_COMPATIBILITY.md](docs/MESHER_COMPATIBILITY.md)。
