package com.authentication.AuthProject.service;

import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.exception.BadRequestException;
import com.authentication.AuthProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

// Generates, stores, verifies, and resends email OTP codes via Memcached.
 
@Slf4j
@RequiredArgsConstructor
@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_TTL_SECONDS = 300;
    private static final int MAX_ATTEMPTS = 3;
    private static final int ATTEMPT_TTL_SECONDS = OTP_TTL_SECONDS;

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String ATTEMPT_KEY_PREFIX = "otp:attempts:";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MemcachedService memcachedService;
    private final OtpDeliveryService otpDeliveryService;
    private final UserRepository userRepository;


    public void issueOtpForSignup(String email) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("User not found for OTP issuance."));

        if (user.isVerified()) {
            throw new BadRequestException("Email is already verified.");
        }

        storeNewOtp(normalizedEmail);
    }



    @Transactional
    public void verifyOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        int attempts = getAttemptCount(normalizedEmail);

        if (attempts >= MAX_ATTEMPTS) {
            throw new BadRequestException("Maximum OTP verification attempts exceeded.");
        }

        String otpKey = otpKey(normalizedEmail);
        String storedOtp = memcachedService.get(otpKey);
        if (storedOtp == null) {
            throw new BadRequestException("OTP expired or not found. Please request a new one.");
        }

        if (!storedOtp.equals(otp)) {
            incrementAttempts(normalizedEmail);
            log.warn("Failed OTP verification attempt for email: {}", normalizedEmail);
            throw new BadRequestException("Invalid OTP.");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("User not found."));
        user.setVerified(true);
        userRepository.save(user);

        memcachedService.delete(otpKey);
        memcachedService.delete(attemptsKey(normalizedEmail));
        log.info("Email verified successfully for: {}", normalizedEmail);
    }




    public void resendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("User not found."));

        if (user.isVerified()) {
            throw new BadRequestException("Email is already verified.");
        }

        memcachedService.delete(otpKey(normalizedEmail));
        memcachedService.delete(attemptsKey(normalizedEmail));
        storeNewOtp(normalizedEmail);
        log.info("OTP resent for email: {}", normalizedEmail);
    }




    private void storeNewOtp(String normalizedEmail) {
        String otp = generateOtp();
        memcachedService.set(otpKey(normalizedEmail), OTP_TTL_SECONDS, otp);
        memcachedService.set(attemptsKey(normalizedEmail), ATTEMPT_TTL_SECONDS, "0");
        otpDeliveryService.sendOtp(otp, normalizedEmail);
        log.debug("Stored new OTP for email: {}", normalizedEmail);
    }



    private void incrementAttempts(String normalizedEmail) {
        int attempts = getAttemptCount(normalizedEmail) + 1;
        memcachedService.set(attemptsKey(normalizedEmail), ATTEMPT_TTL_SECONDS, String.valueOf(attempts));
    }




    private int getAttemptCount(String normalizedEmail) {
        String attempts = memcachedService.get(attemptsKey(normalizedEmail));
        if (attempts == null) {
            return 0;
        }
        try {
            return Integer.parseInt(attempts);
        } catch (NumberFormatException ex) {
            log.warn("Invalid attempt counter for email: {}", normalizedEmail);
            return 0;
        }
    }




    private String generateOtp() {
        int value = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }


    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String otpKey(String normalizedEmail) {
        return OTP_KEY_PREFIX + normalizedEmail;
    }

    private String attemptsKey(String normalizedEmail) {
        return ATTEMPT_KEY_PREFIX + normalizedEmail;
    }
}
