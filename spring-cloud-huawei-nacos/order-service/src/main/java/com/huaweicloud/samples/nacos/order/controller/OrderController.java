package com.huaweicloud.samples.nacos.order.controller;

import com.huaweicloud.samples.nacos.common.dto.Order;
import com.huaweicloud.samples.nacos.order.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 订单服务 Controller
 * 通过 RestTemplate 调用 product-service
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final ProductService productService;

    public OrderController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 创建订单 — 查询商品信息后生成订单
     */
    @GetMapping
    public Map<String, Object> createOrder(@RequestParam Long productId) {
        Map<String, Object> productInfo = productService.getProductWithFallback(productId);

        Order order = new Order();
        order.setProductId(productId);

        // 从 product-service 返回中提取商品信息（可能是 fallback 降级结果）
        if (productInfo.get("product") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> prod = (Map<String, Object>) productInfo.get("product");
            order.setProductName((String) prod.get("name"));
            if (prod.get("price") instanceof Number) {
                order.setAmount(((Number) prod.get("price")).doubleValue());
            }
        }

        // 记录调用的服务实例信息
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

    /**
     * 查看商品列表（透传 RestTemplate 调用）
     */
    @GetMapping("/products")
    public Map<String, Object> listProducts() {
        return productService.listProductsWithFallback();
    }

    // ==================== 灰度路由测试 ====================

    /**
     * 灰度路由测试 — 带 X-Gray-Tag header
     * 参考官方 canary-sample/canaryFallback() 模式
     *
     * 灰度粒度：服务实例级别（通过 routeRule 匹配 X-Gray-Tag header）
     */
    @GetMapping("/gray")
    public Map<String, Object> testGrayRouting(@RequestParam Long productId,
                                                @RequestParam(defaultValue = "gray") String grayTag) {
        Map<String, Object> productInfo = productService.getProductWithGray(productId, grayTag);

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
        result.put("grayTag", grayTag);
        return result;
    }

    /**
     * 限流测试 — 高频调用触发限流规则
     */
    @GetMapping("/test/rate-limit")
    public Map<String, Object> testRateLimit() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("message", "请求正常处理");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
