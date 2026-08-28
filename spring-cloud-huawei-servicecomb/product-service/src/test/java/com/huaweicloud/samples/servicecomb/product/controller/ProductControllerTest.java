package com.huaweicloud.samples.servicecomb.product.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerTest {

    private ProductController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductController();
        ReflectionTestUtils.setField(controller, "port", 8081);
        ReflectionTestUtils.setField(controller, "version", "1.0.0");
    }

    @Test
    void returnsProductAndInstanceMetadata() {
        Map<String, Object> result = controller.getProduct(1L);

        assertThat(result).containsEntry("service", "product-service")
                .containsEntry("version", "1.0.0")
                .containsEntry("port", 8081);
        assertThat(result.get("product")).isNotNull();
    }

    @Test
    void alternatesSuccessfulAndErrorResponses() {
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        assertThat(controller.testError(firstResponse)).containsEntry("status", "ok");
        assertThat(controller.testError(secondResponse)).containsEntry("status", "error");
        assertThat(secondResponse.getStatus()).isEqualTo(502);
    }
}
