package com.huaweicloud.samples.servicecomb.product.v2.controller;

import com.huaweicloud.samples.servicecomb.common.dto.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductV2Controller {

    @Value("${server.port:8083}")
    private int port;

    @Value("${spring.cloud.servicecomb.service.version:2.0.0}")
    private String version;

    private final List<Product> products = Arrays.asList(
            new Product(1L, "iPhone 15", 6999.0, 100),
            new Product(2L, "MacBook Pro 14", 12999.0, 50),
            new Product(3L, "AirPods Pro 2", 1799.0, 200),
            new Product(4L, "Apple Watch Ultra", 5999.0, 80),
            new Product(5L, "iPad Air", 4399.0, 120)
    );

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

    @GetMapping
    public Map<String, Object> listProducts() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "product-service");
        result.put("version", version);
        result.put("port", port);
        result.put("products", products);
        return result;
    }

    private int errorCount = 0;

    @GetMapping("/test/error")
    public Map<String, Object> testError(HttpServletResponse response) {
        errorCount++;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("port", port);
        result.put("version", version);
        if (errorCount % 2 == 0) {
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

    @GetMapping("/version")
    public Map<String, Object> getVersion() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "product-service");
        result.put("version", version);
        result.put("port", port);
        return result;
    }

    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "product-service");
        result.put("version", version);
        result.put("port", port);
        return result;
    }
}
