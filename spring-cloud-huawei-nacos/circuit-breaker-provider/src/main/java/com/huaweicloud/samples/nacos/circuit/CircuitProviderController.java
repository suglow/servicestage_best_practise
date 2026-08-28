package com.huaweicloud.samples.nacos.circuit;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 受熔断规则保护的下游服务，并暴露调用计数用于验证请求是否在进入业务代码前被短路。
 */
@RestController
@RequestMapping("/api/circuit")
public class CircuitProviderController {

    private final AtomicInteger normalCalls = new AtomicInteger();
    private final AtomicInteger errorRateCalls = new AtomicInteger();
    private final AtomicInteger slowCalls = new AtomicInteger();

    @PostMapping("/test-state/reset")
    public void resetCounts() {
        normalCalls.set(0);
        errorRateCalls.set(0);
        slowCalls.set(0);
    }

    @GetMapping("/test-state/call-count")
    public int callCount(@RequestParam("scenario") String scenario) {
        return switch (scenario) {
            case "normal" -> normalCalls.get();
            case "error-rate" -> errorRateCalls.get();
            case "slow-call" -> slowCalls.get();
            default -> throw new IllegalArgumentException("unknown scenario: " + scenario);
        };
    }

    @GetMapping("/normal")
    public String normal() {
        return "OK-normal-" + normalCalls.incrementAndGet();
    }

    @GetMapping("/error-rate")
    public String errorRate(
            @RequestParam(defaultValue = "true") boolean simulateFailure,
            HttpServletResponse response) {
        int count = errorRateCalls.incrementAndGet();
        if (simulateFailure && count % 2 == 0) {
            response.setHeader("X-HTTP-STATUS-CODE", "502");
            response.setStatus(502);
            return "ERROR-502";
        }
        return "OK-" + count;
    }

    @GetMapping("/slow-call")
    public String slowCall(@RequestParam(defaultValue = "150") long delayMs)
            throws InterruptedException {
        slowCalls.incrementAndGet();
        if (delayMs > 0) {
            Thread.sleep(delayMs);
        }
        return "OK-slow";
    }
}
