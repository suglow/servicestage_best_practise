package com.huaweicloud.samples.nacos.order.feign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类（Feign 版）
 *
 * 使用 OpenFeign 声明式调用，并启用 Spring Cloud Huawei 路由与治理能力
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.huaweicloud.samples.nacos.order.feign.client")
public class OrderFeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderFeignApplication.class, args);
    }
}
