package com.example.AuthProject.ratelimit;

import com.example.AuthProject.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String AUTH_PATH_PREFIX = "/api/v1/auth";

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public RateLimitFilter(
            RateLimitService rateLimitService,
            ObjectMapper objectMapper,
            @Value("${app.rate-limit.enabled:true}") boolean enabled
    ) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return path == null || !path.startsWith(AUTH_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            rateLimitService.checkAuthRequest(request.getRequestURI(), resolveClientIp(request));
        } catch (RateLimitExceededException ex) {
            log.warn("Rate limit exceeded path={} ip={} retryAfter={}",
                    request.getRequestURI(), resolveClientIp(request), ex.getRetryAfterSeconds());
            writeTooManyRequests(response, ex);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeTooManyRequests(HttpServletResponse response, RateLimitExceededException ex)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiResponse.failure(ex.getStatus(), ex.getMessage(), ex.getErrors())
        );
    }

    static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String first = forwarded.split(",")[0].trim();
            if (!first.isEmpty()) {
                return first;
            }
        }
        String remote = request.getRemoteAddr();
        return remote != null ? remote : "unknown";
    }
}
