package com.huaweicloud.samples.nacos.order.feign.fallback;

import com.huaweicloud.samples.nacos.order.feign.client.ProductClient;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductClientFallbackFactoryTest {

    @Test
    void includesRequestedProductInFallback() {
        ProductClient fallback = new ProductClientFallbackFactory()
                .create(new IllegalStateException("connection refused"));

        Map<String, Object> result = fallback.getProduct(1L);

        assertThat(result).containsEntry("fallback", true)
                .containsEntry("productId", 1L);
    }

    @Test
    void handlesRegularAndMissingCauses() {
        ProductClientFallbackFactory factory = new ProductClientFallbackFactory();

        assertThat(factory.create(new IllegalStateException("connection refused")).listProducts())
                .containsEntry("fallback", true);
        assertThat(factory.create(null).listProducts())
                .containsEntry("fallback", true);
    }
}
