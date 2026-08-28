package com.huaweicloud.samples.servicecomb.order.feign;

import com.huaweicloud.samples.servicecomb.order.feign.fallback.ProductClientFallbackFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductClientFallbackFactoryTest {

    @Test
    void marksCircuitBreakerFallbackWhenCauseIdentifiesOpenCircuit() {
        ProductClient fallback = new ProductClientFallbackFactory()
                .create(new IllegalStateException("circuitBreaker is open"));

        Map<String, Object> result = fallback.getProduct(1L);

        assertThat(result).containsEntry("fallback", true)
                .containsEntry("circuitBreakerOpen", true)
                .containsEntry("productId", 1L);
    }

    @Test
    void distinguishesRegularProviderFailure() {
        ProductClient fallback = new ProductClientFallbackFactory()
                .create(new IllegalStateException("connection refused"));

        assertThat(fallback.listProducts()).containsEntry("fallback", true)
                .containsEntry("circuitBreakerOpen", false);
    }
}
