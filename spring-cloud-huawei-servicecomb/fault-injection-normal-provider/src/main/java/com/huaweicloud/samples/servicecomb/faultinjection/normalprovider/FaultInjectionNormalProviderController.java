package com.huaweicloud.samples.servicecomb.faultinjection.normalprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FaultInjectionNormalProviderController {

    @Value("${server.port}")
    private int port;

    private final AtomicInteger counter = new AtomicInteger(0);

    @GetMapping("/call")
    public String call() {
        int seq = counter.incrementAndGet();
        return "ok from fault-injection-normal-provider port=" + port + " seq=" + seq;
    }

    @GetMapping("/stats")
    public Map<String, Integer> stats() {
        return Map.of("calls", counter.get());
    }

    @GetMapping("/reset")
    public Map<String, Integer> reset() {
        counter.set(0);
        return stats();
    }
}
