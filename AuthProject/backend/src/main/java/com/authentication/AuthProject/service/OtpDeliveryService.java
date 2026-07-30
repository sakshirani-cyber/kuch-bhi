package com.authentication.AuthProject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


// Delivers OTP codes to users.
// Currently logs only;
 
@Slf4j
@Service
public class OtpDeliveryService {

    @Async("otpTaskExecutor")
    public void sendOtp(String otp, String email) {
        log.info("Sending OTP {} to {}", otp, email);
    }
}
