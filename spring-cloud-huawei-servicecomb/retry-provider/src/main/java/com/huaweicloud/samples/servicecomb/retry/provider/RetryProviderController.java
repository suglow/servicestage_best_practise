package com.huaweicloud.samples.servicecomb.retry.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

@RestController
public class RetryProviderController {

    @Value("${server.port}")
    private int port;

    private int count = 0;

    @RequestMapping("/no-retry")
    public String noRetry() {
        count++;
        if (count % 2 == 0) return "success from port=" + port;
        throw new RuntimeException("simulated error");
    }

    @RequestMapping("/test-retry")
    public String testRetry() {
        count++;
        if (count % 2 == 0) return "success from port=" + port;
        // 返回 503 以触发 ServiceComb 重试策略（retryOnResponseStatus 支持 502/503）
        throw new ServiceUnavailableException("503 from port=" + port);
    }

    @RequestMapping("/status-retry")
    public String statusRetry(@RequestParam("status") int status, HttpServletResponse response) {
        // 动态状态码，由调用方指定（用于测试不同状态码的重试行为）
        response.setStatus(status);
        return "status=" + status + " from port=" + port;
    }

    @RequestMapping("/retry-service-name")
    public String retryServiceName() {
        count++;
        if (count % 2 == 0) return "success from port=" + port;
        throw new ServiceUnavailableException("503 from port=" + port);
    }

    @RequestMapping("/on-same-retry-one")
    public String onSameRetryOne() {
        count++;
        if (count % 2 == 0) return "success from port=" + port;
        throw new ServiceUnavailableException("503 from port=" + port);
    }

    @RequestMapping("/on-same-retry-two")
    public String onSameRetryTwo() {
        count++;
        if (count % 2 == 0) return "success from port=" + port;
        throw new ServiceUnavailableException("503 from port=" + port);
    }

    // 503 Service Unavailable — 触发 ServiceComb 重试策略
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) { super(message); }
    }
}
