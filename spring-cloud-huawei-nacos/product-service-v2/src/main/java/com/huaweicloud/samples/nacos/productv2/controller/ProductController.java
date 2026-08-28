package com.huaweicloud.samples.nacos.productv2.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 商品服务 v2 Controller（灰度版本）
 * 相比 v1 新增：分类过滤、推荐商品、促销标签
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Value("${server.port:8083}")
    private int port;

    @Value("${spring.cloud.nacos.discovery.metadata.version:2.0.0}")
    private String version;

    private final List<Map<String, Object>> products = new ArrayList<>();

    public ProductController() {
        products.add(createProduct(1L, "iPhone 15", 6999.0, 100, "new", Arrays.asList("热门", "推荐")));
        products.add(createProduct(2L, "MacBook Pro 14 (M3)", 13999.0, 40, "upgrade", Arrays.asList("新品", "推荐")));
        products.add(createProduct(3L, "AirPods Pro 2 (USB-C)", 1899.0, 180, "new", Arrays.asList("热门")));
        products.add(createProduct(4L, "Apple Watch Ultra 2", 6499.0, 60, "upgrade", Arrays.asList("新品")));
        products.add(createProduct(5L, "iPad Air (M2)", 4799.0, 100, "new", Arrays.asList("推荐")));
        products.add(createProduct(6L, "Vision Pro", 29999.0, 10, "premium", Arrays.asList("新品", "高端")));
    }

    private Map<String, Object> createProduct(Long id, String name, Double price,
                                               Integer stock, String category, List<String> tags) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", id);
        p.put("name", name);
        p.put("price", price);
        p.put("stock", stock);
        p.put("category", category);
        p.put("tags", tags);
        return p;
    }

    /**
     * 获取单个商品（v2 增强返回）
     */
    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        Map<String, Object> product = products.stream()
                .filter(p -> p.get("id").equals(id))
                .findFirst()
                .orElse(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "product-service");
        result.put("version", version);
        result.put("port", port);
        result.put("product", product);
        result.put("features", Arrays.asList("categories", "tags", "recommendations"));
        return result;
    }

    /**
     * 获取所有商品，支持分类过滤（v2 新功能）
     */
    @GetMapping
    public Map<String, Object> listProducts(
            @RequestParam(required = false) String category) {

        List<Map<String, Object>> filtered = category != null
                ? products.stream()
                    .filter(p -> category.equals(p.get("category")))
                    .toList()
                : products;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "product-service");
        result.put("version", version);
        result.put("port", port);
        result.put("products", filtered);
        result.put("features", Arrays.asList("categories", "tags", "recommendations"));
        return result;
    }

    // ==================== 故障模型测试端点 ====================

    private final AtomicInteger errorCount = new AtomicInteger();

    @GetMapping("/test/error")
    public Map<String, Object> testError(HttpServletResponse response) {
        int currentCount = errorCount.incrementAndGet();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("port", port);
        result.put("version", version);
        if (currentCount % 2 == 0) {
            response.setStatus(502);
            result.put("status", "error");
            result.put("message", "模拟服务错误");
        } else {
            result.put("status", "ok");
            result.put("message", "请求正常处理");
        }
        return result;
    }

    @GetMapping("/test/slow")
    public Map<String, Object> testSlow() throws InterruptedException {
        Thread.sleep(2000);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("port", port);
        result.put("version", version);
        result.put("status", "slow");
        result.put("message", "模拟慢调用（2s）");
        return result;
    }
}
