package com.preeti.authenticationdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OtpNotificationService {

    @Async("taskExecutor")
    public void sendOtp(String email, String otp) {
        log.info("Sending OTP {} to {}", otp, email);
    }

}
