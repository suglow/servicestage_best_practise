package com.huaweicloud.samples.nacos.circuit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CircuitProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircuitProviderApplication.class, args);
    }
}
