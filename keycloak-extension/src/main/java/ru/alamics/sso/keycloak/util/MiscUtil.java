package ru.alamics.sso.keycloak.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MiscUtil {
    private static final String STUB_SETTING_VALUE = "no settings";

    public static long parseLong(String value, long defaultValue) {
        if(value == null || value.isEmpty()) return defaultValue;

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int parsInt(String value, int defaultValue) {
        if(value == null || value.isEmpty()) return defaultValue;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Instant parseInstant(String value) {
        if(value == null || value.isEmpty()) return null;

        try {
            long timestamp = Long.parseLong(value);
            return Instant.ofEpochMilli(timestamp);
        } catch (NumberFormatException e1) {
            try {
                return Instant.parse(value);
            } catch (Exception e2) {
                return null;
            }
        }
    }

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
}
