package com.huaweicloud.samples.servicecomb.faultinjection.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/fault-test")
public class FaultInjectionConsumerController {

    private final RestTemplate restTemplate;

    @Value("${spring.cloud.servicecomb.service.application:demo-application}")
    private String application;

    // 测试端点 1: 调用 normal-provider (有故障注入配置 50%)
    // matchGroup 配置 path="/api/call-normal"，serviceName=fault-injection-normal-provider
    @GetMapping("/call-normal")
    public String callNormal() {
        try {
            String response = restTemplate.getForObject(
                "http://fault-injection-normal-provider/api/call", String.class);
            return "OK: " + response;
        } catch (Exception e) {
            return "INJECTED: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    // 测试端点 2: 调用 fault-injection-error-provider (KIE配置 100% 注入)
    @GetMapping("/call-error")
    public String callError() {
        try {
            String response = restTemplate.getForObject(
                "http://fault-injection-error-provider/api/error", String.class);
            return "OK: " + response;
        } catch (Exception e) {
            return "INJECTED_OR_PROVIDER_ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    // 测试端点 3: 多次调用，统计故障注入比例
    private final AtomicInteger normalOk = new AtomicInteger(0);
    private final AtomicInteger normalFail = new AtomicInteger(0);
    private final AtomicInteger errorOk = new AtomicInteger(0);
    private final AtomicInteger errorFail = new AtomicInteger(0);

    @GetMapping("/stats/{iterations}")
    public String stats(@PathVariable int iterations) {
        normalOk.set(0);
        normalFail.set(0);
        errorOk.set(0);
        errorFail.set(0);

        // 打乱顺序：先 1/2 错误服务，再 1/2 正常服务
        for (int i = 0; i < iterations; i++) {
            // fault-injection-error-provider: 100% 注入 → 几乎全部失败
            try {
                restTemplate.getForObject(
                    "http://fault-injection-error-provider/api/error", String.class);
                errorOk.incrementAndGet();
            } catch (Exception e) {
                errorFail.incrementAndGet();
            }
            // fault-injection-normal-provider: 50% 注入 → 期望 50% / 50%
            try {
                restTemplate.getForObject(
                    "http://fault-injection-normal-provider/api/call", String.class);
                normalOk.incrementAndGet();
            } catch (Exception e) {
                normalFail.incrementAndGet();
            }
        }
        return String.format(
            "fault-injection-normal-provider (50%% 注入预期): ok=%d fail=%d | "
            + "fault-injection-error-provider (100%% 注入预期): ok=%d fail=%d",
            normalOk.get(), normalFail.get(),
            errorOk.get(), errorFail.get());
    }

    // 调试端点：直接检查 Spring Environment 中服务治理属性
    private final org.springframework.core.env.Environment environment;

    public FaultInjectionConsumerController(
            RestTemplate restTemplate,
            org.springframework.core.env.Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    @GetMapping("/preview")
    public ResponseEntity<java.util.Map<String, Object>> preview() {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("application", application);
        // matchGroup
        result.put("servicecomb.matchGroup.callNormalOperation",
                environment.getProperty("servicecomb.matchGroup.callNormalOperation"));
        result.put("servicecomb.matchGroup.callErrorOperation",
                environment.getProperty("servicecomb.matchGroup.callErrorOperation"));
        // faultInjection
        result.put("servicecomb.faultInjection.callNormalOperation",
                environment.getProperty("servicecomb.faultInjection.callNormalOperation"));
        result.put("servicecomb.faultInjection.callErrorOperation",
                environment.getProperty("servicecomb.faultInjection.callErrorOperation"));
        return ResponseEntity.ok(result);
    }
}
