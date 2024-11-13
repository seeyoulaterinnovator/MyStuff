package ru.alamics.sso.keycloak.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

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

    public static String pluralize(int n, String form1, String form2, String form3) {
        n = Math.abs(n);
        if (n % 10 == 1 && n % 100 != 11) {
            return form1;
        } else if (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20)) {
            return form2;
        } else {
            return form3;
        }
    }

    public static String pluralize(int n, List<String> forms) {
        if(forms.size() != 3) throw new IllegalArgumentException();
        return pluralize(n, forms.get(0), forms.get(1), forms.get(2));
    }

    public static String addBom(byte[] data) {
        String text = new String(data, StandardCharsets.UTF_8);
        text = "\ufeff" + text;
        return text;
    }

    public static String notEmpty(String... texts) {
        for(String text : texts) {
            if(text != null && !text.isBlank()) {
                return text;
            }
        }
        return "";
    }
}
