package ru.alamics.sso.keycloak.util;

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

    private MiscUtil() {}
}
