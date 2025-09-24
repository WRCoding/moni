package com.longjunwang.moni.filter;

import com.longjunwang.moni.entity.RequestLog;
import com.longjunwang.moni.service.RequestLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_FIELD_LENGTH = 4000;

    private final RequestLogService requestLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String uri = request.getRequestURI();

        // 1) 绕过 SSE / 其他需要长连接的端点
        if (uri.endsWith("/moni/sse") || uri.equals("/moni/mcp/messages")) {
            filterChain.doFilter(request, response);
            return;
        }
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            try {
                captureAndPersist(wrappedRequest, wrappedResponse);
            } catch (Exception ex) {
                log.debug("Skip request logging due to error: {}", ex.getMessage());
            }
        }
    }

    private void captureAndPersist(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        RequestLog logRecord = new RequestLog();
        logRecord.setMethod(request.getMethod());
        logRecord.setPath(request.getRequestURI());
        logRecord.setQuery(truncate(request.getQueryString()));
        logRecord.setHeaders(buildHeaders(request));
        logRecord.setBody(resolveBody(request));
        logRecord.setClientIp(resolveClientIp(request));
        logRecord.setStatus(response.getStatus());
        logRecord.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        requestLogService.record(logRecord);
    }

    private String buildHeaders(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if ("authorization".equalsIgnoreCase(name)) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values != null && values.hasMoreElements()) {
                if (!builder.isEmpty()) {
                    builder.append("\n");
                }
                builder.append(name).append(": ").append(values.nextElement());
            }
        }
        if (builder.isEmpty()) {
            return null;
        }
        return truncate(builder.toString());
    }

    private String resolveBody(ContentCachingRequestWrapper request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            return "<multipart omitted>";
        }
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        String encoding = request.getCharacterEncoding();
        Charset charset = StandardCharsets.UTF_8;
        try {
            charset = Charset.forName(encoding);
        } catch (Exception ignore) {
            // fall back to UTF-8
        }
        return truncate(new String(content, charset));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= MAX_FIELD_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_FIELD_LENGTH);
    }
}
