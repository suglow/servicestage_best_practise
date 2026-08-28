package com.huaweicloud.samples.nacos.order.feign.controller;

import com.huaweicloud.samples.nacos.common.dto.Order;
import com.huaweicloud.samples.nacos.order.feign.client.ProductClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 订单服务 Controller（Feign 版）
 *
 * 通过 @FeignClient 调用 product-service，并使用统一的路由与治理规则。
 *
 * 灰度标签传播过程：
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  inbound X-Gray-Tag: gray                                       │
 * │    → InvocationContextFilter（headerContextMapper 映射）          │
 * │    → InvocationContext{ gray-tag=gray }                         │
 * │    → Feign 内置 SerializeContextOrderedRequestInterceptor        │
 * │    → x-invocation-context header（自动序列化）                    │
 * │    → 下游 InvocationContextFilter 反序列化                        │
 * │    → RouterServiceInstanceFilter 匹配 routeRule                  │
 * │    → 灰度路由 ✅                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 调用方式：curl -H "X-Gray-Tag: gray" http://localhost:8084/api/orders-fg?productId=1
 */
@RestController
@RequestMapping("/api/orders-fg")
public class OrderFeignController {

    private final ProductClient productClient;

    public OrderFeignController(ProductClient productClient) {
        this.productClient = productClient;
    }

    /**
     * 创建订单 — 灰度路由通过 inbound X-Gray-Tag header 触发
     */
    @GetMapping
    public Map<String, Object> createOrder(@RequestParam Long productId) {
        Map<String, Object> productInfo;
        try {
            productInfo = productClient.getProduct(productId);
        } catch (Exception e) {
            productInfo = new LinkedHashMap<>();
            productInfo.put("fallback", true);
            productInfo.put("error", e.getMessage());
        }

        Order order = new Order();
        order.setProductId(productId);

        if (productInfo.get("product") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> prod = (Map<String, Object>) productInfo.get("product");
            order.setProductName((String) prod.get("name"));
            if (prod.get("price") instanceof Number) {
                order.setAmount(((Number) prod.get("price")).doubleValue());
            }
        }

        if (productInfo.get("port") != null) {
            order.setServicePort(String.valueOf(productInfo.get("port")));
        }
        if (productInfo.get("version") != null) {
            order.setServiceVersion((String) productInfo.get("version"));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);
        result.put("productInfo", productInfo);
        return result;
    }

    @GetMapping("/products")
    public Map<String, Object> listProducts() {
        return productClient.listProducts();
    }

    @GetMapping("/test/rate-limit")
    public Map<String, Object> testRateLimit() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("message", "请求正常处理 (Feign版)");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
