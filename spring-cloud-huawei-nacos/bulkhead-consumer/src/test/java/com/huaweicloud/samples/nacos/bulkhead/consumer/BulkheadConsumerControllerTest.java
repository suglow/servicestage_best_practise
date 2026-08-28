package com.huaweicloud.samples.nacos.bulkhead.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class BulkheadConsumerControllerTest {

    @Test
    void reportsSuccessfulConcurrentCalls() throws Exception {
        RestTemplate restTemplate = new RestTemplate() {
            @Override
            public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
                return responseType.cast("ok");
            }
        };

        assertThat(new BulkheadConsumerController(restTemplate).testBulkNoRateLimiting()).isTrue();
    }

    @Test
    void reportsExactLimitedDistribution() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        RestTemplate restTemplate = new RestTemplate() {
            @Override
            public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
                if (calls.incrementAndGet() > 2) {
                    throw new RestClientException("rejected");
                }
                return responseType.cast("ok");
            }
        };

        assertThat(new BulkheadConsumerController(restTemplate).testBulkRateLimiting()).isTrue();
    }
}
