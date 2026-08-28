package com.huaweicloud.samples.servicecomb.circuitbreaker.provider;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

/**
 * Consumer 侧熔断演示 — Provider 端
 *
 * 提供多个测试端点：
 * 1. /api/circuit/normal    — 正常返回 200
 * 2. /api/circuit/error50   — 50% 错误率（50% 返回 502）
 * 3. /api/circuit/error100  — 100% 错误率（全部返回 502）
 * 4. /api/circuit/slow      — 每次耗时 1.5s（模拟慢调用）
 * 5. /api/circuit/always-ok — 永远返回 200（验证 Consumer 熔断打开后不影响正常路径）
 */
@RestController
@RequestMapping("/api/circuit")
public class CircuitBreakerProviderController {

    private final AtomicInteger normalCalls = new AtomicInteger();
    private final AtomicInteger error50Calls = new AtomicInteger();
    private final AtomicInteger error100Calls = new AtomicInteger();
    private final AtomicInteger slowCalls = new AtomicInteger();
    private final AtomicInteger alwaysOkCalls = new AtomicInteger();

    /**
     * 正常接口 — Consumer 熔断打开后，调用此接口验证是否正常
     */
    @GetMapping("/normal")
    public String normal() {
        return "OK-normal-" + normalCalls.incrementAndGet();
    }

    /**
     * 50% 错误率 — 触发 Consumer 侧熔断
     * Consumer 端看到 50% 错误率，超过配置的 failureRateThreshold=50 则熔断打开
     */
    @GetMapping("/error50")
    public String error50(HttpServletResponse response) {
        int count = error50Calls.incrementAndGet();
        if (count % 2 == 0) {
            response.setStatus(502);
            return "ERROR-502";
        }
        return "OK-" + count;
    }

    /**
     * 100% 错误率 — 用于验证熔断打开后直接降级，不发请求到 Provider
     */
    @GetMapping("/error100")
    public String error100(HttpServletResponse response) {
        error100Calls.incrementAndGet();
        response.setStatus(502);
        return "ERROR-100";
    }

    /**
     * 慢调用 — 每次耗时 1.5s，触发 Consumer 侧慢调用熔断
     */
    @GetMapping("/slow")
    public String slow() throws InterruptedException {
        int count = slowCalls.incrementAndGet();
        Thread.sleep(1500);
        return "OK-slow-" + count;
    }

    /**
     * 永远正常 — 熔断打开后，验证降级逻辑
     */
    @GetMapping("/always-ok")
    public String alwaysOk() {
        return "ALWAYS-OK-" + alwaysOkCalls.incrementAndGet();
    }

    @GetMapping("/stats")
    public Map<String, Integer> stats() {
        return Map.of(
            "normal", normalCalls.get(),
            "error50", error50Calls.get(),
            "error100", error100Calls.get(),
            "slow", slowCalls.get(),
            "alwaysOk", alwaysOkCalls.get());
    }

    @PostMapping("/reset")
    public Map<String, Integer> reset() {
        normalCalls.set(0);
        error50Calls.set(0);
        error100Calls.set(0);
        slowCalls.set(0);
        alwaysOkCalls.set(0);
        return stats();
    }
}
