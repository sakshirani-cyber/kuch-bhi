package com.example.AuthProject.ratelimit;

import com.example.AuthProject.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class RateLimitExceededException extends ApiException {
    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds) {
        super(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too many requests",
                Map.of("rateLimit", "Rate limit exceeded. Try again later.")
        );
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
