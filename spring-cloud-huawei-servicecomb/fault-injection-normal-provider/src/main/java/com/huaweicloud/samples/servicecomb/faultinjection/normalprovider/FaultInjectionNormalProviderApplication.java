package com.huaweicloud.samples.servicecomb.faultinjection.normalprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FaultInjectionNormalProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(FaultInjectionNormalProviderApplication.class, args);
    }
}
