package com.huaweicloud.samples.nacos.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Product Service 调用封装（RestTemplate 版本）
 *
 * 使用 @LoadBalanced RestTemplate，通过 Nacos 服务名 product-service 发起调用，
 * 并由 Spring Cloud Huawei 注入路由与治理能力。
 *
 * 与 Feign 的区别：
 *   - Feign：声明式接口，编译期生成代理，需要 FallbackFactory 手动处理降级
 *   - RestTemplate：直接调用，在本类中捕获异常并构造降级响应
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() { };

    private final RestTemplate restTemplate;

    public ProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ==================== 灰度路由 ====================

    /**
     * 获取单个商品（灰度路由版本）
     * 通过 HttpEntity 设置 header，由路由规则选择对应实例。
     *
     * 注意：此方式绕过了 headerContextMapper 直接设置 InvocationContext，
     * 适用于 Consumer 主动触发灰度路由的场景。
     */
    public Map<String, Object> getProductWithGray(Long id, String grayTag) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Gray-Tag", grayTag);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://product-service/api/products/" + id,
                HttpMethod.GET,
                entity,
                MAP_RESPONSE_TYPE);
        return response.getBody();
    }

    /**
     * 获取单个商品（默认版本，无灰度）
     */
    public Map<String, Object> getProduct(Long id) {
        return get("http://product-service/api/products/" + id);
    }

    /**
     * 获取商品列表
     */
    public Map<String, Object> listProducts() {
        return get("http://product-service/api/products");
    }

    // ==================== 降级处理（供 Controller 调用） ====================

    /**
     * 调用商品服务，异常时返回降级结果
     */
    public Map<String, Object> getProductWithFallback(Long id) {
        try {
            return getProduct(id);
        } catch (Exception e) {
            log.warn("ProductService 调用失败，降级返回: {}", e.getMessage());
            return buildFallback(id);
        }
    }

    /**
     * 获取商品列表，带降级处理
     */
    public Map<String, Object> listProductsWithFallback() {
        try {
            return listProducts();
        } catch (Exception e) {
            log.warn("ProductService.listProducts 调用失败，降级返回: {}", e.getMessage());
            return buildFallback(null);
        }
    }

    private Map<String, Object> get(String url) {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                MAP_RESPONSE_TYPE);
        return response.getBody();
    }

    private Map<String, Object> buildFallback(Long productId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "order-service (fallback)");
        result.put("fallback", true);
        if (productId != null) {
            result.put("productId", productId);
        }
        result.put("message", "商品服务暂时不可用，请稍后重试");
        return result;
    }
}
