# 测试用例

`check.sh` 面向源码和配置，`verify.sh` 面向已经启动的 Compose 环境。脚本当次输出和退出码是唯一结果来源，文档不记录静态“已通过”数字。

| 用例 | 范围 | 关键断言 |
| --- | --- | --- |
| TC-1 | 基础设施 | Compose 可用、Service Center 健康、四个容器运行 |
| TC-2 | 注册发现 | 三个应用已注册，`python-app` 实例公布 Mesher `:30101` endpoint |
| TC-3 | Java Provider | `/api/users` 可达并返回三个演示用户 |
| TC-4 | Python/Mesher 健康 | Flask `/health` 正常，Mesher 代理端口接受连接 |
| TC-5 | Mesher 出向代理 | Flask `/show` 经 Mesher 调用 `java-provider` 并获得成功响应 |
| TC-6 | Mesher 入向代理 | Java Consumer 经服务发现和 Mesher 调用 Flask，完整回路连续成功三次 |

## 运行方式

```bash
bash verify.sh --list
bash verify.sh --tc 1
bash verify.sh --tc 5
bash verify.sh --tc 6
bash verify.sh
```

远程 Docker 或端口转发场景可设置 `DEMO_HOST`、`CSE_SC_PORT`、`MESHER_PORT`、`PYTHON_PORT`、`JAVA_PROVIDER_PORT` 和 `JAVA_CONSUMER_PORT`。

容器或网络尚未就绪时，先查看：

```bash
docker compose ps
docker compose logs --no-color local-cse java-provider python-app java-consumer
```

## 静态检查

```bash
bash check.sh
```

它会检查 Shell 语法、Compose 配置、验证 CLI、Java 17 下的 Maven 测试、Python 契约测试、Markdown 相对链接、Local CSE 压缩包校验和、过时路径及构建产物污染。

如果默认 Maven 仓库不可写，可指定独立目录：

```bash
MAVEN_REPO_LOCAL=/tmp/mesher-demo-m2 bash check.sh
```
