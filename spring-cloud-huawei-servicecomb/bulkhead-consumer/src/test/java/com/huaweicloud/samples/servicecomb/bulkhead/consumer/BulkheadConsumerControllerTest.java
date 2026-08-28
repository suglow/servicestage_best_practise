package com.huaweicloud.samples.servicecomb.bulkhead.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BulkheadConsumerControllerTest {

    @Test
    void aggregatesConcurrentCallResults() throws Exception {
        RestTemplate restTemplate = new RestTemplate() {
            @Override
            public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
                return responseType.cast("ok");
            }
        };

        Map<String, Object> result = new BulkheadConsumerController(restTemplate)
                .testBulkRateLimiting();

        assertThat(result).containsEntry("requested", 10)
                .containsEntry("success", 10)
                .containsEntry("rejected", 0);
    }
}
