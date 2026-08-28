package com.huaweicloud.samples.nacos.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API 网关启动类
 * WebFlux 架构（非 Web MVC）
 *
 * 灰度路由由 Nacos 远程配置驱动，无需自定义过滤器
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
