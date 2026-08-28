package com.huaweicloud.samples.servicecomb.kieconfig.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 通知服务业务配置（模拟真实项目中的多属性配置）。
 *
 * 配置结构（KIE properties 格式）：
 *   app.notification.email.host=smtp.company.com
 *   app.notification.email.port=587
 *   app.notification.email.username=noreply@company.com
 *   app.notification.sms.provider=aliyun
 *   app.limit.qps=500
 *   app.feature.gray-release-enabled=true
 *
 * 官方参考模式：
 *   basic/benchmark/Configuration.java — @ConfigurationProperties 批量绑定
 *   basic/provider/ProviderController.java — @RefreshScope 热刷新
 *
 * 演示目的：
 *   对比 KIE 三种 value_type 在多属性配置场景下的读取效果：
 *     text       → 整个 value 是一个 String，需要手动解析
 *     yaml       → 被解析为嵌套 Map，getProperty() 返回 ""
 *     properties → 每个 key=value 被注入为独立属性 ✅ 推荐
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "app")
public class BusinessConfig {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessConfig.class);

    /** 租户标识 */
    private String tenantId = "default";

    /** 通知服务配置（嵌套对象） */
    private Notification notification = new Notification();

    /** 限流配置（嵌套对象） */
    private LimitConfig limit = new LimitConfig();

    /** 功能开关（嵌套对象） */
    private Feature feature = new Feature();

    // ─── 嵌套类 ───

    public static class Notification {
        private EmailConfig email = new EmailConfig();
        private SmsConfig sms = new SmsConfig();
        private WebhookConfig webhook = new WebhookConfig();

        public EmailConfig getEmail() { return email; }
        public void setEmail(EmailConfig email) { this.email = email; }
        public SmsConfig getSms() { return sms; }
        public void setSms(SmsConfig sms) { this.sms = sms; }
        public WebhookConfig getWebhook() { return webhook; }
        public void setWebhook(WebhookConfig webhook) { this.webhook = webhook; }
    }

    public static class EmailConfig {
        private boolean enabled = false;
        private String host = "localhost";
        private int port = 25;
        private String username = "";
        private String fromAddress = "";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFromAddress() { return fromAddress; }
        public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    }

    public static class SmsConfig {
        private boolean enabled = false;
        private String provider = "aliyun";
        private String apiKey = "";
        private String region = "cn-hangzhou";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
    }

    public static class WebhookConfig {
        private boolean enabled = false;
        private String url = "";
        private String secret = "";
        private int retryCount = 3;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    }

    public static class LimitConfig {
        private int qps = 200;
        private int burstSize = 50;
        private long timeoutMs = 3000;

        public int getQps() { return qps; }
        public void setQps(int qps) { this.qps = qps; }
        public int getBurstSize() { return burstSize; }
        public void setBurstSize(int burstSize) { this.burstSize = burstSize; }
        public long getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
    }

    public static class Feature {
        private boolean grayReleaseEnabled = false;
        private boolean newUiEnabled = false;
        private boolean darkModeEnabled = false;

        public boolean isGrayReleaseEnabled() { return grayReleaseEnabled; }
        public void setGrayReleaseEnabled(boolean grayReleaseEnabled) { this.grayReleaseEnabled = grayReleaseEnabled; }
        public boolean isNewUiEnabled() { return newUiEnabled; }
        public void setNewUiEnabled(boolean newUiEnabled) { this.newUiEnabled = newUiEnabled; }
        public boolean isDarkModeEnabled() { return darkModeEnabled; }
        public void setDarkModeEnabled(boolean darkModeEnabled) { this.darkModeEnabled = darkModeEnabled; }
    }

    // ─── 顶层属性 getters/setters ───

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }

    public LimitConfig getLimit() { return limit; }
    public void setLimit(LimitConfig limit) { this.limit = limit; }

    public Feature getFeature() { return feature; }
    public void setFeature(Feature feature) { this.feature = feature; }

    // ─── 生命周期 ───

    @PostConstruct
    public void init() {
        LOG.info("BusinessConfig @RefreshScope bean 初始化 — "
            + "email={}, sms={}, webhook={}, qps={}, grayRelease={}",
            notification.email.host,
            notification.sms.provider,
            notification.webhook.url,
            limit.qps,
            feature.grayReleaseEnabled);
    }
}
