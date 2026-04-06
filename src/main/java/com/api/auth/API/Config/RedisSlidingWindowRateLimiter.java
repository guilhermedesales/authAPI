package com.api.auth.API.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RedisSlidingWindowRateLimiter {

    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local nowMs = tonumber(ARGV[1])
            local windowMs = tonumber(ARGV[2])
            local maxAttempts = tonumber(ARGV[3])
            local member = ARGV[4]

            redis.call('ZREMRANGEBYSCORE', key, '-inf', nowMs - windowMs)

            local count = redis.call('ZCARD', key)
            if count >= maxAttempts then
                local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
                if oldest[2] == nil then
                    return 1000
                end
                local retryAfterMs = (tonumber(oldest[2]) + windowMs) - nowMs
                if retryAfterMs < 1 then
                    retryAfterMs = 1
                end
                return retryAfterMs
            end

            redis.call('ZADD', key, nowMs, member)
            redis.call('PEXPIRE', key, windowMs + 1000)
            return -1
            """;

    private final StringRedisTemplate redisTemplate;
    private final Map<String, SlidingWindow> localFallbackCounters = new ConcurrentHashMap<>();
    private final DefaultRedisScript<Long> script;

    @Value("${auth.rate-limit.redis.enabled:true}")
    private boolean redisEnabled;

    @Value("${auth.rate-limit.redis.fail-open:true}")
    private boolean failOpen;

    @Value("${auth.rate-limit.redis.key-prefix:auth:ratelimit}")
    private String keyPrefix;

    public RedisSlidingWindowRateLimiter(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.script = new DefaultRedisScript<>();
        this.script.setScriptText(LUA_SCRIPT);
        this.script.setResultType(Long.class);
    }

    public long tryAcquire(String ruleName, String clientIdentifier, int maxAttempts, long windowSeconds) {
        long nowMs = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        String key = keyPrefix + ":" + ruleName + ":" + clientIdentifier;

        if (redisEnabled && redisTemplate != null) {
            try {
                String member = nowMs + ":" + Thread.currentThread().getId() + ":" + Math.random();
                Long retryAfterMs = redisTemplate.execute(
                        script,
                        List.of(key),
                        String.valueOf(nowMs),
                        String.valueOf(windowMs),
                        String.valueOf(maxAttempts),
                        member
                );

                if (retryAfterMs == null) {
                    return failOpen ? -1L : 1L;
                }

                return retryAfterMs < 0 ? -1L : Math.max(1L, (long) Math.ceil(retryAfterMs / 1000.0));
            } catch (Exception e) {
                log.error("[RATE LIMIT] Redis rate-limit failure - key={} failOpen={}", key, failOpen, e);
                if (failOpen) {
                    return -1L;
                }
                return 1L;
            }
        }

        SlidingWindow window = localFallbackCounters.computeIfAbsent(key, ignored -> new SlidingWindow());
        return window.tryAcquire(nowMs, windowMs, maxAttempts);
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


