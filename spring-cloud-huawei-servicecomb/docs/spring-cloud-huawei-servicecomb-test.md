---
title: "spring-cloud-huawei-servicecomb-test 测试报告"
author: "自动化测试执行"
date: "2026-09-08"
lang: zh-CN
---

**文档状态：正式版**  
**文档编号：TR-SC-20260908**  
**版本：V1.0**

| 项目 | 内容 |
|---|---|
| 编制人 | 自动化测试执行 |
| 审核人 | 待填写 |
| 批准人 | 待填写 |
| 报告日期 | 2026-09-08 |
| 代码版本 | `544d7068fe140b58d85bfb1543541d6adf252d19` |

\newpage

# 技术摘要

**结论：11/11 组端到端自测用例通过，通过率 100%，无最终失败或阻塞项。** 11 组端到端自测一次执行全部通过，覆盖注册发现、服务调用、灰度、限流、重试、隔离、舱壁、熔断和故障注入。

本报告只覆盖工程内 `docs/TEST_CASES.md` 与 `verify.sh --tc` 定义的自测用例。`check.sh` 作为前置检查记录，不将其中的单元测试方法重复拆分为端到端用例。测试结果以保存的真实终端会话、退出码和对应截图为依据。

# 测试结论与结果概览

| 项目 | 内容 |
|---|---|
| 计划执行 | 11 组 |
| 实际执行 | 11 组 |
| 通过 | 11 组 |
| 失败 | 0 组 |
| 阻塞 | 0 组 |
| 通过率 | 100% |

**判定：通过。** 当前提交在本次测试环境和执行窗口下满足工程自测用例定义的预期。

# 范围、对象与判定口径

| 项目 | 内容 |
|---|---|
| 测试对象 | Spring Cloud Huawei ServiceComb |
| 工程目录 | `spring-cloud-huawei-servicecomb` |
| 测试用例集名称 | `spring-cloud-huawei-servicecomb-test` |
| 技术栈 | Java 17、Spring Boot 3.4.4、Spring Cloud Huawei、ServiceComb Local CSE 2.1.8、KIE、Docker Compose |
| 用例基线 | `docs/TEST_CASES.md` 与 `verify.sh --list` |
| 纳入范围 | 工程自带的逐组端到端自测用例 |
| 排除范围 | 生产环境、外部云服务、性能容量上限、安全渗透及手工探索测试 |
| 通过标准 | 用例脚本全部断言通过，进程退出码为 0 |
| 失败标准 | 任一断言失败或进程退出码非 0 |

# 测试环境与前置检查

| 项目 | 内容 |
|---|---|
| 操作系统 | Ubuntu 25.04；Linux 6.14.0-37-generic |
| 硬件架构 | x86_64 |
| Java | OpenJDK 17.0.19；Sermant 工程另使用 OpenJDK 8u472 |
| Maven | Apache Maven 3.9.15 |
| Python | Python 3.13.3；Mesher 容器运行 Python 3.11 |
| Docker | Docker Engine 29.2.1；Docker Compose v5.0.2 |
| 文档转换 | Pandoc 3.1.11.1 |

前置检查结果：`bash check.sh` 退出码为 0；18 个 Maven 模块构建成功，全部静态检查和单元测试通过。 [查看原始前置检查日志](test-report-assets/raw/PRECHECK-console.txt)。

# 执行方法与证据规则

1. 执行 `bash check.sh`，确认源码、配置、文档和代码级测试满足工程要求。
2. 使用工程 Compose 的完整 profile 构建并启动测试环境；用例按编号串行执行，避免统计型治理用例互相干扰。
3. 每组用例独立执行 `bash verify.sh --tc <编号>`；GNU `script` 保存真实开始时间、命令、完整标准输出/错误输出、结束时间和退出码。
4. 将最终有效执行日志渲染为终端样式 PNG；Markdown 引用 PNG，DOCX 内嵌同一张图片。
5. 工程执行完成后停止并清理本次 Compose 容器和网络；不删除仓库外资源及非本次创建的孤儿容器。

# 用例执行明细

## SC-E2E-001　spring-cloud-huawei-servicecomb-test-001

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-001 |
| 原始用例 | TC-1 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-001 |
| 测试场景 | 基础设施与注册发现 |
| 用例描述 | 验证 Service Center、KIE 健康状态以及订单服务通过注册中心调用商品服务。 |
| 用例原理 | 先检查 Service Center 与 KIE 的健康接口，确认注册中心和配置中心均可访问；再由 order-service 使用逻辑服务名调用 product-service。脚本校验返回业务字段，以证明服务已注册、Consumer 能发现实例并完成真实跨服务调用。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 1`；终端会话完整保存至 [test-report-assets/raw/TC-1-console.txt](test-report-assets/raw/TC-1-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。3/3 个断言通过。 |
| 执行结果截图 | ![SC-E2E-001 终端执行结果](test-report-assets/screenshots/TC-1-console.png){width=12.2cm} |

## SC-E2E-002　spring-cloud-huawei-servicecomb-test-002

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-002 |
| 原始用例 | TC-2 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-002 |
| 测试场景 | 服务间调用 |
| 用例描述 | 验证 OpenFeign 商品查询与订单创建链路。 |
| 用例原理 | OpenFeign 根据声明式接口生成客户端，并通过 ServiceComb 注册发现解析 product-service 实例。脚本调用商品列表和订单创建接口，检查商品、订单等关键字段，验证接口映射、服务发现、序列化以及下游调用链均正常。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 2`；终端会话完整保存至 [test-report-assets/raw/TC-2-console.txt](test-report-assets/raw/TC-2-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。2/2 个断言通过。 |
| 执行结果截图 | ![SC-E2E-002 终端执行结果](test-report-assets/screenshots/TC-2-console.png){width=12.2cm} |

## SC-E2E-003　spring-cloud-huawei-servicecomb-test-003

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-003 |
| 原始用例 | TC-3 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-003 |
| 测试场景 | 灰度发布 |
| 用例描述 | 验证默认 v1、30/70 权重灰度、全链路 v2、多版本共存和灰度诊断接口。 |
| 用例原理 | KIE 下发灰度路由规则，治理组件依据请求标签和 30/70 权重选择 v1、v2 实例。脚本先验证无标签流量默认命中 v1，再进行 100 次带标签采样并检查容差区间，同时验证标签经过 Gateway、Order 后仍能影响 Product 选址。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 3`；终端会话完整保存至 [test-report-assets/raw/TC-3-console.txt](test-report-assets/raw/TC-3-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。5/5 个断言通过；100 次采样结果为 v1=30、v2=70。 |
| 执行结果截图 | ![SC-E2E-003 终端执行结果](test-report-assets/screenshots/TC-3-console.png){width=12.2cm} |

## SC-E2E-004　spring-cloud-huawei-servicecomb-test-004

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-004 |
| 原始用例 | TC-4 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-004 |
| 测试场景 | Provider 故障端点 |
| 用例描述 | 验证 Provider 错误端点按设计交替产生 HTTP 200 与 502。 |
| 用例原理 | Provider 故障端点按确定规则交替返回 HTTP 200 与 502。脚本连续请求并统计状态码，要求成功和失败同时出现，以确认故障源确实可重复地产生混合结果，并为重试、隔离和熔断测试提供可靠基础。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 4`；终端会话完整保存至 [test-report-assets/raw/TC-4-console.txt](test-report-assets/raw/TC-4-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。1/1 个断言通过；6 次请求为 3 个 200、3 个 502。 |
| 执行结果截图 | ![SC-E2E-004 终端执行结果](test-report-assets/screenshots/TC-4-console.png){width=12.2cm} |

## SC-E2E-005　spring-cloud-huawei-servicecomb-test-005

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-005 |
| 原始用例 | TC-5 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-005 |
| 测试场景 | Gateway 限流 |
| 用例描述 | 向网关商品接口发起 30 次突发请求，验证限流规则生效。 |
| 用例原理 | Gateway 限流规则对单位时间内的请求进行计数，超过阈值的请求直接返回 HTTP 429。脚本瞬时发起 30 个请求并统计结果，要求部分请求正常通过、至少规定数量被拒绝，以证明限流发生在网关治理层。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 5`；终端会话完整保存至 [test-report-assets/raw/TC-5-console.txt](test-report-assets/raw/TC-5-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。1/1 个断言通过；HTTP 200=18、HTTP 429=12。 |
| 执行结果截图 | ![SC-E2E-005 终端执行结果](test-report-assets/screenshots/TC-5-console.png){width=12.2cm} |

## SC-E2E-006　spring-cloud-huawei-servicecomb-test-006

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-006 |
| 原始用例 | TC-6 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-006 |
| 测试场景 | Gateway 路由 |
| 用例描述 | 验证网关到商品服务和订单服务的基础路由。 |
| 用例原理 | Gateway 按路径规则把商品和订单请求分别转发到后端逻辑服务。脚本从统一网关入口访问两类业务接口，并校验响应中的关键业务字段，验证路由匹配、服务发现和转发链路均可用。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 6`；终端会话完整保存至 [test-report-assets/raw/TC-6-console.txt](test-report-assets/raw/TC-6-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。2/2 个断言通过。 |
| 执行结果截图 | ![SC-E2E-006 终端执行结果](test-report-assets/screenshots/TC-6-console.png){width=12.2cm} |

## SC-E2E-007　spring-cloud-huawei-servicecomb-test-007

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-007 |
| 原始用例 | TC-7 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-007 |
| 测试场景 | 重试治理 |
| 用例描述 | 验证无重试与针对 HTTP 503 的 ServiceComb 重试策略。 |
| 用例原理 | 测试端点生成可预测的临时失败，无重试请求保留原始失败分布，而启用重试的请求应在匹配 HTTP 状态码后再次调用。脚本比较不同入口的最终响应和调用表现，确认重试仅在配置条件满足时执行。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 7`；终端会话完整保存至 [test-report-assets/raw/TC-7-console.txt](test-report-assets/raw/TC-7-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。2/2 个断言通过。 |
| 执行结果截图 | ![SC-E2E-007 终端执行结果](test-report-assets/screenshots/TC-7-console.png){width=12.2cm} |

## SC-E2E-008　spring-cloud-huawei-servicecomb-test-008

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-008 |
| 原始用例 | TC-8 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-008 |
| 测试场景 | 实例隔离 |
| 用例描述 | 验证达到最小调用数后实例隔离策略被触发。 |
| 用例原理 | 实例隔离策略先累计达到最小调用数，再根据失败结果决定是否隔离目标实例。脚本重置场景后连续制造异常并观察后续请求结果，验证统计门槛未达到时不提前隔离、达到门槛后策略按预期生效。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 8`；终端会话完整保存至 [test-report-assets/raw/TC-8-console.txt](test-report-assets/raw/TC-8-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。1/1 个断言通过。 |
| 执行结果截图 | ![SC-E2E-008 终端执行结果](test-report-assets/screenshots/TC-8-console.png){width=12.2cm} |

## SC-E2E-009　spring-cloud-huawei-servicecomb-test-009

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-009 |
| 原始用例 | TC-9 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-009 |
| 测试场景 | 舱壁隔离 |
| 用例描述 | 以 maxConcurrentCalls=2 发起 10 个并发请求，验证并发限制。 |
| 用例原理 | 舱壁以 maxConcurrentCalls=2 限制同时进入下游的调用。脚本并发发起 10 个耗时请求，使执行窗口发生重叠，再统计成功和拒绝数量；成功数不超过 2 且拒绝数不少于 8，说明并发槽位限制真实生效。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 9`；终端会话完整保存至 [test-report-assets/raw/TC-9-console.txt](test-report-assets/raw/TC-9-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。1/1 个断言通过；成功=2、拒绝=8。 |
| 执行结果截图 | ![SC-E2E-009 终端执行结果](test-report-assets/screenshots/TC-9-console.png){width=12.2cm} |

## SC-E2E-010　spring-cloud-huawei-servicecomb-test-010

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-010 |
| 原始用例 | TC-10 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-010 |
| 测试场景 | Consumer 熔断 |
| 用例描述 | 连续发起 12 次请求，验证 Consumer 熔断后停止调用 Provider。 |
| 用例原理 | Consumer 连续调用 100% 失败路径，使断路器在统计窗口内达到错误率阈值。脚本先清零 Provider 计数，再发起 12 个请求；若 Provider 实际只收到部分调用，说明剩余请求已由 Consumer 端熔断器提前短路。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 10`；终端会话完整保存至 [test-report-assets/raw/TC-10-console.txt](test-report-assets/raw/TC-10-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。1/1 个断言通过；Provider 实际调用 6/12。 |
| 执行结果截图 | ![SC-E2E-010 终端执行结果](test-report-assets/screenshots/TC-10-console.png){width=12.2cm} |

## SC-E2E-011　spring-cloud-huawei-servicecomb-test-011

| 项目 | 内容 |
|---|---|
| 用例 ID | SC-E2E-011 |
| 原始用例 | TC-11 |
| 用例名称 | spring-cloud-huawei-servicecomb-test-011 |
| 测试场景 | 故障注入 |
| 用例描述 | 验证 KIE 规则加载、50% 故障注入分布以及 100% 注入完全阻止下游调用。 |
| 用例原理 | KIE 将故障注入规则动态下发到 Consumer，治理层在调用 Provider 前按概率直接生成失败。50% 场景通过成功数、失败数和 Provider 计数验证概率注入；100% 场景要求 Provider 调用为零，证明故障发生在下游调用之前。 |
| 执行命令过程 | 在 `spring-cloud-huawei-servicecomb` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 11`；终端会话完整保存至 [test-report-assets/raw/TC-11-console.txt](test-report-assets/raw/TC-11-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。3/3 个断言通过；50% 场景成功=25、注入=25、Provider 调用=25；100% 场景 Provider 调用=0/50。 |
| 执行结果截图 | ![SC-E2E-011 终端执行结果](test-report-assets/screenshots/TC-11-console.png){width=12.2cm} |

# 异常诊断、限制与稳健性说明

未出现用例失败或阻塞。灰度、限流、舱壁与故障注入属于统计型断言，本次样本结果均满足脚本定义的通过条件。

本次结论是对指定代码提交、指定本机环境和一次完整执行窗口的描述性验证，不等同于长期稳定性、性能容量或生产环境兼容性证明。概率/并发类用例的结论受脚本定义的样本数与阈值约束；本报告保留精确样本结果，便于后续复核。

# 建议的后续动作

- 将同一组 `check.sh` 与 `verify.sh` 用例纳入持续集成，保留终端日志或机器可读测试产物。
- 执行四工程测试前先检查 8080～8097、8848、9848～9849、30100 等端口，避免跨工程容器冲突。
- 若升级框架、Nacos、Local CSE、Sermant 或 Mesher 版本，重新执行本报告全部用例并生成新版本报告。

# 待确认事项

当前无影响本次放行结论的待确认事项。审核人和批准人信息由文档接收方补充。

# 签署

| 项目 | 内容 |
|---|---|
| 编制 | 自动化测试执行 |
| 审核 | 待填写 |
| 批准 | 待填写 |
| 日期 | 2026-09-08 |
