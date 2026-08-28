package com.huaweicloud.samples.nacos.order.feign.fallback;

import com.huaweicloud.samples.nacos.order.feign.client.ProductClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ProductClient 降级工厂
 *
 * Feign 调用失败时构造统一降级结果。
 */
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallbackFactory.class);

    @Override
    public ProductClient create(Throwable cause) {
        String message = cause == null ? null : cause.getMessage();
        log.warn("ProductClient 降级触发: {}", message);

        return new ProductClient() {
            @Override
            public Map<String, Object> getProduct(Long id) {
                return buildFallback(id);
            }

            @Override
            public Map<String, Object> listProducts() {
                return buildFallback(null);
            }

            private Map<String, Object> buildFallback(Long productId) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("service", "order-service-feign (fallback)");
                result.put("fallback", true);
                if (productId != null) {
                    result.put("productId", productId);
                }
                result.put("message", "商品服务暂时不可用，请稍后重试 (Feign版)");
                return result;
            }
        };
    }
}
