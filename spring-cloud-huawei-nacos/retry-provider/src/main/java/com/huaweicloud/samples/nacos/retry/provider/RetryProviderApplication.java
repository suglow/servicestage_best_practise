package com.huaweicloud.samples.nacos.retry.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RetryProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(RetryProviderApplication.class, args);
    }
}
