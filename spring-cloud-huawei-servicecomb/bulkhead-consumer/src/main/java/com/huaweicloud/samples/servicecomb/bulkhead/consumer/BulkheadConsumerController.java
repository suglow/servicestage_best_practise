package com.huaweicloud.samples.servicecomb.bulkhead.consumer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
public class BulkheadConsumerController {

    private final RestTemplate restTemplate;

    public BulkheadConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/bulk-rate-limiting")
    public Map<String, Object> testBulkRateLimiting() throws Exception {
        return runConcurrent("/bulk-rate-limiting", 10);
    }

    @RequestMapping("/bulk-no-rate-limiting")
    public Map<String, Object> testBulkNoRateLimiting() throws Exception {
        return runConcurrent("/bulk-no-rate-limiting", 10);
    }

    @RequestMapping("/bulk-service-name")
    public Map<String, Object> testBulkServiceName() throws Exception {
        return runConcurrent("/bulk-service-name", 10);
    }

    private Map<String, Object> runConcurrent(String path, int calls) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(calls);
        try {
            CountDownLatch ready = new CountDownLatch(calls);
            CountDownLatch startGate = new CountDownLatch(1);
            @SuppressWarnings("unchecked")
            Future<Boolean>[] futures = new Future[calls];

            for (int i = 0; i < calls; i++) {
                futures[i] = executor.submit(() -> {
                    ready.countDown();
                    startGate.await();
                    try {
                        restTemplate.getForObject("http://bulkhead-provider" + path, String.class);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
            }

            ready.await();
            long startedAt = System.currentTimeMillis();
            startGate.countDown();
            int successCount = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successCount++;
                }
            }
            long duration = System.currentTimeMillis() - startedAt;

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("path", path);
            result.put("requested", calls);
            result.put("success", successCount);
            result.put("rejected", calls - successCount);
            result.put("durationMs", duration);
            return result;
        } finally {
            executor.shutdownNow();
        }
    }
}
