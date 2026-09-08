---
title: "spring-cloud-huawei-nacos-test 测试报告"
author: "自动化测试执行"
date: "2026-09-08"
lang: zh-CN
---

**文档状态：正式版**  
**文档编号：TR-NAC-20260908**  
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

**结论：12/12 组端到端自测用例通过，通过率 100%，无最终失败或阻塞项。** 12 组端到端自测最终全部通过。首次执行受其他工程遗留容器端口占用影响，部分核心容器未正确加入 Compose 网络；定位并强制重建受影响容器后，6 个失败项全部复测通过。

本报告只覆盖工程内 `docs/TEST_CASES.md` 与 `verify.sh --tc` 定义的自测用例。`check.sh` 作为前置检查记录，不将其中的单元测试方法重复拆分为端到端用例。测试结果以保存的真实终端会话、退出码和对应截图为依据。

# 测试结论与结果概览

| 项目 | 内容 |
|---|---|
| 计划执行 | 12 组 |
| 实际执行 | 12 组 |
| 通过 | 12 组 |
| 失败 | 0 组 |
| 阻塞 | 0 组 |
| 通过率 | 100% |

**判定：通过。** 当前提交在本次测试环境和执行窗口下满足工程自测用例定义的预期。

# 范围、对象与判定口径

| 项目 | 内容 |
|---|---|
| 测试对象 | Spring Cloud Huawei Nacos |
| 工程目录 | `spring-cloud-huawei-nacos` |
| 测试用例集名称 | `spring-cloud-huawei-nacos-test` |
| 技术栈 | Java 17、Spring Boot 3.4.4、Spring Cloud Huawei、Nacos 2.1.0、Docker Compose |
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

前置检查结果：`bash check.sh` 最终退出码为 0；15 个 Maven 模块构建成功，脚本、Compose、配置、文档与仓库卫生检查全部通过。 [查看原始前置检查日志](test-report-assets/raw/PRECHECK-console.txt)。

# 执行方法与证据规则

1. 执行 `bash check.sh`，确认源码、配置、文档和代码级测试满足工程要求。
2. 使用工程 Compose 的完整 profile 构建并启动测试环境；用例按编号串行执行，避免统计型治理用例互相干扰。
3. 每组用例独立执行 `bash verify.sh --tc <编号>`；GNU `script` 保存真实开始时间、命令、完整标准输出/错误输出、结束时间和退出码。
4. 将最终有效执行日志渲染为终端样式 PNG；Markdown 引用 PNG，DOCX 内嵌同一张图片。
5. 工程执行完成后停止并清理本次 Compose 容器和网络；不删除仓库外资源及非本次创建的孤儿容器。

# 用例执行明细

## NAC-E2E-001　spring-cloud-huawei-nacos-test-001

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-001 |
| 原始用例 | TC-1 |
| 用例名称 | spring-cloud-huawei-nacos-test-001 |
| 测试场景 | Nacos 配置与注册发现 |
| 用例描述 | 验证 Nacos 健康状态、灰度配置发布、product-service 双版本注册以及 v1/v2 接口可访问性。 |
| 用例原理 | 先调用 Nacos 健康与配置接口确认控制面可用，再查询服务注册信息，核对 product-service 的 v1、v2 实例是否同时存在。最后直接访问两个版本的业务接口，将注册中心记录与真实服务响应交叉验证，避免仅凭容器存活误判注册成功。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 1`；终端会话完整保存至 [test-report-assets/raw/final-retry/TC-1-console.txt](test-report-assets/raw/final-retry/TC-1-console.txt)，并核验退出码为 `0`。 本表采用环境修复后的有效复测记录。 |
| 执行结果 | **通过**。5/5 个断言通过；双版本注册及两个服务端点均符合预期。 |
| 执行结果截图 | ![NAC-E2E-001 终端执行结果](test-report-assets/screenshots/TC-1-console.png){width=12.2cm} |

## NAC-E2E-002　spring-cloud-huawei-nacos-test-002

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-002 |
| 原始用例 | TC-2 |
| 用例名称 | spring-cloud-huawei-nacos-test-002 |
| 测试场景 | RestTemplate 服务调用 |
| 用例描述 | 验证订单创建、商品列表查询以及默认流量全部路由到 product-service v1。 |
| 用例原理 | order-service 使用带负载均衡能力的 RestTemplate，以逻辑服务名调用 product-service。脚本分别校验订单创建、商品查询和多次默认请求的响应字段，并统计返回版本；只有请求链路可用且默认流量全部命中 v1，才判定服务发现与默认路由正确。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 2`；终端会话完整保存至 [test-report-assets/raw/final-retry/TC-2-console.txt](test-report-assets/raw/final-retry/TC-2-console.txt)，并核验退出码为 `0`。 本表采用环境修复后的有效复测记录。 |
| 执行结果 | **通过**。3/3 个断言通过；默认路由 10/10 命中 v1。 |
| 执行结果截图 | ![NAC-E2E-002 终端执行结果](test-report-assets/screenshots/TC-2-console.png){width=12.2cm} |

## NAC-E2E-003　spring-cloud-huawei-nacos-test-003

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-003 |
| 原始用例 | TC-2F |
| 用例名称 | spring-cloud-huawei-nacos-test-003 |
| 测试场景 | OpenFeign 服务调用 |
| 用例描述 | 验证 OpenFeign 创建订单、默认 v1 路由及带灰度标签的 v2 路由。 |
| 用例原理 | order-service-feign 通过 OpenFeign 接口代理发起服务名调用。普通请求不携带灰度标签，应由默认规则选择 v1；灰度请求携带约定标签，治理规则应选择 v2。脚本对两组请求重复采样，以响应中的版本字段确认代理调用和标签路由同时生效。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 2F`；终端会话完整保存至 [test-report-assets/raw/final-retry/TC-2F-console.txt](test-report-assets/raw/final-retry/TC-2F-console.txt)，并核验退出码为 `0`。 本表采用环境修复后的有效复测记录。 |
| 执行结果 | **通过**。3/3 个断言通过；默认与灰度请求均 10/10 命中目标版本。 |
| 执行结果截图 | ![NAC-E2E-003 终端执行结果](test-report-assets/screenshots/TC-2F-console.png){width=12.2cm} |

## NAC-E2E-004　spring-cloud-huawei-nacos-test-004

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-004 |
| 原始用例 | TC-3 |
| 用例名称 | spring-cloud-huawei-nacos-test-004 |
| 测试场景 | Consumer 灰度路由 |
| 用例描述 | 验证 Consumer 普通流量命中 v1、灰度流量命中 v2，并确认双版本共存。 |
| 用例原理 | Consumer 请求 product-service 时，治理组件从请求头读取灰度标签，并结合 Nacos 中发布的路由配置选择目标实例。脚本分别发送无标签和带标签请求，通过多次响应中的版本字段验证普通流量固定落到 v1、灰度流量固定落到 v2。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 3`；终端会话完整保存至 [test-report-assets/raw/final-retry/TC-3-console.txt](test-report-assets/raw/final-retry/TC-3-console.txt)，并核验退出码为 `0`。 本表采用环境修复后的有效复测记录。 |
| 执行结果 | **通过**。3/3 个断言通过；两类流量均 10/10 命中预期版本。 |
| 执行结果截图 | ![NAC-E2E-004 终端执行结果](test-report-assets/screenshots/TC-3-console.png){width=12.2cm} |

## NAC-E2E-005　spring-cloud-huawei-nacos-test-005

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-005 |
| 原始用例 | TC-3G |
| 用例名称 | spring-cloud-huawei-nacos-test-005 |
| 测试场景 | Gateway 灰度路由 |
| 用例描述 | 验证 Gateway 默认路由、灰度路由以及 Gateway 到下游的全链路灰度传播。 |
| 用例原理 | 请求先进入 Gateway，再转发到订单服务并继续调用商品服务。Gateway 需要保留灰度上下文，下游负载均衡器再依据同一标签选择实例。脚本同时验证网关响应和最终商品版本，确认标签没有在多跳调用中丢失。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 3G`；终端会话完整保存至 [test-report-assets/raw/final-retry/TC-3G-console.txt](test-report-assets/raw/final-retry/TC-3G-console.txt)，并核验退出码为 `0`。 本表采用环境修复后的有效复测记录。 |
| 执行结果 | **通过**。3/3 个断言通过；默认与灰度流量均 10/10 命中预期版本。 |
| 执行结果截图 | ![NAC-E2E-005 终端执行结果](test-report-assets/screenshots/TC-3G-console.png){width=12.2cm} |

## NAC-E2E-006　spring-cloud-huawei-nacos-test-006

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-006 |
| 原始用例 | TC-4 |
| 用例名称 | spring-cloud-huawei-nacos-test-006 |
| 测试场景 | Provider 故障端点 |
| 用例描述 | 验证错误端点产生成功/失败混合响应，并验证慢调用端点确实产生延迟。 |
| 用例原理 | Provider 的错误端点按预设规律返回成功与失败，慢调用端点主动延迟响应。脚本对错误端点连续采样并检查两类状态码均出现，同时测量慢调用耗时；两项都满足才说明故障模拟能力可供后续治理用例使用。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 4`；终端会话完整保存至 [test-report-assets/raw/TC-4-console.txt](test-report-assets/raw/TC-4-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。2/2 个断言通过。 |
| 执行结果截图 | ![NAC-E2E-006 终端执行结果](test-report-assets/screenshots/TC-4-console.png){width=12.2cm} |

## NAC-E2E-007　spring-cloud-huawei-nacos-test-007

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-007 |
| 原始用例 | TC-5 |
| 用例名称 | spring-cloud-huawei-nacos-test-007 |
| 测试场景 | 流量限流 |
| 用例描述 | 对订单服务发起突发并发请求，验证成功响应与 HTTP 429 限流响应同时出现。 |
| 用例原理 | 在短时间内并发发送超过限流阈值的请求，使令牌或配额被快速消耗。脚本统计 HTTP 200 与 429 数量：既要有成功请求证明服务可用，也要有 429 证明超额流量被治理层拒绝，从而排除服务整体不可用造成的假阳性。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 5`；终端会话完整保存至 [test-report-assets/raw/final-retry/TC-5-console.txt](test-report-assets/raw/final-retry/TC-5-console.txt)，并核验退出码为 `0`。 本表采用环境修复后的有效复测记录。 |
| 执行结果 | **通过**。1/1 个断言通过；HTTP 200=2，HTTP 429=6。 |
| 执行结果截图 | ![NAC-E2E-007 终端执行结果](test-report-assets/screenshots/TC-5-console.png){width=12.2cm} |

## NAC-E2E-008　spring-cloud-huawei-nacos-test-008

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-008 |
| 原始用例 | TC-6 |
| 用例名称 | spring-cloud-huawei-nacos-test-008 |
| 测试场景 | Gateway 基础路由 |
| 用例描述 | 验证商品路由、订单路由以及未知路径返回 HTTP 404。 |
| 用例原理 | Gateway 根据路径谓词将商品和订单 URL 转发到对应逻辑服务，并让未配置的路径落入 404。脚本分别访问两条有效路由和一条未知路由，通过业务响应与状态码共同验证路由表的正向匹配和兜底行为。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 6`；终端会话完整保存至 [test-report-assets/raw/TC-6-console.txt](test-report-assets/raw/TC-6-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。3/3 个断言通过。 |
| 执行结果截图 | ![NAC-E2E-008 终端执行结果](test-report-assets/screenshots/TC-6-console.png){width=12.2cm} |

## NAC-E2E-009　spring-cloud-huawei-nacos-test-009

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-009 |
| 原始用例 | TC-7 |
| 用例名称 | spring-cloud-huawei-nacos-test-009 |
| 测试场景 | 重试治理 |
| 用例描述 | 验证无重试、普通重试、状态码重试、服务名级重试及同实例重试等六种策略。 |
| 用例原理 | 故障端点提供可重复的失败序列，Consumer 分别应用无重试、普通重试、状态码重试、服务名级重试和同实例重试等策略。脚本在每个场景前重置状态，再比对最终响应和调用次数，以判断是否按策略触发以及是否在正确范围内重试。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 7`；终端会话完整保存至 [test-report-assets/raw/TC-7-console.txt](test-report-assets/raw/TC-7-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。6/6 个治理场景通过。 |
| 执行结果截图 | ![NAC-E2E-009 终端执行结果](test-report-assets/screenshots/TC-7-console.png){width=12.2cm} |

## NAC-E2E-010　spring-cloud-huawei-nacos-test-010

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-010 |
| 原始用例 | TC-8 |
| 用例名称 | spring-cloud-huawei-nacos-test-010 |
| 测试场景 | 实例隔离 |
| 用例描述 | 验证最小调用数、失败率、慢调用率、强制开关及 404/500 错误码等隔离场景。 |
| 用例原理 | 实例隔离依据最小调用数、失败率或慢调用率累计统计，并在超过阈值后暂时摘除异常实例。脚本逐场景恢复策略、清零 Provider 计数并制造错误或延迟，再验证隔离状态及下游计数停止增长，同时覆盖强制开关和不同错误码配置。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 8`；终端会话完整保存至 [test-report-assets/raw/TC-8-console.txt](test-report-assets/raw/TC-8-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。11/11 个治理场景通过。 |
| 执行结果截图 | ![NAC-E2E-010 终端执行结果](test-report-assets/screenshots/TC-8-console.png){width=12.2cm} |

## NAC-E2E-011　spring-cloud-huawei-nacos-test-011

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-011 |
| 原始用例 | TC-9 |
| 用例名称 | spring-cloud-huawei-nacos-test-011 |
| 测试场景 | 舱壁隔离 |
| 用例描述 | 验证限并发、无限并发以及服务名级舱壁策略。 |
| 用例原理 | 舱壁使用并发信号量限制同一资源可同时执行的请求数。脚本让多个慢请求重叠执行，比较受限、无限制和服务名级配置下的返回结果；只有并发上限按配置生效且未影响非受限场景，才判定隔离边界正确。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 9`；终端会话完整保存至 [test-report-assets/raw/TC-9-console.txt](test-report-assets/raw/TC-9-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。3/3 个并发治理场景通过。 |
| 执行结果截图 | ![NAC-E2E-011 终端执行结果](test-report-assets/screenshots/TC-9-console.png){width=12.2cm} |

## NAC-E2E-012　spring-cloud-huawei-nacos-test-012

| 项目 | 内容 |
|---|---|
| 用例 ID | NAC-E2E-012 |
| 原始用例 | TC-10 |
| 用例名称 | spring-cloud-huawei-nacos-test-012 |
| 测试场景 | Provider 熔断 |
| 用例描述 | 验证错误率与慢调用率触发熔断，并确认熔断后 Provider 实际调用数小于请求数。 |
| 用例原理 | Consumer 在固定统计窗口内持续调用高错误率或高延迟 Provider，使熔断指标越过阈值并打开断路器。脚本同时记录发起请求数和真正进入 Provider Controller 的次数；熔断打开且实际调用数更少，才能证明后续请求在调用前被短路。 |
| 执行命令过程 | 在 `spring-cloud-huawei-nacos` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 10`；终端会话完整保存至 [test-report-assets/raw/TC-10-console.txt](test-report-assets/raw/TC-10-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。8/8 个熔断断言通过。 |
| 执行结果截图 | ![NAC-E2E-012 终端执行结果](test-report-assets/screenshots/TC-10-console.png){width=12.2cm} |

# 异常诊断、限制与稳健性说明

首次 E2E 中 TC-1、TC-2、TC-2F、TC-3、TC-3G、TC-5 失败。诊断发现任务开始前运行的 ServiceComb 容器占用 8080～8084 端口，使 Nacos 核心容器初次创建不完整；其后 `docker inspect` 显示 5 个核心容器的网络端点为空，日志持续出现 `UnknownHostException: nacos`。暂停冲突容器并强制重建 `product-service`、`product-service-v2`、`order-service`、`order-service-feign`、`gateway` 后，上述 6 组用例全部通过。该异常属于测试环境编排问题，不是业务断言缺陷。

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
