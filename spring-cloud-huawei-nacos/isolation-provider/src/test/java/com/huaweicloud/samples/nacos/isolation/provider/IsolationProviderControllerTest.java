package com.huaweicloud.samples.nacos.isolation.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IsolationProviderControllerTest {

    private IsolationProviderController controller;

    @BeforeEach
    void setUp() {
        controller = new IsolationProviderController();
        ReflectionTestUtils.setField(controller, "port", 8092);
        controller.resetCounts();
    }

    @Test
    void keepsScenarioCountersIndependent() {
        assertThat(controller.minimumCalls(false)).contains("8092");
        assertThatThrownBy(() -> controller.minimumCalls(false)).isInstanceOf(RuntimeException.class);
        assertThat(controller.callCount("minimum-calls")).isEqualTo(2);
        assertThat(controller.callCount("failed-percent")).isZero();
    }

    @Test
    void createsConfiguredFailureRatio() {
        int failures = 0;
        for (int i = 0; i < 10; i++) {
            try {
                controller.failedPercent(6);
            } catch (RuntimeException e) {
                failures++;
            }
        }

        assertThat(failures).isEqualTo(6);
        assertThat(controller.callCount("failed-percent")).isEqualTo(10);
    }

    @Test
    void recordsSlowAndStatusCalls() throws Exception {
        assertThat(controller.slowCall(0)).contains("8092");

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(controller.errorCode(500, response)).contains("status=500");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getHeader("X-HTTP-STATUS-CODE")).isEqualTo("500");
        assertThat(controller.callCount("slow-call")).isEqualTo(1);
        assertThat(controller.callCount("error-code")).isEqualTo(1);
    }
}
