package com.huaweicloud.samples.servicecomb.circuitbreaker.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CircuitBreakerConsumerController {

    private static final String PROVIDER = "http://circuit-breaker-provider/api/circuit";

    private final RestTemplate restTemplate;

    public CircuitBreakerConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/api/consumer/circuit/error50")
    public String error50() {
        try {
            return "CALL_OK:" + restTemplate.getForObject(PROVIDER + "/error50", String.class);
        } catch (Exception exception) {
            return failureMessage(exception, 50);
        }
    }

    @GetMapping("/api/consumer/circuit/slow")
    public String slow() {
        try {
            long start = System.currentTimeMillis();
            String result = restTemplate.getForObject(PROVIDER + "/slow", String.class);
            return "OK(duration=" + (System.currentTimeMillis() - start) + "ms):" + result;
        } catch (Exception exception) {
            return failureMessage(exception, 50);
        }
    }

    @GetMapping("/api/consumer/circuit/normal")
    public String normal() {
        try {
            return "NORMAL_OK:" + restTemplate.getForObject(PROVIDER + "/normal", String.class);
        } catch (Exception exception) {
            return failureMessage(exception, 50);
        }
    }

    @GetMapping("/api/consumer/circuit/fallback")
    public String fallback() {
        try {
            return "CALL_OK:" + restTemplate.getForObject(PROVIDER + "/always-ok", String.class);
        } catch (Exception exception) {
            String message = safeMessage(exception);
            if (message.contains("circuitBreaker is open") || message.contains("Consumer circuitBreaker")) {
                return "CONSUMER_CB_OPEN:Fallback triggered by consumer-side circuit breaker";
            }
            return "ERROR:" + abbreviate(message, 80);
        }
    }

    @GetMapping("/api/consumer/circuit/provider-cb")
    public String providerCircuitBreaker() {
        // Let the inbound CircuitBreakerFilter observe the downstream failure.
        return "CALL_OK:" + restTemplate.getForObject(PROVIDER + "/error100", String.class);
    }

    private String failureMessage(Exception exception, int maxLength) {
        String message = safeMessage(exception);
        String prefix = message.contains("circuitBreaker is open") ? "PROVIDER_CB_OPEN:" : "ERROR:";
        return prefix + abbreviate(message, maxLength);
    }

    private String safeMessage(Exception exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    private String abbreviate(String value, int maxLength) {
        return value.substring(0, Math.min(maxLength, value.length()));
    }
}
