package com.example.AuthProject.service;

import com.example.AuthProject.cache.OtpCacheService;
import com.example.AuthProject.dto.ApiResponse;
import com.example.AuthProject.entity.User;
import com.example.AuthProject.exception.ApiException;
import com.example.AuthProject.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;

@Slf4j
@Service
public class OtpService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpCacheService otpCacheService;
    private final OtpNotificationService otpNotificationService;
    private final UserRepository userRepository;
    private final int maxAttempts;

    public OtpService(
            OtpCacheService otpCacheService,
            OtpNotificationService otpNotificationService,
            UserRepository userRepository,
            @Value("${app.otp.max-attempts:3}") int maxAttempts
    ) {
        this.otpCacheService = otpCacheService;
        this.otpNotificationService = otpNotificationService;
        this.userRepository = userRepository;
        this.maxAttempts = maxAttempts;
    }

    public void issueOtp(String email) {
        String otp = generateOtp();
        otpCacheService.clearOtpState(email);
        otpCacheService.putOtp(email, otp);
        otpNotificationService.sendOtp(email, otp);
        log.info("OTP issued for email={}", email);
    }

    @Transactional
    public ApiResponse<Void> verifyOtp(String email, String otp) {
        log.info("OTP verification attempt email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "OTP verification failed",
                        Map.of("email", "User not found")
                ));

        if (user.isVerified()) {
            otpCacheService.clearOtpState(email);
            return ApiResponse.success(HttpStatus.OK, "Email already verified");
        }

        String stored = otpCacheService.getOtp(email).orElse(null);
        if (stored == null) {
            log.warn("OTP missing or expired email={}", email);
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "OTP verification failed",
                    Map.of("otp", "OTP is missing or expired. Please request a new one.")
            );
        }

        int attempts = otpCacheService.getAttempts(email);
        if (attempts >= maxAttempts) {
            log.warn("OTP max attempts exceeded email={} attempts={}", email, attempts);
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "OTP verification failed",
                    Map.of("otp", "Too many incorrect attempts. Please request a new OTP.")
            );
        }

        if (!stored.equals(otp.trim())) {
            int nextAttempts = otpCacheService.incrementAttempts(email);
            log.warn("Incorrect OTP email={} attempts={}", email, nextAttempts);
            if (nextAttempts >= maxAttempts) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "OTP verification failed",
                        Map.of("otp", "Too many incorrect attempts. Please request a new OTP.")
                );
            }
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "OTP verification failed",
                    Map.of("otp", "Invalid OTP")
            );
        }

        int updated = userRepository.markVerifiedByEmail(email);
        if (updated == 0) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "OTP verification failed",
                    Map.of("email", "User not found")
            );
        }

        otpCacheService.clearOtpState(email);
        log.info("Email verified successfully email={}", email);
        return ApiResponse.success(HttpStatus.OK, "Email verified successfully");
    }

    public ApiResponse<Void> resendOtp(String email) {
        log.info("OTP resend requested email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "OTP resend failed",
                        Map.of("email", "User not found")
                ));

        if (user.isVerified()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "OTP resend failed",
                    Map.of("email", "Email is already verified")
            );
        }

        issueOtp(email);
        return ApiResponse.success(HttpStatus.OK, "OTP resent successfully");
    }

    private String generateOtp() {
        int value = RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
