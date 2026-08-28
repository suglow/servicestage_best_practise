package com.huaweicloud.samples.nacos.retry.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class RetryProviderController {

    @Value("${server.port}")
    private int port;

    private final AtomicInteger noRetryCount = new AtomicInteger();
    private final AtomicInteger retryCount = new AtomicInteger();
    private final AtomicInteger status500Count = new AtomicInteger();
    private final AtomicInteger status400Count = new AtomicInteger();
    private final AtomicInteger serviceNameCount = new AtomicInteger();
    private final AtomicInteger sameOneCount = new AtomicInteger();
    private final AtomicInteger sameTwoCount = new AtomicInteger();

    /** 无重试对比 — 交替成功/失败 */
    @GetMapping("/no-retry")
    public String noRetry() {
        if (noRetryCount.incrementAndGet() % 2 == 0) return "success from port=" + port;
        throw new RuntimeException("simulated error");
    }

    /** 基础重试目标 */
    @GetMapping("/test-retry")
    public String testRetry() {
        if (retryCount.incrementAndGet() % 2 == 0) return "success from port=" + port;
        throw new RuntimeException("simulated error");
    }

    /** 按状态码重试 — 首次返回指定状态码，下一次恢复 */
    @GetMapping("/status-retry")
    public String statusRetry(@RequestParam("status") int status, HttpServletResponse response) {
        AtomicInteger counter = status == 500 ? status500Count : status400Count;
        if (counter.incrementAndGet() % 2 == 0) {
            return "success from port=" + port;
        }
        response.setStatus(status);
        return "status=" + status + " from port=" + port;
    }

    /** 服务名限定重试 */
    @GetMapping("/retry-service-name")
    public String retryServiceName() {
        if (serviceNameCount.incrementAndGet() % 2 == 0) return "success from port=" + port;
        throw new RuntimeException("simulated error");
    }

    /** 同实例重试1次 */
    @GetMapping("/on-same-retry-one")
    public String onSameRetryOne() {
        if (sameOneCount.incrementAndGet() % 2 == 0) return "success from port=" + port;
        throw new RuntimeException("simulated error");
    }

    /** 跨实例重试2次 */
    @GetMapping("/on-same-retry-two")
    public String onSameRetryTwo() {
        if (sameTwoCount.incrementAndGet() % 2 == 0) return "success from port=" + port;
        throw new RuntimeException("simulated error");
    }
}
