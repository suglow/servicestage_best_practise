package com.huaweicloud.samples.nacos.retry.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * 重试治理 Consumer — 每个端点有精确的数学断言
 */
@RestController
public class RetryConsumerController {

    private final RestTemplate restTemplate;

    public RetryConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ======== 场景1：无重试 — 一半成功一半失败 ========
    @GetMapping("/no-retry")
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

    // ======== 场景2：有重试 — 全部成功 ========
    @GetMapping("/retry")
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

    // ======== 场景3：按状态码 — 500重试成功, 400不重试 ========
    @GetMapping("/status-retry")
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

    // ======== 场景4：服务名限定 — 策略不生效 ========
    @GetMapping("/retry-service-name")
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

    // ======== 场景5：最多尝试1次 — 不执行重试，一半失败 ========
    @GetMapping("/on-same-retry-one")
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

    // ======== 场景6：同实例重试 — 第二次调用恢复 ========
    @GetMapping("/on-same-retry-two")
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
