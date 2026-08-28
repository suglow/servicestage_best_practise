package com.huaweicloud.samples.servicecomb.order.feign;

import com.huaweicloud.samples.servicecomb.order.feign.fallback.ProductClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
    name = "product-service",
    fallbackFactory = ProductClientFallbackFactory.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    Map<String, Object> getProduct(@PathVariable("id") Long id);

    @GetMapping("/api/products")
    Map<String, Object> listProducts();

    @GetMapping("/api/products/test/error")
    Map<String, Object> testError();

    @GetMapping("/api/products/test/slow")
    Map<String, Object> testSlow();

    @GetMapping("/api/products/version")
    Map<String, Object> getVersion();

    @GetMapping("/api/products/info")
    Map<String, Object> getInfo();
}
