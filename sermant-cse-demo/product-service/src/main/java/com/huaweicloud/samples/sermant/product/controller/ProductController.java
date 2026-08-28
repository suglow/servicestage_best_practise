package com.huaweicloud.samples.sermant.product.controller;

import com.huaweicloud.samples.sermant.common.dto.Product;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Map<Long, Product> PRODUCTS;

    static {
        Map<Long, Product> products = new LinkedHashMap<>();
        products.put(1001L, new Product(1001L, "Sermant Product A", 29.99, "sermant-product"));
        products.put(1002L, new Product(1002L, "Sermant Product B", 39.99, "sermant-product"));
        products.put(1003L, new Product(1003L, "Sermant Product C", 49.99, "sermant-product"));
        PRODUCTS = Collections.unmodifiableMap(products);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        Product product = PRODUCTS.get(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id);
        }
        return product;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong from sermant-product";
    }
}
