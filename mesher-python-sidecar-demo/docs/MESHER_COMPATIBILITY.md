# Mesher 兼容性与配置

## 版本选择

| 组件 | 版本 | 用途 |
| --- | --- | --- |
| Java | 17 | 两个 Spring Boot 模块的构建和运行基线 |
| Spring Boot | 3.4.4 | Java Provider 和 Consumer |
| Spring Cloud | 2024.0.1 | OpenFeign 与服务发现抽象 |
| Spring Cloud Huawei | 1.11.11-2024.0.x | Java 服务的 ServiceComb 注册与发现客户端 |
| Python | 3.11 | Flask 业务运行时 |
| Flask | 3.1.3 | Python REST 接口 |
| requests | 2.32.5 | Python 出向 HTTP 调用 |
| Apache ServiceComb Mesher | v1.8.1 / `f35f82e` | Python 应用的注册、发现和双向代理 |
| Local CSE | 2.1.8 | 本地 Service Center、KIE 和 Dashboard |

Java 版本由 Maven Enforcer 限定为 `[17,18)`；`check.sh` 也会检查当前 JVM 主版本。Mesher Dockerfile 同时校验 tag 对应的短提交号，避免 tag 内容漂移后静默构建不同源码。

## Mesher 关键配置

镜像和入口脚本只做原 Demo 所需的最小覆盖：

- registry 地址固定使用容器网络内的 `http://local-cse:30100`；
- `service.app`、`SERVICE_NAME` 和版本与 Java 服务共享同一 ServiceComb 应用空间；
- Mesher 以 `--service-ports=rest:9000` 启动；
- Python 使用 `HTTP_PROXY=http://127.0.0.1:30101` 进入出向代理；
- 实际容器 IP 用于 Mesher 的入向 endpoint 注册；
- 启动脚本以端口探测代替固定睡眠时间。

Mesher 上游 `start.sh` 仍保留在构建产物中，并对 Alpine 环境下的 `ip`/`ifconfig` 差异做兼容补丁；当前 Python 容器使用项目自己的入口脚本启动二进制。

## 依赖完整性

`local-cse/Local-CSE-2.1.8-linux-amd64.zip` 是 Demo 必需的离线基础设施包，`check.sh` 校验 SHA-256：

```text
0f4270cc7b64699e45ae9bc32f43cc6f0cbdfc55dde99732b2767c4e1f6ca3f3
```

Python 依赖使用精确版本，Java 依赖由根 POM 的三组 BOM 统一管理。Spring Cloud Huawei BOM 优先导入，与当前仓库其他 ServiceComb Demo 的管理顺序一致。

## 常见限制

- Mesher 首次构建需要 GitHub 和 Go 模块仓库网络；Java、Python 镜像首次构建分别需要 Maven Central 和 PyPI。
- Flask 与 Mesher 在同一容器中是本演示的保守结构，不代表生产环境必须采用相同进程模型。
- `python-app` 的健康检查同时覆盖 Flask 和 Mesher 端口；只看到 Flask 返回 200 不能证明代理已就绪。
- 注册成功不等于完整调用成功，应同时运行 TC-2、TC-5 和 TC-6。
