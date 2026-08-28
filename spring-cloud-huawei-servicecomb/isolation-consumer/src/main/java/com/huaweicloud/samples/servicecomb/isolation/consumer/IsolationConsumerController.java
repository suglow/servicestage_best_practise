package com.huaweicloud.samples.servicecomb.isolation.consumer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class IsolationConsumerController {

    private final RestTemplate restTemplate;

    public IsolationConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/test-minimum-calls")
    public String testMinimumCalls() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://isolation-provider/minimum-calls", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 5 && failCount == 5;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/test-failed-percent")
    public String testFailedPercent() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://isolation-provider/failed-percent?thresholds=5", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 5 && failCount == 5;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/test-slow-call")
    public String testSlowCall() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://isolation-provider/slow-call?deferPeriod=5", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 5 && failCount == 5;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/test-force-closed")
    public String testForceClosed() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://isolation-provider/force-closed", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 5 && failCount == 5;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/test-force-open")
    public String testForceOpen() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://isolation-provider/force-open", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 10 && failCount == 0;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/test-force-open-service")
    public String testForceOpenService() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://isolation-provider/force-open-service", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 10 && failCount == 0;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/test-error-code")
    public String testErrorCode() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://isolation-provider/error-code?code=500", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 10 && failCount == 0;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/test-reset-count")
    public String testResetCount() {
        try {
            restTemplate.getForObject("http://isolation-provider/resetCount", String.class);
            return "请求结果符合策略要求";
        } catch (Exception e) {
            return "请求结果不符合策略要求";
        }
    }

    @RequestMapping("/test-combined")
    public String testCombined() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 20; i++) {
            try {
                restTemplate.getForObject("http://isolation-provider/minimum-calls", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 10 && failCount == 10;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/test-error-code-400")
    public String testErrorCode400() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://isolation-provider/error-code?code=400", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 10 && failCount == 0;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }
}
