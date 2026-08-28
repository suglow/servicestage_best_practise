# 测试用例

`verify.sh` 是面向已启动 Docker Compose 环境的端到端验证入口。它支持完整执行和单组执行，失败时返回非零退出码。

## 运行准备

核心用例：

```bash
docker compose up -d --build
```

OpenFeign 用例：

```bash
docker compose --profile feign up -d --build
```

治理用例：

```bash
docker compose --profile governance up -d --build
```

全部用例：

```bash
docker compose --profile all up -d --build
bash verify.sh
```

仓库 CI 会在 push 和 pull request 时运行 `check.sh`。需要验证完整容器链路时，可在 GitHub Actions 中手动触发 `CI` 工作流；`integration` job 会启动 `all` profile 并执行全部用例。

## 用例清单

| 编号 | profile | 验证内容 | 关键断言 |
| --- | --- | --- | --- |
| TC-1 | 默认 | Nacos 健康、配置和注册 | 配置存在，商品双版本已注册且可访问 |
| TC-2 | 默认 | RestTemplate 调用 | 订单创建、商品列表、默认命中 v1 |
| TC-2F | feign | OpenFeign 调用 | 创建订单，普通/灰度请求分别命中 v1/v2 |
| TC-3 | 默认 | Consumer 灰度 | 普通请求全命中 v1，灰度请求全命中 v2 |
| TC-3G | 默认 | Gateway 灰度 | 网关和下游调用保持同一灰度标签 |
| TC-4 | 默认 | Provider 故障端点 | 交替错误同时出现成功和失败，慢调用产生延迟 |
| TC-5 | 默认 | 限流 | 连续请求中同时出现 200 和 429 |
| TC-6 | 默认 | Gateway 基础路由 | 商品、订单路由正确，未知路径返回 404 |
| TC-7 | governance | 重试 | 六种策略的结果均符合预期 |
| TC-8 | governance | 实例隔离 | 11 个场景验证最小调用数、错误率、慢调用率、强制开关和错误码策略 |
| TC-9 | governance | 舱壁 | 三个并发场景返回 `true` |
| TC-10 | governance | Provider 熔断 | 错误率和慢调用均打开熔断，且 Provider 实际调用次数小于发起次数 |

## 选择执行

```bash
bash verify.sh --list
bash verify.sh --tc 1
bash verify.sh --tc tc-3g
bash verify.sh --tc 10
```

编号不区分大小写，`TC-` 前缀可省略。脚本一次只接受一个测试组；需要测试多个组时可顺序执行多条命令。

## 结果与退出码

- `PASS`：断言满足。
- `FAIL`：断言失败，脚本最终退出码为 1。
- 参数错误：退出码为 2。
- `WARN`：保留的非阻断统计，目前默认用例不会产生。

服务启动较慢时，脚本会按测试组等待对应入口，最长约 60 秒。治理场景本身可能需要额外几十秒。

TC-8 会在每个状态型场景开始前恢复策略并重置 Provider 计数，因此可以连续执行。除了检查响应结果，它还会确认隔离后下游实际调用次数停止增长，避免把普通业务失败误判为隔离成功。

TC-10 同样支持连续执行。Consumer 先恢复策略，再发起固定数量的请求；Provider Controller 记录真正进入业务代码的次数。只有熔断打开且至少一个请求在进入 Controller 前被短路时，用例才会通过。

## 常用诊断

```bash
docker compose --profile all ps
docker compose --profile all logs nacos-init
docker compose --profile all logs order-service gateway
docker compose --profile all logs retry-consumer
```

如果使用远程主机：

```bash
DEMO_HOST=192.168.1.20 bash verify.sh --tc 3G
```

如果修改了 namespace，需要让 Compose 和验证脚本使用同一个值：

```bash
NACOS_NAMESPACE=test docker compose --profile all up -d --build
NACOS_NAMESPACE=test bash verify.sh --tc 1
```

更多按症状分类的检查步骤见[故障排查](TROUBLESHOOTING.md)。
