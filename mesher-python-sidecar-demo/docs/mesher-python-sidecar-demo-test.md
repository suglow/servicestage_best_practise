---
title: "mesher-python-sidecar-demo-test 测试报告"
author: "自动化测试执行"
date: "2026-09-08"
lang: zh-CN
---

**文档状态：正式版**  
**文档编号：TR-MSH-20260908**  
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

**结论：6/6 组端到端自测用例通过，通过率 100%，无最终失败或阻塞项。** 6 组端到端自测一次执行全部通过，确认 Java、Python 与 Mesher Sidecar 的注册发现、出向代理和完整双向调用链。

本报告只覆盖工程内 `docs/TEST_CASES.md` 与 `verify.sh --tc` 定义的自测用例。`check.sh` 作为前置检查记录，不将其中的单元测试方法重复拆分为端到端用例。测试结果以保存的真实终端会话、退出码和对应截图为依据。

# 测试结论与结果概览

| 项目 | 内容 |
|---|---|
| 计划执行 | 6 组 |
| 实际执行 | 6 组 |
| 通过 | 6 组 |
| 失败 | 0 组 |
| 阻塞 | 0 组 |
| 通过率 | 100% |

**判定：通过。** 当前提交在本次测试环境和执行窗口下满足工程自测用例定义的预期。

# 范围、对象与判定口径

| 项目 | 内容 |
|---|---|
| 测试对象 | Mesher Python Sidecar Demo |
| 工程目录 | `mesher-python-sidecar-demo` |
| 测试用例集名称 | `mesher-python-sidecar-demo-test` |
| 技术栈 | Java 17、Python 3.11/Flask、Mesher 1.8.1、ServiceComb Local CSE 2.1.8、Docker Compose |
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

前置检查结果：前置检查首次因宿主机缺少 Flask 而在 Python 测试导入阶段失败；将 `python-app/requirements.txt` 声明的依赖安装到 `/tmp` 隔离目录后复测退出码为 0。最终 Java 4 个单元测试、Python 3 个单元测试及其余检查全部通过。 [查看原始前置检查日志](test-report-assets/raw/PRECHECK-console.txt)。

# 执行方法与证据规则

1. 执行 `bash check.sh`，确认源码、配置、文档和代码级测试满足工程要求。
2. 使用工程 Compose 的完整 profile 构建并启动测试环境；用例按编号串行执行，避免统计型治理用例互相干扰。
3. 每组用例独立执行 `bash verify.sh --tc <编号>`；GNU `script` 保存真实开始时间、命令、完整标准输出/错误输出、结束时间和退出码。
4. 将最终有效执行日志渲染为终端样式 PNG；Markdown 引用 PNG，DOCX 内嵌同一张图片。
5. 工程执行完成后停止并清理本次 Compose 容器和网络；不删除仓库外资源及非本次创建的孤儿容器。

# 用例执行明细

## MSH-E2E-001　mesher-python-sidecar-demo-test-001

| 项目 | 内容 |
|---|---|
| 用例 ID | MSH-E2E-001 |
| 原始用例 | TC-1 |
| 用例名称 | mesher-python-sidecar-demo-test-001 |
| 测试场景 | 基础设施 |
| 用例描述 | 验证 Service Center 健康状态和 local-cse、java-provider、python-app、java-consumer 四个容器运行状态。 |
| 用例原理 | 先检查 Service Center 健康状态，再检查 local-cse、java-provider、python-app、java-consumer 四个容器是否运行。该用例确认注册中心、业务进程和 Sidecar 所依赖的容器网络均已具备，为后续链路测试建立有效基线。 |
| 执行命令过程 | 在 `mesher-python-sidecar-demo` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 1`；终端会话完整保存至 [test-report-assets/raw/TC-1-console.txt](test-report-assets/raw/TC-1-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。6/6 个断言通过。 |
| 执行结果截图 | ![MSH-E2E-001 终端执行结果](test-report-assets/screenshots/TC-1-console.png){width=12.2cm} |

## MSH-E2E-002　mesher-python-sidecar-demo-test-002

| 项目 | 内容 |
|---|---|
| 用例 ID | MSH-E2E-002 |
| 原始用例 | TC-2 |
| 用例名称 | mesher-python-sidecar-demo-test-002 |
| 测试场景 | 注册发现 |
| 用例描述 | 验证三个应用均已注册，并确认 python-app 对外公布 Mesher 30101 端口。 |
| 用例原理 | Java Provider、Java Consumer 和 Python 应用分别向 Service Center 注册；Python 应用对外公布的实例端点实际指向 Mesher 的 30101 入向代理端口。脚本查询注册数据并核对端口，确认其他服务发现后会先访问 Sidecar 而非直接访问 Flask。 |
| 执行命令过程 | 在 `mesher-python-sidecar-demo` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 2`；终端会话完整保存至 [test-report-assets/raw/TC-2-console.txt](test-report-assets/raw/TC-2-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。4/4 个断言通过。 |
| 执行结果截图 | ![MSH-E2E-002 终端执行结果](test-report-assets/screenshots/TC-2-console.png){width=12.2cm} |

## MSH-E2E-003　mesher-python-sidecar-demo-test-003

| 项目 | 内容 |
|---|---|
| 用例 ID | MSH-E2E-003 |
| 原始用例 | TC-3 |
| 用例名称 | mesher-python-sidecar-demo-test-003 |
| 测试场景 | Java Provider API |
| 用例描述 | 验证 Java Provider 接口就绪并返回三个演示用户。 |
| 用例原理 | 直接调用 Java Provider 的 /api/users 接口作为下游能力基线。脚本检查接口可达性和三个演示用户，先证明 Provider 自身数据与 HTTP 服务正常，从而在后续代理链路失败时能够区分下游故障与 Mesher 故障。 |
| 执行命令过程 | 在 `mesher-python-sidecar-demo` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 3`；终端会话完整保存至 [test-report-assets/raw/TC-3-console.txt](test-report-assets/raw/TC-3-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。2/2 个断言通过。 |
| 执行结果截图 | ![MSH-E2E-003 终端执行结果](test-report-assets/screenshots/TC-3-console.png){width=12.2cm} |

## MSH-E2E-004　mesher-python-sidecar-demo-test-004

| 项目 | 内容 |
|---|---|
| 用例 ID | MSH-E2E-004 |
| 原始用例 | TC-4 |
| 用例名称 | mesher-python-sidecar-demo-test-004 |
| 测试场景 | Flask 与 Mesher 健康 |
| 用例描述 | 验证 Python 健康接口以及 Mesher 代理端口可连接。 |
| 用例原理 | 分别访问 Flask /health 接口并探测 Mesher 代理端口。前者确认 Python 业务进程可以处理请求，后者确认 Sidecar 已监听代理入口；两项同时通过，才能说明业务容器与代理进程均已就绪。 |
| 执行命令过程 | 在 `mesher-python-sidecar-demo` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 4`；终端会话完整保存至 [test-report-assets/raw/TC-4-console.txt](test-report-assets/raw/TC-4-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。2/2 个断言通过。 |
| 执行结果截图 | ![MSH-E2E-004 终端执行结果](test-report-assets/screenshots/TC-4-console.png){width=12.2cm} |

## MSH-E2E-005　mesher-python-sidecar-demo-test-005

| 项目 | 内容 |
|---|---|
| 用例 ID | MSH-E2E-005 |
| 原始用例 | TC-5 |
| 用例名称 | mesher-python-sidecar-demo-test-005 |
| 测试场景 | Mesher 出向代理 |
| 用例描述 | 验证 Flask 应用通过 Mesher 调用 java-provider。 |
| 用例原理 | Flask 不直接保存 Java Provider 地址，而是把出向请求发送给本地 Mesher；Mesher 根据逻辑服务名从 Service Center 发现实例并转发。脚本调用 Flask /show 并校验下游数据，验证出向代理、注册发现和响应回传过程。 |
| 执行命令过程 | 在 `mesher-python-sidecar-demo` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 5`；终端会话完整保存至 [test-report-assets/raw/TC-5-console.txt](test-report-assets/raw/TC-5-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。2/2 个断言通过。 |
| 执行结果截图 | ![MSH-E2E-005 终端执行结果](test-report-assets/screenshots/TC-5-console.png){width=12.2cm} |

## MSH-E2E-006　mesher-python-sidecar-demo-test-006

| 项目 | 内容 |
|---|---|
| 用例 ID | MSH-E2E-006 |
| 原始用例 | TC-6 |
| 用例名称 | mesher-python-sidecar-demo-test-006 |
| 测试场景 | Mesher 入向与完整调用链 |
| 用例描述 | 验证 Java Consumer→Mesher→Flask→Mesher→Java Provider 完整链路连续执行。 |
| 用例原理 | Java Consumer 先通过服务发现访问 python-app 公布的 Mesher 入向端口，Mesher 将请求转给 Flask；Flask 随后再次经 Mesher 出向代理调用 Java Provider，响应沿原路返回。脚本连续执行三次，以验证双向代理和完整多跳链路的基本稳定性。 |
| 执行命令过程 | 在 `mesher-python-sidecar-demo` 工程根目录、完整 Compose 测试环境已启动的条件下执行 `bash verify.sh --tc 6`；终端会话完整保存至 [test-report-assets/raw/TC-6-console.txt](test-report-assets/raw/TC-6-console.txt)，并核验退出码为 `0`。 |
| 执行结果 | **通过**。2/2 个断言通过；完整链路连续成功 3/3。 |
| 执行结果截图 | ![MSH-E2E-006 终端执行结果](test-report-assets/screenshots/TC-6-console.png){width=12.2cm} |

# 异常诊断、限制与稳健性说明

E2E 用例未出现失败或阻塞。前置检查异常属于宿主机测试依赖缺失，不涉及业务代码修改。

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
