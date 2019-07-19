package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;

import javax.ejb.Stateless;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Stateless
public class HashProvider {

    private MessageDigest digest;

    public HashProvider() {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("Error initializing HashProvider: " + e.getMessage() + "; will use no-op impl", e);
        }
    }

    public String getHash(String s) {
        return digest != null? bytesToHex(digest.digest(s.getBytes(StandardCharsets.UTF_8))) : s;
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


}
