package com.huaweicloud.samples.servicecomb.circuitbreaker.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CircuitBreakerProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircuitBreakerProviderApplication.class, args);
    }
}
