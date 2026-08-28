package com.huaweicloud.samples.nacos.order.controller;

import com.huaweicloud.samples.nacos.common.dto.Order;
import com.huaweicloud.samples.nacos.order.service.ProductService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    @Test
    void createsOrderFromProductResponse() {
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("name", "Demo Product");
        product.put("price", 99.5);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("product", product);
        response.put("port", 8081);
        response.put("version", "1.0.0");

        ProductService productService = mock(ProductService.class);
        when(productService.getProductWithFallback(1L)).thenReturn(response);

        Map<String, Object> result = new OrderController(productService).createOrder(1L);
        Order order = (Order) result.get("order");

        assertThat(order.getProductId()).isEqualTo(1L);
        assertThat(order.getProductName()).isEqualTo("Demo Product");
        assertThat(order.getAmount()).isEqualTo(99.5);
        assertThat(order.getServicePort()).isEqualTo("8081");
        assertThat(order.getServiceVersion()).isEqualTo("1.0.0");
    }
}
