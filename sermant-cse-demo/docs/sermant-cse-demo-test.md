---
title: "sermant-cse-demo-test 测试报告"
author: "自动化测试执行"
date: "2026-09-08"
lang: zh-CN
---

**文档状态：正式版**  
**文档编号：TR-SER-20260908**  
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

**结论：3/3 组端到端自测用例通过，通过率 100%，无最终失败或阻塞项。** 3 组端到端自测一次执行全部通过，确认基础设施、Sermant 注册元数据和基于逻辑服务名的服务发现链路正确。

本报告只覆盖工程内 `docs/TEST_CASES.md` 与 `verify.sh --tc` 定义的自测用例。`check.sh` 作为前置检查记录，不将其中的单元测试方法重复拆分为端到端用例。测试结果以保存的真实终端会话、退出码和对应截图为依据。

# 测试结论与结果概览

| 项目 | 内容 |
|---|---|
| 计划执行 | 3 组 |
| 实际执行 | 3 组 |
| 通过 | 3 组 |
| 失败 | 0 组 |
| 阻塞 | 0 组 |
| 通过率 | 100% |

**判定：通过。** 当前提交在本次测试环境和执行窗口下满足工程自测用例定义的预期。

# 范围、对象与判定口径

| 项目 | 内容 |
|---|---|
| 测试对象 | Sermant CSE Demo |
| 工程目录 | `sermant-cse-demo` |
| 测试用例集名称 | `sermant-cse-demo-test` |
| 技术栈 | Java 8、Spring Boot 2.7.18、Sermant Agent 2.0.0、ServiceComb Local CSE 2.1.8、Docker Compose |
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

前置检查结果：使用 OpenJDK 8u472 执行 `bash check.sh`，退出码为 0；4 个 Maven 模块构建成功，全部检查通过。 [查看原始前置检查日志](test-report-assets/raw/PRECHECK-console.txt)。

# 执行方法与证据规则

1. 执行 `bash check.sh`，确认源码、配置、文档和代码级测试满足工程要求。
2. 使用工程 Compose 的完整 profile 构建并启动测试环境；用例按编号串行执行，避免统计型治理用例互相干扰。
3. 每组用例独立执行 `bash verify.sh --tc <编号>`；GNU `script` 保存真实开始时间、命令、完整标准输出/错误输出、结束时间和退出码。
4. 将最终有效执行日志渲染为终端样式 PNG；Markdown 引用 PNG，DOCX 内嵌同一张图片。
5. 工程执行完成后停止并清理本次 Compose 容器和网络；不删除仓库外资源及非本次创建的孤儿容器。

# 用例执行明细

## SER-E2E-001　sermant-cse-demo-test-001

| 项目 | 内容 |
|---|---|
| 用例 ID | SER-E2E-001 |
| 原始用例 | TC-1 |
| 用例名称 | sermant-cse-demo-test-001 |
| 测试场景 | 基础设施 |
| 用例描述 | 验证 Docker Compose、Service Center 健康状态以及 product-service、order-service 容器运行状态。 |
| 用例原理 | 先验证 Docker Compose 命令和 Service Center 健康接口，再检查 product-service、order-service 容器状态。该用例从编排工具、基础设施和应用进程三个层面确认测试前提，避免将环境未启动误判为业务功能故障。 |
| 执行命令过程 | 在 `sermant-cse-demo` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 1`；终端会话完整保存至 [test-report-assets/raw/TC-1-console.txt](test-report-assets/raw/TC-1-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。4/4 个断言通过。 |
| 执行结果截图 | ![SER-E2E-001 终端执行结果](test-report-assets/screenshots/TC-1-console.png){width=12.2cm} |

## SER-E2E-002　sermant-cse-demo-test-002

| 项目 | 内容 |
|---|---|
| 用例 ID | SER-E2E-002 |
| 原始用例 | TC-2 |
| 用例名称 | sermant-cse-demo-test-002 |
| 测试场景 | Sermant 注册 |
| 用例描述 | 验证 product-service 已注册，并且注册元数据中的 framework.name 为 Sermant。 |
| 用例原理 | product-service 启动时由 Sermant Java Agent 无侵入拦截框架生命周期，并向 Service Center 注册服务实例及框架元数据。脚本查询注册中心，既检查服务名存在，也核对 framework.name=Sermant，以证明注册动作确由 Agent 完成。 |
| 执行命令过程 | 在 `sermant-cse-demo` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 2`；终端会话完整保存至 [test-report-assets/raw/TC-2-console.txt](test-report-assets/raw/TC-2-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。2/2 个断言通过。 |
| 执行结果截图 | ![SER-E2E-002 终端执行结果](test-report-assets/screenshots/TC-2-console.png){width=12.2cm} |

## SER-E2E-003　sermant-cse-demo-test-003

| 项目 | 内容 |
|---|---|
| 用例 ID | SER-E2E-003 |
| 原始用例 | TC-3 |
| 用例名称 | sermant-cse-demo-test-003 |
| 测试场景 | 服务发现调用 |
| 用例描述 | 验证 Consumer 按逻辑服务名调用 Provider，并返回预期商品与来源字段。 |
| 用例原理 | order-service 不使用 Provider 的固定地址，而是向 Service Center 查询 product-service 实例后完成负载均衡调用。脚本检查返回商品字段和 source=sermant-product，确认逻辑服务名解析、网络调用及真实 Provider 响应完整贯通。 |
| 执行命令过程 | 在 `sermant-cse-demo` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 3`；终端会话完整保存至 [test-report-assets/raw/TC-3-console.txt](test-report-assets/raw/TC-3-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。3/3 个断言通过。 |
| 执行结果截图 | ![SER-E2E-003 终端执行结果](test-report-assets/screenshots/TC-3-console.png){width=12.2cm} |

# 异常诊断、限制与稳健性说明

未出现用例失败或阻塞。Compose 检测到两个旧命名孤儿容器 `sermant-product`、`sch-consumer`；它们不属于当前 Compose 服务，本次未删除，也未影响测试。

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
