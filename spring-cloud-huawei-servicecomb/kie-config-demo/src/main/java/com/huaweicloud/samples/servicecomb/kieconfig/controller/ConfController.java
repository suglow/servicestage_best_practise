package com.huaweicloud.samples.servicecomb.kieconfig.controller;

import com.huaweicloud.samples.servicecomb.kieconfig.config.BusinessConfig;
import com.huaweicloud.samples.servicecomb.kieconfig.config.ConfigChangeListener;
import com.huaweicloud.samples.servicecomb.kieconfig.config.DemoConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * KIE 配置演示 REST API。
 *
 * 官方推荐模式：
 *   @RefreshScope + @Value —— 配置热刷新（参考 basic/provider/ProviderController.java）
 *   @ConfigurationProperties —— 静态配置绑定（参考 basic/benchmark/Configuration.java）
 *
 * 端点：
 *   GET  /api/conf/current  — @ConfigurationProperties 读配置
 *   GET  /api/conf/value    — @RefreshScope + @Value 读配置（官方模式）
 *   GET  /api/conf/env      — environment.getProperty() 对比读
 *   GET  /api/conf/history  — 查看配置变更历史
 *   POST /api/conf/refresh  — 手动触发配置刷新
 */
@RestController
@RequestMapping("/api/conf")
@RefreshScope  // ← 官方 ProviderController 模式：配置热刷新
public class ConfController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfController.class);

    // ========== @RefreshScope + @Value（官方推荐的热刷新方式）==========

    @Value("${demo.conf.text-message:default-text-value}")
    private String textMessage;

    @Value("${demo.conf.props-message:default-props-value}")
    private String propsMessage;

    // ========== 依赖注入 ==========

    private final DemoConfigProperties config;
    private final BusinessConfig businessConfig;
    private final ConfigChangeListener changeListener;
    private final ContextRefresher contextRefresher;
    private final ConfigurableEnvironment environment;

    public ConfController(DemoConfigProperties config,
                          BusinessConfig businessConfig,
                          ConfigChangeListener changeListener,
                          ContextRefresher contextRefresher,
                          ConfigurableEnvironment environment) {
        this.config = config;
        this.businessConfig = businessConfig;
        this.changeListener = changeListener;
        this.contextRefresher = contextRefresher;
        this.environment = environment;
    }

    // ========== GET /api/conf/current — @ConfigurationProperties 读配置 ==========

    @GetMapping("/current")
    public Map<String, Object> current() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("module", "kie-config-demo");
        result.put("description", "ServiceComb KIE 配置中心演示");
        result.put("textMessage", textMessage);
        result.put("yamlMessage", config.getYamlMessage());     // 仅 @ConfigurationProperties
        result.put("propsMessage", propsMessage);
        result.put("refreshTimestamp", config.getRefreshTimestamp() > 0
            ? Instant.ofEpochMilli(config.getRefreshTimestamp()).toString()
            : "not-refreshed-yet");
        return result;
    }

    // ========== GET /api/conf/value — @RefreshScope + @Value 读配置（官方模式）==========

    @GetMapping("/value")
    public Map<String, Object> value() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("textMessage", textMessage);
        result.put("propsMessage", propsMessage);
        result.put("refreshTimestamp", config.getRefreshTimestamp() > 0
            ? Instant.ofEpochMilli(config.getRefreshTimestamp()).toString()
            : "not-refreshed-yet");
        result.put("note", "@RefreshScope + @Value (官方 ProviderController 模式)");
        return result;
    }

    // ========== GET /api/conf/env — environment.getProperty() 对比读 ==========

    @GetMapping("/env")
    public Map<String, Object> envRead() {
        Map<String, Object> result = new LinkedHashMap<>();
        String textVal = environment.getProperty("demo.conf.text-message", "<empty>");
        String yamlVal = environment.getProperty("demo.conf.yaml-message", "<empty>");
        String propsVal = environment.getProperty("demo.conf.props-message", "<empty>");
        result.put("text", textVal);
        result.put("yaml", yamlVal);
        result.put("properties", propsVal);
        result.put("note", "environment.getProperty() — yaml 类型返回空");
        return result;
    }

    // ========== GET /api/conf/business — 多属性业务配置（@ConfigurationProperties 嵌套对象）==========

    @GetMapping("/business")
    public Map<String, Object> business() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("description", "多属性业务配置 — 模拟真实项目中的通知服务 + 限流 + 功能开关");

        // 租户标识
        result.put("tenantId", businessConfig.getTenantId());

        // 通知服务 — 邮件
        Map<String, Object> email = new LinkedHashMap<>();
        email.put("enabled", businessConfig.getNotification().getEmail().isEnabled());
        email.put("host", businessConfig.getNotification().getEmail().getHost());
        email.put("port", businessConfig.getNotification().getEmail().getPort());
        email.put("username", businessConfig.getNotification().getEmail().getUsername());
        email.put("fromAddress", businessConfig.getNotification().getEmail().getFromAddress());
        result.put("email", email);

        // 通知服务 — 短信
        Map<String, Object> sms = new LinkedHashMap<>();
        sms.put("enabled", businessConfig.getNotification().getSms().isEnabled());
        sms.put("provider", businessConfig.getNotification().getSms().getProvider());
        sms.put("apiKey", maskSecret(businessConfig.getNotification().getSms().getApiKey()));
        sms.put("region", businessConfig.getNotification().getSms().getRegion());
        result.put("sms", sms);

        // 通知服务 — Webhook
        Map<String, Object> webhook = new LinkedHashMap<>();
        webhook.put("enabled", businessConfig.getNotification().getWebhook().isEnabled());
        webhook.put("url", businessConfig.getNotification().getWebhook().getUrl());
        webhook.put("secret", maskSecret(businessConfig.getNotification().getWebhook().getSecret()));
        webhook.put("retryCount", businessConfig.getNotification().getWebhook().getRetryCount());
        result.put("webhook", webhook);

        // 限流配置
        Map<String, Object> limit = new LinkedHashMap<>();
        limit.put("qps", businessConfig.getLimit().getQps());
        limit.put("burstSize", businessConfig.getLimit().getBurstSize());
        limit.put("timeoutMs", businessConfig.getLimit().getTimeoutMs());
        result.put("limit", limit);

        // 功能开关
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("grayReleaseEnabled", businessConfig.getFeature().isGrayReleaseEnabled());
        feature.put("newUiEnabled", businessConfig.getFeature().isNewUiEnabled());
        feature.put("darkModeEnabled", businessConfig.getFeature().isDarkModeEnabled());
        result.put("feature", feature);

        return result;
    }

    // ========== GET /api/conf/compare — 对比 3 种 value_type 对多属性配置的差异 ==========

    @GetMapping("/compare")
    public Map<String, Object> compareValueTypes() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("description", "对比 KIE 三种 value_type 在多属性配置下的行为差异");

        // ① value_type=text — getProperty() 返回 String（无法自动拆分为子属性）
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("app.limit.qps", environment.getProperty("app.limit.qps", "<empty>"));
        text.put("app.feature.gray-release-enabled", environment.getProperty("app.feature.gray-release-enabled", "<empty>"));
        text.put("app.notification.email.host", environment.getProperty("app.notification.email.host", "<empty>"));
        text.put("verdict", "text 类型的 value 是一个完整的 String，KIE 不拆分属性 → 子属性全部 <empty>");
        result.put("text_type", text);

        // ② value_type=yaml — getProperty() 返回 ""（Map 无法序列化）
        Map<String, Object> yaml = new LinkedHashMap<>();
        yaml.put("app.limit.qps", environment.getProperty("app.limit.qps", "<empty>"));
        yaml.put("app.feature.gray-release-enabled", environment.getProperty("app.feature.gray-release-enabled", "<empty>"));
        yaml.put("verdict", "yaml 类型的 value 被 KIE 解析为嵌套 Map，getProperty() 返回 '' → 全部 <empty>");
        result.put("yaml_type", yaml);

        // ③ value_type=properties — getProperty() 返回每个独立属性 ✅
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("app.limit.qps", environment.getProperty("app.limit.qps", "<empty>"));
        props.put("app.feature.gray-release-enabled", environment.getProperty("app.feature.gray-release-enabled", "<empty>"));
        props.put("app.notification.email.host", environment.getProperty("app.notification.email.host", "<empty>"));
        props.put("app.notification.email.port", environment.getProperty("app.notification.email.port", "<empty>"));
        props.put("verdict", "properties 类型将每一行 key=value 注入为独立的 Environment 属性 ✅");
        result.put("props_type", props);

        // ④ @ConfigurationProperties 绑定效果（嵌套对象自动映射）
        Map<String, Object> bound = new LinkedHashMap<>();
        bound.put("app.limit.qps", businessConfig.getLimit().getQps());
        bound.put("app.feature.gray-release-enabled", businessConfig.getFeature().isGrayReleaseEnabled());
        bound.put("app.notification.email.host", businessConfig.getNotification().getEmail().getHost());
        bound.put("app.notification.email.port", businessConfig.getNotification().getEmail().getPort());
        bound.put("verdict", "properties 类型 + @ConfigurationProperties 嵌套绑定 → 完整读取 ✅");
        result.put("configurationProperties_bound", bound);

        return result;
    }

    // ========== GET /api/conf/debug-props — 读多行值原始内容（绕过 actuator 脱敏）==========

    @GetMapping("/debug-props")
    public Map<String, Object> debugProps() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("app.multi-cont (续行符)", environment.getProperty("app.multi-cont", "<empty>"));
        result.put("app.json-config (JSON字符串)", environment.getProperty("app.json-config", "<empty>"));
        result.put("test.real.multiline (text类型)", environment.getProperty("test.real.multiline", "<empty>"));

        // ========== fileSource 验证测试 ==========
        // testing.yaml 在 fileSource 中 → 应被 YamlPropertiesFactoryBean 扁平化
        result.put("✅ test.from-filesource.message (testing.yaml 在 fileSource 中)",
                environment.getProperty("test.from-filesource.message", "<empty>"));
        result.put("✅ test.from-filesource.value (testing.yaml 在 fileSource 中)",
                environment.getProperty("test.from-filesource.value", "<empty>"));

        // test-other.yaml 不在 fileSource 中 → 不应被扁平化
        result.put("❌ test.not-in-filesource.message (test-other.yaml 不在 fileSource 中)",
                environment.getProperty("test.not-in-filesource.message", "<empty>"));
        result.put("❌ test.not-in-filesource.value (test-other.yaml 不在 fileSource 中)",
                environment.getProperty("test.not-in-filesource.value", "<empty>"));

        // 直接读 key 名本身（原始 KIE 条目）
        result.put("← 直接读 test-other.yaml key",
                environment.getProperty("test-other.yaml", "<empty>"));
        result.put("← 直接读 testing.yaml key",
                environment.getProperty("testing.yaml", "<empty>"));

        return result;
    }

    // ========== GET /api/conf/test-direct — 直接读 test-other.yaml key（验证 fileSource 过滤）==========
    @GetMapping("/test-direct")
    public Map<String, Object> testDirect() {
        // 直接用 key 名读（原始 KIE 条目名）
        Object yamlKeyVal = environment.getProperty("test-other.yaml");
        // 读扁平化后的子属性
        String msgVal = environment.getProperty("test.not-in-filesource.message", "<empty>");
        String valVal = environment.getProperty("test.not-in-filesource.value", "<empty>");
        return Map.of(
            "getProperty(\"test-other.yaml\")", yamlKeyVal,
            "getProperty(\"test-other.yaml\") type", yamlKeyVal == null ? "null" : yamlKeyVal.getClass().getName(),
            "getProperty(\"test.not-in-filesource.message\")", msgVal,
            "getProperty(\"test.not-in-filesource.value\")", valVal
        );
    }

    // ========== GET /api/conf/history — 配置变更历史 ==========

    @GetMapping("/history")
    public Map<String, Object> history() {
        List<String> history = changeListener.getChangeHistory();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("changeCount", history.size());
        result.put("history", history);
        return result;
    }

    // ========== POST /api/conf/refresh — 手动触发刷新 ==========

    @PostMapping("/refresh")
    public Map<String, String> refresh() {
        LOG.info("手动触发配置刷新: ContextRefresher.refresh()");
        long before = config.getRefreshTimestamp();
        Set<String> keys = contextRefresher.refresh();
        long after = config.getRefreshTimestamp();

        Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "refreshed");
        result.put("refreshedKeys", keys.toString());
        result.put("timestampBefore", String.valueOf(before));
        result.put("timestampAfter", String.valueOf(after));
        result.put("timestampChanged", String.valueOf(after > before));
        return result;
    }

    // ========== DELETE /api/conf/history — 清空历史 ==========

    @DeleteMapping("/history")
    public Map<String, String> clearHistory() {
        changeListener.clearHistory();
        return Map.of("status", "cleared");
    }

    // ========== 工具方法 ==========

    /** 对敏感信息脱敏显示 */
    private String maskSecret(String value) {
        if (value == null || value.isEmpty()) return "<not-set>";
        if (value.length() <= 4) return "****";
        return value.substring(0, 3) + "****";
    }
    // ========== GET /api/conf/raw-data — ConfigConverter.currentData（绕过 Actuator 过滤）==========

    @GetMapping("/raw-data")
    public Map<String, Object> rawData() {
        // 通过 Spring Boot ConfigurableEnvironment 直接读所有属性
        Map<String, Object> result = new LinkedHashMap<>();
        for (PropertySource<?> ps : environment.getPropertySources()) {
            if (!(ps instanceof EnumerablePropertySource<?> enumerable)) {
                continue;
            }
            String psName = ps.getName();
            if (psName.contains("servicecomb") || psName.contains("kie") || psName.contains("bootstrap")) {
                Map<String, Object> props = new LinkedHashMap<>();
                for (String key : enumerable.getPropertyNames()) {
                    if (key.contains("test.") || key.contains("myapp") || key.contains("testing")) {
                        props.put(key, ps.getProperty(key));
                    }
                }
                if (!props.isEmpty()) {
                    result.put(psName, props);
                }
            }
        }
        return result;
    }

    // ========== GET /api/conf/scan-test — 动态扫描所有 test.* 属性 ==========
    @GetMapping("/scan-test")
    public Map<String, Object> scanTest() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("=== 所有 test.* 属性（通过 getProperties）===", "");
        // 动态扫描所有以 test. 开头的属性
        for (PropertySource<?> ps : environment.getPropertySources()) {
            if (!(ps instanceof EnumerablePropertySource<?> enumerable)) {
                continue;
            }
            for (String key : enumerable.getPropertyNames()) {
                if (key.startsWith("test.")) {
                    Object val = ps.getProperty(key);
                    result.put(key, val == null ? "null" : val.toString());
                }
            }
        }
        return result;
    }
}
