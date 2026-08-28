package com.huaweicloud.samples.nacos.retry.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryProviderControllerTest {

    private RetryProviderController controller;

    @BeforeEach
    void setUp() {
        controller = new RetryProviderController();
        ReflectionTestUtils.setField(controller, "port", 8090);
    }

    @Test
    void alternatesFailureAndSuccessIndependently() {
        assertThatThrownBy(controller::noRetry).isInstanceOf(RuntimeException.class);
        assertThat(controller.noRetry()).contains("8090");

        assertThatThrownBy(controller::testRetry).isInstanceOf(RuntimeException.class);
        assertThat(controller.testRetry()).contains("8090");
    }

    @Test
    void restoresAfterConfiguredStatusResponse() {
        MockHttpServletResponse first = new MockHttpServletResponse();
        MockHttpServletResponse second = new MockHttpServletResponse();

        assertThat(controller.statusRetry(500, first)).contains("status=500");
        assertThat(first.getStatus()).isEqualTo(500);
        assertThat(controller.statusRetry(500, second)).contains("success");
        assertThat(second.getStatus()).isEqualTo(200);
    }
}
