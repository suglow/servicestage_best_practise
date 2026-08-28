# 教程与测试矩阵

| 教程章节 | 功能 | 代码入口 | 配置入口 | 验证命令 |
| --- | --- | --- | --- | --- |
| 4～5 | 基础设施、注册与发现 | `local-cse`、核心服务 | 各模块 `bootstrap.yml` | `bash verify.sh --tc 1` |
| 7 | OpenFeign 调用 | `order-service` | `order-service` 配置 | `bash verify.sh --tc 2` |
| 8 | 灰度发布 | `gateway`、商品双版本 | `kie-init/init-route-rules.sh` | `bash verify.sh --tc 3` |
| 9.1 | Provider 故障模型 | `product-service` | 商品服务配置 | `bash verify.sh --tc 4` |
| 9.2 | Consumer 熔断 | `circuit-breaker-*` | Consumer 熔断规则 | `bash verify.sh --tc 10` |
| 10 | Gateway 限流 | `gateway` | Gateway 限流规则 | `bash verify.sh --tc 5` |
| 11 | Gateway 路由 | `gateway` | Gateway 路由配置 | `bash verify.sh --tc 6` |
| 12 | 重试 | `retry-*` | `retry-consumer/application.yml` | `bash verify.sh --tc 7` |
| 13 | 实例隔离 | `isolation-*` | `isolation-consumer/application.yml` | `bash verify.sh --tc 8` |
| 14 | 舱壁 | `bulkhead-*` | `bulkhead-consumer/application.yml` | `bash verify.sh --tc 9` |
| 15 | 故障注入 | `fault-injection-*` | KIE 故障注入规则 | `bash verify.sh --tc 11` |

## 规则作用范围速查

| 能力 | 本示例的观察对象 |
| --- | --- |
| 注册发现、灰度、实例隔离、舱壁 | 服务实例及实例元数据 |
| OpenFeign、重试、故障注入 | Consumer 对目标服务的调用 |
| Gateway 路由与限流 | Gateway 入站路径 |
| Consumer 熔断 | Consumer 入站 API 及其下游调用是否继续发生 |

完整用例前置条件和断言见 [测试用例](TEST_CASES.md)，配置加载细节见 [KIE 配置指南](KIE_CONFIG.md)。
