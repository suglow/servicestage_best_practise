package com.huaweicloud.samples.mesher.provider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Value("${server.port:7001}")
    private int port;

    private final List<Map<String, Object>> users = List.of(
            Map.of("id", 1, "name", "Alice", "email", "alice@example.com"),
            Map.of("id", 2, "name", "Bob", "email", "bob@example.com"),
            Map.of("id", 3, "name", "Charlie", "email", "charlie@example.com"));

    @GetMapping
    public Map<String, Object> listUsers() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "java-provider");
        result.put("port", port);
        result.put("users", users);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> user = users.stream()
                .filter(candidate -> ((Number) candidate.get("id")).longValue() == id.longValue())
                .findFirst()
                .orElse(null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "java-provider");
        result.put("port", port);
        result.put("user", user);
        return result;
    }
}
