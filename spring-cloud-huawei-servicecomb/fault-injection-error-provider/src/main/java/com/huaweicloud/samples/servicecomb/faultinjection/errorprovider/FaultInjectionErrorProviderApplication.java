package com.huaweicloud.samples.servicecomb.faultinjection.errorprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FaultInjectionErrorProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(FaultInjectionErrorProviderApplication.class, args);
    }
}
