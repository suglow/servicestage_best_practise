package com.huaweicloud.samples.nacos.order.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    @Test
    void returnsFallbackWhenProductCallFails() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(
                eq("http://product-service/api/products/1"),
                eq(HttpMethod.GET),
                eq(HttpEntity.EMPTY),
                org.mockito.ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenThrow(new RestClientException("connection refused"));

        Map<String, Object> result = new ProductService(restTemplate).getProductWithFallback(1L);

        assertThat(result).containsEntry("fallback", true)
                .containsEntry("productId", 1L);
    }
}
