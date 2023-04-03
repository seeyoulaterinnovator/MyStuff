package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;

import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public enum ActivationCodeType {
    CODE_TO_EMAIL(4, 300L, EXPIRE_INCOMING_CALL_EMAIL_CODE),
    CODE_BY_PHONE_NUMBER(4, 20L, EXPIRE_INCOMING_CALL_CODE),
    CODE_TO_SMS(4, 300L, EXPIRE_SMS_VIBER_CODE);

    private final int lengthCode;
    private final SettingConstants propertyConstant;
    private long expiredSeconds;

    ActivationCodeType(int lengthCode, long expiredSeconds, SettingConstants propertyConstant) {
        this.lengthCode = lengthCode;
        this.expiredSeconds = expiredSeconds;
        this.propertyConstant = propertyConstant;
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

    public static void init() {
        SettingsService settingsService = Lookup.lookup(SettingsService.class);
        for (ActivationCodeType activationCodeType : ActivationCodeType.values()) {
            long timeValue = settingsService.getSettingsLongValue(activationCodeType.getPropertyConstant(), "user");
            if (timeValue <= -1) {
                timeValue = 0;
            }
            activationCodeType.setExpiredSeconds(timeValue);
        }

    }

    public int getLengthCode() {
        return lengthCode;
    }

    public long getExpiredSeconds() {
        return expiredSeconds;
    }

    private void setExpiredSeconds(long expiredSeconds) {
        this.expiredSeconds = expiredSeconds;
    }

    public SettingConstants getPropertyConstant() {
        return propertyConstant;
    }
}
