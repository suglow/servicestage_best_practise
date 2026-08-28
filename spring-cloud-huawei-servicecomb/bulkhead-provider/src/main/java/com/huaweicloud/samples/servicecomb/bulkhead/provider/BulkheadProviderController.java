package com.huaweicloud.samples.servicecomb.bulkhead.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BulkheadProviderController {

    @Value("${server.port}")
    private int port;

    @Value("${spring.cloud.servicecomb.service.version:1.0.0}")
    private String version;

    @RequestMapping("/bulk-rate-limiting")
    public String bulkRateLimiting() throws InterruptedException {
        Thread.sleep(1000);
        return "provider port=" + port + " version=" + version;
    }

    @RequestMapping("/bulk-no-rate-limiting")
    public String bulkNoRateLimiting() throws InterruptedException {
        Thread.sleep(500);
        return "provider port=" + port + " version=" + version;
    }

    @RequestMapping("/bulk-service-name")
    public String bulkServiceName() throws InterruptedException {
        Thread.sleep(1000);
        return "provider port=" + port + " version=" + version;
    }
}
