package ru.alamics.sso.registration.phone;

import ru.alamics.sso.property.PropertyConstants;

import static ru.alamics.sso.property.PropertyConstants.*;

public enum  ActivationCodeType {
    CODE_TO_EMAIL(4, 300L, EXPIRE_INCOMING_CALL_EMAIL_CODE),
    CODE_BY_PHONE_NUMBER(4, 20L, EXPIRE_INCOMING_CALL_CODE),
    CODE_TO_SMS(6, 300L, EXPIRE_SMS_VIBER_CODE);

    private final int lengthCode;
    private long expiredSeconds;
    private PropertyConstants propertyConstant;

    ActivationCodeType(int lengthCode, long expiredSeconds, PropertyConstants propertyConstant) {
        this.lengthCode = lengthCode;
        this.expiredSeconds = expiredSeconds;
        this.propertyConstant = propertyConstant;
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

    public PropertyConstants getPropertyConstant(){
        return propertyConstant;
    }
}
