package com.huaweicloud.samples.servicecomb.faultinjection.errorprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FaultInjectionErrorProviderController {

    @Value("${server.port}")
    private int port;

    private final AtomicInteger counter = new AtomicInteger(0);

    @GetMapping("/error")
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String error() {
        int seq = counter.incrementAndGet();
        return "fault-injection-error-provider error seq=" + seq;
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
