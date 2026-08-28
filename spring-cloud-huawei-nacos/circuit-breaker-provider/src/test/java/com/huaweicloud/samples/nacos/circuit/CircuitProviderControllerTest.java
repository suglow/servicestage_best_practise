package com.huaweicloud.samples.nacos.circuit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitProviderControllerTest {

    private CircuitProviderController controller;

    @BeforeEach
    void setUp() {
        controller = new CircuitProviderController();
        controller.resetCounts();
    }

    @Test
    void alternatesErrorRateResponsesAndCountsControllerCalls() {
        MockHttpServletResponse first = new MockHttpServletResponse();
        MockHttpServletResponse second = new MockHttpServletResponse();

        assertThat(controller.errorRate(true, first)).isEqualTo("OK-1");
        assertThat(controller.errorRate(true, second)).isEqualTo("ERROR-502");
        assertThat(second.getStatus()).isEqualTo(502);
        assertThat(second.getHeader("X-HTTP-STATUS-CODE")).isEqualTo("502");
        assertThat(controller.callCount("error-rate")).isEqualTo(2);
    }

    @Test
    void resetsAllScenarioCounters() throws Exception {
        controller.normal();
        controller.slowCall(0);
        controller.resetCounts();

        assertThat(controller.callCount("normal")).isZero();
        assertThat(controller.callCount("slow-call")).isZero();
    }
}
