package ru.alamics.sso.keycloak.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MiscUtil {
    private static final String STUB_SETTING_VALUE = "no settings";

    public static String notEmptySettingsValue(String value, String... altValues) {
        if(!isEmptySettingsValue(value)) {
            return value;
        }
        for(String altValue : altValues) {
            if(!isEmptySettingsValue(altValue)) {
                return altValue;
            }
        }
        return value;
    }

    public static boolean isEmptySettingsValue(String value) {
        return value == null || value.isEmpty() || STUB_SETTING_VALUE.equals(value);
    }

    public static boolean isPhoneNumber(String value) {
        return value != null && !value.isEmpty() && value.matches("^79[0-9]{9}$");
    }
}
