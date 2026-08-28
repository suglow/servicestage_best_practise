package com.huaweicloud.samples.sermant.product.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductControllerTest {
    private final ProductController controller = new ProductController();

    @Test
    void returnsKnownProduct() {
        assertThat(controller.getProduct(1001L).getSource()).isEqualTo("sermant-product");
        assertThat(controller.getProduct(1001L).getPrice()).isEqualTo(29.99);
    }

    @Test
    void returnsNotFoundForUnknownProduct() {
        assertThatThrownBy(() -> controller.getProduct(9999L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
    }
}
