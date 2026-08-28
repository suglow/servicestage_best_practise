package com.huaweicloud.samples.mesher.provider.controller;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest {

    private final UserController controller = new UserController();

    @Test
    void listsAllDemoUsers() {
        Map<String, Object> response = controller.listUsers();

        assertThat(response.get("service")).isEqualTo("java-provider");
        assertThat((List<?>) response.get("users")).hasSize(3);
    }

    @Test
    void findsUserWhenPathVariableIsLong() {
        Map<String, Object> response = controller.getUser(1L);

        assertThat(response.get("user")).isNotNull();
        assertThat(((Map<?, ?>) response.get("user")).get("name")).isEqualTo("Alice");
    }
}
