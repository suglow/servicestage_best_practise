package com.huaweicloud.samples.servicecomb.kieconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * ServiceCenter 配置中心演示服务。
 *
 * 演示内容：
 * 1. 连接 ServiceComb KIE 配置中心
 * 2. 使用 @ConfigurationProperties + @RefreshScope 绑定动态配置
 * 3. 通过 @EventListener 监听 RefreshEvent 观察配置变更
 * 4. 对比 KIE value_type=text vs yaml vs properties 的行为差异
 */
@SpringBootApplication
@EnableDiscoveryClient
public class KieConfigDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KieConfigDemoApplication.class, args);
    }
}
