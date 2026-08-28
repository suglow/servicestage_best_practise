package com.huaweicloud.samples.servicecomb.order.controller;

import com.huaweicloud.samples.servicecomb.common.dto.Order;
import com.huaweicloud.samples.servicecomb.order.feign.ProductClient;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
        ProductClient productClient = new StubProductClient(response);

        OrderController controller = new OrderController(productClient);
        Map<String, Object> result = controller.createOrder(1L);

        Order order = (Order) result.get("order");
        assertThat(order.getProductId()).isEqualTo(1L);
        assertThat(order.getProductName()).isEqualTo("Demo Product");
        assertThat(order.getAmount()).isEqualTo(99.5);
        assertThat(order.getServicePort()).isEqualTo("8081");
        assertThat(order.getServiceVersion()).isEqualTo("1.0.0");
    }

    private record StubProductClient(Map<String, Object> response) implements ProductClient {

        @Override
        public Map<String, Object> getProduct(Long id) {
            return response;
        }

        @Override
        public Map<String, Object> listProducts() {
            return response;
        }

        @Override
        public Map<String, Object> testError() {
            return response;
        }

        @Override
        public Map<String, Object> testSlow() {
            return response;
        }

        @Override
        public Map<String, Object> getVersion() {
            return response;
        }

        @Override
        public Map<String, Object> getInfo() {
            return response;
        }
    }
}
