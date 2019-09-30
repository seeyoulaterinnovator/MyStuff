package ru.alamics.sso.registration.phone;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public enum  ActivationCodeType {
    CODE_TO_EMAIL(4, 300L),
    CODE_BY_PHONE_NUMBER(4, 20L),
    CODE_TO_SMS(6, 300L);

    private final int lengthCode;
    private long expiredSeconds;

    ActivationCodeType(int lengthCode, long expiredSeconds) {
        this.lengthCode = lengthCode;
        this.expiredSeconds = expiredSeconds;
    }

    public static ActivationCodeType fromString(String authNote) {
        try {
            if (authNote == null || authNote.isBlank()) {
                return valueOf(authNote);
            }
        } catch (IllegalArgumentException ignore) {
        }

        return null;
    }

    public int getLengthCode() {
        return lengthCode;
    }

    public long getExpiredSeconds() {
        return expiredSeconds;
    }

    public void setExpiredSeconds(long expiredSeconds) {
        this.expiredSeconds = expiredSeconds;
    }
}
