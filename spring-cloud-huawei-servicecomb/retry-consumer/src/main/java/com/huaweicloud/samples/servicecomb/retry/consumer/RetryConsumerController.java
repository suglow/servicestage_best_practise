package com.huaweicloud.samples.servicecomb.retry.consumer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class RetryConsumerController {

    private final RestTemplate restTemplate;

    public RetryConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/no-retry")
    public String testNoRetry() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://retry-provider/no-retry", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = failCount == 5 && successCount == 5;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/retry")
    public String testRetry() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://retry-provider/test-retry", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 10 && failCount == 0;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/status-retry")
    public String testStatusRetry() {
        int count500 = 0, error500 = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://retry-provider/status-retry?status=500", String.class);
                count500++;
            } catch (Exception e) { error500++; }
        }
        int count400 = 0, error400 = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://retry-provider/status-retry?status=400", String.class);
                count400++;
            } catch (Exception e) { error400++; }
        }
        boolean ok = count500 == 10 && error400 == 5 && count400 == 5;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/retry-service-name")
    public String testRetryServiceName() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://retry-provider/retry-service-name", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 5 && failCount == 5;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/on-same-retry-one")
    public String testRetryOnSameOne() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://retry-provider/on-same-retry-one", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 5 && failCount == 5;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }

    @RequestMapping("/on-same-retry-two")
    public String testRetryOnSameTwo() {
        int failCount = 0, successCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.getForObject("http://retry-provider/on-same-retry-two", String.class);
                successCount++;
            } catch (Exception e) { failCount++; }
        }
        boolean ok = successCount == 10 && failCount == 0;
        return ok ? "请求结果符合策略要求" : "请求结果不符合策略要求";
    }
}
