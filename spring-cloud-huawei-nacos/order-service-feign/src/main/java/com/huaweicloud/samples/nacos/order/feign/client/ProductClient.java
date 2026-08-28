package com.huaweicloud.samples.nacos.order.feign.client;

import com.huaweicloud.samples.nacos.order.feign.fallback.ProductClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Product Client — 声明式 HTTP 客户端
 *
 * 核心机制：
 * - OpenFeign 声明式接口，运行期生成动态代理
 * - `name = "product-service"` 通过 Nacos 解析实例列表
 * - `fallbackFactory` 在调用失败时提供统一降级响应
 * - Spring Cloud Huawei 为调用注入灰度与治理能力
 *
 * 灰度路由传播过程：
 *   inbound X-Gray-Tag: gray
 *     → InvocationContextFilter（headerContextMapper 映射）→ InvocationContext
 *     → Feign 内置 SerializeContextOrderedRequestInterceptor（自动序列化 x-invocation-context）
 *     → 下游 InvocationContextFilter（反序列化）→ RouterServiceInstanceFilter（匹配 routeRule）
 *     → 灰度路由 ✅
 *
 * 调用方式：curl -H "X-Gray-Tag: gray" http://localhost:8084/api/orders-fg?productId=1
 */
@FeignClient(
    name = "product-service",
    fallbackFactory = ProductClientFallbackFactory.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    Map<String, Object> getProduct(@PathVariable("id") Long id);

    @GetMapping("/api/products")
    Map<String, Object> listProducts();

}
