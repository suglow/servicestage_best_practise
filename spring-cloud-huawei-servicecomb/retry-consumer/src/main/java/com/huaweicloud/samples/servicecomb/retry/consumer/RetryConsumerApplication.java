package com.huaweicloud.samples.servicecomb.retry.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class RetryConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RetryConsumerApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);   // 连接超时 2s
        factory.setReadTimeout(2000);       // 读取超时 2s（单次调用上限）
        return new RestTemplate(factory);
    }
}
