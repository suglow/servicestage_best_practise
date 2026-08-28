package com.huaweicloud.samples.nacos.circuit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 熔断场景驱动器。响应同时给出业务结果和 Provider 实际调用次数。
 */
@RestController
@RequestMapping("/api/consumer/circuit")
public class CircuitConsumerController {

    private static final String PROVIDER = "http://circuit-breaker-provider/api/circuit";
    private final RestTemplate restTemplate;

    public CircuitConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/normal")
    public Map<String, Object> normal() {
        resetCounts();
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            result.put("response", restTemplate.getForObject(PROVIDER + "/normal", String.class));
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        result.put("providerCalls", callCount("normal"));
        result.put("circuitOpen", false);
        return result;
    }

    @GetMapping("/error-rate")
    public Map<String, Object> errorRate() throws InterruptedException {
        return runScenario(
                "error-rate",
                "/error-rate?simulateFailure=false",
                "/error-rate?simulateFailure=true",
                12);
    }

    @GetMapping("/slow-call")
    public Map<String, Object> slowCall() throws InterruptedException {
        return runScenario(
                "slow-call",
                "/slow-call?delayMs=0",
                "/slow-call?delayMs=150",
                6);
    }

    private Map<String, Object> runScenario(
            String scenario, String preparePath, String testPath, int attempts)
            throws InterruptedException {
        boolean preparationReady = prepareRule(preparePath);
        resetCounts();

        int successes = 0;
        for (int i = 0; i < attempts; i++) {
            try {
                restTemplate.getForObject(PROVIDER + testPath, String.class);
                successes++;
            } catch (Exception ignored) {
                // Provider 错误和熔断拒绝将在下方通过实际调用次数区分。
            }
        }

        int providerCalls = callCount(scenario);
        int rejectedCalls = attempts - providerCalls;
        int providerErrors = providerCalls - successes;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", scenario);
        result.put("preparationReady", preparationReady);
        result.put("attempts", attempts);
        result.put("successes", successes);
        result.put("providerErrors", providerErrors);
        result.put("rejectedCalls", rejectedCalls);
        result.put("providerCalls", providerCalls);
        result.put("circuitOpen", preparationReady && rejectedCalls > 0);
        return result;
    }

    private boolean prepareRule(String path) throws InterruptedException {
        Thread.sleep(5100);
        int consecutiveSuccesses = 0;
        int attempts = 0;
        while (consecutiveSuccesses < 10 && attempts < 30) {
            attempts++;
            try {
                restTemplate.getForObject(PROVIDER + path, String.class);
                consecutiveSuccesses++;
            } catch (Exception e) {
                consecutiveSuccesses = 0;
                Thread.sleep(100);
            }
        }
        return consecutiveSuccesses == 10;
    }

    private void resetCounts() {
        restTemplate.postForObject(PROVIDER + "/test-state/reset", null, Void.class);
    }

    private int callCount(String scenario) {
        Integer count = restTemplate.getForObject(
                PROVIDER + "/test-state/call-count?scenario=" + scenario, Integer.class);
        return count == null ? -1 : count;
    }
}
