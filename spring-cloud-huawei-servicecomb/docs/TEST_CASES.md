# 测试用例

`verify.sh` 是已启动 Docker Compose 环境的端到端验证入口。本文档只定义运行范围、关键断言和诊断方式；实际结果以脚本当次输出和退出码为准。

## 运行准备

| 用例范围 | 启动命令 | 可运行用例 |
| --- | --- | --- |
| 核心链路 | `docker compose up -d --build` | TC-1、2、4、5、6 |
| 配置与灰度 | `docker compose --profile config up -d --build` | 核心链路 + TC-3 |
| 治理专题 | `docker compose --profile governance up -d --build` | 核心链路 + TC-7～11 |
| 全部功能 | `docker compose --profile all up -d --build` | TC-1～11 |

`kie-init` 是一次性初始化任务，成功后显示 `Exited (0)` 属于正常状态。完整验证命令：

```bash
docker compose --profile all up -d --build
bash verify.sh
```

仓库 CI 会在 push 和 pull request 时运行 `check.sh`。需要验证完整容器链路时，可在 GitHub Actions 中手动触发 `CI` 工作流；`integration` job 会启动 `all` profile 并执行全部用例。

## 用例清单

| 编号 | 功能 | 关键断言 |
| --- | --- | --- |
| TC-1 | 基础设施和注册发现 | SC、KIE 可访问，Order 能通过服务名调用 Product |
| TC-2 | OpenFeign 服务调用 | 商品列表和订单创建响应包含预期业务字段 |
| TC-3 | 灰度发布 | 默认命中 v1；灰度流量符合 30/70 容差；全链路命中 v2；双版本共存；诊断接口识别当前规则 |
| TC-4 | Provider 故障端点 | 6 次请求中同时出现 HTTP 200 和 502 |
| TC-5 | Gateway 限流 | 30 个突发请求中至少 3 个返回 429 |
| TC-6 | Gateway 基础路由 | 商品和订单路径均能通过 Gateway 访问 |
| TC-7 | 重试治理 | 无重试端点可观察失败分布，重试端点返回符合策略要求 |
| TC-8 | 实例隔离 | 最小调用数场景返回符合策略要求 |
| TC-9 | 舱壁隔离 | 10 个并发请求中成功数不超过 2，拒绝数不少于 8 |
| TC-10 | Consumer 熔断 | 12 个入站请求中，Provider 实际调用次数小于 12 |
| TC-11 | 故障注入 | KIE 规则已加载；50% 注入同时出现成功和失败；100% 注入阻止全部下游调用 |

## 单个用例执行

```bash
bash verify.sh --list
bash verify.sh --tc 1
bash verify.sh --tc 3
bash verify.sh --tc 10
bash verify.sh --tc 11
```

脚本一次只接受一个编号。缺少编号或编号不在 1～11 范围内时退出码为 2。

## 结果判定

- `PASS`：断言满足。
- `FAIL`：断言失败，脚本最终退出码为 1。
- 参数或宿主机命令缺失：退出码为 2。
- `WARN`：非阻断信息，不等同于用例通过。

脚本会先等待当前用例需要的服务，最长约 90 秒。执行 TC-3 或全部用例时，还会等待 Gateway 连续 5 次将无标签请求路由到 v1，以确认 KIE 动态路由规则已经稳定加载。TC-3 包含 100 次权重采样，TC-9 包含并发等待，TC-11 包含概率统计，因此运行时间会长于普通 HTTP 用例。

## 关键用例说明

### TC-3：灰度路由

无标签请求必须命中 v1。带 `X-Gray-Tag: gray` 的 100 次请求中，v1 应为 20～40 次，v2 应为 60～80 次。全链路请求还必须证明灰度上下文经过 Gateway 和 Order 后继续作用于 Product 调用。

### TC-10：Consumer 熔断

脚本先清零 Provider 计数，再向 Consumer 的 100% 失败路径发起 12 次请求。只有 Provider 业务代码实际执行次数小于 12，才能证明后续请求在 Consumer 入口被短路。详细原理见 [Consumer 熔断测试](CIRCUIT_BREAKER_TEST.md)。

### TC-11：故障注入

50% 概率用例要求成功数和失败数都大于 0，且 Provider 调用数等于成功数；100% 用例要求 50 次调用全部被注入故障，Provider 调用数为 0。配置结构见 [KIE 配置指南](KIE_CONFIG.md)。

## 常用诊断

```bash
docker compose --profile all ps
docker compose --profile all logs kie-init
docker compose --profile all logs order-service gateway
docker compose --profile all logs circuit-breaker-consumer
docker compose --profile all logs fault-injection-consumer
```

远程 Docker 主机或端口转发场景可指定验证地址：

```bash
DEMO_HOST=192.168.1.20 bash verify.sh --tc 1
```
