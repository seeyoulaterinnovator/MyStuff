package ru.alamics.sso.keycloak.auth.requiredactions;

import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SsoFormValidation {

    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FIRST_NAME = "firstName";
    public static final String FIELD_PHONE = "phone";

    // Actually allow same emails like angular. See ValidationTest.testEmailValidation()
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9!#$%&'*+/=?^_`{|}~.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*");

    private static void addError(List<FormMessage> errors, String field, String message){
        errors.add(new FormMessage(field, message));
    }

    public static List<FormMessage> validateUpdateProfileForm(RealmModel realm, MultivaluedMap<String, String> formData) {
        List<FormMessage> errors = new ArrayList<>();

        if (isBlank(formData.getFirst(FIELD_FIRST_NAME))) {
            addError(errors, FIELD_FIRST_NAME, Messages.MISSING_FIRST_NAME);
        }

        if (isBlank(formData.getFirst(FIELD_EMAIL))) {
            addError(errors, FIELD_EMAIL, Messages.MISSING_EMAIL);
        } else if (!isEmailValid(formData.getFirst(FIELD_EMAIL))) {
            addError(errors, FIELD_EMAIL, Messages.INVALID_EMAIL);
        }

        if (isBlank(formData.getFirst(FIELD_PHONE))) {
            addError(errors, FIELD_PHONE, "missingPhoneNumberMessage");
        }  if (!isPhoneValid(formData.getFirst(FIELD_PHONE))) {
            addError(errors, FIELD_PHONE, "invalidPhoneMessage");
        }

        return errors;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isEmailValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isPhoneValid(String phone) {
        return phone.length() == 11;
    }
}
