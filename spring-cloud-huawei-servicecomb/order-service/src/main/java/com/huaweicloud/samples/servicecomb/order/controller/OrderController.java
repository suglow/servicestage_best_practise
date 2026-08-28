package com.huaweicloud.samples.servicecomb.order.controller;

import com.huaweicloud.samples.servicecomb.common.dto.Order;
import com.huaweicloud.samples.servicecomb.order.feign.ProductClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final ProductClient productClient;

    public OrderController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Value("${server.port:8082}")
    private int port;

    @Value("${spring.cloud.servicecomb.service.version:1.0.0}")
    private String version;

    @GetMapping
    public Map<String, Object> createOrder(@RequestParam Long productId) {
        Map<String, Object> productInfo = productClient.getProduct(productId);

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

    @GetMapping("/test/error")
    public Map<String, Object> testCircuitBreaker() {
        return productClient.testError();
    }

    @GetMapping("/test/slow")
    public Map<String, Object> testSlow() {
        return productClient.testSlow();
    }

    @GetMapping("/test/rate-limit")
    public Map<String, Object> testRateLimit() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("message", "请求正常处理");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 灰度路由版本诊断
     * 用于灰度测试用例（TC-01~TC-10）
     * 返回当前 order-service 的版本和端口信息
     */
    @GetMapping("/version")
    public Map<String, Object> version() {
        Map<String, Object> result = productClient.getVersion();
        result.put("orderService", "order-service");
        result.put("orderPort", port);
        result.put("orderVersion", version);
        return result;
    }

    /**
     * 灰度标签透传诊断
     * 返回当前请求携带的灰度标签（从 InvocationContext 获取）
     * 用于 TC-06 标签透传验证
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = productClient.getInfo();
        result.put("orderService", "order-service");
        result.put("orderPort", port);
        result.put("orderVersion", version);
        return result;
    }

    /**
     * 灰度标签接收验证端点
     * 用于 TC-06：验证灰度标签透传到下游服务
     * 返回当前 order-service 的版本信息
     */
    @GetMapping("/echo")
    public Map<String, Object> echo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "order-service");
        result.put("version", version);
        result.put("port", port);
        result.put("status", "received");
        return result;
    }
}
