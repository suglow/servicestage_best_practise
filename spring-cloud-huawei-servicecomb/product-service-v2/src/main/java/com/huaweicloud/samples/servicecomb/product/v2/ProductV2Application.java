package com.huaweicloud.samples.servicecomb.product.v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductV2Application {

    public static void main(String[] args) {
        SpringApplication.run(ProductV2Application.class, args);
    }
}
