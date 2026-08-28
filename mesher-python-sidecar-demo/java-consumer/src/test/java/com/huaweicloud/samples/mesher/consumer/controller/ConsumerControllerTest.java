package com.huaweicloud.samples.mesher.consumer.controller;

import com.huaweicloud.samples.mesher.consumer.feign.PythonAppClient;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumerControllerTest {

    @Test
    void delegatesShowRequestToPythonApp() {
        PythonAppClient client = () -> Map.of("status", "success", "service", "python-app");
        ConsumerController controller = new ConsumerController(client);

        Map<String, Object> response = controller.show();

        assertThat(response.get("service")).isEqualTo("java-consumer");
        Map<?, ?> pythonResponse = (Map<?, ?>) response.get("python_app_response");
        assertThat(pythonResponse.get("status")).isEqualTo("success");
    }

    @Test
    void returnsDemoErrorPayloadWhenPythonAppCallFails() {
        PythonAppClient client = () -> {
            throw new IllegalStateException("python-app unavailable");
        };
        ConsumerController controller = new ConsumerController(client);

        Map<String, Object> response = controller.show();

        Map<?, ?> pythonResponse = (Map<?, ?>) response.get("python_app_response");
        assertThat(pythonResponse.get("status")).isEqualTo("error");
        assertThat(pythonResponse.get("error")).isEqualTo("python-app unavailable");
    }
}
