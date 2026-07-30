package com.preeti.authenticationdemo.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RateLimitingService {

    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    @Getter
    public static class RateLimitResult {
        private final boolean consumed;
        private final long remainingTokens;
        private final long waitForRefillSeconds;

        public RateLimitResult(boolean consumed, long remainingTokens, long waitForRefillSeconds) {
            this.consumed = consumed;
            this.remainingTokens = remainingTokens;
            this.waitForRefillSeconds = Math.max(1, waitForRefillSeconds);
        }
    }

    public RateLimitResult tryConsume(String clientIp, String requestUri) {
        String cacheKey = resolveCacheKey(clientIp, requestUri);
        Bucket bucket = bucketCache.get(cacheKey, key -> createBucketForUri(requestUri));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        long retrySeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

        if (!probe.isConsumed()) {
            log.warn("Rate limit exceeded for IP '{}' on URI '{}'. Retry after {}s", clientIp, requestUri, retrySeconds);
        }

        return new RateLimitResult(probe.isConsumed(), probe.getRemainingTokens(), retrySeconds);
    }

    private String resolveCacheKey(String clientIp, String requestUri) {
        String routeKey = resolveRouteCategory(requestUri);
        return "rate_limit:" + clientIp + ":" + routeKey;
    }

    private String resolveRouteCategory(String uri) {
        if (uri == null) {
            return "general";
        }
        if (uri.contains("/login")) return "login";
        if (uri.contains("/signup")) return "signup";
        if (uri.contains("/resend-otp")) return "resend_otp";
        if (uri.contains("/verify-otp")) return "verify_otp";
        return "general";
    }

    private Bucket createBucketForUri(String requestUri) {
        Bandwidth limit = resolveBandwidthForUri(requestUri);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bandwidth resolveBandwidthForUri(String uri) {
        String routeKey = resolveRouteCategory(uri);
        return switch (routeKey) {
            case "login" -> Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build();
            case "signup" -> Bandwidth.builder().capacity(3).refillGreedy(3, Duration.ofMinutes(1)).build();
            case "resend_otp" -> Bandwidth.builder().capacity(3).refillGreedy(3, Duration.ofMinutes(1)).build();
            case "verify_otp" -> Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build();
            default -> Bandwidth.builder().capacity(20).refillGreedy(20, Duration.ofMinutes(1)).build();
        };
    }

}
