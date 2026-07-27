package com.authentication.AuthProject.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PhoneHashService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${app.encryption.phone-hash-key}")
    private String hashKeyBase64;

    private SecretKeySpec hashKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = Base64.getDecoder().decode(hashKeyBase64);
        hashKey = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
    }

    public String hash(String phoneNumber) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hashKey);
            byte[] hashBytes = mac.doFinal(phoneNumber.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash phone number.", exception);
        }
    }
}
