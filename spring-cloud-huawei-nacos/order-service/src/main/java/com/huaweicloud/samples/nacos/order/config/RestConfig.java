package com.huaweicloud.samples.nacos.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置（参考官方 canary-sample）
 *
 * 灰度路由依赖 spring-cloud-starter-huawei-router 的自动机制：
 * 1. InvocationContextFilter（jakarta.servlet.Filter）在请求入口从 HTTP Header 提取灰度 header
 * 2. 写入 InvocationContext，供 WebMvcServiceInstanceFilter 读取
 * 3. RestTemplate 使用 @LoadBalanced，由 spring-cloud-starter-huawei-governance 包装
 *
 */
@Configuration
public class RestConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
