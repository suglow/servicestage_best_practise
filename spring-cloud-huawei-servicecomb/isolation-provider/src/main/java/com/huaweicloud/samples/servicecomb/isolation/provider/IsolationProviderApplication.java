package com.huaweicloud.samples.servicecomb.isolation.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IsolationProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(IsolationProviderApplication.class, args);
    }
}
