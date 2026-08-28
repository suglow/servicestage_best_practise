# 循序教程

本教程以可观察结果为主线。每一步都可以用 `verify.sh` 单独验证，适合首次了解 Spring Cloud Huawei 与 Nacos 的开发者。

## 第 1 步：启动注册与配置中心

```bash
docker compose up -d nacos nacos-init
docker compose logs nacos-init
bash verify.sh --tc 1
```

观察要点：

- Nacos 在 `8848` 提供控制台和 HTTP API。
- `nacos-init` 创建 `dev` namespace。
- 初始化任务向 `CORE_GROUP` 发布灰度配置，并回读确认。

## 第 2 步：观察双版本服务注册

```bash
docker compose up -d --build product-service product-service-v2
curl -s http://localhost:8081/api/products
curl -s http://localhost:8083/api/products
```

两个进程使用相同服务名注册，但实例元数据中的版本不同。调用方因此可以继续使用稳定的逻辑服务名，同时由规则选择具体实例。

## 第 3 步：按服务名调用

```bash
docker compose up -d --build order-service
curl -s 'http://localhost:8082/api/orders?productId=1'
bash verify.sh --tc 2
```

`order-service` 使用带负载均衡能力的 `RestTemplate` 调用 `http://product-service`。应用代码不保存容器地址，实例变化由 Nacos 发现结果处理。

## 第 4 步：验证灰度路由

```bash
curl -s 'http://localhost:8082/api/orders?productId=1'
curl -s -H 'X-Gray-Tag: gray' 'http://localhost:8082/api/orders?productId=1'
bash verify.sh --tc 3
```

第一条请求返回端口 `8081`，第二条返回 `8083`。规则位于 `scripts/gray-routing.yaml`，由 Nacos Config 分发给 Consumer 与 Gateway。

## 第 5 步：从 Gateway 进入

```bash
docker compose up -d --build gateway
curl -s http://localhost:8080/api/products
curl -s -H 'X-Gray-Tag: gray' http://localhost:8080/api/products
bash verify.sh --tc 3G
bash verify.sh --tc 5
bash verify.sh --tc 6
```

TC-3G 验证灰度标签从 Gateway 经过订单服务继续传递到商品服务，避免只在第一跳生效。TC-5 通过突发请求验证订单测试端点限流，TC-6 验证 Gateway 的商品、订单和未知路径处理。

## 第 6 步：切换到 OpenFeign

```bash
docker compose --profile feign up -d --build
curl -s 'http://localhost:8084/api/orders-fg?productId=1'
bash verify.sh --tc 2F
```

`order-service-feign` 与 `order-service` 访问同一个逻辑服务，区别仅在客户端编程模型。FallbackFactory 将调用异常转换为稳定响应。

## 第 7 步：学习治理专题

```bash
docker compose --profile governance up -d --build
bash verify.sh --tc 4
bash verify.sh --tc 7
bash verify.sh --tc 8
bash verify.sh --tc 9
bash verify.sh --tc 10
```

建议顺序：

1. 故障模型：先确认交替错误和慢调用端点产生预期输入。
2. 重试：理解失败后是否重试、在哪里重试。
3. 实例隔离：理解何时暂时移除故障实例。
4. 舱壁：理解并发上限如何保护下游。
5. 熔断：理解 Provider 错误率、慢调用窗口和请求短路。

每个专题都有独立 Provider/Consumer，规则配置在对应治理模块的 `application.yml`，端点断言集中在 `verify.sh`。

## 第 8 步：修改并验证规则

namespace、group、共享配置加载和初始化回读的完整说明见[配置生命周期](CONFIGURATION.md)。

修改配置前先保留一个干净提交，然后编辑对应 YAML。灰度规则可重新运行初始化任务发布：

```bash
docker compose run --rm nacos-init
bash verify.sh --tc 3
```

治理规则修改后重建对应模块：

```bash
docker compose --profile governance up -d --build retry-consumer
bash verify.sh --tc 7
```

## 第 9 步：提交前检查

```bash
bash check.sh
docker compose --profile all up -d --build
bash verify.sh
docker compose --profile all down
```

`check.sh` 验证脚本 CLI 与语法、Compose profile 服务数、Nacos 环境变量、JDK 版本、编译和单元测试、Markdown 本地链接、文档范围，以及仓库中是否误提交构建产物。
