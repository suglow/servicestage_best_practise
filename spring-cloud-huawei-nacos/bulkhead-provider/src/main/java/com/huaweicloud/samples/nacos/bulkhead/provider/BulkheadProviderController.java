package com.huaweicloud.samples.nacos.bulkhead.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 舱壁隔离 Provider — 模拟业务耗时
 */
@RestController
public class BulkheadProviderController {

    @Value("${server.port}")
    private int port;

    @Value("${spring.cloud.nacos.discovery.metadata.version:1.0.0}")
    private String version;

    /** 模拟耗时1s — maxWaitDuration=0 时并发超限直接拒绝 */
    @GetMapping("/bulk-rate-limiting")
    public String bulkRateLimiting() throws InterruptedException {
        Thread.sleep(1000);
        return "provider port=" + port + " version=" + version;
    }

    /** 模拟耗时500ms — maxWaitDuration=10000 时排队等待可通过 */
    @GetMapping("/bulk-no-rate-limiting")
    public String bulkNoRateLimiting() throws InterruptedException {
        Thread.sleep(500);
        return "provider port=" + port + " version=" + version;
    }

    /** 按服务名限定 — 对指定 consumer 生效 */
    @GetMapping("/bulk-service-name")
    public String bulkServiceName() throws InterruptedException {
        Thread.sleep(1000);
        return "provider port=" + port + " version=" + version;
    }
}
