package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;

import javax.ejb.Stateless;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class HashGenerator {

    public static String getSecretHash(String s) {
        try {
            return bytesToHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            log.error("Error initializing HashGenerator: " + e.getMessage() + "; will use no-op impl", e);
        }
        return s;
    }

    public static String getSecretHashMD5(String s) {
        try {
            return bytesToHex(MessageDigest.getInstance("MD5").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            log.error("Error initializing HashGenerator: " + e.getMessage() + "; will use no-op impl", e);
        }
        return s;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
