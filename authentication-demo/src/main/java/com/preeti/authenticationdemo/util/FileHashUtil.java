package com.preeti.authenticationdemo.util;

import com.preeti.authenticationdemo.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.security.MessageDigest;

@Slf4j
public class FileHashUtil {

    public static String calculateSha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(bytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception exception) {
            log.error("Failed to calculate SHA-256 hash", exception);
            throw new FileStorageException("Error calculating SHA-256 file checksum", exception);
        }
    }
}
