package com.preeti.authenticationdemo.security;

import com.preeti.authenticationdemo.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private FilterChain filterChain;

    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter(rateLimitingService);
    }

    @Test
    @DisplayName("Filter: Allowed request passes through filter chain with HTTP 200")
    void doFilter_AllowedRequest_ProceedsInChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(rateLimitingService.tryConsume(eq("127.0.0.1"), anyString()))
                .thenReturn(new RateLimitingService.RateLimitResult(true, 4, 0));

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals("4", response.getHeader("X-Rate-Limit-Remaining"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter: Blocked request returns HTTP 429 Too Many Requests")
    void doFilter_ExceededRequest_Returns429() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(rateLimitingService.tryConsume(eq("127.0.0.1"), anyString()))
                .thenReturn(new RateLimitingService.RateLimitResult(false, 0, 45));

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        assertEquals("0", response.getHeader("X-Rate-Limit-Remaining"));
        assertEquals("45", response.getHeader("X-Rate-Limit-Retry-After-Seconds"));
        verify(filterChain, never()).doFilter(request, response);
    }
}
