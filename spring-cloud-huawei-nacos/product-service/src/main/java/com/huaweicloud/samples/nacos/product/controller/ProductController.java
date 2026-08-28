package com.huaweicloud.samples.nacos.product.controller;

import com.huaweicloud.samples.nacos.common.dto.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 商品服务 Controller
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Value("${server.port:8081}")
    private int port;

    @Value("${spring.cloud.nacos.discovery.metadata.version:1.0.0}")
    private String version;

    private final List<Product> products = Arrays.asList(
            new Product(1L, "iPhone 15", 6999.0, 100),
            new Product(2L, "MacBook Pro 14", 12999.0, 50),
            new Product(3L, "AirPods Pro 2", 1799.0, 200),
            new Product(4L, "Apple Watch Ultra", 5999.0, 80),
            new Product(5L, "iPad Air", 4399.0, 120)
    );

    /**
     * 获取单个商品
     */
    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        Product product = products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "product-service");
        result.put("version", version);
        result.put("port", port);
        result.put("product", product);
        return result;
    }

    /**
     * 获取所有商品列表
     */
    @GetMapping
    public Map<String, Object> listProducts() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "product-service");
        result.put("version", version);
        result.put("port", port);
        result.put("products", products);
        return result;
    }

    // ==================== 故障模型测试端点 ====================

    private final AtomicInteger errorCount = new AtomicInteger();

    /**
     * 交替返回成功/502，为治理专题提供稳定的错误输入
     */
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

    /**
     * 模拟慢调用，为治理专题提供稳定的慢调用输入
     */
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
