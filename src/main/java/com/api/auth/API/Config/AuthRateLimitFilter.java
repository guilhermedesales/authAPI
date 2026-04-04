package com.api.auth.API.Config;

import com.api.auth.Application.DTOs.Common.ErrorDTO;
import com.api.auth.Application.Utils.ErrorMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final ClientIpResolver clientIpResolver;
    private final ObjectMapper objectMapper;
    private final Map<String, SlidingWindow> counters = new ConcurrentHashMap<>();

    @Value("${auth.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${auth.rate-limit.login.max-attempts:10}")
    private int loginMaxAttempts;

    @Value("${auth.rate-limit.login.window-seconds:60}")
    private long loginWindowSeconds;

    @Value("${auth.rate-limit.verify-code.max-attempts:6}")
    private int verifyCodeMaxAttempts;

    @Value("${auth.rate-limit.verify-code.window-seconds:600}")
    private long verifyCodeWindowSeconds;

    @Value("${auth.rate-limit.forgot-password-request.max-attempts:5}")
    private int forgotPasswordRequestMaxAttempts;

    @Value("${auth.rate-limit.forgot-password-request.window-seconds:900}")
    private long forgotPasswordRequestWindowSeconds;

    @Value("${auth.rate-limit.forgot-password-verify.max-attempts:8}")
    private int forgotPasswordVerifyMaxAttempts;

    @Value("${auth.rate-limit.forgot-password-verify.window-seconds:600}")
    private long forgotPasswordVerifyWindowSeconds;

    @Value("${auth.rate-limit.refresh.max-attempts:30}")
    private int refreshMaxAttempts;

    @Value("${auth.rate-limit.refresh.window-seconds:60}")
    private long refreshWindowSeconds;

    public AuthRateLimitFilter(ClientIpResolver clientIpResolver, ObjectMapper objectMapper) {
        this.clientIpResolver = clientIpResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitRule rule = resolveRule(request);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = clientIpResolver.resolve(request);
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "unknown";
        }

        String key = rule.ruleName + ":" + clientIp;
        long now = System.currentTimeMillis();
        long windowMs = rule.windowSeconds * 1000L;

        SlidingWindow window = counters.computeIfAbsent(key, ignored -> new SlidingWindow());
        long retryAfterSeconds = window.tryAcquire(now, windowMs, rule.maxAttempts);
        if (retryAfterSeconds >= 0) {
            log.warn("[RATE LIMIT] Blocked request - rule={} ip={} uri={} method={} retryAfterSec={}",
                    rule.ruleName, clientIp, request.getRequestURI(), request.getMethod(), retryAfterSeconds);
            writeRateLimitResponse(response, retryAfterSeconds);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeRateLimitResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        int tooManyRequestsStatus = org.springframework.http.HttpStatus.TOO_MANY_REQUESTS.value();
        response.setStatus(tooManyRequestsStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(Math.max(1L, retryAfterSeconds)));

        ErrorDTO body = new ErrorDTO(
                tooManyRequestsStatus,
                ErrorMessages.Auth.RATE_LIMIT_EXCEDIDO,
                null
        );

        objectMapper.writeValue(response.getWriter(), body);
    }

    private RateLimitRule resolveRule(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }

        String uri = request.getRequestURI();
        return switch (uri) {
            case "/auth/login" -> new RateLimitRule("login", loginMaxAttempts, loginWindowSeconds);
            case "/auth/login/verify-code" -> new RateLimitRule("verify-code", verifyCodeMaxAttempts, verifyCodeWindowSeconds);
            case "/auth/esqueci-senha/request" -> new RateLimitRule("forgot-password-request", forgotPasswordRequestMaxAttempts, forgotPasswordRequestWindowSeconds);
            case "/auth/esqueci-senha/verify-code" -> new RateLimitRule("forgot-password-verify", forgotPasswordVerifyMaxAttempts, forgotPasswordVerifyWindowSeconds);
            case "/auth/refresh-token" -> new RateLimitRule("refresh", refreshMaxAttempts, refreshWindowSeconds);
            default -> null;
        };
    }

    private record RateLimitRule(String ruleName, int maxAttempts, long windowSeconds) {
    }

    private static final class SlidingWindow {

        private final Deque<Long> requests = new ArrayDeque<>();

        synchronized long tryAcquire(long nowMs, long windowMs, int maxAttempts) {
            while (!requests.isEmpty() && nowMs - requests.peekFirst() >= windowMs) {
                requests.pollFirst();
            }

            if (requests.size() >= maxAttempts) {
                long oldest = requests.peekFirst();
                long retryAfterMs = (oldest + windowMs) - nowMs;
                return Math.max(1L, (long) Math.ceil(retryAfterMs / 1000.0));
            }

            requests.addLast(nowMs);
            return -1L;
        }
    }
}
