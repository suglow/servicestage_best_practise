package com.huaweicloud.samples.servicecomb.kieconfig.config;

import com.google.common.eventbus.Subscribe;
import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置变更事件监听器（双重监听）。
 *
 * 使用官方推荐的两种事件机制：
 *
 * ① GovernanceEventManager + @Subscribe（ServiceComb 官方推荐）
 *    参考: basic/consumer/ConfigListen.java
 *    监听 GovernanceConfigurationChangedEvent（KIE 配置变更后触发）
 *    可获取变更的配置 key 集合
 *
 * ② @EventListener(RefreshScopeRefreshedEvent)（Spring Cloud 标准）
 *    监听 RefreshScopeRefreshedEvent（@RefreshScope bean 重建完成后触发）
 *    此时可读取到更新后的配置值
 *
 * 完整链路：
 *   KIE PUT 修改配置
 *     ↓
 *   KieConfigWatcher 长轮询返回变更
 *     ↓
 *   ConfigWatch 收到变更 → 发布 RefreshEvent
 *     ↓
 *   ContextRefreshEventListener 处理 RefreshEvent
 *     ↓
 *   @RefreshScope bean 销毁 + 重建
 *     ↓
 *   RefreshScopeRefreshedEvent 发布 ← 本类监听此事件
 */
@Component
public class ConfigChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigChangeListener.class);

    private final DemoConfigProperties configProperties;
    private final ConfigurableEnvironment environment;

    /** 配置变更历史记录 */
    private final List<String> changeHistory = new ArrayList<>();

    // ==================== 方式一：GovernanceEventManager + @Subscribe（官方推荐） ====================

    /**
     * 在构造函数中注册到 GovernanceEventManager（官方 ConfigListen.java 模式）。
     */
    public ConfigChangeListener(DemoConfigProperties configProperties,
                                ConfigurableEnvironment environment) {
        this.configProperties = configProperties;
        this.environment = environment;
        GovernanceEventManager.register(this);  // ← 注册 KIE 配置变更监听
    }

    /**
     * 监听 KIE 配置变更事件。
     *
     * 当 KIE 配置中心的任何 key 发生变更时，此方法被调用。
     * event.getChangedConfigurations() 返回变更的 key 名称集合。
     */
    @Subscribe
    public void onEnvironmentChangeEvent(GovernanceConfigurationChangedEvent event) {
        String timeStr = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        LOG.info("🔔 GovernanceConfigurationChangedEvent — changed keys: {}", 
                 event.getChangedConfigurations());

        String record = String.format("[GovernanceEvent] [%s] keys=%s",
            timeStr, event.getChangedConfigurations());
        changeHistory.add(record);
    }

    // ==================== 方式二：@EventListener(RefreshScopeRefreshedEvent) ====================

    /**
     * 监听 RefreshScope 刷新完成事件。
     *
     * RefreshScopeRefreshedEvent 在 @RefreshScope bean 全部重建完毕后发布，
     * 此时可以读取到更新后的配置值。
     */
    @EventListener
    public void onRefreshScopeRefreshed(RefreshScopeRefreshedEvent event) {
        long now = System.currentTimeMillis();
        String timeStr = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(now));

        LOG.info("╔══════════════════════════════════════════════════════════════╗");
        LOG.info("║  🔄 KIE 配置变更 — @RefreshScope bean 已重建                ║");
        LOG.info("║  时间: {}                                       ║", timeStr);
        LOG.info("╠══════════════════════════════════════════════════════════════╣");

        // 打印 3 种 value_type 的读取效果
        String textVal = configProperties.getTextMessage();
        String yamlFromConfig = configProperties.getYamlMessage();
        String yamlFromEnv = environment.getProperty("demo.conf.yaml-message", "<empty>");
        String propsVal = configProperties.getPropsMessage();

        LOG.info("║  [text]       → {}", textVal);
        LOG.info("║  [yaml]  conf → {} (environment={})", yamlFromConfig, yamlFromEnv);
        LOG.info("║  [props]      → {}", propsVal);
        LOG.info("║                                                            ║");
        LOG.info("║  KIE value_type 差异:                                       ║");
        LOG.info("║    text       → String → getProperty() 正常 ✅              ║");
        LOG.info("║    yaml       → Map    → getProperty() 返回空 ❌            ║");
        LOG.info("║    properties → String → getProperty() 正常 ✅              ║");
        LOG.info("╚══════════════════════════════════════════════════════════════╝");

        configProperties.setRefreshTimestamp(now);

        String record = String.format("[RefreshScope] [%s] text=%s yaml=%s props=%s",
            timeStr, textVal, yamlFromConfig, propsVal);
        changeHistory.add(record);
    }

    // ==================== 工具方法 ====================

    public List<String> getChangeHistory() {
        return new ArrayList<>(changeHistory);
    }

    public void clearHistory() {
        changeHistory.clear();
    }
}
