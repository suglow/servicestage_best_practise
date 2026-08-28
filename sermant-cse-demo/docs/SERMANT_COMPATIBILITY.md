# Sermant 兼容性与配置

## 版本选择

| 组件 | 版本 | 原因 |
| --- | --- | --- |
| Java | 8 | 与 Sermant 2.0.0 官方示例和 Agent 运行约束一致 |
| Spring Boot | 2.7.18 | 使用 Spring Cloud 2021.0.x，避开 Boot 3.x Agent 类加载兼容性风险 |
| Spring Cloud | 2021.0.8 | 与 Boot 2.7.18 匹配 |
| Spring Cloud Huawei | 1.11.15-2021.0.x | Consumer 的 ServiceComb 发现客户端 |
| Sermant | 2.0.0 | Provider 注册插件版本；构建时校验 SHA-256 |

这个示例有意不升级到参考工程使用的 Java 17/Spring Boot 3.4，因为 Provider 的核心目标是验证 Sermant JavaAgent 注册能力，而不是演示 Boot 3 迁移。

## Agent 配置

镜像构建时对官方配置做最小覆盖：

- `register.service.address` 指向 `http://local-cse:30100`。
- `servicecomb.service.enableSpringRegister` 设置为 `true`。
- 关闭动态配置服务并将类型设为 `NOP`，避免示例依赖 ZooKeeper。
- 运行时通过 `CAS_APPLICATION_NAME` 和 `CAS_INSTANCE_VERSION` 设置 Service Center 元数据。

Provider 代码不导入 `org.apache.servicecomb`，也不声明 Spring Cloud Huawei 注册客户端；这正是本项目要验证的 Agent 接入边界。

## 常见限制

- Agent 下载依赖网络；Docker 构建失败时先检查 GitHub 连通性。
- Java 8 是当前构建和运行基线，`check.sh` 会拒绝其他 Java 主版本。
- 其他 JavaAgent 可能与字节码增强产生冲突，生产环境应单独验证。
- Sermant Provider 的注册成功不等于 Consumer 已发现；必须同时检查注册列表和端到端调用。
