package com.huaweicloud.samples.nacos.productv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 商品服务 v2（灰度版本）启动类
 * 与 v1 使用相同的 spring.application.name = product-service
 * 通过 Nacos metadata version 区分版本
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductV2Application {

    public static void main(String[] args) {
        SpringApplication.run(ProductV2Application.class, args);
    }
}
