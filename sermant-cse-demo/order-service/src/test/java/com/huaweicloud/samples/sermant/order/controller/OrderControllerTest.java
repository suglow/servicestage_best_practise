package com.huaweicloud.samples.sermant.order.controller;

import com.huaweicloud.samples.sermant.common.dto.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderControllerTest {
    @Test
    void discoversProductByLogicalServiceName() {
        RestOperations restOperations = mock(RestOperations.class);
        Product product = new Product(1001L, "Sermant Product A", 29.99, "sermant-product");
        when(restOperations.getForObject("http://product-service/api/products/{id}", Product.class, 1001L))
                .thenReturn(product);

        Product result = new OrderController(restOperations).getProduct(1001L);

        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getSource()).isEqualTo("sermant-product");
    }
}
