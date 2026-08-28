package com.huaweicloud.samples.servicecomb.kieconfig.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;

/**
 * 动态配置属性类。
 *
 * 官方推荐的使用模式：
 *   @ConfigurationProperties — 静态配置绑定（basic/benchmark/Configuration.java）
 *   @RefreshScope            — 配置热刷新（basic/provider/ProviderController.java）
 *
 * 将两种模式合并，实现：
 *   - 批量属性绑定（@ConfigurationProperties prefix="demo.conf"）
 *   - 热刷新（@RefreshScope — KIE 变更后自动重建此 bean）
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "demo.conf")
public class DemoConfigProperties {

    private static final Logger LOG = LoggerFactory.getLogger(DemoConfigProperties.class);

    /** KIE value_type=text 的配置值（String，可直接读取） */
    private String textMessage = "default-text-value";

    /**
     * KIE value_type=yaml 的配置值。
     * 注意：KIE 中 value_type=yaml 会将 YAML 解析为 Map，
     * environment.getProperty() 返回空字符串，但
     * @ConfigurationProperties 的嵌套绑定可能仍能读取。
     */
    private String yamlMessage = "default-yaml-value";

    /** KIE value_type=properties 的配置值（String，可正常读取） */
    private String propsMessage = "default-props-value";

    /** 上次配置刷新时间戳 */
    private long refreshTimestamp = 0;

    @PostConstruct
    public void init() {
        LOG.info("DemoConfigProperties @RefreshScope bean 初始化: text={}, yaml={}, props={}",
            textMessage, yamlMessage, propsMessage);
    }

    // ─── getters / setters ───

    public String getTextMessage() { return textMessage; }
    public void setTextMessage(String textMessage) { this.textMessage = textMessage; }

    public String getYamlMessage() { return yamlMessage; }
    public void setYamlMessage(String yamlMessage) { this.yamlMessage = yamlMessage; }

    public String getPropsMessage() { return propsMessage; }
    public void setPropsMessage(String propsMessage) { this.propsMessage = propsMessage; }

    public long getRefreshTimestamp() { return refreshTimestamp; }
    public void setRefreshTimestamp(long refreshTimestamp) { this.refreshTimestamp = refreshTimestamp; }

    @Override
    public String toString() {
        return String.format(
            "DemoConfigProperties{text='%s', yaml='%s', props='%s', refreshAt=%s}",
            textMessage, yamlMessage, propsMessage,
            refreshTimestamp > 0 ? Instant.ofEpochMilli(refreshTimestamp).toString() : "never"
        );
    }
}
