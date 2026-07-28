package com.alin.lin.config;

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

/** 將同一請求的識別資訊同時帶入技術 Log 與不可覆寫稽核事件。 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = headerOrDefault(request, "X-Request-ID", UUID.randomUUID().toString());
        String traceId = extractTraceId(request.getHeader("traceparent"));
        MDC.put("requestId", requestId);
        if (traceId != null) MDC.put("traceId", traceId);
        response.setHeader("X-Request-ID", requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
            MDC.remove("traceId");
        }
    }

    private String headerOrDefault(HttpServletRequest request, String name, String fallback) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? fallback : value.substring(0, Math.min(value.length(), 128));
    }

    private String extractTraceId(String traceparent) {
        if (traceparent == null) return null;
        String[] parts = traceparent.split("-");
        return parts.length >= 4 && parts[1].matches("[0-9a-fA-F]{32}") ? parts[1].toLowerCase() : null;
    }
}
