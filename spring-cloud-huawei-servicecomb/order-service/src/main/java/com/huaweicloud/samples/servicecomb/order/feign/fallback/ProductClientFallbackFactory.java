package com.huaweicloud.samples.servicecomb.order.feign.fallback;

import com.huaweicloud.samples.servicecomb.order.feign.ProductClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallbackFactory.class);

    @Override
    public ProductClient create(Throwable cause) {
        log.warn("ProductClient fallback triggered, cause: {}", cause.getMessage());

        boolean circuitOpen = cause.getMessage() != null
                && cause.getMessage().contains("circuitBreaker");

        return new ProductClient() {

            @Override
            public Map<String, Object> getProduct(Long id) {
                return buildFallback(id, circuitOpen);
            }

            @Override
            public Map<String, Object> listProducts() {
                return buildFallback(null, circuitOpen);
            }

            @Override
            public Map<String, Object> testError() {
                return buildFallback(null, circuitOpen);
            }

            @Override
            public Map<String, Object> testSlow() {
                return buildFallback(null, circuitOpen);
            }

            @Override
            public Map<String, Object> getVersion() {
                return buildFallback(null, circuitOpen);
            }

            @Override
            public Map<String, Object> getInfo() {
                return buildFallback(null, circuitOpen);
            }
        };
    }

    private Map<String, Object> buildFallback(Long productId, boolean circuitOpen) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "order-service (fallback)");
        result.put("fallback", true);
        result.put("circuitBreakerOpen", circuitOpen);
        if (productId != null) {
            result.put("productId", productId);
        }
        result.put("message", circuitOpen
                ? "商品服务熔断已开启，请稍后重试"
                : "商品服务暂时不可用，请稍后重试");
        return result;
    }
}
