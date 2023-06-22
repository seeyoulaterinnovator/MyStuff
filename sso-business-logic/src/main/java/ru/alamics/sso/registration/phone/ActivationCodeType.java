package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;

import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public enum ActivationCodeType {
    CODE_TO_EMAIL(EXPIRE_INCOMING_CALL_EMAIL_CODE, 6, 300L, 60L, EXPIRE_TIME_TO_RESEND_MAIL),
    CODE_BY_PHONE_NUMBER(EXPIRE_INCOMING_CALL_CODE, 4, 300L, 60L, EXPIRE_TIME_TO_RESEND_CALL),
    CODE_TO_SMS(EXPIRE_SMS_VIBER_CODE, 4, 300L, 60L, EXPIRE_TIME_TO_RESEND_SMS);

    private final int lengthCode;
    private final SettingConstants propertyCodeTimeConstant;
    private final SettingConstants propertyTimeConstant;
    private long expiredCodeSeconds;
    private long expiredSecondsToResend;

    ActivationCodeType(SettingConstants propertyCodeTimeConstant, int lengthCode, long expiredCodeSeconds,
                       long expiredSecondsToResend, SettingConstants propertyTimeConstant) {
        this.lengthCode = lengthCode;
        this.expiredCodeSeconds = expiredCodeSeconds;
        this.propertyCodeTimeConstant = propertyCodeTimeConstant;
        this.expiredSecondsToResend = expiredSecondsToResend;
        this.propertyTimeConstant = propertyTimeConstant;
    }

    public static ActivationCodeType fromString(String authNote) {
        try {
            if (authNote == null || authNote.isEmpty()) {
                return valueOf(authNote);
            }
        } catch (IllegalArgumentException ignore) {
        }

        return null;
    }

    /**
     * Функция переопределяет ENUM элементы значениями из настроек, которые указываются в админке
     */
    public static void init(String realmId) {
        SettingsService settingsService = Lookup.lookup(SettingsService.class);
        for (ActivationCodeType activationCodeType : ActivationCodeType.values()) {
            long codeTimeValue = settingsService.getSettingsLongValue(activationCodeType.getPropertyCodeTimeConstant(), realmId);
            long timeToResend = settingsService.getSettingsLongValue(activationCodeType.getPropertyTimeConstant(), realmId);
            if (codeTimeValue <= -1) {
                codeTimeValue = 0;
            }
            if (timeToResend <= -1) {
                timeToResend = 0;
            }
            activationCodeType.setExpiredCodeSeconds(codeTimeValue);
            activationCodeType.setExpiredSecondsToResend(timeToResend);
        }

    }

    public int getLengthCode() {
        return lengthCode;
    }

    public long getExpiredCodeSeconds() {
        return expiredCodeSeconds;
    }

    private void setExpiredCodeSeconds(long expiredCodeSeconds) {
        this.expiredCodeSeconds = expiredCodeSeconds;
    }
    private void setExpiredSecondsToResend(long expiredSecondsToResend) {
        this.expiredSecondsToResend = expiredSecondsToResend;
    }

    public SettingConstants getPropertyCodeTimeConstant() {
        return propertyCodeTimeConstant;
    }

    public long getExpiredSecondsToResend() {
        return expiredSecondsToResend;
    }

    public SettingConstants getPropertyTimeConstant() {
        return propertyTimeConstant;
    }
}
