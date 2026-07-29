package com.example.AuthProject.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class RateLimitService {

    private static final String AUTH_PREFIX = "/api/v1/auth";

    private final ProxyManager<String> proxyManager;

    private final int authLimit;
    private final long authWindowSeconds;
    private final int loginLimit;
    private final long loginWindowSeconds;
    private final int registerLimit;
    private final long registerWindowSeconds;
    private final int verifyOtpLimit;
    private final long verifyOtpWindowSeconds;
    private final int resendOtpLimit;
    private final long resendOtpWindowSeconds;
    private final int updateUsernameLimit;
    private final long updateUsernameWindowSeconds;
    private final int updatePasswordLimit;
    private final long updatePasswordWindowSeconds;

    public RateLimitService(
            ProxyManager<String> bucket4jProxyManager,
            @Value("${app.rate-limit.auth-limit:60}") int authLimit,
            @Value("${app.rate-limit.auth-window-seconds:60}") long authWindowSeconds,
            @Value("${app.rate-limit.login-limit:5}") int loginLimit,
            @Value("${app.rate-limit.login-window-seconds:60}") long loginWindowSeconds,
            @Value("${app.rate-limit.register-limit:10}") int registerLimit,
            @Value("${app.rate-limit.register-window-seconds:3600}") long registerWindowSeconds,
            @Value("${app.rate-limit.verify-otp-limit:10}") int verifyOtpLimit,
            @Value("${app.rate-limit.verify-otp-window-seconds:60}") long verifyOtpWindowSeconds,
            @Value("${app.rate-limit.resend-otp-limit:3}") int resendOtpLimit,
            @Value("${app.rate-limit.resend-otp-window-seconds:300}") long resendOtpWindowSeconds,
            @Value("${app.rate-limit.update-username-limit:5}") int updateUsernameLimit,
            @Value("${app.rate-limit.update-username-window-seconds:604800}") long updateUsernameWindowSeconds,
            @Value("${app.rate-limit.update-password-limit:5}") int updatePasswordLimit,
            @Value("${app.rate-limit.update-password-window-seconds:604800}") long updatePasswordWindowSeconds
    ) {
        this.proxyManager = bucket4jProxyManager;
        this.authLimit = authLimit;
        this.authWindowSeconds = authWindowSeconds;
        this.loginLimit = loginLimit;
        this.loginWindowSeconds = loginWindowSeconds;
        this.registerLimit = registerLimit;
        this.registerWindowSeconds = registerWindowSeconds;
        this.verifyOtpLimit = verifyOtpLimit;
        this.verifyOtpWindowSeconds = verifyOtpWindowSeconds;
        this.resendOtpLimit = resendOtpLimit;
        this.resendOtpWindowSeconds = resendOtpWindowSeconds;
        this.updateUsernameLimit = updateUsernameLimit;
        this.updateUsernameWindowSeconds = updateUsernameWindowSeconds;
        this.updatePasswordLimit = updatePasswordLimit;
        this.updatePasswordWindowSeconds = updatePasswordWindowSeconds;
    }

    public void checkAuthRequest(String requestUri, String clientIp) {
        String relative = stripAuthPrefix(requestUri);
        String ip = (clientIp == null || clientIp.isBlank()) ? "unknown" : clientIp.trim();

        consumeOrThrow("rl:auth:ip:" + ip, authLimit, authWindowSeconds);

        if (relative.equals("/login") || relative.startsWith("/login/")) {
            consumeOrThrow("rl:login:ip:" + ip, loginLimit, loginWindowSeconds);
            return;
        }
        if (relative.equals("/register") || relative.startsWith("/register/")) {
            consumeOrThrow("rl:register:ip:" + ip, registerLimit, registerWindowSeconds);
            return;
        }
        if (relative.equals("/verify-otp") || relative.startsWith("/verify-otp/")) {
            consumeOrThrow("rl:verify-otp:ip:" + ip, verifyOtpLimit, verifyOtpWindowSeconds);
            return;
        }
        if (relative.equals("/resend-otp") || relative.startsWith("/resend-otp/")) {
            consumeOrThrow("rl:resend-otp:ip:" + ip, resendOtpLimit, resendOtpWindowSeconds);
            return;
        }

        String updateUsernameEmail = emailAfterPrefix(relative, "/update-username/");
        if (updateUsernameEmail != null) {
            consumeOrThrow(
                    "rl:update-username:email:" + updateUsernameEmail,
                    updateUsernameLimit,
                    updateUsernameWindowSeconds
            );
            return;
        }

        String updatePasswordEmail = emailAfterPrefix(relative, "/update-password/");
        if (updatePasswordEmail != null) {
            consumeOrThrow(
                    "rl:update-password:email:" + updatePasswordEmail,
                    updatePasswordLimit,
                    updatePasswordWindowSeconds
            );
        }
    }

    private void consumeOrThrow(String key, int limit, long windowSeconds) {
        try {
            Bucket bucket = proxyManager.getProxy(key, configuration(limit, windowSeconds));
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (!probe.isConsumed()) {
                long retryAfter = Math.max(1L, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
                throw new RateLimitExceededException(retryAfter);
            }
        } catch (RateLimitExceededException ex) {
            throw ex;
        } catch (Exception e) {
            log.warn("Bucket4j rate limit check failed key={}: {}", key, e.getMessage());
        }
    }

    private static Supplier<BucketConfiguration> configuration(int limit, long windowSeconds) {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillIntervally(limit, Duration.ofSeconds(windowSeconds))
                        .build())
                .build();
    }

    private static String stripAuthPrefix(String requestUri) {
        if (requestUri == null) {
            return "";
        }
        String path = requestUri;
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        if (path.startsWith(AUTH_PREFIX)) {
            path = path.substring(AUTH_PREFIX.length());
        }
        if (path.isEmpty()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static String emailAfterPrefix(String relativePath, String prefix) {
        if (!relativePath.startsWith(prefix)) {
            return null;
        }
        String email = relativePath.substring(prefix.length());
        if (email.isBlank()) {
            return null;
        }
        int slash = email.indexOf('/');
        if (slash >= 0) {
            email = email.substring(0, slash);
        }
        return email.trim().toLowerCase();
    }
}
