package com.preeti.authenticationdemo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.preeti.authenticationdemo.dto.ErrorResponse;
import com.preeti.authenticationdemo.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public RateLimitingFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // Only enforce rate limits on REST API endpoints
        if (requestUri.startsWith("/api/v1/auth/")) {
            String clientIp = resolveClientIp(request);
            RateLimitingService.RateLimitResult result = rateLimitingService.tryConsume(clientIp, requestUri);

            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(result.getRemainingTokens()));

            if (!result.isConsumed()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(result.getWaitForRefillSeconds()));

                String errorMessage = String.format(
                        "Too many requests. Rate limit exceeded for this endpoint. Please try again in %d seconds.",
                        result.getWaitForRefillSeconds()
                );

                ErrorResponse errorResponse = ErrorResponse.of(
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        errorMessage
                );

                objectMapper.writeValue(response.getOutputStream(), errorResponse);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }

}
