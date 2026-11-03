package com.edu.boot.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Filter thiết lập Correlation ID (traceId) cho mọi HTTP request.
 * <p>
 * Luồng hoạt động:
 * 1. Đọc header {@code X-Trace-Id} từ client (nếu có và hợp lệ).
 * 2. Nếu thiếu hoặc không hợp lệ, tự sinh một {@link UUID} v4 mới.
 * 3. Đưa {@code traceId} vào SLF4J {@link MDC} để phục vụ logging tập trung.
 * 4. Đính kèm header {@code X-Trace-Id} vào response gửi về client.
 * 5. Dọn dẹp {@code traceId} khỏi MDC trong khối {@code finally} để tránh rò rỉ bộ nhớ.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_TRACE_KEY = "traceId";

    // Cho phép UUID hoặc chuỗi định danh an toàn (chữ cái, số, gạch ngang, gạch dưới, tối đa 64 ký tự)
    private static final Pattern SAFE_TRACE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request);

        try {
            MDC.put(MDC_TRACE_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_KEY);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String headerTraceId = request.getHeader(TRACE_ID_HEADER);
        if (headerTraceId != null && !headerTraceId.isBlank()) {
            String trimmed = headerTraceId.trim();
            if (SAFE_TRACE_ID_PATTERN.matcher(trimmed).matches()) {
                return trimmed;
            }
        }
        return UUID.randomUUID().toString();
    }
}

