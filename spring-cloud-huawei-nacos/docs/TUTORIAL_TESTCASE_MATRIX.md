# 教程与测试矩阵

| 教程步骤 | 功能 | 代码入口 | 配置入口 | 验证命令 |
| --- | --- | --- | --- | --- |
| 1 | Nacos 初始化 | `scripts/nacos-init.sh` | `docker-compose.yml` | `bash verify.sh --tc 1` |
| 2 | 双版本注册 | `product-service*` | 各模块 `application.yml` | `bash verify.sh --tc 1` |
| 3 | RestTemplate 调用 | `order-service` | `order-service/application.yml` | `bash verify.sh --tc 2` |
| 4 | Consumer 灰度 | `order-service` | `scripts/gray-routing.yaml` | `bash verify.sh --tc 3` |
| 5 | Gateway 路由与灰度、订单端点限流 | `gateway`、`order-service` | Gateway 配置、订单限流规则与远程规则 | `bash verify.sh --tc 3G`、`--tc 5`、`--tc 6` |
| 6 | OpenFeign 调用 | `order-service-feign` | 模块配置 | `bash verify.sh --tc 2F` |
| 7.0 | Provider 故障模型 | `product-service` | 商品服务故障端点 | `bash verify.sh --tc 4` |
| 7.1 | 重试 | `retry-*` | `retry-consumer/application.yml` | `bash verify.sh --tc 7` |
| 7.2 | 实例隔离 | `isolation-*` | `isolation-consumer/application.yml` | `bash verify.sh --tc 8` |
| 7.3 | 舱壁 | `bulkhead-*` | `bulkhead-consumer/application.yml` | `bash verify.sh --tc 9` |
| 7.4 | Provider 熔断 | `circuit-breaker-*` | `circuit-breaker-provider/application.yml` | `bash verify.sh --tc 10` |
| 8 | 修改并验证规则 | 对应治理模块 | 远程灰度规则或模块配置 | 重跑对应 `--tc` 用例 |
| 9 | 提交前检查 | 全工程 | `pom.xml`、Compose | `bash check.sh` |

完整学习路径使用 `--profile all`。只学习基础能力时使用默认 profile；只学习高级治理时使用 `--profile governance`。
