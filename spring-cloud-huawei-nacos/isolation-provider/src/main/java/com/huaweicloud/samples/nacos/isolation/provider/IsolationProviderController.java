package com.huaweicloud.samples.nacos.isolation.provider;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实例隔离 Provider：为每种场景维护独立、可观测的调用计数。
 */
@RestController
public class IsolationProviderController {

    private static final int WINDOW_SIZE = 10;

    @Value("${server.port}")
    private int port;

    private final AtomicInteger minimumCalls = new AtomicInteger();
    private final AtomicInteger failedPercentCalls = new AtomicInteger();
    private final AtomicInteger slowCalls = new AtomicInteger();
    private final AtomicInteger forceClosedCalls = new AtomicInteger();
    private final AtomicInteger forceOpenCalls = new AtomicInteger();
    private final AtomicInteger forceOpenServiceCalls = new AtomicInteger();
    private final AtomicInteger errorCodeCalls = new AtomicInteger();

    @PostMapping("/test-state/reset")
    public String resetCounts() {
        minimumCalls.set(0);
        failedPercentCalls.set(0);
        slowCalls.set(0);
        forceClosedCalls.set(0);
        forceOpenCalls.set(0);
        forceOpenServiceCalls.set(0);
        errorCodeCalls.set(0);
        return "counts reset, port=" + port;
    }

    @GetMapping("/test-state/call-count")
    public int callCount(@RequestParam("scenario") String scenario) {
        return switch (scenario) {
            case "minimum-calls" -> minimumCalls.get();
            case "failed-percent" -> failedPercentCalls.get();
            case "slow-call" -> slowCalls.get();
            case "force-closed" -> forceClosedCalls.get();
            case "force-open" -> forceOpenCalls.get();
            case "force-open-service" -> forceOpenServiceCalls.get();
            case "error-code" -> errorCodeCalls.get();
            default -> throw new IllegalArgumentException("unknown scenario: " + scenario);
        };
    }

    @GetMapping("/minimum-calls")
    public String minimumCalls(@RequestParam(defaultValue = "false") boolean healthy) {
        int count = minimumCalls.incrementAndGet();
        if (!healthy && count % 2 == 0) {
            throw new RuntimeException("simulated error");
        }
        return success();
    }

    @GetMapping("/failed-percent")
    public String failedPercent(@RequestParam int failuresPerWindow) {
        int count = failedPercentCalls.incrementAndGet();
        int position = (count - 1) % WINDOW_SIZE;
        if (position < failuresPerWindow) {
            throw new RuntimeException("simulated error " + count);
        }
        return success();
    }

    @GetMapping("/slow-call")
    public String slowCall(@RequestParam int slowCallsPerWindow) throws InterruptedException {
        int count = slowCalls.incrementAndGet();
        int position = (count - 1) % WINDOW_SIZE;
        if (position < slowCallsPerWindow) {
            Thread.sleep(200);
        }
        return success();
    }

    @GetMapping("/force-closed")
    public String forceClosed() {
        int count = forceClosedCalls.incrementAndGet();
        if (count % 2 == 0) {
            throw new RuntimeException("simulated error");
        }
        return success();
    }

    @GetMapping("/force-open")
    public String forceOpen() {
        forceOpenCalls.incrementAndGet();
        return success();
    }

    @GetMapping("/force-open-service")
    public String forceOpenService() {
        forceOpenServiceCalls.incrementAndGet();
        return success();
    }

    @GetMapping("/error-code")
    public String errorCode(@RequestParam int code, HttpServletResponse response) {
        errorCodeCalls.incrementAndGet();
        response.setHeader("X-HTTP-STATUS-CODE", String.valueOf(code));
        response.setStatus(code);
        return "status=" + code + " from port=" + port;
    }

    private String success() {
        return "success from port=" + port;
    }
}
