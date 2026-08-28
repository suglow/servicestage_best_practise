package com.huaweicloud.samples.nacos.bulkhead.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 舱壁隔离 Consumer — 并发测试端点
 */
@RestController
public class BulkheadConsumerController {

    private final RestTemplate restTemplate;

    public BulkheadConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ======== 场景1：maxWaitDuration=0 → 并发超限直接拒绝 ========
    @GetMapping("/bulk-rate-limiting")
    public boolean testBulkRateLimiting() throws InterruptedException {
        ConcurrentResult result = runConcurrent("/bulk-rate-limiting");
        return result.completed() && result.successes() == 2 && result.failures() == 8;
    }

    // ======== 场景2：maxWaitDuration=10000 → 排队等待全部通过 ========
    @GetMapping("/bulk-no-rate-limiting")
    public boolean testBulkNoRateLimiting() throws InterruptedException {
        ConcurrentResult result = runConcurrent("/bulk-no-rate-limiting");
        return result.completed() && result.successes() == 10 && result.failures() == 0;
    }

    // ======== 场景3：serviceName 限定 → 只对指定 Provider 生效 ========
    @GetMapping("/bulk-service-name")
    public boolean testBulkServiceName() throws InterruptedException {
        ConcurrentResult result = runConcurrent("/bulk-service-name");
        return result.completed() && result.successes() == 2 && result.failures() == 8;
    }

    private ConcurrentResult runConcurrent(String path) throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(10);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(10);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        restTemplate.getForObject("http://bulkhead-provider" + path, String.class);
                        successes.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        failures.incrementAndGet();
                    } catch (Exception e) {
                        failures.incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }
            boolean allReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            boolean completed = done.await(20, TimeUnit.SECONDS);
            return new ConcurrentResult(
                    successes.get(), failures.get(), allReady && completed);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private record ConcurrentResult(int successes, int failures, boolean completed) {
    }
}
