# 快速开始

## 1. 检查环境

运行 Demo 需要 Docker Engine、Docker Compose v2、bash、curl 和 python3。Docker 构建阶段需要访问 GitHub、Maven Central、PyPI 及基础镜像仓库。

只运行 `check.sh` 时，还需要：

- Java 17 和 Maven 3.8 或更高版本；
- Flask 3.1.3 和 requests 2.32.5。

可使用独立虚拟环境准备 Python 依赖：

```bash
python3 -m venv .venv
. .venv/bin/activate
pip install -r python-app/requirements.txt
```

## 2. 构建 Mesher 基础镜像

Python 业务镜像从本地 Mesher 镜像复制二进制，因此首次启动前先执行：

```bash
docker build -f mesher/Dockerfile -t mesher-sidecar-v181:local mesher
```

构建会拉取 Mesher `v1.8.1`，校验其短提交号为 `f35f82e`，再生成精简运行镜像。

## 3. 启动四个服务

```bash
docker compose up -d --build
docker compose ps
```

等待 `local-cse`、`java-provider`、`python-app` 和 `java-consumer` 进入运行/健康状态。Java 模块由根 Dockerfile 统一构建；Python 容器会先等待 Mesher 代理端口就绪，再启动 Flask。

## 4. 验证

```bash
curl -fsS http://localhost:30100/health
curl -fsS http://localhost:7001/api/users
curl -fsS http://localhost:9000/show
curl -fsS http://localhost:7002/show
bash verify.sh
```

验证成功同时说明：三个应用已注册、`python-app` 公布 Mesher endpoint、Python 出向调用经过 Mesher，以及 Java Consumer 能通过逻辑服务名进入 Mesher 再调用 Flask。

## 5. 停止

```bash
docker compose down
```

端口和 ServiceComb 元数据可通过 `.env` 覆盖；变量示例见项目根目录 `.env.example`。
