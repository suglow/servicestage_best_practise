package com.huaweicloud.samples.servicecomb.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 灰度路由诊断接口
 *
 * 功能：诊断当前请求的灰度标签透传状态
 * 访问：GET /gateway/gray-diagnose
 * Header: X-Gray-Tag: gray
 */
@RestController
@RequestMapping("/gateway")
public class GrayDiagnoseController {

    @GetMapping(value = "/gray-diagnose", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> diagnose(
            @RequestHeader(value = "X-Gray-Tag", required = false) String grayTag) {

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, String> headers = new LinkedHashMap<>();
        boolean hasGrayTag = grayTag != null && !grayTag.isEmpty();
        boolean matchesGrayRule = "gray".equals(grayTag);
        if (hasGrayTag) {
            headers.put("X-Gray-Tag", grayTag);
        }

        result.put("timestamp", Instant.now().toString());
        result.put("status", matchesGrayRule ? "OK" : "DEFAULT");
        result.put("message", matchesGrayRule
            ? "X-Gray-Tag=gray matches the 30/70 gray route."
            : "The request does not match X-Gray-Tag=gray; the default route selects v1.0.0.");
        result.put("exists", hasGrayTag);
        result.put("matchesGrayRule", matchesGrayRule);
        result.put("headers", headers);
        result.put("routeRule", "X-Gray-Tag=gray: v1/v2=30/70; default: v1=100%");
        result.put("version", "1.0.0");

        return Mono.just(result);
    }

    /**
     * 简化版健康检查（用于 TC-01 环境检查）
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> health() {
        Map<String, String> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "gateway");
        health.put("grayRouting", "enabled");
        return Mono.just(health);
    }
}
