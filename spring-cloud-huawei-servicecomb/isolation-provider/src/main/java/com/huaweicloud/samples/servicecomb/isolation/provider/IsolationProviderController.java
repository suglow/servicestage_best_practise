package com.huaweicloud.samples.servicecomb.isolation.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class IsolationProviderController {

    @Value("${server.port}")
    private int port;

    @Value("${spring.cloud.servicecomb.service.version:1.0.0}")
    private String version;

    private int errorCount = 0;

    @RequestMapping("/resetCount")
    public String resetCount() {
        errorCount = 0;
        return "count reset, port=" + port;
    }

    @RequestMapping("/minimum-calls")
    public String minimumCalls() {
        errorCount++;
        if (errorCount % 2 == 0) throw new RuntimeException("simulated error");
        return "success from port=" + port;
    }

    @RequestMapping("/failed-percent")
    public String failedPercent(@RequestParam("thresholds") int thresholds) {
        errorCount++;
        if (errorCount <= thresholds) throw new RuntimeException("simulated error " + errorCount);
        return "success from port=" + port;
    }

    @RequestMapping("/slow-call")
    public String slowCall(@RequestParam("deferPeriod") int deferPeriod) {
        errorCount++;
        if (errorCount <= deferPeriod) throw new RuntimeException("simulated slow error " + errorCount);
        return "success from port=" + port;
    }

    @RequestMapping("/force-closed")
    public String forceClosed() {
        errorCount++;
        if (errorCount % 2 == 0) throw new RuntimeException("simulated error");
        return "success from port=" + port;
    }

    @RequestMapping("/force-open")
    public String forceOpen() {
        return "success from port=" + port;
    }

    @RequestMapping("/force-open-service")
    public String forceOpenService() {
        return "success from port=" + port;
    }

    @RequestMapping("/error-code")
    public String errorCode(@RequestParam("code") int code, HttpServletResponse response) {
        response.setStatus(code);
        return "status=" + code + " from port=" + port;
    }
}
