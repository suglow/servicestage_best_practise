package com.huaweicloud.samples.sermant.order.controller;

import com.huaweicloud.samples.sermant.common.dto.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final RestOperations restOperations;

    public OrderController(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @GetMapping("/products/{productId}")
    public Product getProduct(@PathVariable Long productId) {
        return restOperations.getForObject(
                "http://product-service/api/products/{id}", Product.class, productId);
    }

    @GetMapping("/ping")
    public String ping() {
        return restOperations.getForObject("http://product-service/api/products/ping", String.class);
    }
}
