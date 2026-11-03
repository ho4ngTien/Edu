package com.edu.boot.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraceIdFilterTest {

    private TraceIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new TraceIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate new UUID traceId when X-Trace-Id header is absent")
    void shouldGenerateTraceIdWhenHeaderAbsent() throws ServletException, IOException {
        AtomicReference<String> mdcTraceIdDuringChain = new AtomicReference<>();

        FilterChain filterChain = (req, res) -> {
            mdcTraceIdDuringChain.set(MDC.get(TraceIdFilter.MDC_TRACE_KEY));
        };

        filter.doFilter(request, response, filterChain);

        String generatedTraceId = mdcTraceIdDuringChain.get();
        assertThat(generatedTraceId).isNotNull();
        // Kiểm tra đúng định dạng UUID
        assertThat(UUID.fromString(generatedTraceId)).isNotNull();
        // Kiểm tra response header
        assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo(generatedTraceId);
        // Kiểm tra MDC đã được dọn sạch sau request
        assertThat(MDC.get(TraceIdFilter.MDC_TRACE_KEY)).isNull();
    }

    @Test
    @DisplayName("Should reuse valid client-provided X-Trace-Id header")
    void shouldReuseValidClientProvidedTraceId() throws ServletException, IOException {
        String customTraceId = "req-client-12345-abcde";
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, customTraceId);

        AtomicReference<String> mdcTraceIdDuringChain = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> {
            mdcTraceIdDuringChain.set(MDC.get(TraceIdFilter.MDC_TRACE_KEY));
        };

        filter.doFilter(request, response, filterChain);

        assertThat(mdcTraceIdDuringChain.get()).isEqualTo(customTraceId);
        assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo(customTraceId);
        assertThat(MDC.get(TraceIdFilter.MDC_TRACE_KEY)).isNull();
    }

    @Test
    @DisplayName("Should generate new traceId when client provides invalid characters in X-Trace-Id")
    void shouldGenerateNewTraceIdWhenClientProvidesInvalidCharacters() throws ServletException, IOException {
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "invalid/header;with<script>");

        AtomicReference<String> mdcTraceIdDuringChain = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> {
            mdcTraceIdDuringChain.set(MDC.get(TraceIdFilter.MDC_TRACE_KEY));
        };

        filter.doFilter(request, response, filterChain);

        String traceId = mdcTraceIdDuringChain.get();
        assertThat(traceId).isNotEqualTo("invalid/header;with<script>");
        assertThat(UUID.fromString(traceId)).isNotNull();
        assertThat(MDC.get(TraceIdFilter.MDC_TRACE_KEY)).isNull();
    }

    @Test
    @DisplayName("Should always clear MDC even if filter chain throws an exception")
    void shouldClearMdcEvenIfFilterChainThrowsException() {
        FilterChain throwingChain = (req, res) -> {
            assertThat(MDC.get(TraceIdFilter.MDC_TRACE_KEY)).isNotNull();
            throw new RuntimeException("Simulated error in downstream filter");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, throwingChain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Simulated error in downstream filter");

        // MDC phải được dọn sạch dù có exception
        assertThat(MDC.get(TraceIdFilter.MDC_TRACE_KEY)).isNull();
    }
}

