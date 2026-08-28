package com.huaweicloud.samples.nacos.isolation.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * 实例隔离 Consumer：每个端点执行一个可重复的场景并返回断言结果。
 */
@RestController
public class IsolationConsumerController {

    private static final String PROVIDER = "http://isolation-provider";

    private final RestTemplate restTemplate;

    public IsolationConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/low-minimum-calls")
    public boolean testLowMinimumCalls() throws InterruptedException {
        if (!prepareRule("/minimum-calls?healthy=true")) {
            return false;
        }
        resetCounts();
        int failures = invokeRepeatedly("/minimum-calls", 9);
        return failures == 4 && callCount("minimum-calls") == 9;
    }

    @GetMapping("/up-minimum-calls")
    public boolean testUpMinimumCalls() throws InterruptedException {
        if (!prepareRule("/minimum-calls?healthy=true")) {
            return false;
        }
        resetCounts();
        int failures = invokeRepeatedly("/minimum-calls", 30);
        int providerCalls = callCount("minimum-calls");
        return failures > 5 && providerCalls >= 10 && providerCalls < 30;
    }

    @GetMapping("/up-failed-percent")
    public boolean testUpFailedPercent() throws InterruptedException {
        if (!prepareRule("/failed-percent?failuresPerWindow=0")) {
            return false;
        }
        resetCounts();
        int failures = invokeRepeatedly("/failed-percent?failuresPerWindow=6", 30);
        int providerCalls = callCount("failed-percent");
        return providerCalls > 0 && providerCalls < 30 && failures > providerCalls;
    }

    @GetMapping("/low-failed-percent")
    public boolean testLowFailedPercent() throws InterruptedException {
        if (!prepareRule("/failed-percent?failuresPerWindow=0")) {
            return false;
        }
        resetCounts();
        int failures = invokeRepeatedly("/failed-percent?failuresPerWindow=4", 30);
        return failures == 12 && callCount("failed-percent") == 30;
    }

    @GetMapping("/low-slow-call-percent")
    public boolean testLowSlowCall() throws InterruptedException {
        if (!prepareRule("/slow-call?slowCallsPerWindow=0")) {
            return false;
        }
        resetCounts();
        int failures = invokeRepeatedly("/slow-call?slowCallsPerWindow=4", 30);
        return failures == 0 && callCount("slow-call") == 30;
    }

    @GetMapping("/up-slow-call-percent")
    public boolean testUpSlowCall() throws InterruptedException {
        if (!prepareRule("/slow-call?slowCallsPerWindow=0")) {
            return false;
        }
        resetCounts();
        int failures = invokeRepeatedly("/slow-call?slowCallsPerWindow=6", 30);
        int providerCalls = callCount("slow-call");
        return providerCalls > 0 && providerCalls < 30 && failures > 0;
    }

    @GetMapping("/force-closed")
    public boolean testForceClosed() {
        resetCounts();
        int failures = invokeRepeatedly("/force-closed", 30);
        return failures == 15 && callCount("force-closed") == 30;
    }

    @GetMapping("/force-open")
    public boolean testForceOpen() {
        resetCounts();
        int failures = invokeRepeatedly("/force-open", 30);
        return failures == 30 && callCount("force-open") == 0;
    }

    @GetMapping("/force-open-service")
    public boolean testForceOpenService() {
        resetCounts();
        int failures = invokeRepeatedly("/force-open-service", 20);
        return failures == 20 && callCount("force-open-service") == 0;
    }

    @GetMapping("/error-code-500")
    public boolean testErrorCode500() throws InterruptedException {
        if (!prepareRule("/error-code?code=200")) {
            return false;
        }
        resetCounts();
        int serverErrors = 0;
        int isolated = 0;
        for (int i = 0; i < 30; i++) {
            try {
                restTemplate.getForObject(PROVIDER + "/error-code?code=500", String.class);
            } catch (HttpServerErrorException e) {
                if (e.getStatusCode().value() == 500) {
                    serverErrors++;
                } else if (e.getStatusCode().value() == 503) {
                    isolated++;
                }
            } catch (Exception e) {
                isolated++;
            }
        }
        int providerCalls = callCount("error-code");
        return serverErrors > 0 && isolated > 0 && providerCalls < 30;
    }

    @GetMapping("/error-code-404")
    public boolean testErrorCode404() throws InterruptedException {
        if (!prepareRule("/error-code?code=200")) {
            return false;
        }
        resetCounts();
        int notFound = 0;
        int unexpected = 0;
        for (int i = 0; i < 30; i++) {
            try {
                restTemplate.getForObject(PROVIDER + "/error-code?code=404", String.class);
            } catch (HttpClientErrorException.NotFound e) {
                notFound++;
            } catch (Exception e) {
                unexpected++;
            }
        }
        return notFound == 30 && unexpected == 0 && callCount("error-code") == 30;
    }

    private boolean prepareRule(String path) throws InterruptedException {
        Thread.sleep(1100);
        int consecutiveSuccesses = 0;
        int attempts = 0;
        while (consecutiveSuccesses < 10 && attempts < 40) {
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
        restTemplate.postForObject(PROVIDER + "/test-state/reset", null, String.class);
    }

    private int callCount(String scenario) {
        Integer count = restTemplate.getForObject(
                PROVIDER + "/test-state/call-count?scenario=" + scenario, Integer.class);
        return count == null ? -1 : count;
    }

    private int invokeRepeatedly(String path, int calls) {
        int failures = 0;
        for (int i = 0; i < calls; i++) {
            try {
                restTemplate.getForObject(PROVIDER + path, String.class);
            } catch (Exception e) {
                failures++;
            }
        }
        return failures;
    }
}
