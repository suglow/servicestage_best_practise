# 测试用例

`check.sh` 面向源码和配置，`verify.sh` 面向已经启动的 Compose 环境。脚本当次输出和退出码是唯一结果来源，文档不记录静态“已通过”数字。

| 用例 | 范围 | 关键断言 |
| --- | --- | --- |
| TC-1 | 基础设施 | Compose 可用、Service Center 健康、两个应用容器运行 |
| TC-2 | Sermant 注册 | `product-service` 出现在注册列表，且 `framework.name=Sermant` |
| TC-3 | 服务发现调用 | Consumer 通过逻辑服务名调用 Provider，返回预期商品和 `source=sermant-product` |

## 运行方式

```bash
bash verify.sh --list
bash verify.sh --tc 1
bash verify.sh --tc 2
bash verify.sh --tc 3
bash verify.sh
```

远程 Docker 或端口转发场景可设置 `DEMO_HOST`、`CSE_SC_PORT`、`ORDER_PORT` 和 `PRODUCT_PORT`。网络或容器尚未就绪时，先查看：

```bash
docker compose ps
docker compose logs --no-color local-cse product-service order-service
```

## 静态检查

```bash
bash check.sh
```

它会检查 Shell 语法、Compose 配置、验证 CLI、Java 8 下的 Maven 测试、Markdown 相对链接、过时路径和构建产物污染。
