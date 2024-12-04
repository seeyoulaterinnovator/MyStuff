package ru.alamics.sso.keycloak.util;

public class PhoneFormatter {
    public static String formatPhoneNumber(String number) {
        String normalizedNumber = number.replaceAll("[^0-9]", "");
        if (normalizedNumber.length() == 11) {
            return normalizedNumber.replaceAll("(\\d)(\\d{3})(\\d{3})(\\d{2})(\\d{2})", "$1 $2 $3 $4 $5");
        }
        return number;
    }
}
