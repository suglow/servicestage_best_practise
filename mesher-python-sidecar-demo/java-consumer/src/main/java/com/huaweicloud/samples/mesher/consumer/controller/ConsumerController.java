package com.huaweicloud.samples.mesher.consumer.controller;

import com.huaweicloud.samples.mesher.consumer.feign.PythonAppClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ConsumerController {

    private final PythonAppClient pythonAppClient;

    @Value("${server.port:7002}")
    private int port;

    public ConsumerController(PythonAppClient pythonAppClient) {
        this.pythonAppClient = pythonAppClient;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "service", "java-consumer", "port", port);
    }

    @GetMapping("/show")
    public Map<String, Object> show() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "java-consumer");
        result.put("port", port);
        try {
            result.put("python_app_response", pythonAppClient.show());
        } catch (Exception exception) {
            result.put("python_app_response", Map.of(
                    "status", "error",
                    "error", exception.getMessage()));
        }
        return result;
    }
}
