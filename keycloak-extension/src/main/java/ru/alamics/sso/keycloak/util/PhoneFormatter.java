package ru.alamics.sso.keycloak.util;

import lombok.extern.slf4j.Slf4j;

import javax.swing.text.MaskFormatter;
import java.text.ParseException;

@Slf4j
public class PhoneFormatter {
    public static String formatPhoneNumber(String number) {

        String phoneMask = "# ### ### ## ##";
        MaskFormatter maskFormatter;
        StringBuilder sb = new StringBuilder();
        try {
            maskFormatter = new MaskFormatter(phoneMask);
            maskFormatter.setValueContainsLiteralCharacters(false);
            String maskNumber = maskFormatter.valueToString(number);

            sb.append(maskNumber);
            sb.insert(0, "+");

            return sb.toString();
        } catch (ParseException e) {
            log.error("number parse exception" + " " + e.getMessage());
        } finally {
            sb.append(number);
        }
        return sb.toString();
    }
}
